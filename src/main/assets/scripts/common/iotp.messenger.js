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
