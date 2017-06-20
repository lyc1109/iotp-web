/**
 * 服务订单处理动作
 *
 * @author huchiwei
 * @create 2016-12-29
 */
var ServiceOrderActions = (function(App){
    var baseUrl = "/shop/serviceOrder/";
    var dataGrid = null;

    var MDU = {
        /**
         * 设置Grid实例，用于刷新表格，否则刷新页面
         * @param grid 数据表格实例
         */
        setDataGrid: function (grid) {
            dataGrid = grid;
        },

        /**
         * 刷新
         */
        refresh: function () {
            if(dataGrid !== null){
                dataGrid.reload();
            }else{
                window.location.reload();
            }
        },

        /**
         * 分派给服务商
         */
        assign: function (serviceOrderId, shopServiceId) {
            ShopVendorSelector.select2Assign(shopServiceId, function (shop) {
                App.ajax.post({
                    url: baseUrl + serviceOrderId + "/assignToVendor",
                    data: {"serviceVendorId": shop.id},
                    onSuccess: function () {
                        toast("派单成功");
                        MDU.refresh();
                    },
                    onFail: function (resp) {
                        Messenger.alert("派单失败, 原因:" + resp.returnMsg);
                    }
                });
            });
        },

        /**
         * 重新分派给服务商
         */
        reassign: function (serviceOrderId, shopServiceId) {
            ShopVendorSelector.select2Assign(shopServiceId, function (shop) {
                App.ajax.post({
                    url: baseUrl + serviceOrderId + "/reassign",
                    data: {"serviceVendorId": shop.id},
                    onSuccess: function () {
                        toast("重新派单成功");
                        MDU.refresh();
                    },
                    onFail: function (resp) {
                        Messenger.alert("派单失败, 原因:" + resp.returnMsg);
                    }
                });
            });
        },

        /**
         * 拒绝订单
         * @param id 订单id
         */
        reject: function (id) {
            Messenger.propup({
                message: "请填写拒绝理由"
            }, function (reason) {
                if(reason.trim() === ""){
                    toast("请填写拒绝理由", "error");
                    return ;
                }
                App.ajax.post({
                    url: baseUrl + id + "/reject",
                    data: {rejectReason: reason},
                    onSuccess: function () {

                        toast("订单已拒绝");
                        MDU.refresh();
                    }
                });
            });
        }
    };

    return MDU;
}(App));
