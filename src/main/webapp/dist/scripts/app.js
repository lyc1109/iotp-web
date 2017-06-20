/**
 * 品智云通用APP脚本入口
 *
 * @author hucw
 * @date   2016-11-09
 */
var App = (function () {
    return {
        // 常量相关方法
        constants: {
            formatGender: function (gender) {
                if (gender === "M") {
                    return "男";
                } else if (gender === "F") {
                    return "女";
                } else if (gender === "N") {
                    return "未知";
                } else {
                    return gender;
                }
            },

            formatAge: function (age) {
                var ages = {
                    5: "50后",
                    6: "60后",
                    7: "70后",
                    8: "80后",
                    9: "90后",
                    0: "00后"
                };
                return ages[age];
            }
        },

        // 缓存相关方法
        cache: {
            put: function (key, value) {
                window.localStorage.setItem(key, value);
            },

            get: function (key) {
                return window.localStorage.getItem(key);
            },

            getAsBoolean: function (key) {
                var value = window.localStorage.getItem(key);
                return value === true || value === "true" || value === 1 || value === -1;
            }
        }
    };
}());

/**
 * 短消息提示组件
 *
 * @author huchiwei
 * @create 2016-09-21
 */

var toast = (function (toastr) {
    // 默认配置
    var DEFAULTS = {
        positionClass: 'toast-top-center',
        showMethod: 'slideDown',
        showDuration: 600,
        timeOut: 3000
    };

    var _methods = {
        success: function(message){
            toastr.success(message);
        },
        error: function(message){
            toastr.error(message);
        },
        info: function(message){
            toastr.info(message);
        },
        warn: function(message){
            toastr.warn(message);
        }
    };

    return function (message, type, config) {
        config = config || {};
        $.extend(toastr.options, DEFAULTS, config);

        type = type || "success";
        if (_methods[type]) {
            _methods[type](message);
        } else {
            console.log("toast method is not found:" + type + ", you can pass method: success|error|warn|info");
        }
    };
}(window.toastr));

/**
 * 消息提示组件
 *
 * @author huchiwei
 * @create 2016-09-28
 */

var Messenger = (function (swal) {
    /**
     * 处理配置
     *
     * @param message
     * @param type
     * @param options
     */
    function getSetting(message, type, options){
        var DEFAULTS = {
            title: '',
            message: '',
            type: 'success',
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            allowEscapeKey: false,
            allowOutsideClick: false
        };

        if(typeof message === "string"){
            return $.extend({}, DEFAULTS, options || {}, {title: message, type: type});
        }else{
            var settings =  $.extend({}, DEFAULTS, options || {}, {type: type}, message);
            settings.text = settings.message;
            return settings;
        }
    }

    return {
        alert: function (message, callback) {
            swal(message)
                .then(function () {
                    if (typeof callback === "function") {
                        callback.call(this);
                    }
                    swal.close();
                });
        },

        info: function (message, callback) {
            swal(getSetting(message, "info"))
                .then(function () {
                    if (typeof callback === "function") {
                        callback.call(this);
                    }
                    swal.close();
                });
        },

        success: function (message, callback) {
            swal(getSetting(message, "success"))
                .then(function () {
                    if (typeof callback === "function") {
                        callback.call(this);
                    }
                    swal.close();
                });
        },

        warning: function (message, callback) {
            swal(
                getSetting(message, "warning", {
                    showCancelButton: true,
                    confirmButtonColor: "#f0ad4e"
                }))
                .then(function () {
                    if (typeof callback === "function") {
                        callback.call(this);
                    }
                    swal.close();
                });
        },

        error: function (message, callback) {
            swal(getSetting(message, "error", {
                showCancelButton: true,
                confirmButtonColor: "#d9534f"
            }))
                .then(function () {
                    if (typeof callback === "function") {
                        callback.call(this);
                    }
                    swal.close();
                });
        },

        confirm: function (message, callback) {
            swal(getSetting(message, "question", {showCancelButton: true}))
                .then(function () {
                    if (typeof callback === "function") {
                        callback.call(this);
                    }
                    swal.close();
                });
        },

        propup: function (message, callback) {
            var setting = {
                title: "",
                text: "",
                input: "text",
                inputPlaceholder: "",
                inputValue: "",
                showCancelButton: true
            };

            if (typeof message === "string") {
                setting = getSetting(message, "", setting);
            } else {
                setting = $.extend(setting, message);
                setting = getSetting(setting.message, "", setting);
            }

            setting.inputValidator = function (value) {
                return new Promise(function (resolve, reject) {
                    if (value) {
                        resolve()
                    } else {
                        reject('请填写内容！')
                    }
                })
            };

            swal(setting)
                .then(function (inputValue) {
                    if (typeof callback === "function") {
                        callback.call(this, inputValue);
                    }
                    swal.close();
                });
        },

        close: function () {
            swal.close();
        }
    };
}(window.swal));

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


