var ShopVendorSelector=function(e){function t(e){new BootGrid({id:"selectShopVendorGrid",url:o+"/select/page",data:{shopServiceId:e},selection:!0,multiSelect:!1,rowSelect:!0,rowCount:10,formatters:{addressFormatter:function(e,t){return t.province+t.city+t.area+t.address}},onSelected:function(e){n=e[0]}})}var o="/shop/vendor",n=null;return{select2Assign:function(r,i){new e({id:"selectShopVendorDlg",title:"选择服务商",url:o+"/select",width:800,height:500,onLoaded:function(){t(r)},onOk:function(){if(null===n)return void toast("请选择服务商","error");i.call(this,n),this.close()}})}}}(Dlg);