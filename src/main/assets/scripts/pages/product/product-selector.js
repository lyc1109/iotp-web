/**
 * 产品、配件选择器
 *
 * @author huchiwei
 * @create 2017-05-18
 */
var ProductSelector = (function ($) {
    var _baseUrl = "/shop/product";
    var _bootGrid = null;
    var _result = [];

    var _productType = "CP";
    var _multi = false;

    /**
     * 创建选择对话框
     *
     * @param conf
     */
    function createDlg(callback) {
        var title = "选择产品信息";
        if(_productType === "PJ") {
            title = "选择配件";
        }

        new Dlg({
            id: "selectProductDlg",
            title: title,
            url: _baseUrl + '/selectProducts',
            width: 800,
            height: 500,
            onLoaded: function () {
                initGrid();
            },
            onOk: function () {
                if (_result === null || _result.length === 0) {
                    toast("请选择产品信息", "error");
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
            id: "product4selectGrid",
            url: "/shop/product",
            data: {
                productType: _productType
            },
            selection: true,
            multiSelect: _multi,
            rowSelect: true,
            rowCount: 10,
            formatters: {
                productTypeFormatter: function (column, row) {
                    if(row.productType === "CP"){
                        return row.deviceType === "I" ? "智能设备" : "普通产品";
                    }else{
                        return row.productTypeTitle;
                    }
                }
            },
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
         * 单选产品
         *
         * @param callback
         */
        selectProduct: function (callback) {
            createDlg(callback);
        },

        /**
         * 多选产品
         *
         * @param callback
         */
        selectProducts: function (callback) {
            _multi = true;
            createDlg(callback);
        },

        /**
         * 单选配件
         *
         * @param callback
         */
        selectPart: function (callback) {
            _productType = "PJ";
            createDlg(callback);
        },

        /**
         * 多选配件
         *
         * @param callback
         */
        selectParts: function (callback) {
            _productType = "PJ";
            _multi = true;
            createDlg(callback);
        }
    };
})(jQuery);
