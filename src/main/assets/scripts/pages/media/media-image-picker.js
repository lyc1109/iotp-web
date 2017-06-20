/**
 * 图片选择器
 *
 * @author huchiwei
 * @create 2016-11-25
 */
var ImagePicker = (function () {
    var DEFAULTS = {
        id: "btnImagePicker",               // 目标元素id, 默认btnImagePicker
        title: "选择图片",                   //  标题
        width: 900,                         // 宽度
        height: 530,                        // 高度
        multiple: false,                    // 是否多选，默认false
        onOk: null                          // 确定函数，传递已选择的图片信息
    };

    var MDU = function (element, config) {
        this.options  = {};
        this.$element = null;
        this.dlg      = null;          // 对话框实例

        if(typeof element === 'object'){
            this.options = $.extend({}, DEFAULTS, element);
            this.$element = $("#" + this.options.id);
        }else{
            this.$element = $("#" + element);
            if(config){
                this.options = $.extend({}, this.options, config);
            }
        }

        this.imageSpage = null;
        this.groupId = null;

        this._init();
    };

    MDU.prototype = {
        getImages: function () {
            var _images = [];

            var $checkeds = this.dlg.$smodal.find('.images-item.checked');
            if($checkeds.length === 0) {
                return _images;
            }

            $checkeds.each(function () {
                _images.push({
                    id: $(this).data("id"),
                    name: $(this).data("name"),
                    url: $(this).data("url"),
                    fileSize: $(this).data("filesize")
                });
            });
            return _images;
        },

        _init: function () {
            var _opts = this.options;
            var that = this;
            this.dlg = new Dlg({
                element: this.$element[0],
                url: "/media/images/picker?multiple=" + _opts.multiple,
                title: _opts.title,
                width: _opts.width,
                height: _opts.height,
                buttons: [{
                    id: "btnCancel",
                    class: "btn-default",
                    text: "取消"
                },{
                    id: "btnOk",
                    class: "btn-primary",
                    text: "确定"
                },{
                    id: "btnImageUploader",
                    class: "btn-success pull-left",
                    text: "上传图片"
                }],
                onOk: function () {
                    var images = that.getImages();
                    if(images.length === 0){
                        Messenger.error("请选择图片");
                        return false;
                    }
                    that.options.onOk.call(that, images);
                    this.close();
                },
                onLoaded: function () {
                    that._onLoaded();
                    new Uploader({
                        id: "btnImageUploader",
                        multi: true,
                        managable: true,
                        prefix: $("#mediaGroupPrefix").val(),
                        saveGroup: function () {
                            var group = "";
                            var $groupItem = $(".group-item.active");
                            if($groupItem.length > 0 && $groupItem.data("name") !== "全部图片"){
                                group = $groupItem.data("name");
                            }
                            return group;
                        },
                        onComplete: function () {
                            var dlg = that.dlg;
                            $.get(dlg.options.url, dlg.options.data, function (htmlData) {
                                dlg.$smodal.find(".body").html(htmlData);
                                that._onLoaded();
                            })
                        }
                    });
                }
            });
        },

        _onLoaded: function () {
            var that = this;
            var dlg = this.dlg;
            var $smodal = dlg.$smodal;
            var multiple = $smodal.find("#multiple").val() === true || $smodal.find("#multiple").val() === 'true';

            that.imageSpage = new Spage({
                id: $smodal.attr("id") + " #imagesSpage",
                url: "/media/images/page4picker",
                onLoaded: function () {

                    // 选择图片
                    $smodal.find(".images-item")
                        .off("click")
                        .on("click", function() {
                            if(multiple){
                                $(this).toggleClass("checked");
                            }else{
                                $smodal.find(".images-item.checked").removeClass("checked");
                                $(this).addClass("checked");
                            }
                        });

                    // 分组筛选
                    $smodal.find(".group-item")
                        .off("click")
                        .on('click', function() {
                            $(".images-picker")
                                .find(".group-item")
                                .removeClass("active");
                            $(this).addClass("active");

                            that.groupId = $(this).data("id");
                            that.imageSpage.refresh({
                                groupId: that.groupId
                            });
                        });
                }
            });
        }
    };

    return MDU;
}());

