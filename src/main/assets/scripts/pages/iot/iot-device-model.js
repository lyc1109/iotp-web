/**
 * 智能产品配置信息
 *
 * @author huchiwei
 * @create 2016-12-08
 */
window.IotDeviceModel = (function ($, App) {
    return {
        /**
         * 初始化页面处理
         */
        init: function () {
            this._initSelectParts();
            this._initDeleteParts();
            this._initAddParts();

            var that = this;
            $("#btnSave").on("click", function () {
                var parsley = $('#iotForm').parsley();
                if(parsley.validate() !== true){
                    return;
                }
                that.save();
            });
        },

        /**
         * 保存智能配置
         */
        save: function () {
            $("body").mask('<i class="fa fa-spinner fa-pulse"></i> 正在保存...');
            var parts = this._getParts();
            App.ajax.post({
                url: "/iot/deviceModel/" + $("#entityId").val() + "/save",
                data: {
                    modelName: $("#modelName").val(),
                    memos: $("#memos").val(),
                    modelParts: JSON.stringify(parts)
                },
                onSuccess: function () {
                    toast("配置保存成功");
                    setTimeout(function () {
                        window.location.reload();
                    }, 1000);
                },
                onFinally: function () {
                    $("body").unmask();
                }
            })
        },

        // ==========================================================================
        // private methods ==========================================================
        /**
         * 初始化配件列表选择
         *
         * @private
         */
        _initSelectParts: function (selector) {
            selector = selector || ".part-productId";
            $(selector).select2({
                ajax: {
                    url: '/shop/product/parts/list/select2',
                    processResults: function (data) {
                        var results = [];
                        if(data.returnCode === 0){
                            var parts = data.parts;
                            if(parts.length > 0){
                                $.each(parts, function (idx, part) {
                                    results.push({
                                        id: part.id,
                                        text: part.name + "("+ part.itemCode +")"
                                    })
                                });
                            }
                        }
                        return { results: results };
                    }
                }
            });

            this._getParts();
        },

        /**
         * 组装页面上配置的配件信息
         *
         * @returns {Array}
         * @private
         */
        _getParts: function () {
            var parts = [];
            $.each($(".part-item"), function () {
                var name = $(this)
                    .find("option:selected")
                    .text();

                // 获取编码
                var startIdx = name.lastIndexOf("(");
                var endIdx = name.lastIndexOf(")");
                var code = name.substring(startIdx+1, endIdx);

                // 获取名称
                name = name.substring(0, startIdx);
                var partProductId = $(this)
                    .find(".part-productId")
                    .val();
                var totalWaterflow = $(this)
                    .find(".part-totalWaterflow")
                    .val();
                var orderNo = $(this)
                    .find(".part-orderNo")
                    .val();
                parts.push({
                    id: $(this).data("id"),
                    status: $(this).data("status"),
                    productId: partProductId,
                    name: name,
                    code: code,
                    totalWaterflow: totalWaterflow,
                    orderNo: orderNo
                });
            });
            return parts;
        },

        /**
         * 删除页面上配件信息
         *
         * @returns {Array}
         * @private
         */
        _initDeleteParts: function () {
            var that = this;
            $(document).on("click", ".part-action-delete", function () {
                if($(".part-item:visible").length === 1){
                    toast("该设备至少需要一个配件信息", "error");
                    return ;
                }

                // 标记该项状态删除，值为9，后台再根据此状态进行删除
                $(this).closest(".part-item")
                    .data("status", 9)
                    .removeAttr("id")
                    .fadeOut();
                that._refreshOrderNo();
            });
        },

        /**
         * 添加页面上配件信息
         *
         * @returns {Array}
         * @private
         */
        _initAddParts: function () {
            var that = this;
            $(".part-action-add").on("click", function () {
                var tpl = $.templates("#partTpl");
                $("#partsContainer").append(tpl.render({}));

                that._refreshOrderNo();

                var lastSelect = "#" + $(".part-item:last").attr("id") + " .part-productId";
                that._initSelectParts(lastSelect);
            });
        },

        /**
         * 刷新排序号
         * @private
         */
        _refreshOrderNo: function () {
            var num = 1;
            $.each($(".part-item"), function () {
                var status = $(this).data("status");
                if(status !== 9){
                    $(this)
                        .find(".part-orderNo")
                        .val(num);
                    $(this).attr("id", "partItem" + num);
                    num++;
                }
            });
        }
    };
}(jQuery, App));
