/**
 * Created by lyc on 2017/6/8.
 */
let vm = new Vue({
    el: '#home',
    data () {
        return {
            cloak: true,
            orderListName: 'doing',
            agendaName: 'agenda',
            totalDeviceCount: 0,
            memberCount: 0,
            balance: '￥0.00',
            isHover: false,
            activeClass: 'animated slideInLeft',
            leaveClass: 'animated slideOutRight',
            agendaLabel: '我的待办',
            chartsData: [],
            oldVal: [],
            moduleBox: [
                {
                    icon: 'iconfont icon-xinjianfuwudingdan',
                    label: '新建服务订单',
                    bg: 'rgba(255, 198, 75, 0.90)',
                    href: '/shop/serviceOrder/create',
                    hover: 'rotateY(0deg)'
                },
                {
                    icon: 'iconfont icon-fuwudingdan',
                    label: '新建租赁订单',
                    bg: 'rgba(50, 168, 238, 0.75)',
                    href: '/shop/serviceOrder/create',
                    hover: 'rotateY(0deg)'
                },
                {
                    icon: 'iconfont icon-dingdanchaxun',
                    label: '订单查询',
                    bg: '#00dacc',
                    href: '/shop/serviceOrder',
                    hover: 'rotateY(0deg)'
                },
                {
                    icon: 'iconfont icon-fuwushangguanli',
                    label: '服务商管理',
                    bg: 'rgba(227, 69, 81, 0.7)',
                    href: '/shop/vendor',
                    hover: 'rotateY(0deg)'
                }
            ],
            deviceBox: [
                {
                    icon: 'iconfont icon-zaixian',
                    title: '租赁设备',
                    value: 0
                },
                {
                    icon: 'iconfont icon-zaixian',
                    title: '在线设备',
                    value: 0
                },
                {
                    icon: 'iconfont icon-tingzhi',
                    title: '异常设备',
                    value: 0
                }
            ],
            deviceBox2: [
                {
                    icon: 'iconfont icon-chengshifugai',
                    title: '城市覆盖',
                    value: 0
                },
                {
                    icon: 'iconfont icon-qianyuefuwushang',
                    title: '签约服务商',
                    value: 0
                },
                {
                    icon: 'iconfont icon-fuwudingdan',
                    title: '服务订单',
                    value: 0
                }
            ],
            deviceBox3: [
                {
                    icon: 'iconfont icon-yajinzonge',
                    title: '押金总额',
                    value: '￥0.00'
                },
                {
                    icon: 'iconfont icon-yuchongzhiyajin',
                    title: '预充值押金',
                    value: '￥0.00'
                },
                {
                    icon: 'iconfont icon-keyongyue',
                    title: '可用余额',
                    value: '￥0.00'
                }
            ],
            orderList: [
                {
                    label: '执行中订单',
                    name: 'doing'
                },
                {
                    label: '最新订单',
                    name: 'newest'
                }
            ],
            doingData: [],
            newestData: [],
            agendaTab: [
                {
                    prop: 'processFormEntityType',
                    label: '待办类型',
                    width: ''
                },
                {
                    prop: 'subject',
                    label: '标题',
                    width: ''
                }
            ],
            agendaData: [],
            helpLogo: [
                {
                    icon: 'iconfont icon-xingshourumen',
                    label: '新手入门',
                    href: 'http://www.xohaa.net/help/content/details/Ljloz9/mjyWgB?pageCode=help'
                },
                {
                    icon: 'iconfont icon-lianxiwomen',
                    label: '联系我们',
                    href: 'http://www.xohaa.net/help/content/details/k6rAjm/Ljloz9?pageCode=faq='
                }
            ]
        }
    },
    created(){
        this.fetchData()
    },
    mounted(){
        this.$nextTick(() => {
            this.deviceNums()
            this.scroll()
        })
    },
    methods: {
        fetchData(){
          let self = this
            // 激活总数、会员总数、资金池
            axios('/api/v1/home')
                .then((res) => {
                    // self.totalDeviceCount = res.data.data.totalDeviceCount
                    // self.memberCount = res.data.data.memberCount
                    // self.balance = res.data.data.balance.formatMoney()
                    // self.deviceBox[0].value = res.data.data.leaseDeviceCount
                    // self.deviceBox[1].value = res.data.data.onlineDeviceCount
                    // self.deviceBox[2].value = res.data.data.errorDeviceCount
                    // self.deviceBox2[0].value = res.data.data.cityCount
                    // self.deviceBox2[1].value = res.data.data.vendorCount
                    // self.deviceBox2[2].value = res.data.data.serviceOrderCount
                    // self.deviceBox3[0].value = res.data.data.totalDeposit.formatMoney()
                    // self.deviceBox3[1].value = res.data.data.rentalBalance.formatMoney()
                    // self.deviceBox3[2].value = res.data.data.availableBalance.formatMoney()
                    // self.numAnimate(self.totalDeviceCount,res.data.data.totalDeviceCount)
                    self.chartsData = res.data.data.deviceActiveDatas
                    // self.oldVal = [res.data.data.totalDeviceCount,res.data.data.memberCount,res.data.data.balance,res.data.data.leaseDeviceCount,res.data.data.onlineDeviceCount,res.data.data.errorDeviceCount,
                    //     res.data.data.cityCount,res.data.data.vendorCount,res.data.data.serviceOrderCount,res.data.data.totalDeposit,res.data.data.rentalBalance,res.data.data.availableBalance]
                    // 设备总数
                    if(res.data.data.totalDeviceCount > 0) {
                        let totalDeviceCountAnimate = setInterval(() => {
                            this.totalDeviceCount = Math.floor(Math.random() * Math.pow(10, res.data.data.totalDeviceCount.toString().length))
                        },10)
                        setTimeout(() => {
                            clearInterval(totalDeviceCountAnimate)
                            this.totalDeviceCount = res.data.data.totalDeviceCount
                        },1000)
                    }else {
                        this.totalDeviceCount = res.data.data.totalDeviceCount
                    }
                    // 会员总数
                    if(res.data.data.memberCount > 0) {
                        let memberCountAnimate = setInterval(() => {
                            this.memberCount = Math.floor(Math.random() * Math.pow(10, res.data.data.memberCount.toString().length))
                        },10)
                        setTimeout(() => {
                            clearInterval(memberCountAnimate)
                            this.memberCount = res.data.data.memberCount
                        },1000)
                    }else {
                        this.memberCount = res.data.data.memberCount
                    }
                    // 租赁设备
                    if(res.data.data.leaseDeviceCount > 0) {
                        let leaseDeviceCountAnimate = setInterval(() => {
                            this.deviceBox[0].value = Math.floor(Math.random() * Math.pow(10, res.data.data.leaseDeviceCount.toString().length))
                        },10)
                        setTimeout(() => {
                            clearInterval(leaseDeviceCountAnimate)
                            this.deviceBox[0].value = res.data.data.leaseDeviceCount
                        },1000)
                    }else {
                        this.deviceBox[0].value = res.data.data.leaseDeviceCount
                    }
                    // 在线设备
                    if(res.data.data.onlineDeviceCount > 0) {
                        let onlineDeviceCountAnimate = setInterval(() => {
                            this.deviceBox[1].value = Math.floor(Math.random() * Math.pow(10, res.data.data.onlineDeviceCount.toString().length))
                        },10)
                        setTimeout(() => {
                            clearInterval(onlineDeviceCountAnimate)
                            this.deviceBox[1].value = res.data.data.onlineDeviceCount
                        },1000)
                    }else {
                        this.deviceBox[1].value = res.data.data.onlineDeviceCount
                    }
                    // 异常设备
                    if(res.data.data.errorDeviceCount > 0){
                        let errorDeviceCountAnimate = setInterval(() => {
                            this.deviceBox[2].value = Math.floor(Math.random() * Math.pow(10,res.data.data.errorDeviceCount.toString().length))
                        },10)
                        setTimeout(() => {
                            clearInterval(errorDeviceCountAnimate)
                            this.deviceBox[2].value = res.data.data.errorDeviceCount
                        },1000)
                    }else {
                        this.deviceBox[2].value = res.data.data.errorDeviceCount
                    }
                    // 城市覆盖
                    if(res.data.data.cityCount > 0) {
                        let cityCountAnimate = setInterval(() => {
                            this.deviceBox2[0].value = Math.floor(Math.random() * Math.pow(10, res.data.data.cityCount.toString().length))
                        },10)
                        setTimeout(() => {
                            clearInterval(cityCountAnimate)
                            this.deviceBox2[0].value = res.data.data.cityCount
                        },1000)
                    }else {
                        this.deviceBox2[0].value = res.data.data.cityCount
                    }
                    // 签约服务商
                    if(res.data.data.vendorCount > 0) {
                        let vendorCountAnimate = setInterval(() => {
                            this.deviceBox2[1].value = Math.floor(Math.random() * Math.pow(10, res.data.data.vendorCount.toString().length))
                        },10)
                        setTimeout(() => {
                            clearInterval(vendorCountAnimate)
                            this.deviceBox2[1].value = res.data.data.vendorCount
                        },1000)
                    }else {
                        this.deviceBox2[1].value = res.data.data.vendorCount
                    }
                    // 可用余额
                    if(res.data.data.availableBalance > 0) {
                        let availableBalanceAnimate = setInterval(() => {
                            this.deviceBox3[2].value = Math.floor(Math.random() * Math.pow(10, res.data.data.availableBalance.toString().length)).formatMoney()
                        },10)
                        setTimeout(() => {
                            clearInterval(availableBalanceAnimate)
                            this.deviceBox3[2].value = res.data.data.availableBalance.formatMoney()
                        },1000)
                    }else {
                        this.deviceBox3[2].value = res.data.data.availableBalance
                    }
                    // 押金总额
                    if(res.data.data.totalDeposit > 0) {
                        let totalDepositAnimate = setInterval(() => {
                            this.deviceBox3[0].value = Math.floor(Math.random() * Math.pow(10, res.data.data.totalDeposit.toString().length)).formatMoney()
                        },10)
                        setTimeout(() => {
                            clearInterval(totalDepositAnimate)
                            this.deviceBox3[0].value = res.data.data.totalDeposit.formatMoney()
                        },1000)
                    }else {
                        this.deviceBox3[0].value = res.data.data.totalDeposit
                    }
                    // 预充值押金
                    if(res.data.data.rentalBalance > 0) {
                        let rentalBalanceAnimate = setInterval(() => {
                            this.deviceBox3[1].value = Math.floor(Math.random() * Math.pow(10, res.data.data.rentalBalance.toString().length)).formatMoney()
                        },10)
                        setTimeout(() => {
                            clearInterval(rentalBalanceAnimate)
                            this.deviceBox3[1].value = res.data.data.rentalBalance.formatMoney()
                        },1000)
                    }else {
                        this.deviceBox3[1].value = res.data.data.rentalBalance
                    }
                    // 服务订单
                    if(res.data.data.serviceOrderCount > 0) {
                        let serviceOrderCountAnimate = setInterval(() => {
                            this.deviceBox2[2].value = Math.floor(Math.random() * Math.pow(10, res.data.data.serviceOrderCount.toString().length))
                        },10)
                        setTimeout(() => {
                            clearInterval(serviceOrderCountAnimate)
                            this.deviceBox2[2].value = res.data.data.serviceOrderCount
                        },1000)
                    }else {
                        this.deviceBox2[2].value = res.data.data.serviceOrderCount
                    }
                    // 资金池余额
                    if(res.data.data.balance > 0) {
                        let balanceAnimate = setInterval(() => {
                            this.balance = Math.floor(Math.random() * Math.pow(10, res.data.data.balance.toString().length)).formatMoney()
                        },10)
                        setTimeout(() => {
                            clearInterval(balanceAnimate)
                            this.balance = res.data.data.balance.formatMoney()
                        },1000)
                    }else {
                        this.balance = res.data.data.balance
                    }
                })
                .catch((err) => {
                    console.log(err)
                })

            // 执行中订单
            axios.get('/api/v1/serviceOrders/processing')
                .then((res) => {
                    self.doingData = res.data.data.fileList
                    self.orderList[0].label = '执行中订单（' + res.data.data.fileList.length + '）'
                })
                .catch((err) => {
                    console.log(err)
                })
            // 最新订单
            axios.get('/api/v1/serviceOrders/newest')
                .then((res) => {
                    self.newestData = res.data.data.fileList
                    self.orderList[1].label = '最新订单（' + res.data.data.fileList.length + '）'
                })
                .catch((err) => {
                    console.log(err)
                })
            // 我的待办
            axios.get('/api/v1/workflow/instances/myTodoList')
                .then((res) => {
                    self.agendaData = res.data.data.fileList
                    self.agendaLabel = '我的待办（' + res.data.data.fileList.length + '）'
                })
                .catch((err) => {
                    console.log(err)
                })
        },
        deviceNums () {
            $(function(){
                let chart = null
                $('#device-nums').highcharts({
                    chart: {
                        type: 'line',
                        events: {
                            load: function(e){
                                let categories = []
                                let data1 = []
                                let data2 = []
                                vm.chartsData.forEach((value,index,array) => {
                                    categories.push(moment(array[index][0]).format('dddd'))
                                    data1.push(array[index][1])
                                    data2.push(array[index][2])
                                })
                                let xAxis = chart.xAxis[0]
                                let series1 = chart.series[0]
                                let series2 = chart.series[1]
                                xAxis.update({
                                    categories: categories
                                })
                                series1.update({
                                    data: data1
                                })
                                series2.update({
                                    data: data2
                                })
                            }
                        }
                    },
                    title: {
                        text: ''
                    },
                    xAxis: {
                        categories: [],
                        tickmarkPlacement: 'on',
                        title: {
                            enabled: false
                        },
                        // labels: {
                        //     rotation: 0
                        // }
                    },
                    yAxis: {
                        title: {
                            text: ''
                        },
                        labels: {
                            formatter: function () {
                                return this.value
                            }
                        },
                        tickPixelInterval: '40'
                    },
                    tooltip: {
                        split: true,
                        valueSuffix: '台'
                    },
                    credits: {
                        text: ''
                    },
                    series: [{
                        name: '设备激活总数',
                        data: [],
                        color: '#49b949'
                    }, {
                        name: '租赁设备激活总数',
                        data: [],
                        color: '#63a7ee'
                    }],
                    legend: {
                        itemStyle: {
                            color: '#757575',
                            fontWeight: 'normal'
                        }
                    }
                }, function(c) {
                    chart = c
                })
                // chart.xAxis.categories.forEach((value,index,array) => {
                //     array[index] = vm.chartsData[index][0]
                // })
            })
        },
        doingClass(row,index){
            if(index%2 === 0){
                return 'doingStripe'
            }
        },
        doingID(index){
            let self = this
            if(sessionStorage){
                sessionStorage.setItem('serviceID',self.doingData[index].id)
                let serviceID = sessionStorage.getItem('serviceID')
                window.location.href = '/shop/serviceOrder/' + serviceID
            }
        },
        newestID(index){
            let self = this
            if(sessionStorage){
                sessionStorage.setItem('serviceID',self.newestData[index].id)
                let serviceID = sessionStorage.getItem('serviceID')
                window.location.href = '/shop/serviceOrder/' + serviceID
            }
        },
        agendaID(index){
            let self = this
            if(sessionStorage){
                sessionStorage.setItem('expenseId',self.agendaData[index].processInstanceId)
            }
            window.location.href = self.agendaData[index].processFormUrl
        },
        scrollBar(obj){
            $(obj).stop().animate({
                marginTop : "-40px"
            },800,function(){
                $(this).css({marginTop : "0px"}).find("li:first").appendTo(this)
            })
        },
        // 消息滚动动画
        scroll(){
            let timer = setInterval(() => {
                this.scrollBar('.message-box-main')
            },2000)
            // let timer = null
            $(".message-box-main").hover(function(){
                clearInterval(timer)
            },function(){
                timer = setInterval(() => {
                    vm.scrollBar('.message-box-main')
                },2000)
            })
        },
        boxEnter(index){
            if(this.moduleBox[index].hover === 'rotateY(0deg)'){
                this.moduleBox[index].hover = 'rotateY(360deg)'
            }else{
                this.moduleBox[index].hover = 'rotateY(0deg)'
            }
        },
        boxLeave(index){
            if(this.moduleBox[index].hover === 'rotateY(0deg)'){
                this.moduleBox[index].hover = 'rotateY(0deg)'
            }else{
                this.moduleBox[index].hover = 'rotateY(360deg)'
            }
        },
        // numAnimate() {
        //     let self = this
        //     let newVal = [self.totalDeviceCount,self.memberCount,self.balance,self.deviceBox[0].value,self.deviceBox[1].value,self.deviceBox[2].value,self.deviceBox2[0].value,self.deviceBox2[1].value,
        //         self.deviceBox2[2].value,self.deviceBox3[0].value,self.deviceBox3[1].value,self.deviceBox3[2].value]
        //     let oldVal = self.oldVal
        //     newVal.forEach((value,index,array) => {
        //         if(oldVal[index] > 0) {
        //             let rentalBalanceAnimate = setInterval(() => {
        //                 array[index] = Math.floor(Math.random() * Math.pow(10, oldVal[index].toString().length))
        //             },10)
        //             setTimeout(() => {
        //                 clearInterval(rentalBalanceAnimate)
        //                 array[index] = oldVal[index]
        //             },1000)
        //         }else {
        //             array[index] = oldVal[index]
        //         }
        //     })
        // }
    },
    watch: {
        orderListName(newVal,oldVal){
            let self = this
            if(screen.width <769){
                self.activeClass = ''
                self.leaveClass = ''
            }else{
                if(newVal === 'doing'){
                    self.activeClass = 'animated slideInLeft'
                    self.leaveClass = 'animated slideOutRight'
                }else if(newVal === 'newest'){
                    self.activeClass = 'animated slideInRight'
                    self.leaveClass = 'animated slideOutLeft'
                }
            }
        }
    }
})