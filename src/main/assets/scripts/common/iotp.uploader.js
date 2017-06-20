/**
 * 媒体资源上传组件
 *
 * @author hucw
 * @date   2016-11-23
 */

var Uploader = (function (moment, md5, plupload) {
    // 默认配置
    var DEFAULTS = {
        pluploadParams: {},                         // plupload插件本身相关配置
        id: "btnUploader",                          // 上传按钮id
        mediaType: 0,                               // 上传文件过滤，可选 0:图片|1:语音|2:视频|9:普通附件, 默认0:图片
        maxFileSize: 1024*1024*10,                  // 上传文件大小限制,默认10M
        prefix: "",                                 // 路径前缀,如"shop_xxx/images"
        enableProgress: true,                       // 启用上传进度条
        progressPosition: "bottom",                 // 进度条位置，默认bottom, 可选|top|right|bottom|left
        startOnAdd: true,                           // 是否选择文件后立即上传
        fileNamePolicy: "random",                   // 上传文件名策略，默认随机生成文件名，可选 random|local，local保持原文件名
        multi: false,                               // 是否可以多选，默认false

        saveUrl: "/media/save",                     // 保存上传后的文件路径到服务器，若为空则不执行保存
        saveGroup: "",                              // 上传的文件保存到哪个分组
        managable: false,                           // 上传后的文件是否可被管理，默认false
        startOnSave: true,                          // 上传完文件后是否立即保存
        targetId: -1,                               // 素材关联目标id
        targetType: "",                             // 素材关联目标类型
        onAdd: null,                                // 添加文件后回调
        onComplete: null,                           // 所有文件上传成功回调，传入上传成功文件路径数组
        onUploading: null,                          // 上传中回调
        onFail: null                                // 上传失败回调
    };

    /**
     * Uploader构建函数
     *
     * @param element 文件上传目标按钮id
     * @param setting 配置
     */
    var MDU = function (setting) {
        this.options      = $.extend({}, DEFAULTS, setting);

        this.element      = this.options.id;
        this.$element     = $("#" + this.element);

        // plupload插件实例
        this.instance     = null;

        this.files    = [];  // 所有最终上传文件信息

        // 阿里oss签名信息
        this.ossSignature = {
            accessKeyId: "",
            expire: "",
            host: "",
            startPath: "",
            policy: "",
            signature: ""
        };

        var that = this;
        this._initOssSignature(function () {
            that._init();
        });
    };


    MDU.prototype = {

        /**
         * 保存上传完毕文件
         */
        save: function () {
            var that = this;
            var saveUrl = this.options.saveUrl;

            var saveFiles = that.renderMedias();
            if(saveUrl !== "" && saveUrl !== null && saveFiles.length > 0){
                var group = "";
                if(typeof that.options.saveGroup === "function"){
                    group = that.options.saveGroup.call(this);
                }else{
                    group = that.options.saveGroup;
                }
                $.ajax({
                    url: saveUrl,
                    data: {
                        group: group,
                        managable: that.options.managable,
                        medias: JSON.stringify(saveFiles),
                        targetId: that.options.targetId,
                        targetType: that.options.targetType
                    },
                    dataType: "json",
                    type: "POST"
                })
                    .done(function (resp) {
                        if(resp.returnCode === 0){
                            if(typeof that.options.onComplete === "function"){
                                that.options.onComplete.call(that, resp);
                            }
                            // 清空已上传文件
                            that.files = [];
                        }else{
                            toast("已上传文件信息保存失败", "error");
                            if(typeof that.options.onFail === "function"){
                                that.options.onFail.call(that, saveFiles);
                            }
                        }
                    })
                    .fail(function () {
                        toast("已上传文件信息保存失败", "error");

                        if(typeof that.options.onFail === "function"){
                            that.options.onFail.call(that, saveFiles);
                        }
                    });
            }else{
                if(typeof that.options.onComplete === "function"){
                    that.options.onComplete.call(that, saveFiles);
                }
            }
        },

        renderMedias: function () {
            var that = this;
            if(this.files.length > 0){
                var medias = [];
                $.each(this.files, function (idx, file) {
                    medias.push({
                        mediaType: that.options.mediaType,
                        name: file.name,
                        path: file.path,
                        fileSize: file.size
                    });
                });
                return medias;
            }
            return [];
        },

        /**
         * 初始化
         */
        _init: function () {
            var that = this;

            // 构建参数
            var ossSignature = that.ossSignature;
            var params = $.extend({}, {

                "browse_button": that.element,

                url: ossSignature.host,

                // oss需要的签名设置
                "multipart_params": {
                    "success_action_status": '200', //让服务端返回200,不然，默认会返回204
                    "key": '${filename}',
                    "policy": ossSignature.policy,
                    "OSSAccessKeyId": ossSignature.accessKeyId,
                    "signature": ossSignature.signature
                },

                filters: {
                    "mime_types": that._getMediaFilter(),
                    "max_file_size": that.options.maxFileSize
                },

                // 默认单选
                "multi_selection": that.options.multi,
                "prevent_duplicates": false,

                init: {
                    /**
                     * 文件选择事件
                     *
                     * @param uploader
                     * @param files 文件列表
                     * @constructor
                     */
                    FilesAdded: function(uploader, files){
                        // 处理上传进度提示
                        if(that.options.enableProgress){
                            that._showProgressPopover(files);
                        }

                        // 判断是否立即上传
                        if(that.options.startOnAdd){
                            uploader.start();
                        }else{
                            if(typeof that.options.onAdd === "function") {
                                that.options.onAdd.call(that, uploader, files);
                            }
                        }
                    },

                    /**
                     * 开始上传前处理
                     *
                     * @param uploader
                     * @param file
                     * @constructor
                     */
                    BeforeUpload: function(uploader, file){
                        // 重新设置上传到oss的绝对路径
                        var multipartParams = uploader.getOption("multipart_params")
                        var filePath = ossSignature.startPath + "/" + that._buildPath(file.name, that.options.prefix);

                        multipartParams.key = filePath;

                        uploader.setOption("multipart_params", multipartParams);

                        file.path = "/" + filePath
                    },

                    /**
                     * 上传进度处理
                     *
                     * @param uploader
                     * @param file
                     * @constructor
                     */
                    UploadProgress: function(uploader, file) {
                        if(typeof that.options.onUploading === "function"){
                            that.options.onUploading.call(that, file.percent);
                        }else{
                            if(that.options.enableProgress){
                                $("."+ that.element + "-popover")
                                    .find("#" + file.id)
                                    .css("width", file.percent + "%");
                            }
                        }
                    },

                    /**
                     * 单个文件上传完毕
                     *
                     * @param uploader
                     * @param file
                     * @param response
                     * @constructor
                     */
                    FileUploaded: function(uploader, file, response){
                        if(response.status===200){
                            that.files.push(file);
                        }else{
                            toast("文件上传失败,请尝试重新上传", 'error');
                        }
                    },

                    /**
                     * 所有文件已上传完毕
                     */
                    UploadComplete: function () {
                        // 关闭上传进度提示
                        if(that.options.enableProgress){
                            that.$element.popover("hide");
                        }

                        // 执行保存
                        if(that.options.startOnSave){
                            that.save();
                        }

                    },

                    /**
                     * 上传异常
                     *
                     * @param uploader
                     * @param err
                     * @constructor
                     */
                    Error: function(uploader, err){
                        toast("文件上传失败,请尝试重新上传", 'error');

                        if(typeof that.options.onFail === "function") {
                            that.options.onFail.call(that, uploader, err);
                        }
                    }
                }
            }, that.options.pluploadParams);

            that.instance = new plupload.Uploader(params);
            that.instance.init();

            // 初始化进度popover
            if(that.options.enableProgress){
                that._initProgressPopover();
            }
        },

        /**
         * 初始化oss签名信息
         */
        _initOssSignature: function (callback) {
            var that = this;

            var cacheSignature = App.cache.get("ossSignature");
            var refresh = true;
            if(cacheSignature){
                that.ossSignature = JSON.parse(cacheSignature);

                // 判断是否过期
                if(moment().isBefore(moment(that.ossSignature.expire))) {
                    refresh = false;
                }
            }

            if(refresh){
                $.ajax({
                    url: "/sys/oss/signature",
                    type: "GET",
                    dataType: "json"
                })
                    .done(function (resp) {
                        that.ossSignature = $.extend(that.ossSignature, resp);
                        App.cache.put("ossSignature", JSON.stringify(that.ossSignature));

                        if(typeof callback === "function") {
                            callback.call(this);
                        }
                    })
                    .fail(function (resp) {
                        console.log("获取oss签名失败" + JSON.stringify(resp));
                    })
            }else{
                if(typeof callback === "function") {
                    callback.call(this);
                }
            }
        },

        /**
         * 构建文件上传路径
         *
         * @param fileName   文件路径
         * @param prefix      分类
         * @returns {string} 上传路径
         */
        _buildPath: function(fileName, prefix){
            var now = moment();
            var extension = fileName.substr(fileName.lastIndexOf("."));

            var path = "";
            if(prefix){
                path = prefix + "/";
            }
            path += now.format("YYYYMMDD") + "/" + now.format("HHmm") + "/";

            if(this.options.fileNamePolicy === "random"){
                path += md5(fileName) + extension;
            }else{
                path += fileName;
            }
            return path;
        },

        /**
         * 初始化进度条容器
         * @private
         */
        _initProgressPopover: function () {
            var that = this;

            var popoverHtml = '<div class="popover '+ that.element +'-popover progress-popover" id="" role="tooltip">';
            popoverHtml += '<div class="arrow"></div>';
            popoverHtml += '<h3 class="popover-title"></h3>';
            popoverHtml += '<div class="popover-content"></div></div>';
            this.$element.popover({
                placement: that.options.progressPosition + " auto",
                container: 'body',
                html: true,
                trigger: "manual", // 手动控制
                template: popoverHtml,
                content: "00000"
            });

            // 显示市替换进度内容
            that.$element.on("shown.bs.popover", function () {
                $("."+ that.element + "-popover .popover-content").html($("#"+ that.element +"PopoverContent").html());
            });
        },

        /**
         * 显示进度条
         * @private
         */
        _showProgressPopover: function (files) {
            var that = this;
            var $popoverContent = $("#"+ that.element +"PopoverContent");
            $popoverContent.empty();

            var $tips = $("<div class='progress-container'/>");
            $.each(files, function(idx, file) {
                var $progressItem = $("<div/>").addClass("progress-item");
                $progressItem.append($("<div/>")
                    .addClass("filename")
                    .text(file.name));

                var $progress = $("<div/>").addClass("progress");
                var $progressBar = $("<div/>")
                    .addClass("progress-bar progress-bar-striped active")
                    .attr({
                        'id': file.id,
                        'role': 'progressbar',
                        'aria-valuenow': 1,
                        'aria-valuemin': 0,
                        'aria-valuemax': 100
                    })
                    .css('width', '1%');
                $progress.append($progressBar);
                $progressItem.append($progress);
                $tips.append($progressItem);
            });

            if($popoverContent.length > 0){
                $popoverContent.html($tips);
            }else{
                $("body").append($("<div id='"+ that.element +"PopoverContent' class='hidden'/>").append($tips));
            }
            that.$element.popover("toggle");
        },

        /**
         * 获取文件类型过滤配置
         * @returns {*}
         * @private
         */
        _getMediaFilter: function () {
            var filter = [];
            var mediaType = this.options.mediaType;
            if(mediaType === 0){
                filter = {
                    title: "Image files",
                    extensions: "jpg,gif,png,jpeg,ico"
                };
            }else if(mediaType === 1){
                filter = {
                    title: "Audio files",
                    extensions: "mp3,ogg,wav,amr,aud,wma,wave,mpeg-4,aiff,au,mpeg"
                };
            }else if(mediaType === 2){
                filter = {
                    title: "Video files",
                    extensions: "mp4,rmvb,rm,wmv,asf,asx,mpg,mpeg,mpe,3gp,mov,m4v,avi,mkv,flv"
                };
            }else if(mediaType === 9){
                filter = {
                    title: "Gen files",
                    extensions: "txt,html,htm,pdf,doc,docx,xls,xlsx,ppt,pptx,jpg,gif,png,jpeg,ico,mp3,ogg,wav,amr,aud,wma,wave,mpeg-4,aiff,au,mpeg,mp4,rmvb,rm,wmv,asf,asx,mpg,mpeg,mpe,3gp,mov,m4v,avi,mkv,flv"
                };
            }
            return [filter];
        }
    };

    return MDU;
}(moment, md5, plupload));
