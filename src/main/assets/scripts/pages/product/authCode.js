/**
 * 二维码处理脚本
 *
 * @author huchiwei
 * @create 2016-12-08
 */
var AuthCode = (function (App) {
    var baseUrl = "/shop/product/authCode";
    var authCodeGrid = null;

    var mdu = {
        /**
         * 初始化Grid
         *
         * @param gridId grid容器id
         * @param data   grid请求参数
         */
        initGrid: function (gridId, data) {
            authCodeGrid = new BootGrid({
                id: gridId,
                url: baseUrl,
                data: data || {},
                formatters: {
                    formatSn: function (column, rows) {
                        return rows.authCodeStart + "~" + rows.authCodeEnd;
                    },
                    productCodeFormatter: function (column, rows) {
                        if($("#type").val() === "leaseProduct"){
                            return rows.leaseProductCode;
                        }else{
                            return rows.itemCode;
                        }
                    },
                    productNameFormatter: function (column, rows) {
                        if($("#type").val() === "leaseProduct"){
                            return rows.leaseProductName;
                        }else{
                            return rows.product.name;
                        }
                    },
                    commands: function (column, rows) {
                        var btns = '<button type="button" class="btn btn-xs btn-default cmd-qrcode" ';
                        btns += 'data-row-id="'+ rows.id +'"';
                        btns += '>打印二维码</button> ';

                        btns += ' <button type="button" class="btn btn-xs btn-default cmd-used" data-row-id="'+ rows.id +'">使用记录</button>';
                        return btns;
                    }
                },
                onLoaded: function () {
                    $(".cmd-qrcode")
                        .off("click")
                        .on("click", function () {
                            mdu.openQRCodeDlg($(this).data("rowId"), data);
                        });

                    $(".cmd-used")
                        .off("click")
                        .on("click", function () {
                            mdu.viewUsed($(this).data("rowId"));
                        });
                }
            })
        },

        gridReload: function () {
            if(authCodeGrid !== null) {
                authCodeGrid.reload();
            }
        },

        openCreateDlg: function (data, callback) {
            new Dlg({
                id: "createAuthCodeDlg",
                title: "新建产品授权信息",
                height: 300,
                url: baseUrl + "/create",
                data: data,
                onOk: function () {
                    var instance = $('#authCodeForm').parsley();
                    if(instance.validate() !== true){
                        return;
                    }

                    var formData = $("#authCodeForm").serializeObject();
                    if(parseInt(formData.codeCount) > 1000){
                        toast("每次授权数量不得超于1000", "error");
                        return;
                    }

                    /*if(formData.productionLine === "" || formData.productionLine.length === 0){
                        toast("请填写生产线", "error");
                        return ;
                    }*/

                    var that = this;
                    mdu.save(formData, function () {
                        that.close();

                        if(typeof callback === "function") {
                            callback.call(this);
                        }
                    });
                }
            });
        },

        save: function (formData, callback) {
            $("body").mask('<i class="fa fa-spinner fa-pulse"></i> 正在保存...');
            App.ajax.post({
                url: baseUrl + "/-1",
                data: formData,
                onSuccess: function (resp) {
                    if(typeof callback === "function") {
                        callback.call(this, resp);
                    }
                },
                onFinally: function () {
                    $("body").unmask();
                }
            });
        },

        openQRCodeDlg: function (confuseId, data) {
            new Dlg({
                title: "打印下载二维码",
                url: baseUrl + "/" + confuseId + "/qrcode/toPrint",
                data: data || {},
                height: 270,
                onOk: function () {
                    var that = this;
                    var data = $.extend({}, data, {
                        size: $("#qrcodeSize").val()
                    });
                    mdu.doPrint($("#authCodeId").val(), data, function () {
                        that.close();
                    });
                }
            });
        },

        /**
         * 执行打印二维码操作
         *
         * @param authCodeId
         * @param data
         * @param callBack
         */
        doPrint: function (authCodeId, data, callBack) {
            $("body").mask('<i class="fa fa-spinner fa-pulse"></i> 正在处理...');
            var winRef = window.open("/shop/product/authCode/qrcode/predownload", "正在下载，请稍候...", "_blank");
            App.ajax.get({
                url: baseUrl + "/" + authCodeId + "/qrcode/doPrint",
                data: data,
                onSuccess: function (resp) {
                    if(typeof callBack === "function") {
                        callBack.call(this, resp);
                    }

                    setTimeout(function () {
                        winRef.location = baseUrl + "/" + authCodeId + "/qrcode/download?qrcodePath=" + resp.folder;
                    }, 600);
                },
                onFail: function () {
                    Messenger.error({
                        title: "错误提示",
                        message: "抱歉，二维码打印失败。"
                    });
                },
                onFinally: function () {
                    $("body").unmask();
                }
            });
        },

        /**
         * 查看授权码使用情况
         *
         * @param confuseId
         */
        viewUsed: function (confuseId) {
            new Dlg({
                title: "授权码使用情况",
                url: baseUrl + "/" + confuseId + "/viewUsed",
                width: 800,
                height: 450,
                onLoaded: function () {
                    new BootGrid({
                        id: "authCodeUsedGrid",
                        url: baseUrl + "/" + confuseId + "/viewUsed/page",
                        rowCount: 10
                    });
                }
            });
        }
    };


    return mdu;
})(App);

