var ShopEmployeeSelector=function(e){function n(e){var n="选择服务人员";"CUSTOM_SERVICER"===c&&(n="选择客服人员"),new Dlg({id:"selectEmployeeDlg",title:n,url:o+"/select",width:800,height:500,onLoaded:function(){t()},onOk:function(){if(null===l||0===l.length)return void toast("请选择人员信息","error");e.call(this,l),this.close()}})}function t(){i=new BootGrid({id:"employee4selectGrid",url:o,data:{employeeRole:c},selection:!0,multiSelect:r,rowSelect:!0,rowCount:10,onSelected:function(e){-1===_.findIndex(l,function(n){return n.id===e[0].id})&&l.push(e[0])},onDeSelected:function(e){_.remove(l,function(n){return n.id===e[0].id})}})}var o="/shop/employee",i=null,l=[],c="SERVICE_MAN",r=!1;return{selectServiceMan:function(e){n(e)},selectServiceMans:function(e){r=!0,n(e)}}}(jQuery);