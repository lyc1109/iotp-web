/**
 * 员工信息选择器
 *
 * @author huchiwei
 * @create 2017-05-18
 */
var ShopEmployeeSelector = (function ($) {
    var _baseUrl = "/shop/employee";
    var _bootGrid = null;
    var _result = [];

    var _role = "SERVICE_MAN";
    var _multi = false;

    /**
     * 创建选择对话框
     *
     * @param conf
     */
    function createDlg(callback) {
        var title = "选择服务人员";
        if(_role === "CUSTOM_SERVICER") {
            title = "选择客服人员";
        }

        new Dlg({
            id: "selectEmployeeDlg",
            title: title,
            url: _baseUrl + '/select',
            width: 800,
            height: 500,
            onLoaded: function () {
                initGrid();
            },
            onOk: function () {
                if (_result === null || _result.length === 0) {
                    toast("请选择人员信息", "error");
                    return;
                }

                callback.call(this, _result);
                this.close();
            }
        });
    }

    /**
     * 初始化选择表格
     */
    function initGrid() {
        _bootGrid = new BootGrid({
            id: "employee4selectGrid",
            url: _baseUrl,
            data: {
                employeeRole: _role
            },
            selection: true,
            multiSelect: _multi,
            rowSelect: true,
            rowCount: 10,
            onSelected: function (rows) {
                var idx = _.findIndex(_result, function (n) {
                    return n.id === rows[0].id
                });
                if(idx === -1) {
                    _result.push(rows[0]);
                }
            },
            onDeSelected: function (rows) {
                _.remove(_result, function(n) {
                    return n.id === rows[0].id;
                });
            }
        });
    }

    return {
        /**
         * 单选服务人员
         *
         * @param callback
         */
        selectServiceMan: function (callback) {
            createDlg(callback);
        },

        /**
         * 多选服务人员
         *
         * @param callback
         */
        selectServiceMans: function (callback) {
            _multi = true;
            createDlg(callback);
        }
    };
})(jQuery);
