/**
 * 产品编辑脚本
 *
 * @author huchiwei
 * @create 2017-01-11
 */
var ProductForm = (function (ImagePicker) {
    var canSubmit = false;

    var mdu = {
        init: function () {
            $('#productCategoryConfuseIds').on('change', function (evt) {
                var vals = $(this).val(); //.split(",");
                var newVals = [];
                if (vals.length > 3) {
                    toast("对不起, 最多可选取三个分类!", "error");
                    for (var i = 0; i < 3; i++) {
                        newVals.push(vals[i]);
                    }
                    $(this).val(newVals);
                    $('#productCategoryConfuseIds').trigger('change.select2');
                }
            });

            $('#productSpecGroupConfuseId')
                .on('change', function () {
                    if($(this).val() !== "-1"){
                        mdu.renderProductSpec();
                        $("#productItemCode")
                            .removeAttr("data-parsley-required")
                            .val("");
                        $("#productItemCodeFormGroup").hide();
                    }else{
                        $("#productItemCode").attr("data-parsley-required", true);
                        $("#productItemCodeFormGroup").show();
                    }
                    mdu.renderProductSpec();
                })
                .trigger("change");

            new ImagePicker({
                id: "btnCoverImagePicker",
                onOk: function (resp) {
                    $("#coverImagePrev").attr("src", resp[0].url);
                    $("#coverImagePrev").show();
                    $("#coverImageConfuseId").val(resp[0].id);
                }
            });

            // 产品类型
            $(':radio[name="productType"]').on('ifChecked', function(event){
                if($(this).val() === "CP"){
                    $("#deviceType").show();

                    if($(':checked[name="deviceType"]').val() === "I"){
                        $("#iotDeviceInfo").show();
                        $("#iotDeviceType").attr("data-parsley-required", true);
                        $("#iotDeviceModel").attr("data-parsley-required", true);
                    }else{
                        $("#iotDeviceInfo").hide();
                        $("#iotDeviceType").removeAttr("data-parsley-required");
                        $("#iotDeviceModel").removeAttr("data-parsley-required");
                    }
                }else{
                    $("#deviceType").hide();
                    $("#iotDeviceInfo").hide();
                    $("#iotDeviceType").removeAttr("data-parsley-required");
                    $("#iotDeviceModel").removeAttr("data-parsley-required");
                }
            });

            // 发布配件默认隐藏设备类型
            if($(':radio[name="productType"]:checked').val() !== "CP"){
                $("#deviceType").hide();
                $("#iotDeviceInfo").hide();
            }

            // 设备类型编号监听
            $(':radio[name="deviceType"]').on('ifChecked', function(event){
                if($(':checked[name="deviceType"]').val() === "I"){
                    $("#iotDeviceInfo").show();
                    $("#iotDeviceType").attr("data-parsley-required", true);
                    $("#iotDeviceModel").attr("data-parsley-required", true);
                }else{
                    $("#iotDeviceInfo").hide();
                    $("#iotDeviceType").removeAttr("data-parsley-required");
                    $("#iotDeviceModel").removeAttr("data-parsley-required");
                }
            });
            if($(':checked[name="deviceType"]').val() === "I"){
                $("#iotDeviceType").attr("data-parsley-required", true);
                $("#iotDeviceModel").attr("data-parsley-required", true);
            }

            // 异步获取智能设备型号列表
            $('#iotDeviceType').on("change", function () {
                /*$('#iotDeviceModel').select2('data', {iotDeviceType: $(this).val()});*/
                $('#iotDeviceModel')
                    .val(null)
                    .trigger('change.select2');
            });

            if($(':checked[name="deviceType"]').val() === "I"){
                $("#iotDeviceInfo").show();
                $('#iotDeviceType').trigger("change");
            }

            // 智能设备型号
            $('#iotDeviceModel').select2({
                ajax: {
                    url: '/iot/deviceModel/list',
                    data: function(params){
                        return $.extend(params, {
                            iotDeviceType: $('#iotDeviceType').val()
                        });
                    },
                    processResults: function (data) {
                        var results = [];
                        if(data.returnCode === 0){
                            var models = data.iotDeviceModels;
                            if(models.length > 0){
                                $.each(models, function (idx, model) {
                                   results.push({
                                       id: model.modelCode,
                                       text: model.modelName
                                   })
                                });
                            }
                        }
                        return { results: results };
                    }
                }
            });
            if($("#iotDeviceModelVal").val() !== ""){
                $('#iotDeviceModel').append('<option value="'+ $("#iotDeviceModelVal").val() +'" selected="selected">' + $("#iotDeviceModelName").val() + '</option>');
            }
        },


        /**
         * 渲染商品规格信息
         */
        renderProductSpec: function() {
            var specGroupId = $('#productSpecGroupConfuseId').val();
            if (specGroupId === "-1") {
                $(".product-spec-no").show();
                $(".product-spec").hide();
                $(".product-spec-container").empty();
            } else {
                $(".product-spec-no").hide();
                $(".product-spec-container").load("/shop/product/loadSpec?productId=" + $("#_id").val() + "&specGroupId=" + specGroupId, function() {
                    $(".select-image").each(function(){
                        var id = $(this).attr("id");
                        new ImagePicker({
                            id: id,
                            onOk: function (resp) {
                                $("#specCoverImage_" + id).attr("src", resp[0].url);
                                $("#specCoverImageId_" + id).val(resp[0].id);
                            }
                        });
                    });

                    $(".cmd-spec-delete").on("click", function(){
                        // 判断一下还有几个
                        var length = $(".spec-row").length;
                        if (length === 1) {
                            toast("对不起, 商品至少需要一个规格信息!", "error");
                        } else {
                            $(this)
                                .closest(".spec-row")
                                .slideUp("slow", function() {
                                    $(this).remove();
                                });
                        }
                    });
                    $(".product-spec").show();
                });
            }
        },

        /**
         * 获取已选择规格信息
         */
        getSpecs: function () {
            var specRows = [];
            if ($('#productSpecGroupConfuseId').val() !== "-1") {
                $(".spec-row").each(function() {
                    var specItemCover = $(this)
                        .find(".spec-item-cover")
                        .val();
                    var specItemId = $(this).data("specid");
                    var specItemCode = $(this)
                        .find(".spec-item-code")
                        .val();
                    var specItemPrice = parseFloat(100 * $(this)
                            .find(".spec-item-price")
                            .val());

                    if(specItemPrice < 0) {
                        Messenger.warning({
                            title: "错误提示",
                            message: "规格价格必须大于0"
                        });
                        specRows = false;
                        return false;
                    }
                    
                    if (specItemCode === "" || specItemPrice === "") {
                        Messenger.warning({
                            title: "规格必填提示",
                            message: "请填写产品规格信息中的货号以及价格"
                        });
                        specRows = false;
                        return false;
                    }

                    var item = {
                        productSpecConfuseId: specItemId,
                        coverImageConfuseId: specItemCover,
                        itemCode: specItemCode,
                        price: specItemPrice
                    };

                    $(this)
                        .find(".spec-item-def")
                        .each(function(){
                            var defIndex = $(this).data("specIndex");
                            item["specName0" + defIndex] = $(this).data("specName");
                            item["specVal0" + defIndex] = $(this).data("specVal");
                        });
                    specRows.push(item);
                });

                // 判断规格中货号是否重复
                // 根据itemCode去重后的规格列表
                if(specRows !== false){
                    var unionSpecRows = _.unionBy(specRows, "itemCode");
                    if(unionSpecRows.length !== specRows.length){
                        Messenger.warning({
                            title: "货号重复",
                            message: "规格列表中有货号重复，不允许重复添加。"
                        });
                        specRows = false;
                        return false;
                    }
                }
            }
            return specRows;
        },

        /**
         * 检测货号是否已存在
         */
        checkExistItemCode: function (callBack) {
            var data = {productId: $("#_id").val()};
            var specs = this.getSpecs();

            if(specs === false){
                return;
            }

            if(specs !== null && specs.length > 0){
                data.specs = JSON.stringify(specs);
            }else{
                var productItemCode = $("#productItemCode").val();
                if(productItemCode === "" || productItemCode.length === 0){
                    Messenger.error("请输入商品货号，或者至少添加一个商品规格");
                    return;
                }
                data.itemCode = productItemCode;
            }

            $("body").mask('<i class="fa fa-spinner fa-pulse"></i> 正在校验...');

            App.ajax.get({
                url: '/shop/product/existItemCode',
                data: data,
                onSuccess: function (response) {
                    $("body").unmask();

                    if(typeof callBack === "function"){
                        response.specs = specs;
                        callBack.call(this, response);
                    }
                },
                onFinally: function () {
                    $("body").unmask();
                }
            })
        },

        /**
         * 提交表单
         *
         * @returns {boolean}
         */
        submit: function () {
            if(canSubmit) {
                return true;
            }

            this.checkExistItemCode(function (response) {
                if(response.isExist === true){
                    //"商品货号已存在：" + response.existItemCode
                    var aHtml = "";
                    $.each(response.existProducts, function (idx, product) {
                        if(aHtml !== "") {
                            aHtml += ",";
                        }
                        aHtml += '<a href="/shop/product/'+ product.id +'" target="_blank">' + product.itemCode + '</a>';
                    });
                    $.each(response.existProductSpecs, function (idx, productSpec) {
                        if(aHtml !== "") {
                            aHtml += ",";
                        }
                        aHtml += '<a href="/shop/product/'+ productSpec.productId +'" target="_blank">' + productSpec.itemCode + '</a>';
                    });

                    Messenger.warning({
                        title: "货号重复提醒",
                        html: aHtml
                    });
                    canSubmit = false;
                }else{
                    canSubmit = true;

                    // 转换价格
                    var price = Math.round(100 * $("#dblPrice").val());
                    $("#price").val(price);
                    $("#listPrice").val(price);

                    // 拼接规格
                    var specs = mdu.getSpecs();
                    if(specs === false) {
                        return false;
                    }

                    $("#specs").val(JSON.stringify(response.specs));

                    // 重新提交表单
                    $("#iotForm").submit();
                }
            });

            return false;
        }
    };

    return mdu;
}(ImagePicker));

