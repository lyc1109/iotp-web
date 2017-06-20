/**
 * 品智云通用APP脚本入口
 *
 * @author hucw
 * @date   2016-11-09
 */
var App = (function () {
    return {
        // 常量相关方法
        constants: {
            formatGender: function (gender) {
                if (gender === "M") {
                    return "男";
                } else if (gender === "F") {
                    return "女";
                } else if (gender === "N") {
                    return "未知";
                } else {
                    return gender;
                }
            },

            formatAge: function (age) {
                var ages = {
                    5: "50后",
                    6: "60后",
                    7: "70后",
                    8: "80后",
                    9: "90后",
                    0: "00后"
                };
                return ages[age];
            }
        },

        // 缓存相关方法
        cache: {
            put: function (key, value) {
                window.localStorage.setItem(key, value);
            },

            get: function (key) {
                return window.localStorage.getItem(key);
            },

            getAsBoolean: function (key) {
                var value = window.localStorage.getItem(key);
                return value === true || value === "true" || value === 1 || value === -1;
            }
        }
    };
}());
