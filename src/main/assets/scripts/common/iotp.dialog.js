/**
 * 对话框定义
 *
 * @author huchiwei
 * @create 2016-09-09
 */
var Dlg = (function ($) {
    var DEFAULTS = {
        id: "",                                        // 对话框id唯一标识
        title: "",                                     // 标题，String|Function, 不需要标题设为false.
        width: 500,                                    // 宽度，默认auto，最小宽度400px(移动端290px)，最大宽度100%(平板以上屏幕768px)
        height: 400,                                   // 高度，默认auto
        className: '',                                 // 内置样式
        url: "",                                       // 异步获取HTML Url
        data: {},                                      // 参数
        template: "",                                  // 对话框内容，string|Function, 若参数url不为空则此参数无效
        buttons: [],                                   // 按钮数组，例如：[{id: "btnCancel",class: "btn-default",text: "取消"}]
        onOk: null,                                    // 默认确定按钮回调
        onLoaded: null                                 // 内容加载完毕回调事件
    };

    var MDU = function(settings){
        this.$smodal        = null;
        this.options        = $.extend({}, DEFAULTS, settings);
        this.loading        = true;

        if(settings.element){
            var that = this;
            $(settings.element).on("click", function () {
                that._init();
            });
        }else{
            this._init();
        }
    };

    MDU.prototype = {
        // 指定构造器，避免多实例冲突
        constructor: MDU,

        // 关闭对话框
        close: function () {
            this.$smodal.remove();
        },


        // ===========================================
        // private methods
        _init: function () {
            var that = this;
            var modalHtml = that._buildModalHtml(that.options);
            $(document.body).append(modalHtml);
            that.$smodal = $("#" + that.options.id);
            that._loadContent(that);

            var $btnOk = that.$smodal.find("#btnOk");
            if($btnOk.length > 0){
                $btnOk.off().on("click", function () {
                    var result = null;
                    var onOkFn = onOkFn || null;
                    if(typeof onOkFn === "function"){
                        result = onOkFn.call(that);
                    }

                    if(typeof that.options.onOk === "function"){
                        if(result !== null && result !== false){
                            that.options.onOk.call(that, result);
                            that.close();
                        }else{
                            that.options.onOk.call(that);
                        }
                    }else{
                        that.close();
                    }
                });
            }

            that.$smodal.on("click", '[data-dismiss="smodal"]', function () {
                that.close();
            });
        },

        _buildBtns: function () {
            var buttons = this.options.buttons;
            if(buttons.length === 0){
                buttons = [{
                    id: "btnCancel",
                    class: "btn-default",
                    text: "取消"
                },{
                    id: "btnOk",
                    class: "btn-primary",
                    text: "确定"
                }];
            }

            var btnHtml = "";
            $.each(buttons, function (idx, btn) {
                btnHtml += '<button type="button" class="btn '+ btn['class'] +'" id="'+  btn.id +'"';
                if(btn.id === "btnCancel") {
                    btnHtml += ' data-dismiss="smodal" ';
                }
                btnHtml += '>'+ btn.text +'</button>';
            });

            return '<div class="footer">' + btnHtml + '</div>';
        },

        _loadContent: function () {
            var that = this;

            var $body = that.$smodal.find(".body");
            var setting = that.options;
            $body.css({
                width: setting.width,
                height: setting.height
            });

            if(setting.url && setting.url !== ""){
                $.get(setting.url, setting.data, function (htmlData) {
                    $body.html(htmlData);
                    var onLoaded = onLoaded || null;
                    if(typeof onLoaded === 'function'){
                        onLoaded.call(that);
                    }else if(typeof that.options.onLoaded === "function"){
                        that.options.onLoaded.call(that);
                    }
                })
            }else{
                var body = "";
                if(typeof setting.template === 'function'){
                    body = setting.template.call();
                }else{
                    body = setting.template;
                }
                $body.html(body);
            }
        },

        _buildLoading: function () {
            return '<div class="loading"><i class="fa fa-spinner fa-pulse fa-2x fa-fw"></i><span class="sr-only">努力加载中...</span></div>';
        },

        _buildModalHtml: function () {
            var setting = this.options;
            if(setting.id === "") {
                setting.id = new Date().getTime() + Math.floor(Math.random() * (1000 - 100) + 100);
            }

            var modalHtml = '<div class="smodal '+ setting.className + '" id="'+ setting.id +'">';
            modalHtml += '  <div class="backdrop">';
            modalHtml += '      <div class="content">';

            // 是否需要标题
            if(typeof setting.title === "function") {
                setting.title = setting.title();
            }
            if(setting.title !== false){
                modalHtml += '      <div class="heading">';
                modalHtml += '          <button type="button" class="close" data-dismiss="smodal"><span aria-hidden="true">×</span><span class="sr-only">Close</span></button>';
                modalHtml += '          <h4>'+ setting.title +'</h4>';
                modalHtml += '      </div>';
            }

            modalHtml += '<div class="body">'+ this._buildLoading() +'</div>';

            // 添加按钮
            modalHtml += this._buildBtns(setting.buttons);

            modalHtml += '      </div>';
            modalHtml += '  </div>';
            modalHtml += '</div>';
            return modalHtml;
        }
    };

    return MDU;
}(jQuery));

(function ($) {
    /* --- jquery plugin Definition --- */

    /**
     * jquery plugin 定义
     *
     * @param option
     * @constructor
     */
    function Plugin(option) {
        return this.each(function () {
            var $this = $(this);                                          // 当前元素对象
            var data = $this.data('bs.dlg');                             // 获取实例
            var options = $.extend({element: this}, $this.data(), typeof option === 'object' && option);

            // 实例化插件信息
            if (!data) {
                $this.data('bs.dlg', (data = new Dlg(options)));
            }

            // 如果option是字符串则调用对应方法
            if (typeof option === 'string') {
                if (data[option]) {
                    data[option]();
                } else {
                    $.error('Method ' + option + ' does not exist on jQuery.dlg');
                }
            }
        });
    }

    $.fn.dlg = Plugin;
    $.fn.dlg.Constructor = Dlg;
}(jQuery));