/**
 * 对jquery ajax请求封装处理
 *
 * @author huchiwei
 * @create 2016-11-11
 */

(function (App) {

    var DEFAULTS = {
        url: "",
        data: {},
        dataType: "json",
        refresh: true,

        onSuccess: null,
        onFail: null,
        onFinally: null
    };

    var successCallBack = function (setting, resp) {
        if (resp.returnCode === 0) {
            if (typeof setting.onSuccess === "function") {
                setting.onSuccess.call(this, resp);
            } else {
                toast("操作成功");

                if (setting.refresh === true) {
                    window.location.reload();
                }
            }
        } else {
            if (typeof setting.onFail === "function") {
                setting.onFail.call(this, resp);
            } else {
                var error = "抱歉，系统异常无法执行操作。";
                if (resp.returnMsg) {
                    error = resp.returnMsg;
                }
                toast(error, "error");
            }
        }
    };

    var failCallBack = function (setting, resp) {
        if (typeof setting.onFail === "function") {
            setting.onFail.call(this, resp);
        } else {
            var error = "抱歉，系统异常无法执行操作。";
            if (resp.responseText) {
                try {
                    error = JSON.parse(resp.responseText).returnMsg;
                } catch (err) {
                    // do nothing
                }
            }
            toast(error, "error");
        }
    };

    App.ajax = {
        /**
         * ajax post 请求
         * @param setting 详细参考默认参数
         */
        post: function (setting) {
            // show Loading

            var _options = $.extend({}, {type: "POST"}, DEFAULTS, setting);

            $.ajax(_options)
                .done(function (resp) {
                    successCallBack(_options, resp);
                })
                .fail(function (resp) {
                    failCallBack(_options, resp);
                })
                .always(function () {
                    if (typeof _options.onFinally === "function") {
                        _options.onFinally.call(this);
                    }
                });
        },

        /**
         * ajax get 请求
         * @param setting 详细参考默认参数
         */
        get: function (setting) {
            // show Loading

            var _options = $.extend({}, {type: "GET"}, DEFAULTS, setting);

            $.ajax(_options)
                .done(function (resp) {
                    successCallBack(_options, resp);
                })
                .fail(function (resp) {
                    failCallBack(_options, resp);
                })
                .always(function () {
                    if (typeof _options.onFinally === "function") {
                        _options.onFinally.call(this);
                    }
                });
        },

        /**
         * ajax patch 请求
         * @param setting 详细参考默认参数
         */
        patch: function (setting) {
            // show Loading
            var _options = $.extend({}, {type: "PATCH"}, DEFAULTS, setting);

            $.ajax(_options)
                .done(function (resp) {
                    successCallBack(_options, resp);
                })
                .fail(function (resp) {
                    failCallBack(_options, resp);
                })
                .always(function () {
                    if (typeof _options.onFinally === "function") {
                        _options.onFinally.call(this);
                    }
                });
        },

        /**
         * ajax delete 请求
         * @param setting 详细参考默认参数
         */
        del: function (setting) {
            // show Loading
            var _options = $.extend({}, {type: "DELETE"}, DEFAULTS, setting);

            $.ajax(_options)
                .done(function (resp) {
                    successCallBack(_options, resp);
                })
                .fail(function (resp) {
                    failCallBack(_options, resp);
                })
                .always(function () {
                    if (typeof _options.onFinally === "function") {
                        _options.onFinally.call(this);
                    }
                });
        }
    };
}(App));

