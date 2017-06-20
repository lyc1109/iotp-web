/**
 * 服务商工单处理
 *
 * @author huchiwei
 * @create 2016-12-29
 */

var WorkOrderActions = (function($, App){
    var baseUrl = "/shop/workOrder/";
    var dataGrid = null;

    return {
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
         * 分派给服务人员
         *
         * @param workOrderId 工单id
         * @param isReassign  是否重新分配，默认false
         */
        assign: function (workOrderId, isReassign) {
            isReassign = isReassign || false;
            ShopEmployeeSelector.selectServiceMan(function (rows) {
                var url = baseUrl + workOrderId + "/assign";
                if(isReassign){
                    url = baseUrl + workOrderId + "/reassign";
                }

                App.ajax.post({
                    url: url,
                    data: {
                        "assigneeId": rows[0].userDto.id
                    },
                    onSuccess: function () {
                        toast("派单成功");
                        WorkOrderActions.refresh();
                    },
                    onFail: function (resp) {
                        Messenger.alert("派单失败, 原因:" + resp.returnMsg);
                    }
                });
            });
            /*new Dlg({
                title: "派单 - 请选择一个服务人员",
                width: 800,
                height: 400,
                url: "/shop/shop/selectServiceMan",
                onOk: function (result) {
                    var selectServiceManId = result.selectServiceManId;
                    var url = baseUrl + workOrderId + "/assign";

                    if(isReassign){
                        url = baseUrl + workOrderId + "/reassign";
                    }

                    App.ajax.post({
                        url: url,
                        data: {"assigneeId": selectServiceManId},
                        onSuccess: function () {
                            toast("派单成功");
                            WorkOrderActions.refresh();
                        },
                        onFail: function (resp) {
                            Messenger.alert("派单失败, 原因:" + resp.returnMsg);
                        }
                    });
                }
            });*/
        },

        /**
         * 拒绝订单
         * @param workOrderId 订单id
         */
        reject: function (workOrderId) {
            Messenger.propup({
                message: "请填写拒绝理由"
            }, function (rejectReason) {
                if(rejectReason.trim() === ""){
                    toast("请填写拒绝理由", "error");
                    return ;
                }
                App.ajax.post({
                    url: baseUrl + workOrderId + "/reject",
                    data: {rejectReason: rejectReason},
                    onSuccess: function () {
                        toast("订单已拒绝");
                        WorkOrderActions.refresh();
                    }
                });
            });
        },

        /**
         * 确认取消订单
         * @param workOrderId 订单id
         */
        confirmCancel: function (workOrderId) {
            Messenger.confirm("是否确认取消该工单？", function () {
                App.ajax.post({
                    url: baseUrl + workOrderId + "/confirmCancel",
                    onSuccess: function () {
                        toast("工单已取消");
                        WorkOrderActions.refresh();
                    }
                });
            })
        }
    };
}(jQuery, App));
