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
