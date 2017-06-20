/**
 * 短信验证码相关
 *
 * @author huchiwei
 * @create 2016-11-11
 */
var Sms = (function ($, App) {
    var mdu = function (element, mobile, checkUser) {
        this.$element = $(element);
        this.$molie = $(mobile);
        this.checkUser = checkUser || false;

        this.sec = 60;
        this.interval = 0;

        this.canSend = false;

        this._onKeyupForMobile();
        this._onClickForSmsCode();
    };


    mdu.prototype = {
        sendValidCode: function () {
            var that = this;

            App.ajax.post({
                url: "/sys/sms/validcode",
                data: {
                    mobile: that.$molie.val(),
                    checkUser: that.checkUser
                },
                onSuccess: function (resp) {
                    var code = "";
                    if (resp.code) {
                        code = "验证码：" + resp.code;
                    }
                    toast("短信发送成功，请注意查收。" + code);

                    that.toggleAction(false);
                    that.secCountDown();

                    that.$element.trigger("sms.sendSuccess");
                }
            });
        },

        checkValidCode: function (code, success) {
            var that = this;

            App.ajax.post({
                url: "/sys/sms/validcode/check",
                data: {
                    mobile: that.$molie.val(),
                    code: code
                },
                onSuccess: function (resp) {
                    if (!resp.isMatch) {
                        toast("验证码输入不正确", "error");
                    } else {
                        success.call(this);
                    }
                }
            });
        },

        toggleAction: function (enable) {
            // 避免倒数过程中修改手机号码导致按钮状态不对
            if (this.sec !== 60 && this.interval !== 0) {
                return;
            }

            if (enable) {
                this.$element.prop("disabled", false).addClass("btn-success");
                this.canSend = true;
            } else {
                this.$element.prop("disabled", true).removeClass("btn-success");
                this.canSend = false;
            }
        },

        secCountDown: function () {
            var that = this;
            that.interval = setInterval(function () {
                that.sec--;
                that.$element.text(that.sec + "s");

                if (that.sec === 0) {
                    clearInterval(that.interval);
                    that.sec = 60;
                    that.interval = 0;
                    that.$element.text("重新获取");
                    that.toggleAction(true);
                }
            }, 1000);
        },

        reset: function () {
            clearInterval(this.interval);
            this.sec = 60;
            this.interval = 0;
            this.$element.text("重新获取");
            this.toggleAction(true);
        },

        /**
         * 监听手机号码输入框时间
         */
        _onKeyupForMobile: function () {
            var that = this;

            var reg = /^1[3|4|5|7|8][0-9]{9}$/;
            that.$molie.attr("data-parsley-pattern", reg);
            that.$molie.on("keyup blur", function () {
                var val = $(this).val();
                if (reg.test(val)) {
                    that.toggleAction(true);
                } else {
                    that.toggleAction(false);
                }
            });
        },

        /**
         * 监听获取验证码点击事件
         */
        _onClickForSmsCode: function () {
            var that = this;
            that.$element
                .off("click")
                .on("click", function () {
                    if (that.canSend) {
                        that.sendValidCode();
                    }
                });
        }
    };

    return mdu;
})(jQuery, App);
