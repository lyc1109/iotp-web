/**
 * Created by lyc on 2017/5/22.
 */
let vm = new Vue({
    el: '#rechargeorders',
    data (){
        return {
            cloak: true,
            rechargeordersTab: [
                {
                    title: '充值订单流水号',
                    content: '',
                    url: 'javascript:;'
                },
                {
                    title: '客户姓名',
                    content: '',
                    url: 'javascript:;'
                },
                {
                    title: '租赁设备',
                    content: '',
                    url: 'javascript:;',
                    style: 'blue'
                },
                {
                    title: '充值套餐',
                    content: '',
                    url: 'javascript:;'
                },
                {
                    title: '支付总余额',
                    content: '',
                    url: 'javascript:;'
                },
                {
                    title: '支付时间',
                    content: '',
                    url: 'javascript:;'
                },
                {
                    title: '租赁设备可用期',
                    content: '',
                    url: 'javascript:;'
                },
                {
                    title: '订单创建时间',
                    content: '',
                    url: 'javascript:;'
                },
                {
                    title: '支付凭证号',
                    content: '',
                    url: 'javascript:;'
                }
            ]
        }
    },
    created(){
        this.fetchData()
    },
    methods: {
        fetchData(){
            let self = this
            let id = sessionStorage.getItem('id')
            let rechargeId = sessionStorage.getItem('rechargeId')
            axios.get('/api/v1/lease/rechargeorders/' + rechargeId)
                .then(function(res){
                    let leaseEndDate = new Date(res.data.data.leaseEndDate).Format('yyyy-MM-dd')
                    let created = new Date(res.data.data.created).Format('yyyy-MM-dd')
                    let paidAt = new Date(res.data.data.paidAt).Format('yyyy-MM-dd')
                    self.rechargeordersTab[0].content = res.data.data.orderId
                    self.rechargeordersTab[1].content = res.data.data.lesseeName
                    self.rechargeordersTab[2].content = res.data.data.leaseProductName
                    self.rechargeordersTab[3].content = res.data.data.leaseProductPackageName
                    self.rechargeordersTab[4].content = (res.data.data.totalAmount/100).toFixed(2)
                    self.rechargeordersTab[5].content = paidAt
                    self.rechargeordersTab[6].content = leaseEndDate
                    self.rechargeordersTab[7].content = created
                    self.rechargeordersTab[8].content = res.data.data.transactionId
                    // self.rechargeordersTab[1].url = '/shop/member/' + res.data.data.lesseeId
                    self.rechargeordersTab[2].url = '/lease/devices/' + rechargeId
                })
        }
    }
})