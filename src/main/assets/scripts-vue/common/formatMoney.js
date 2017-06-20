/**
 * Created by lyc on 2017/6/12.
 */
Number.prototype.formatMoney = function(places,symbol,thousand,decimal){
    places = !isNaN(places = Math.abs(places)) ? places : 2
    symbol = symbol !== undefined ? symbol : '￥'
    thousand = thousand || ','
    decimal = decimal || '.'
    let number = this
    let neg = number < 0 ? '-' : ''
    let i = parseInt(number = Math.abs(+number || 0).toFixed(places),10) + ''
    let j = (j = i.length) > 3 ? j % 3 : 0
    return symbol + neg + (j ? i.substr(0,j) + thousand : '') + i.substr(j).replace(/(\d{3})(?=\d)/g,'$1' + thousand) + (places ? decimal + Math.abs(number - i).toFixed(places).slice(2) : '')
}