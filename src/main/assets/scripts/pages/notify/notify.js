/**
 * 消息通知脚本
 *
 * 使用方式： new UserNotify(setting)
 *
 * @author huchiwei
 * @create 2016-12-29
 */

var UserNotify = (function ($, App, moment) {

    var mdu = {
        resetUnReadCount: function (count) {
            $("#unreadNotifyCount").text(count);
            if(count > 0){
                $("#unreadNotifyCount").show();
            }else{
                $("#unreadNotifyCount").hide();
            }
        },

        getUnreadCount: function () {
            var that = this;
            App.ajax.get({
                url: "/notify/unread",
                onSuccess: function (resp) {
                    that.resetUnReadCount(resp.countUnread);
                },
                onFail: function () {
                    // 不做任何提示
                    $("#unreadNotifyCount").hide();
                }
            });
        },

        readAll: function (callback) {
            var that = this;
            App.ajax.post({
                url: "/notify/readAll",
                onSuccess: function () {
                    that.resetUnReadCount(0);

                    if(typeof callback === "function") {
                        callback.call(that);
                    }
                }
            });
        },

        markRead: function (id, callback) {
            var that = this;
            App.ajax.post({
                url: "/notify/"+ id +"/read",
                onSuccess: function () {
                    that.resetUnReadCount(parseInt($("#unreadNotifyCount").text())-1);

                    if(typeof callback === "function") {
                        callback.call(that);
                    }
                }
            });
        }
    };

    return mdu;
})(jQuery, App, moment);

