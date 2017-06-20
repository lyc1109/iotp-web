/**
 * 简易分页插件,主要用于页面数据平铺，如图片平铺分页显示
 *
 * @author huchiwei
 * @create 2016-11-24
 */

var Spage = (function ($) {
    var DEFAULTS = {
        id: "spage",                                // 分页条容器id，默认
        url: "",                                    // 获取分页布局模板
        params: {},                                 // 请求参数
        startPage: 0,                               // 分页起始页数
        pageSize: 12,                               // 分页数量
        onLoaded: null                              // 加载完毕回调
    };

    var MDU = function(settings){
        this.options        = $.extend({}, DEFAULTS, settings);

        if(this.options.element){
            this.$element = $(this.options.element);
        }else{
            this.$element = $("#" + this.options.id);
        }

        this.$body          = null;
        this.$toolbar       = null;

        this.loading        = false;

        this.pageRequest    = {
            page: 0,
            size: this.options.pageSize || 12
        };

        this.pagination     = {
            totalPages: 0,
            totalElements: 0,
            isLast: 0,
            pageNumber: 0
        };

        this._init();
        this.fetch();
    };

    MDU.prototype = {
        // 指定构造器，避免多实例冲突
        constructor: MDU,

        fetch: function () {
            var url = this.options.url;
            if(url === ""){
                $.error('Spage must be setting this option: url');
                return;
            }

            var that = this;
            that.loading = true;

            $.ajax({
                url: url,
                data: $.extend({}, that.pageRequest, that.options.params),
                dataType: "html",
                type: "GET"
            })
                .done(function (html) {
                    that.$body.html(html);

                    that._renderPagination();

                    if(typeof that.options.onLoaded === "function") {
                        that.options.onLoaded.call(that);
                    }
                })
                .fail(function (resp) {
                    $.error('fail to fetch data' + JSON.stringify(resp));
                })
                .always(function () {
                    that.loading = false;
                });
        },

        curPage: function () {
            var curPage = this.pagination.pageNumber;
            if(this.options.startPage === 0) {
                curPage--;
            }
            return curPage;
        },

        next: function () {
            if(this.pagination.isLast) {
                return;
            }
            this.pageRequest.page++;
            this.fetch();
        },

        prev: function () {
            if(this.pagination.pageNumber === this.options.startPage) {
                return;
            }

            this.pageRequest.page--;
            this.fetch();
        },

        goPage: function () {
            var that = this;
            var targetPage = that.$toolbar
                .find("#spagePageNumber")
                .val();

            if(targetPage !== "" && targetPage !== null){
                targetPage = parseInt(targetPage);
                if(targetPage > that.pagination.totalPages || targetPage < that.options.startPage){
                    toast("请输入正确的页数", "error");
                    return;
                }

                if(that.options.startPage === 0){
                    targetPage--;

                    if(targetPage < 0){
                        targetPage = 0;
                    }
                    that.pageRequest.page = targetPage;
                }
                that.fetch();
            }
        },

        refresh: function (params) {
            this.options.params = $.extend({}, this.options.params, typeof params === "object" ? params : {});
            this.pageRequest.page = this.options.startPage;
            this.fetch();
        },

        /**
         * 初始化
         * @private
         */
        _init: function () {
            this.$element
                .append($('<div class="spage-body"/>'))
                .append($('<div class="spage-toolbar"/>'));
            this.$body = this.$element.find(".spage-body");
            this.$toolbar = this.$element.find(".spage-toolbar");

            // 构建分页条
            /*var pagination = "<div class='inline valign-middle'>共<span id='spageTotalEelemets'>0</span>条</div>";*/
            var pagination = "<button class='btn btn-default btn-prev'><span class='spage-prev'></span></button>";
            pagination += "<div class='inline valign-middle'><span id='spageCurPage'>0</span>/<span id='spageTotalPages'>0</span></div>";
            pagination += "<button class='btn btn-default btn-next'><span class='spage-next'></span></button>";
            pagination += "<input type='number' class='form-control' id='spagePageNumber'/>";
            pagination += "<button class='btn btn-default btn-gopage'>跳转</button>";
            this.$toolbar.append(pagination);

            var that = this;
            that.$toolbar
                .find(".btn-prev")
                .off("click")
                .on("click", function () {
                    that.prev();
                });
            that.$toolbar
                .find(".btn-next")
                .off("click")
                .on("click", function () {
                    that.next();
                });
            that.$toolbar
                .find(".btn-gopage")
                .off("click")
                .on("click", function () {
                    that.goPage();
                });
        },

        /**
         * 渲染分页条
         * @private
         */
        _renderPagination: function () {
            var that = this;
            var $pageInfo = that.$body.find(".spage-data");
            that.pagination = {
                totalPages: parseInt($pageInfo.data("totalpages")),
                totalElements: parseInt($pageInfo.data("totalelements")),
                pageNumber: parseInt($pageInfo.data("pagenumber")),
                isLast: $pageInfo.data("islast") === "true" || $pageInfo.data("islast") === true
            };

            var curPage = that.pagination.pageNumber;
            if(that.options.startPage === 0) {
                curPage++;
            }
            that.$toolbar.find("#spageCurPage").text(curPage);
            that.$toolbar.find("#spageTotalPages").text(that.pagination.totalPages);
            that.$toolbar.find("#spageTotalEelemets").text(that.pagination.totalElements);

            if(that.pagination.totalPages === 0){
                that.$toolbar.hide();
            }else{
                that.$toolbar.show();

                if(that.pagination.pageNumber === 0){
                    that.$toolbar
                        .find(".btn-prev")
                        .hide();
                }else{
                    that.$toolbar
                        .find(".btn-prev")
                        .show();
                }

                if(that.pagination.isLast){
                    that.$toolbar
                        .find(".btn-next")
                        .hide();
                }else{
                    that.$toolbar
                        .find(".btn-next")
                        .show();
                }
            }
        }
    };

    return MDU;
})(jQuery);


(function ($) {
    /* --- Jquery Plugin Definition --- */
    /**
     * Jquery 插件函数
     * @param option
     * @constructor
     */
    function Plugin(option){
        return this.each(function () {
            var $this   = $(this);                                            // 当前元素对象
            var data    = $this.data('bs.spage');                             // 获取实
            var options = $.extend({element: this}, $this.data(), typeof option === 'object' && option);

            // 实例化插件信息
            if (!data) {
                $this.data('bs.spage', (data = new Spage(options)));
            }

            // 如果option是字符串则调用对应方法
            if (typeof option === 'string'){
                if(data[option]){
                    data[option]();
                }else{
                    $.error('Method ' +  option + ' does not exist on jQuery.spage');
                }
            }
        });
    }

    $.fn.spage             = Plugin;
    $.fn.spage.Constructor = Spage;

})(jQuery);
