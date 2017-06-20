/**
 * 图片素材管理
 *
 * @author huchiwei
 * @create 2016-10-31
 */
var MediaImg = (function ($, App, Messenger, Spage, Uploader) {
    var _imgSpage = null;
    var mdu = {
        init: function () {
            this._initSpage();
            this._initUploader();
            this._initMediaGroup();
            this._initMediaActions();
        },

        /**
         * 新增分组
         */
        addGroup: function () {
            Messenger.propup({
                message: "新建分组",
                inputPlaceholder: "请输入分组名称"
            }, function (groupName) {
                if(groupName.trim() === ""){
                    toast("请输入分组名称", "error");
                    return;
                }

                App.ajax.post({
                    url: "/media/group/create",
                    data: {group: groupName},
                    onSuccess: function (resp) {
                        window.location.reload();
                    }
                });
            });
        },

        /**
         * 编辑分组
         *
         * @param id   分组id
         * @param name 分组名称
         */
        editGroup: function (id, name) {
            Messenger.propup({
                message: "编辑分组",
                inputPlaceholder: "请输入分组名称",
                inputValue: name
            }, function (groupName) {
                if(groupName.trim() === ""){
                    toast("请输入分组名称", "error");
                    return;
                }

                App.ajax.patch({
                    url: "/media/group/" + id,
                    data: {group: groupName},
                    onSuccess: function () {
                        window.location.reload();
                    }
                });
            });
        },

        /**
         * 删除分组
         *
         * @param id   分组id
         * @param name 分组名称
         */
        deleteGroup: function (id, name) {
            /*Messenger.confirm("是否确定删除分组:" + name, function () {
                /!*App.ajax.del({
                    url: "/media/group/" + id,
                    onSuccess: function () {
                        window.location.reload();
                    }
                });*!/

            });*/
            window.swal({
                title: '是否确定删除分组:'+name+'?',
                type: 'question',
                showCancelButton: true,
                confirmButtonText: '确定',
                cancelButtonText: '取消',
            }).then(function (isConfirm) {
                if(isConfirm === true){
                    window.swal({
                        title: '是否同时删除分组 ['+name+'] 中所有的图片?',
                        type: 'question',
                        showCancelButton: true,
                        confirmButtonText: '确定',
                        cancelButtonText: '取消',
                    }).then(function (isConfirm) {
                        if(isConfirm === true){
                            App.ajax.del({
                                url: "/media/group/media/" + id,
                                onSuccess: function () {
                                    window.location.reload();
                                }
                            });
                        }
                    },function (dismiss) {
                        if(dismiss === 'cancel'){
                            App.ajax.del({
                                url: "/media/group/" + id,
                                onSuccess: function () {
                                    window.location.reload();
                                }
                            });
                        }
                    })


                }
            })

        },

        /**
         * 同步分组的media数量显示
         *
         * @param id 分组id
         * @param name 分组名称
         * @param num 新增/删除数量
         */
        updateCount: function (id,name,num) {
            var group = $(".group-item[data-id='"+id+"']");
            var groupAll = $(".group-item[data-name='全部图片']");
            var count = group.data("mediaCount");
            var countAll = groupAll.data("mediaCount");
            if(id === null || name === '全部分类'){
                groupAll.find(".text-second").text("(" + (countAll + num) + ")");
                groupAll.data("mediaCount",countAll + num);

            }else{
                group.find(".text-second").text("(" + (count + num) + ")");
                group.data("mediaCount",count + num);

                groupAll.find(".text-second").text("(" + (countAll + num) + ")");
                groupAll.data("mediaCount",countAll + num);

            }
        },

        /**
         * 初始化图片分页数据
         *
         * @private
         */
        _initSpage: function () {
            _imgSpage = new Spage({
                id: "spage4images",
                url: "/media/images/page",
                pageSize: 18,
                onLoaded: function () {
                    /*$(".fancybox").fancybox({
                        prevEffect: 'fade',
                        nextEffect: 'fade',
                        maxWidth: '800',
                        autoCenter: true,
                        helpers	: {
                            title	: {
                                type: 'outside'
                            }
                        }
                    });*/
                    $(".media-img").on("click", function () {
                        var $ickeck = $(this)
                            .siblings(".check")
                            .find(".media-icheck");
                        $ickeck.iCheck("toggle");
                    });

                    $('.media-icheck').on('ifChecked', function(event){
                        $(this)
                            .closest(".images-item")
                            .addClass("active");
                        mdu._toggleActions();

                    });
                    $('.media-icheck').on('ifUnchecked', function(event){
                        $(this)
                            .closest(".images-item")
                            .removeClass("active");
                        mdu._toggleActions();

                    });

                    $('.media-icheck').iCheck({
                        checkboxClass: 'icheckbox_square-blue',
                        radioClass: 'iradio_square-blue'
                    });
                }
            });
        },

        // 初始化图片上传
        _initUploader: function () {
            var uploadCount = 0;
            new Uploader({
                id: "btnImageUploader",
                multi: true,
                managable: true,
                startOnAdd: false,
                prefix: $("#mediaGroupPrefix").val(),
                saveGroup: function () {
                    var group = "";
                    var $groupItem = $(".group-item.active");
                    if($groupItem.length > 0 && $groupItem.data("name") !== "全部图片"){
                        group = $groupItem.data("name");
                    }
                    return group;
                },
                onAdd: function (uploader,files) {
                    uploadCount = files.length;
                    uploader.start();
                },
                onComplete: function () {
                    var curGroupId = $(".group-item.active").data("id");
                    var curGroupName = $(".group-item.active").data("name");
                    mdu.updateCount(curGroupId,curGroupName,uploadCount);
                    _imgSpage.refresh();

                }
            });
        },

        // 初始化分组信息
        _initMediaGroup: function () {
            var that = this;

            $("#btnAddGroup").on("click", function () {
                that.addGroup();
            });

            $(".group-edit").on("click", function () {
                that.editGroup($(this).data("id"), $(this).data("name"));
            });

            $(".group-delete").on("click", function () {
                that.deleteGroup($(this).data("id"), $(this).data("name"));
            });

            $(".group-name").on("click", function () {
                $(".group-name")
                    .closest("li")
                    .removeClass("active");
                $(this)
                    .closest("li")
                    .addClass("active");

                $("#groupName").text($(this).data("name"));

                _imgSpage.refresh({
                    groupId: $(this).data("id")
                });
            });
        },

        _initMediaActions: function () {
            $("#checkAll").on("ifChecked", function () {
                $(".images-item").addClass("active");
                $(".images-item")
                    .find(".media-icheck")
                    .iCheck("check");
                mdu._toggleActions();
            });
            $("#checkAll").on("ifUnchecked", function () {
                $(".images-item").removeClass("active");
                $(".images-item")
                    .find(".media-icheck")
                    .iCheck("uncheck");
                mdu._toggleActions();
            });

            // 删除素材
            $("#btnMediaDelete").on("click", function () {
                var mediaIds = mdu._getCheckedItemIds();
                if(mediaIds === null){
                    toast("请选择需要处理的素材", "error");
                    return;
                }
                Messenger.confirm("是否确定删除素材？", function () {
                    App.ajax.post({
                        url: "/media/delete",
                        data: {
                            mediaIds: mediaIds
                        },
                        onSuccess: function () {
                            _imgSpage.refresh();
                            var curGroupId = $(".group-item.active").data("id");
                            var curGroupName = $(".group-item.active").data("name");
                            mdu.updateCount(curGroupId,curGroupName,(0-(mediaIds.split(",").length)));
                        }
                    })
                });
            });
        },

        _toggleActions: function () {
            if($(".images-item.active").length > 0){
                if($("#mediaActions").is(":hidden")) {
                    $("#mediaActions").fadeIn();
                }
            }else{
                $("#mediaActions").fadeOut();
            }
        },

        /**
         * 获取已选择的素材id
         *
         * @returns {*}
         * @private
         */
        _getCheckedItemIds: function () {
            if($(".images-item.active").length === 0) {
                return null;
            }

            var checkItems=[];
            $.each($(".images-item.active"), function () {
                checkItems.push($(this).data("id"));
            });
            return checkItems.join(",");
        }
    };

    return mdu;
})(jQuery, App, Messenger, Spage, Uploader);
