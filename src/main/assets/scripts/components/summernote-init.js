/**
 * 初始化summernote编辑器脚本
 *
 * @author huchiwei
 * @create 2016-12-27
 */
$(function () {
    if(typeof ImagePicker === undefined) {
        console.log("请依赖图片选择器: /dist/scripts/pages/media/media-image-picker.js");
        return;
    }

    if($(".summernote").length > 0){
        $(".summernote").summernote({
            height: 'auto',
            minHeight: 300,
            maxHeight: 1000,
            lang: 'zh-CN',
            toolbar: [
                ['style', ['style']],
                ['font', ['bold', 'italic', 'underline', 'strikethrough']],
                ['fontsize', ['fontsize']],
                ['color', ['color']],
                ['para', ['ul', 'ol', 'paragraph']],
                /*['height', ['height']],*/
                ['insert', ['table', 'link', 'pickImg']],
                ['misc', ['codeview']]
            ],
            buttons: {
                // 自定义图片选择按钮
                pickImg: function (context) {
                    var ui = $.summernote.ui;
                    var button = ui.button({
                        className: 'summernote-image-picker',
                        contents: '<i class="fa fa-picture-o"/>',
                        tooltip: '图片上传',
                        click: function () {
                            context.invoke('editor.saveRange');
                        }
                    });
                    return button.render();   // return button as jquery object
                }
            },
            callbacks: {
                onInit: function() {
                    var $ele = $(this);

                    var btnPickerId = 'summernoteImagePicker' + new Date().getTime() + Math.floor(Math.random() * (1000 - 100) + 100);
                    $ele.next(".note-editor")
                        .find(".summernote-image-picker")
                        .attr("id", btnPickerId);
                    new ImagePicker({
                        id: btnPickerId,
                        multiple: true,
                        onOk: function (resp) {
                            $ele.summernote('restoreRange');
                            $ele.summernote('focus');

                            $.each(resp, function (idx, img) {
                                $ele.summernote('insertImage', img.url);
                            });
                        }
                    });
                },
                onFocus: function() {
                    $(this).summernote('saveRange');
                },
                onKeydown: function() {
                    $(this).summernote('saveRange');
                }
            }
        });
    }
});
