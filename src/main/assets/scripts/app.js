/**
 * app通用初始化JS
 *
 * @author huchiwei
 * @create 2016-09-13
 */
$(function () {
    // 手机隐藏/显示菜单事件
    $(document).on('click', '[data-toggle-state="aside-toggle"]', function (e) {
        $("body").toggleClass("sidebar-open");
    });

    // 大屏幕收起/展开事件
    $(document).on('click', '[data-toggle-state="aside-collapsed"]', function (e) {
        $("body").toggleClass("sidebar-collapsed");

        // 缓存展开与否
        App.cache.put("collapsed", $("body").hasClass("sidebar-collapsed"));

        // 移除收起后展开的二级菜单
        if(!$("body").hasClass("sidebar-collapsed")){
            var $existSubNav = $(".sidebar").find("#subnav");
            if($existSubNav.length > 0){
                $existSubNav.remove();
            }
        }
    });

    // 判断是否已设置收起
    if(App.cache.getAsBoolean("collapsed") === true){
        $("body").addClass("sidebar-collapsed");
    }else{
        // 平板默认收起
        if($(window).width() > 767 && $(window).width() < 900){
            $("body").addClass("sidebar-collapsed");
        }
    }

    // 菜单点击事件
    var _top = 0;
    $(document).on('click', '.sidebar-item', function (e) {
        if($(this).find(".sidebar-subnav").length === 0) {
            return;
        }

        if(!$("body").hasClass("sidebar-collapsed")){
            // 收起除当前元素外其它已展开二级菜单
            $(".sidebar-item")
                .not($(this))
                .find(".collapse.in")
                .removeClass("in");
            $(".sidebar-item")
                .not($(this))
                .find("a .pull-right")
                .removeClass("fa-angle-up")
                .addClass("fa-angle-down");

            $(this).off('hidden.bs.collapse,shown.bs.collapse')
                .on('hidden.bs.collapse', function () {
                    $(this)
                        .find("a .pull-right")
                        .removeClass("fa-angle-up")
                        .addClass("fa-angle-down");
                })
                .on('shown.bs.collapse', function () {
                    $(this)
                        .find("a .pull-right")
                        .removeClass("fa-angle-down")
                        .addClass("fa-angle-up");
                });
        }else{
            // 先移除已显示二级菜单
            if(_top > 0 || _top === $(this).offset().top){
                var $existSubNav = $(".sidebar").find("#subnav");
                if($existSubNav.length > 0){
                    $existSubNav.remove();
                }
            }

            // 重复点击则直接返回并重置标识
            if(_top === $(this).offset().top){
                _top = 0;
                return;
            }

            // 浮动显示二级菜单
            _top = $(this).offset().top;
            var $subnav = $(this).find(".sidebar-subnav")
                .clone();
            $subnav
                .removeAttr("style")
                .attr("id", "subnav")
                .addClass("subnav-floating")
                .css("top", _top)
                .appendTo($(".sidebar"));
        }
    });

    // 初始化Jquery通用插件
    $('.icheck').iCheck({
        checkboxClass: 'icheckbox_square-blue',
        radioClass: 'iradio_square-blue'
    });

    // 初始化select2
    $(".select2").select2({language: 'zh-CN'});

    // 日期选择
    $(".date-picker").datetimepicker({
        format: 'yyyy-mm-dd',
        minView: 2,
        todayBtn: true,
        todayHighlight: true,
        keyboardNavigation: true,
        language: 'zh-CN',
        autoclose: true
    });
    $(".datetime-picker").datetimepicker({
        format: 'yyyy-mm-dd hh:ii',
        todayBtn: true,
        todayHighlight: true,
        keyboardNavigation: true,
        language: 'zh-CN',
        autoclose: true
    });

    // 数字格式化
    $(".currency").accounting('formatMoney', { "symbol": "" });
    $(".currency-rmb").accounting('formatMoney', { "symbol": "￥" });
    $(".float").accounting('formatNumber', { "precision": 2 });

    // 返回
    $(".goBack").on("click", function () {
        window.history.back();
    });

    // 颜色选择
    // $('INPUT.minicolors').minicolors({control: $(this).attr('data-control') || 'wheel'});

    // 初始化表单校验
    if($(".parsley-form").length > 0){
        $(".parsley-form").parsley({
            trigger: "blur",
            excluded: 'input[type=button], input[type=submit], input[type=reset]'
        });
    }

    // 替换图片错误
    if($("img").length > 0){
        $('img').each(function(){
            var error = false;
            if (!this.complete) {
                error = true;
            }

            if (typeof this.naturalWidth !== "undefined" && this.naturalWidth === 0) {
                error = true;
            }

            if(error){
                $(this)
                    .bind('error.replaceSrc',function(){
                        this.src = "/dist/images/placeholder.png";

                        $(this).unbind('error.replaceSrc');
                    })
                    .trigger('load');
            }
        });
    }

    // 轮询未读消息数量
    var getUnreadNofifyCount = function () {
        App.ajax.get({
            url: "/notify/unread",
            onSuccess: function (resp) {
                $("#unreadNotifyCount").text(resp.countUnread);
                if(resp.countUnread > 0){
                    $("#unreadNotifyCount").show();
                }else{
                    $("#unreadNotifyCount").hide();
                }
            },
            onFail: function () {
                // 不做任何提示
                $("#unreadNotifyCount").hide();
            }
        });
    };
    setInterval(function () {
        getUnreadNofifyCount();
    }, 10000);
    getUnreadNofifyCount();

    $("#sideUl").on("click",function(){
        sessionStorage.setItem('fundType','')
        sessionStorage.setItem('fundStartDate','')
        sessionStorage.setItem('fundEndDate','')
        sessionStorage.setItem('fundSearch','')
        sessionStorage.setItem('fundSize',1)
        sessionStorage.setItem('devicePage',1)
        sessionStorage.setItem('appCurrent',1)
        sessionStorage.setItem('deviceType','')
        sessionStorage.setItem('deviceProduct','')
        sessionStorage.setItem('deviceSearch','')
        sessionStorage.setItem('tabName','交易记录')
    })
});


