/**
 * 服务商选择器
 *
 * @author huchiwei
 * @create 2016-11-11
 */
var ShopVendorSelector = (function (Dlg) {
    var _baseUrl = "/shop/vendor";
    var _selectedShop = null;

    /**
     * 初始化选择数据表格
     *
     * @param shopServiceId
     * @private
     */
    function _initGrid(shopServiceId) {
        new BootGrid({
            id: "selectShopVendorGrid",
            url: _baseUrl + "/select/page",
            data: {
                shopServiceId: shopServiceId
            },
            selection: true,
            multiSelect: false,
            rowSelect: true,
            rowCount: 10,
            formatters: {
                addressFormatter: function (column, row) {
                    return row.province + row.city + row.area + row.address;
                }
            },
            onSelected: function (rows) {
                _selectedShop = rows[0];
            }
        });
    }

    return {
        select2Assign: function (shopServiceId, callback) {
            new Dlg({
                id: "selectShopVendorDlg",
                title: "选择服务商",
                url: _baseUrl + '/select',
                width: 800,
                height: 500,
                onLoaded: function () {
                    _initGrid(shopServiceId);
                },
                onOk: function () {
                    if (_selectedShop === null) {
                        toast("请选择服务商", "error");
                        return;
                    }

                    callback.call(this, _selectedShop);
                    this.close();
                }
            });
        }
    };
}(Dlg));