/**
 * app通用初始化JS
 *
 * @author huchiwei
 * @create 2016-09-13
 */
$(function () {
    // 手机隐藏/显示菜单事件
    $(document).on('click', '[data-toggle-state="aside-toggle"]', function (e) {
        $("body").toggleClass("sidebar-open");
    });

    // 大屏幕收起/展开事件
    $(document).on('click', '[data-toggle-state="aside-collapsed"]', function (e) {
        $("body").toggleClass("sidebar-collapsed");

        // 缓存展开与否
        App.cache.put("collapsed", $("body").hasClass("sidebar-collapsed"));

        // 移除收起后展开的二级菜单
        if(!$("body").hasClass("sidebar-collapsed")){
            var $existSubNav = $(".sidebar").find("#subnav");
            if($existSubNav.length > 0){
                $existSubNav.remove();
            }
        }
    });

    // 判断是否已设置收起
    if(App.cache.getAsBoolean("collapsed") === true){
        $("body").addClass("sidebar-collapsed");
    }else{
        // 平板默认收起
        if($(window).width() > 767 && $(window).width() < 900){
            $("body").addClass("sidebar-collapsed");
        }
    }

    // 菜单点击事件
    var _top = 0;
    $(document).on('click', '.sidebar-item', function (e) {
        if($(this).find(".sidebar-subnav").length === 0) {
            return;
        }

        if(!$("body").hasClass("sidebar-collapsed")){
            // 收起除当前元素外其它已展开二级菜单
            $(".sidebar-item")
                .not($(this))
                .find(".collapse.in")
                .removeClass("in");
            $(".sidebar-item")
                .not($(this))
                .find("a .pull-right")
                .removeClass("fa-angle-up")
                .addClass("fa-angle-down");

            $(this).off('hidden.bs.collapse,shown.bs.collapse')
                .on('hidden.bs.collapse', function () {
                    $(this)
                        .find("a .pull-right")
                        .removeClass("fa-angle-up")
                        .addClass("fa-angle-down");
                })
                .on('shown.bs.collapse', function () {
                    $(this)
                        .find("a .pull-right")
                        .removeClass("fa-angle-down")
                        .addClass("fa-angle-up");
                });
        }else{
            // 先移除已显示二级菜单
            if(_top > 0 || _top === $(this).offset().top){
                var $existSubNav = $(".sidebar").find("#subnav");
                if($existSubNav.length > 0){
                    $existSubNav.remove();
                }
            }

            // 重复点击则直接返回并重置标识
            if(_top === $(this).offset().top){
                _top = 0;
                return;
            }

            // 浮动显示二级菜单
            _top = $(this).offset().top;
            var $subnav = $(this).find(".sidebar-subnav")
                .clone();
            $subnav
                .removeAttr("style")
                .attr("id", "subnav")
                .addClass("subnav-floating")
                .css("top", _top)
                .appendTo($(".sidebar"));
        }
    });

    // 初始化Jquery通用插件
    $('.icheck').iCheck({
        checkboxClass: 'icheckbox_square-blue',
        radioClass: 'iradio_square-blue'
    });

    // 初始化select2
    $(".select2").select2({language: 'zh-CN'});

    // 日期选择
    $(".date-picker").datetimepicker({
        format: 'yyyy-mm-dd',
        minView: 2,
        todayBtn: true,
        todayHighlight: true,
        keyboardNavigation: true,
        language: 'zh-CN',
        autoclose: true
    });
    $(".datetime-picker").datetimepicker({
        format: 'yyyy-mm-dd hh:ii',
        todayBtn: true,
        todayHighlight: true,
        keyboardNavigation: true,
        language: 'zh-CN',
        autoclose: true
    });

    // 数字格式化
    $(".currency").accounting('formatMoney', { "symbol": "" });
    $(".currency-rmb").accounting('formatMoney', { "symbol": "￥" });
    $(".float").accounting('formatNumber', { "precision": 2 });

    // 返回
    $(".goBack").on("click", function () {
        window.history.back();
    });

    // 颜色选择
    // $('INPUT.minicolors').minicolors({control: $(this).attr('data-control') || 'wheel'});

    // 初始化表单校验
    if($(".parsley-form").length > 0){
        $(".parsley-form").parsley({
            trigger: "blur",
            excluded: 'input[type=button], input[type=submit], input[type=reset]'
        });
    }

    // 替换图片错误
    if($("img").length > 0){
        $('img').each(function(){
            var error = false;
            if (!this.complete) {
                error = true;
            }

            if (typeof this.naturalWidth !== "undefined" && this.naturalWidth === 0) {
                error = true;
            }

            if(error){
                $(this)
                    .bind('error.replaceSrc',function(){
                        this.src = "/dist/images/placeholder.png";

                        $(this).unbind('error.replaceSrc');
                    })
                    .trigger('load');
            }
        });
    }

    // 轮询未读消息数量
    var getUnreadNofifyCount = function () {
        App.ajax.get({
            url: "/notify/unread",
            onSuccess: function (resp) {
                $("#unreadNotifyCount").text(resp.countUnread);
                if(resp.countUnread > 0){
                    $("#unreadNotifyCount").show();
                }else{
                    $("#unreadNotifyCount").hide();
                }
            },
            onFail: function () {
                // 不做任何提示
                $("#unreadNotifyCount").hide();
            }
        });
    };
    setInterval(function () {
        getUnreadNofifyCount();
    }, 10000);
    getUnreadNofifyCount();

    $("#sideUl").on("click",function(){
        sessionStorage.setItem('fundType','')
        sessionStorage.setItem('fundStartDate','')
        sessionStorage.setItem('fundEndDate','')
        sessionStorage.setItem('fundSearch','')
        sessionStorage.setItem('fundSize',1)
        sessionStorage.setItem('devicePage',1)
        sessionStorage.setItem('appCurrent',1)
        sessionStorage.setItem('deviceType','')
        sessionStorage.setItem('deviceProduct','')
        sessionStorage.setItem('deviceSearch','')
        sessionStorage.setItem('tabName','交易记录')
    })
});



