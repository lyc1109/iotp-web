/**
 * 浏览器处理
 *
 * @author huchiwei
 * @create 2016-11-11
 */

(function ($, App) {

    /**
     * 浏览器处理构造函数
     *
     * @constructor
     */
    function Browser() {
        this.userAgent = navigator.userAgent.toLowerCase();
        this.platform = navigator.platform;
        this.appName = navigator.appName;
        this.appVersion = navigator.appVersion;
        this.clientStrings = [
            { name: 'Windows 10', reg: /(Windows 10.0|Windows NT 10.0)/ },
            { name: 'Windows 8.1', reg: /(Windows 8.1|Windows NT 6.3)/ },
            { name: 'Windows 8', reg: /(Windows 8|Windows NT 6.2)/ },
            { name: 'Windows 7', reg: /(Windows 7|Windows NT 6.1)/ },
            { name: 'Windows Vista', reg: /Windows NT 6.0/ },
            { name: 'Windows Server 2003', reg: /Windows NT 5.2/ },
            { name: 'Windows XP', reg: /(Windows NT 5.1|Windows XP)/ },
            { name: 'Windows 2000', reg: /(Windows NT 5.0|Windows 2000)/ },
            { name: 'Windows ME', reg: /(Win 9x 4.90|Windows ME)/ },
            { name: 'Windows 98', reg: /(Windows 98|Win98)/ },
            { name: 'Windows 95', reg: /(Windows 95|Win95|Windows_95)/ },
            { name: 'Windows NT 4.0', reg: /(Windows NT 4.0|WinNT4.0|WinNT|Windows NT)/ },
            { name: 'Windows CE', reg: /Windows CE/ },
            { name: 'Windows 3.11', reg: /Win16/ },
            { name: 'Android', reg: /Android/ },
            { name: 'Open BSD', reg: /OpenBSD/ },
            { name: 'Sun OS', reg: /SunOS/ },
            { name: 'Linux', reg: /(Linux|X11)/ },
            { name: 'iOS', reg: /(iPhone|iPad|iPod)/ },
            { name: 'Mac OS X', reg: /Mac OS X/ },
            { name: 'Mac OS', reg: /(MacPPC|MacIntel|Mac_PowerPC|Macintosh)/ },
            { name: 'QNX', reg: /QNX/ },
            { name: 'UNIX', reg: /UNIX/ },
            { name: 'BeOS', reg: /BeOS/ },
            { name: 'OS/2', reg: /OS\/2/ },
            { name: 'Search Bot', reg: /(nuhk|Googlebot|Yammybot|Openbot|Slurp|MSNBot|Ask Jeeves\/Teoma|ia_archiver)/ }
        ];
    }

    Browser.prototype = {
        getBrowserInfo: function () {
            var that = this;
            var tem = [];

            // ['Chrome/52','Chrome','52']
            var bsInfo = that.userAgent.match(/(opera|chrome|safari|firefox|msie|trident(?=\/))\/?\s*(\d+)/i) || [];
            if (/trident/i.test(bsInfo[1])) {
                tem = /\brv[ :]+(\d+)/g.exec(that.userAgent) || [];
                bsInfo[1] = 'IE';
                bsInfo[2] = tem[1] || ''; // IE浏览器版本
            }

            if (bsInfo[1] === 'Chrome') {
                tem = that.userAgent.match(/\b(OPR|Edge)\/(\d+)/);
                if (tem !== null) {
                    bsInfo[1] = 'Opera';
                    bsInfo[2] = tem[1] || '';
                }
            }

            bsInfo = bsInfo[2] ? [bsInfo[1], bsInfo[2]] : [that.appName, that.appVersion, '-?'];
            if ((tem = that.userAgent.match(/version\/(\d+)/i)) !== null) {
                bsInfo.splice(1, 1, tem[1]);
            }

            return {
                browser: bsInfo[0],
                version: bsInfo[1]
            }
        },

        getOSInfo: function () {
            var that = this;
            var userAgent = navigator.userAgent;

            var osInfo = {
                os: '',
                version: ''
            };

            // 匹配系统版本
            $.each(that.clientStrings, function (idx, item) {
                if (item.reg.test(userAgent)) {
                    osInfo.os = item.name;
                    return false;  // break
                }
            });

            // 精确匹配Window系统
            if (/Windows/.test(osInfo.os)) {
                osInfo.version = /Windows (.*)/.exec(osInfo.os)[1];
                osInfo.os = 'Windows';
            }

            // 匹配移动系统
            switch (osInfo.os) {
                case 'Mac OS X':
                    osInfo.version = /Mac OS X (10[\.\_\d]+)/.exec(userAgent)[1];
                    break;

                case 'Android':
                    osInfo.version = /Android ([\.\_\d]+)/.exec(userAgent)[1];
                    break;

                case 'iOS':
                    osInfo.version = /OS (\d+)_(\d+)_?(\d+)?/.exec(that.appVersion);
                    osInfo.version = osInfo.version[1] + '.' + osInfo.version[2] + '.' + (osInfo.version[3] | 0);
                    break;

                default:
                    osInfo.version = "未知版本";
                    break;
            }

            return osInfo;
        }
    };

    App.browser = Browser;
}(jQuery, App));
