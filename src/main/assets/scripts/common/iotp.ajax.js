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