/**
 * 浏览器处理
 *
 * @author huchiwei
 * @create 2016-11-11
 */

(function ($, App) {

    /**
     * 浏览器处理构造函数
     *
     * @constructor
     */
    function Browser() {
        this.userAgent = navigator.userAgent.toLowerCase();
        this.platform = navigator.platform;
        this.appName = navigator.appName;
        this.appVersion = navigator.appVersion;
        this.clientStrings = [
            { name: 'Windows 10', reg: /(Windows 10.0|Windows NT 10.0)/ },
            { name: 'Windows 8.1', reg: /(Windows 8.1|Windows NT 6.3)/ },
            { name: 'Windows 8', reg: /(Windows 8|Windows NT 6.2)/ },
            { name: 'Windows 7', reg: /(Windows 7|Windows NT 6.1)/ },
            { name: 'Windows Vista', reg: /Windows NT 6.0/ },
            { name: 'Windows Server 2003', reg: /Windows NT 5.2/ },
            { name: 'Windows XP', reg: /(Windows NT 5.1|Windows XP)/ },
            { name: 'Windows 2000', reg: /(Windows NT 5.0|Windows 2000)/ },
            { name: 'Windows ME', reg: /(Win 9x 4.90|Windows ME)/ },
            { name: 'Windows 98', reg: /(Windows 98|Win98)/ },
            { name: 'Windows 95', reg: /(Windows 95|Win95|Windows_95)/ },
            { name: 'Windows NT 4.0', reg: /(Windows NT 4.0|WinNT4.0|WinNT|Windows NT)/ },
            { name: 'Windows CE', reg: /Windows CE/ },
            { name: 'Windows 3.11', reg: /Win16/ },
            { name: 'Android', reg: /Android/ },
            { name: 'Open BSD', reg: /OpenBSD/ },
            { name: 'Sun OS', reg: /SunOS/ },
            { name: 'Linux', reg: /(Linux|X11)/ },
            { name: 'iOS', reg: /(iPhone|iPad|iPod)/ },
            { name: 'Mac OS X', reg: /Mac OS X/ },
            { name: 'Mac OS', reg: /(MacPPC|MacIntel|Mac_PowerPC|Macintosh)/ },
            { name: 'QNX', reg: /QNX/ },
            { name: 'UNIX', reg: /UNIX/ },
            { name: 'BeOS', reg: /BeOS/ },
            { name: 'OS/2', reg: /OS\/2/ },
            { name: 'Search Bot', reg: /(nuhk|Googlebot|Yammybot|Openbot|Slurp|MSNBot|Ask Jeeves\/Teoma|ia_archiver)/ }
        ];
    }

    Browser.prototype = {
        getBrowserInfo: function () {
            var that = this;
            var tem = [];

            // ['Chrome/52','Chrome','52']
            var bsInfo = that.userAgent.match(/(opera|chrome|safari|firefox|msie|trident(?=\/))\/?\s*(\d+)/i) || [];
            if (/trident/i.test(bsInfo[1])) {
                tem = /\brv[ :]+(\d+)/g.exec(that.userAgent) || [];
                bsInfo[1] = 'IE';
                bsInfo[2] = tem[1] || ''; // IE浏览器版本
            }

            if (bsInfo[1] === 'Chrome') {
                tem = that.userAgent.match(/\b(OPR|Edge)\/(\d+)/);
                if (tem !== null) {
                    bsInfo[1] = 'Opera';
                    bsInfo[2] = tem[1] || '';
                }
            }

            bsInfo = bsInfo[2] ? [bsInfo[1], bsInfo[2]] : [that.appName, that.appVersion, '-?'];
            if ((tem = that.userAgent.match(/version\/(\d+)/i)) !== null) {
                bsInfo.splice(1, 1, tem[1]);
            }

            return {
                browser: bsInfo[0],
                version: bsInfo[1]
            }
        },

        getOSInfo: function () {
            var that = this;
            var userAgent = navigator.userAgent;

            var osInfo = {
                os: '',
                version: ''
            };

            // 匹配系统版本
            $.each(that.clientStrings, function (idx, item) {
                if (item.reg.test(userAgent)) {
                    osInfo.os = item.name;
                    return false;  // break
                }
            });

            // 精确匹配Window系统
            if (/Windows/.test(osInfo.os)) {
                osInfo.version = /Windows (.*)/.exec(osInfo.os)[1];
                osInfo.os = 'Windows';
            }

            // 匹配移动系统
            switch (osInfo.os) {
                case 'Mac OS X':
                    osInfo.version = /Mac OS X (10[\.\_\d]+)/.exec(userAgent)[1];
                    break;

                case 'Android':
                    osInfo.version = /Android ([\.\_\d]+)/.exec(userAgent)[1];
                    break;

                case 'iOS':
                    osInfo.version = /OS (\d+)_(\d+)_?(\d+)?/.exec(that.appVersion);
                    osInfo.version = osInfo.version[1] + '.' + osInfo.version[2] + '.' + (osInfo.version[3] | 0);
                    break;

                default:
                    osInfo.version = "未知版本";
                    break;
            }

            return osInfo;
        }
    };

    App.browser = Browser;
}(jQuery, App));

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

/**
 * Jquery扩展
 *
 * @author hucw
 * @date   2016-11-09
 */
(function ($) {

    /**
     * Form数据序列化为JSON格式
     * @returns form json
     */
    $.fn.serializeObject = function(){
        var o = {};
        var a = this.serializeArray();
        $.each(a, function() {
            if (o[this.name] !== undefined) {
                if (!o[this.name].push) {
                    o[this.name] = [o[this.name]];
                }
                o[this.name].push(this.value || '');
            } else {
                o[this.name] = this.value || '';
            }
        });

        return o;
    };
}(jQuery));

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
