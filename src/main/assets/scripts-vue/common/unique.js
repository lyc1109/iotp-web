/**
 * Created by lyc on 2017/4/24.
 */
Array.prototype.unique=function(){
    let res = [];
    let json = {};
    for(var i=0;i<this.length;i++){
        if(!json[this[i].name]){
            res.push(this[i]);
            json[this[i].name] = !0;
        }
    }
    return res;
}