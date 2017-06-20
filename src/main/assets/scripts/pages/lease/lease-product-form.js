/**
 * lease-product-form
 *
 * @author huchiwei
 * @create 2017-04-11
 */
var LeaseProductForm = (function () {
    var flashCount = 0;
    var selectedProduct = null;
    var canSubmit = false;

    return {
        init: function () {
            var that = this;

            $("#btnSelectProduct").on("click", function () {
                that.selectProduct();
            });

            new ImagePicker({
                id: "btnSelectMedias",
                multiple: true,
                onOk: function (resp) {
                    var tpl = "";
                    $.each(resp, function (idx, item) {
                        tpl += '<div class="media-items" data-id="'+ item.id +'">';
                        tpl += '    <img src="'+ item.url +'!wh100" width="80" height="80"/>';
                        tpl += '    <i class="fa fa-times-circle media-item-remove"></i>';
                        tpl += '</div>';
                    });
                    $("#btnSelectMedias").before(tpl);
                }
            });

            $(document).on("click", ".media-item-remove", function () {
                $(this)
                    .closest(".media-items")
                    .fadeOut("600", function () {
                        $(this).remove();
                    })
            });
        },

        selectProduct: function () {
            var that = this;
            new Dlg({
                id: "selectProductDlg",
                title: "选择企业产品",
                url: '/shop/product/selectProducts',
                width: 800,
                height: 500,
                onLoaded: function () {
                    that._initProductGrid();
                },
                onOk: function () {
                    if(selectedProduct === null){
                        toast("请选择产品", "error");
                        return;
                    }

                    $("#productName").val(selectedProduct.name);
                    $("#productConfuseId").val(selectedProduct.id);
                    $("#productSpecConfuseId").val("");

                    $("#name").val(selectedProduct.name);

                    // 多规格
                    if(selectedProduct.multiSpec === true){
                        $("#productSpecs").empty();
                        $.each(selectedProduct.specs, function (idx, item) {
                            var option = '<option value="' + item.id + '">' + item.itemCode;

                            // 规格item
                            if(item.specItems !== null && item.specItems.length > 0){
                                option += "(";
                                $.each(item.specItems, function (itemIdx, spec) {
                                    if(itemIdx > 0) {
                                        option += ",";
                                    }
                                    option += spec.name + ": " +spec.value;
                                });
                                option += ")";
                            }

                            option += '</option>';
                            $("#productSpecs").append(option);

                            if(idx === 0) {
                                $("#code").val(item.itemCode);
                            }
                        });
                        $("#productSpecsGroup").show();

                        $("#productSpecs")
                            .off("change")
                            .on("change", function () {
                                $("#productSpecConfuseId").val($(this).val());
                            })
                            .trigger("change");

                        // 去掉必填校验提示
                        $("#iotForm")
                            .parsley()
                            .validate();

                        // 闪烁提示
                        that._flashWarn();
                    }else{
                        $("#productSpecs").empty();
                        $("#productSpecsGroup").hide();
                        $("#code").val(selectedProduct.itemCode);
                        $("#productSpecConfuseId").val(null);
                    }

                    this.close();
                }
            });
        },

        isExist: function (callBack) {
            $("body").mask('<i class="fa fa-spinner fa-pulse"></i> 正在校验...');
            App.ajax.get({
                url: "/lease/product/isExist",
                data: {
                    entityId: $("#id").val(),
                    name: $("#name").val(),
                    code: $("#code").val()
                },
                onSuccess: function (resp) {
                    $("body").unmask();
                    if(typeof callBack === "function"){
                        callBack.call(this, resp);
                    }
                },
                onFinally: function () {
                    $("body").unmask();
                }
            })
        },

        // 提交订单
        submit: function(){
            if(canSubmit) {
                return true;
            }


            // 校验名称、代码是否已存在
            this.isExist(function (resp) {
                if (resp.isExist === true) {
                    canSubmit = false;
                    Messenger.warning({
                        title: "重复提示",
                        message: resp.errorMsg
                    });
                } else {
                    // 重新提交表单
                    canSubmit = true;
                    $("#iotForm").submit();
                }
            });

            return false;
        },

        _initProductGrid: function () {
            new BootGrid({
                id: "product4selectGrid",
                url: "/shop/product",
                data: {
                    productType: "CP",
                    deviceType: "I"
                },
                selection: true,
                multiSelect: false,
                rowSelect: true,
                rowCount: 10,
                formatters: {
                    productTypeFormatter: function (column, row) {
                        if (row.productType === "CP") {
                            return  row.deviceType === "I" ? "智能设备" : "普通产品";
                        } else {
                            return row.productTypeTitle;
                        }
                    }
                },
                onSelected: function (rows) {
                    selectedProduct = rows[0];
                }
            });
        },

        _flashWarn: function () {
            var that = this;
            if(flashCount < 5){
                $("#selectSpecWarn").fadeOut(600, function () {
                    $(this).fadeIn(600, function () {
                        flashCount++;
                        that._flashWarn();
                    });
                })
            }else{
                $("#selectSpecWarn").hide();
            }
        }
    };
}());
