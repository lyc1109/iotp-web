/**
 * BootGrid插件封装简化使用
 * 文档: http://www.jquery-bootgrid.com/Documentation
 *
 * @author huchiwei
 * @create 2016-09-18
 */

var BootGrid = (function ($, App, Messenger, toast, moment) {
    var DEFAULTS = {
        id: "dataGrid",                   // 数据表格id,默认: dataGrid
        url: "",                          // 数据来源URL
        data: {},                         // 请求参数
        formatters: {},                   // 格式化函数，默认提供了link,commands,formatGender,formatAge,formatDate,formatDateTime这几个函数
        rowCount: 15,                     // 每页数量
        multiSelect: true,                // 是否可多选

        onInitialized: null,              // 初始化完毕回调：function(){}
        onLoaded: null,                   // 数据加载完毕回调：function(){}
        onSelected: null,                 // 选择行数据回调，传递已选择行数据：function(row){},
        onDeSelected: null                  // 取消选择行数据回调，传递已选择行数据：function(row){},
    };

    var MDU = function (setting) {
        this.options       = $.extend({}, DEFAULTS, setting);
        this.$element      = $("#" + this.options.id);
        this.$container    = this.$element.closest(".data-grid-container");
        this.requestParams = this.options.data || {};
        init.call(this);
    };

    // =============================================
    // 对外公开函数
    MDU.prototype = {
        reload: function (params) {
            this.requestParams = $.extend({}, this.requestParams, typeof params === "object" ? params : {});
            this.requestParams.current = this.getCurrentPage();
            this.$element.bootgrid("reload");
        },

        search: function (searchPhrase) {
            if(typeof searchPhrase === "string"){
                this.$element.bootgrid("search", searchPhrase);
            }else if(typeof searchPhrase === "object"){
                this.reload(searchPhrase);
            }
        },

        getSelectedRows: function () {
            return this.$element.bootgrid("getSelectedRows");
        },

        getCurrentPage: function(){
            return this.$element.bootgrid("getCurrentPage");
        },

        getTotalPageCount: function () {
            return this.$element.bootgrid("getTotalPageCount");
        },

        // 常用操作
        openEditForm: function (entityId) {
            window.open(this.options.url + "/" + entityId + "/edit", "_self");
        },

        deleteEntity: function (entityId) {
            var that = this;
            Messenger.confirm("是否确定删除该项？", function () {
                App.ajax.del({
                    url: that.options.url + "/" + entityId + "/delete",
                    onSuccess: function () {
                        toast("数据已删除");
                        that.refresh();
                    }
                })
            });
        }
    };

    // =============================================
    // 私有函数
    /**
     * 初始化函数
     */
    function init() {
        var that = this;

        // 构建默认格式化参数
        buildDefaultFormatters.call(that);

        // 请求参数处理
        that.options.requestHandler = function (request) {
            if(that.requestParams.current) {
                request.current = that.requestParams.current;
                delete that.requestParams.current;
            } else {
                var currentPage = $("#gridCurPage").val();
                if(currentPage && currentPage > 0){
                    request.current = currentPage;
                }
            }
            return $.extend({}, request, that.requestParams);
        };

        // 事件监听
        eventListener.call(that);

        // 初始化bootgrid
        that.$element.bootgrid(that.options);
    }

    /**
     * 解析字段值
     * @param column     字段
     * @param row        行数据
     * @returns {string} 返回字段对应的值
     */
    function parseColumnValue(column, row) {
        var value = "";
        var columnId = column.id;
        if(columnId.indexOf(".")){
            var columnIds = columnId.split(".");
            $.each(columnIds, function(index, id){
                value = (index===0) ? row[id] : value[id];
            });
        }else{
            value = row[column.id];
        }
        return value;
    }

    /**
     * 构建默认格式化函数
     * @returns {Object|void|*}
     */
    function buildDefaultFormatters() {
        var that = this;
        that.options.formatters = $.extend({
            // 链接
            link: function (column, row) {
                return '<a href="' + that.options.url + '/'  + row.id + '">'+ parseColumnValue(column, row) +'</a>';
            },

            // 默认添加操作
            commands: function(column, row){
                var commands = "<button type=\"button\" class=\"btn btn-xs btn-default command-edit\" data-row-id=\"" + row.id + "\">编辑</button> ";
                commands += "<button type=\"button\" class=\"btn btn-xs btn-default command-delete\" data-row-id=\"" + row.id + "\">删除</button> ";
                return commands;
            },

            // 格式化性别输出
            formatGender: function (column, row) {
                return App.constants.formatGender(parseColumnValue(column, row));
            },

            // 格式化性别输出
            formatAge: function (column, row) {
                return App.constants.formatAge(parseColumnValue(column, row));
            },

            // 格式化日期输出
            formatDate: function (column, row) {
                var date = parseColumnValue(column, row);
                return (date !== null && date !== "") ? moment(date).format("YYYY-MM-DD") : "";
            },

            // 格式化日期时间输出
            formatDateTime: function (column, row) {
                var date = parseColumnValue(column, row);
                return (date !== null && date !== "") ? moment(date).format("YYYY-MM-DD HH:mm") : "";
            }
        }, that.options.formatters);
    }


    /**
     * 初始化操作
     *
     * @private
     */
    function _initCommands() {
        var that = this;

        $(".command-edit").on("click", function(){
            that.openEditForm($(this).data("rowId"));
        });

        $(".command-delete").on("click", function(){
            var rowId = $(this).data("rowId");
            that.deleteEntity(rowId);
        });
    }

    /**
     * 事件监听
     */
    function eventListener() {
        var that = this;
        var options = that.options;
        that.$element
            .on("initialized.rs.jquery.bootgrid", function (){
                that.$container.fadeIn();

                // 绑定刷新按钮
                if($(".grid-refresh").length > 0){
                    $(".grid-refresh").on("click", function () {
                        that.reload();
                    })
                }

                // 绑定搜索框
                if($(".grid-search").length > 0){
                    var $search = $(".grid-search:eq(0)");
                    var $btnSearch = $(".grid-search-go:eq(0)");
                    if(typeof $btnSearch === "undefined"){
                        $btnSearch = $search.closest("div").find("button:eq(0)");
                    }
                    $btnSearch.on("click", function () {
                        that.search($search.val());
                    });

                    var lastTime;
                    $search.keyup(function(event){
                        if(event.keyCode === 13){
                            that.search($search.val());
                        }else{
                            //我们可以用jQuery的event.timeStamp来标记时间，这样每次的keyup事件都会修改lastTime的值，lastTime必需是全局变量
                            lastTime = event.timeStamp;
                            setTimeout(function(){
                                //如果时间差为0，也就是你停止输入0.5s之内都没有其它的keyup事件产生，这个时候就可以去请求服务器了
                                if(lastTime - event.timeStamp === 0){
                                    that.search($search.val());
                                }
                            },500);
                        }
                    });
                }

                if(typeof options.onInitialized === "function") {
                    options.onInitialized.call(that);
                }
            })
            .on("loaded.rs.jquery.bootgrid", function (){
                if(typeof options.onLoaded === "function") {
                    options.onLoaded.call(that);
                }

                // 将分页信息显示在URL上
                var href = window.location.href;
                var currentPage = $(this).bootgrid("getCurrentPage");
                var idx = href.indexOf("currentPage");

                if(idx > -1){
                    var currentText = href.substr(idx-1, idx+9);
                    href = href.replace(currentText, "");
                }

                var lastIdx = href.lastIndexOf("#");
                if(lastIdx > -1 && lastIdx === href.length-1) {
                    href = href.replace("#", "");
                }

                if(href.indexOf("&") > -1 || href.indexOf("?") > -1){
                    href += "&currentPage=" + currentPage;
                }else{
                    href += "?currentPage=" + currentPage;
                }
                window.history.replaceState({}, 0, href);

                // 当前页重置为0
                $("#gridCurPage").val(0);
                delete that.requestParams.current;

                // 绑定编辑、删除操作
                _initCommands.call(that);
            })
            .on("selected.rs.jquery.bootgrid", function (e, rows){
                if(typeof options.onSelected === "function") {
                    options.onSelected.call(that, rows);
                }
            })
            .on("deselected.rs.jquery.bootgrid", function (e, rows){
                if(typeof options.onSelected === "function") {
                    options.onDeSelected.call(that, rows);
                }
            });
    }

    return MDU;
}(jQuery, App, Messenger, toast, moment));


(function ($) {
    /* --- Jquery Plugin Definition --- */

    var $bootgrid = $(".data-grid");
    if($bootgrid.length > 0){
        $bootgrid.each(function () {
            var id = $(this).attr("id");
            if(id === "" || id === null || typeof id === "undefined"){
                id = new Date().getTime() + Math.floor(Math.random() * (1000 - 100) + 100);
                $(this).attr("id", id);
            }

            var data = $(this).data();
            data.id = id;
            new BootGrid(data);
        });

    }
}(jQuery));
