/**
 * 服务商注册表单脚本处理
 *
 * @author huchiwei
 * @create 2016-11-11
 */
var ShopVendorJoinForm = (function ($, App, Sms, Uploader) {
    var smsInstance = null;

    var step = 0;
    var isExistShop = false;
    var hasCheckedCode = false;

    var isChecking = false;

    var checkVendorMobile = "";     // 已校验服务商的手机号码，避免重复校验
    var smsCodeMobile = "";         // 已获取验证码的手机号码，修改手机号码需重新校验验证码

    var mdu = {
        initForm: function () {
            // 初始化地址选择
            $("#distpicker").distpicker({
                placeholder: false
            });
            $("#province,#city")
                .on("change", function () {
                    if($("#area").find("option").length===0){
                        $("#area").hide();
                    }else{
                        $("#area").show();
                    }
                })
                .trigger("change");

            // Logo选择
            this.initImgUploader();

            // 短信验证
            smsInstance  = new Sms("#btnSentSmsCode", "#userMobile");
            $("#btnSentSmsCode").on("sms.sendSuccess", function () {
                console.log("验证码发送成功");
                smsCodeMobile = $("#userMobile").val();
            });

            // 检查服务商信息
            $("#userMobile").on("keyup blur", function () {
                var reg = /^1[3|4|5|7|8][0-9]{9}$/;
                var mobile = $(this).val();
                if(reg.test(mobile) && !isChecking && checkVendorMobile !== mobile){
                    if($(this).is(":focus")) {
                        $(this).blur();
                    }

                    mdu.findExistShops(mobile);
                    $("#contactTelephone").val(mobile);
                    $("#contactMobile").val(mobile);
                    $("#contactMobileText").val(mobile);
                    checkVendorMobile = mobile;
                }
            });

            // 检查2次密码是否一致
            $("#userLoginPwd,#confirmLoginPwd").on("blur", function () {
               if($("#confirmLoginPwd").val() !== "" && $("#userLoginPwd").val() !== "") {
                   mdu.confirmPassword();
               }
            });

            // 下一步
            $(".cmd-next").on("click", function () {
                mdu.nextStep();
            });
            $(".cmd-prev").on("click", function () {
                mdu.prevStep();
            });

            // 新建表单
            $(document).on("click", ".cmd-new-shop", function () {
                $("#shopsContainer").hide();
                $("#shopInfos").show();
                $(":radio[name=shopId]").prop("checked", false);
                if(isExistShop) {
                    $(".cmd-select-shop")
                        .closest("div")
                        .show();
                }
            });
            $(document).on("click", ".cmd-select-shop", function () {
                $("#shopsContainer").show();
                $("#shopInfos").hide();
                $(":radio[name=shopId]:first").prop("checked", true);
            });

            // 提交表单
            $(".cmd-save").on("click", function () {
                mdu.submitForm();
            });
        },

        initImgUploader: function () {
            new Uploader({
                id: "btnImageUploader",
                prefix: "ShopLogo",
                onComplete: function (resp) {
                    var img = resp.files[0];
                    $("#shopLogo").attr("src", img.path);
                    $("#logoImageId").val(img.id);
                }
            });

            // 上传资质文件
            new Uploader({
                id: "btnSelectBusinessLicenseImg",
                onComplete: function (resp) {
                    var img = resp.files[0];
                    $("#businessLicenseImg").attr("src", img.path);
                    $("#businessLicenseImg")
                        .closest("div")
                        .show();
                    $("#businessLicenseImgId").val(img.id);
                }
            });
        },

        findExistShops: function (mobile) {
            $("body").mask("正在检查服务商信息");
            isChecking = true;
            App.ajax.get({
                url: "/shop/vendor/find/exist",
                data: {
                    mobile: mobile,
                    factoryShopId: $("#shopFactoryId").val()
                },
                onSuccess: function (resp) {
                    if(resp.user){
                        $("#userId").val(resp.user.id);
                        $("#userInfos").hide();
                        $("#userInfos")
                            .find(".control-label")
                            .removeClass("required");
                        $("#userInfos")
                            .find(".form-control")
                            .removeAttr("data-parsley-required");
                    } else {
                        $("#userId").val("");
                        $("#userInfos").show();
                        $("#userInfos")
                            .find(".control-label")
                            .addClass("required");
                        $("#userInfos")
                            .find(".form-control")
                            .attr("data-parsley-required", true);
                    }

                    if(resp.isExist) {
                        isExistShop = true;

                        var tpl = $.templates("#shopTpl");
                        $("#shopsContainer")
                            .html(tpl.render({
                                shops: resp.shops
                            }))
                            .show();

                        $(":radio[name=shopId]:first").prop("checked", true);
                        $("#shopInfos").hide();
                    } else {
                        isExistShop = false;

                        $("#shopsContainer").hide();
                        $("#shopInfos").show();
                    }
                },
                onFinally: function () {
                    isChecking = false;
                    setTimeout(function () {
                        $("body").unmask();
                    }, 1000);
                }
            })
        },

        confirmPassword: function () {
            var pwd1 = $("#userLoginPwd").val();
            var pwd2 = $("#confirmLoginPwd").val();
            if(pwd1 !== pwd2){
                $("#confirmLoginPwdError").show();
            }else{
                $("#confirmLoginPwdError").hide();
            }
        },

        nextStep: function () {
            if(step >= 2) {
                return;
            }

            if(step === 0){
                var userFormParsley = $('#userRegisterForm').parsley();
                if(userFormParsley.validate() !== true){
                    return;
                }

                // 手机号码修改过需要重新验证验证码
                if(smsCodeMobile !== "" && smsCodeMobile !== $("#userMobile").val()) {
                    hasCheckedCode = false;
                    smsInstance.reset();
                    $("#smsCode").val("");
                    toast("请重新获取验证码", "error");
                    return;
                }

                if(hasCheckedCode){
                    $('#userRegisterForm').hide();
                    $('#shopVendorForm').fadeIn();
                    if(!isExistShop) {
                        $("#shopsContainer").hide();
                        $("#shopInfos").show();
                    }

                    step++;
                    $(".step-item").removeClass("active");
                    $(".step-item")
                        .eq(step)
                        .addClass("active");
                } else {
                    smsInstance.checkValidCode($("#smsCode").val(), function () {
                        hasCheckedCode = true;

                        $('#userRegisterForm').hide();
                        $('#shopVendorForm').fadeIn();

                        if(!isExistShop) {
                            $("#shopsContainer").hide();
                            $("#shopInfos").show();
                        }

                        step++;
                        $(".step-item").removeClass("active");
                        $(".step-item")
                            .eq(step)
                            .addClass("active");
                    });
                }

            } else if(step === 1) {
                $('#userRegisterForm').hide();
                $('#shopVendorForm').hide();
                $('#successTips').show();

                step++;
                $(".step-item").removeClass("active");
                $(".step-item")
                    .eq(step)
                    .addClass("active");
            }
        },

        prevStep: function () {
            if(step === 0) {
                return;
            }

            if(step === 1){
                $('#userRegisterForm').fadeIn();
                $('#shopVendorForm').hide();
            }

            step--;
            $(".step-item").removeClass("active");
            $(".step-item")
                .eq(step)
                .addClass("active");
        },

        checkIsExistName: function (shopName, callback) {
            App.ajax.get({
                url: "/shop/shop/isExist",
                data: {
                    shopName: shopName
                },
                onSuccess: function (resp) {
                    callback.call(this, resp.isExist);
                }
            })
        },

        submitForm: function () {
            var selectShopId = "";
            if(isExistShop && $(":radio[name=shopId]:checked").length > 0) {
                selectShopId = $(":radio[name=shopId]:checked").val();
            }

            if(selectShopId === ""){
                var instance = $('#shopVendorForm').parsley();
                if(instance.validate() !== true){
                    return;
                }

                if($("#logoImageId").val() === "" || $("#logoImageId").val() === null) {
                    toast("请选择公司Logo", "error");
                    return false;
                }

                if($("#businessLicenseImgId").val() === "" || $("#businessLicenseImgId").val() === null) {
                    toast("请上传资质文件图片，如营业执照", "error");
                    return false;
                }

                if(!$("#agreement").is(":checked")){
                    toast("您必须同意并遵守《品智云服务平台用户服务协议》才可以提交申请哦！", "error");
                    return false;
                }

                mdu.checkIsExistName($("#shopName").val(), function (isExist) {
                    if(isExist) {
                        Messenger.error("抱歉，该企业名称已存在，请重新填写。")
                    } else {
                        mdu.doSubmitForm();
                    }
                });
            } else {
                mdu.doSubmitForm();
            }
        },

        doSubmitForm: function () {
            Messenger.confirm("是否确定提交服务商申请?", function () {
                var data = $("#shopVendorForm").serializeObject();
                data = $.extend({}, data, $("#userRegisterForm").serializeObject());

                App.ajax.post({
                    url: "/shop/vendor/join/" + $("#shopId").val() + "/save",
                    data: data,
                    onSuccess: function () {
                        mdu.nextStep();
                    },
                    onFail: function (resp) {
                        var msg = "抱歉，申请注册失败。";
                        if (resp.returnMsg && resp.returnMsg !== null) {
                            msg += "原因：" + resp.returnMsg;
                        }
                        Messenger.error(msg);
                    }
                });
            });
        }
    };

    return mdu;
})(jQuery, App, Sms, Uploader);
