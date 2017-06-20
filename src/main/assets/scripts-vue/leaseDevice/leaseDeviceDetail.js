/**
 * Created by lyc on 2017/4/20.
 */
let vm = new Vue({
    el: '#equipDetail',
    data () {
        return {
            activeName: '支出明细',
            type: '所有类型',
            product: '所有产品',
            city: '所有地区',
            detailBtn: '',
            isFocus1: false,
            isFocus: false,
            active: '基本信息',
            equipInfoShade: false,
            imgSrc: '',
            filterTotal: 0,
            filterReplace: 0,
            padding: {
                padding: '10px'
            },
            deviceLogPage: {
                current: 1,
                size: 4
            },
            totalElements: 0,
            moreText: '查看更多',
            isBlue: false,
            moreIcon: true,
            isNull: false,
            noFilterDetails: false,
            filterDetails: true,
            iotpStatus: '',
            iotpStatusDetail: '',
            reminder: false,
            estimatedDays: '',
            dialogVisible: false,
            maintain: false,
            modal: false,
            modalBody: false,
            onoff: false,
            successAnimate: false,
            noData: false,
            waterRecordBtn: '周',
            waterDate: '',
            equipStateToggle: false,
            _chart: null,
            dateSize: 'tiny',
            orders: {},
            deviceLog: [
                {
                    createdAt: '2017-06-19',
                    logContext: '是不是很好'
                },
                {
                    createdAt: '2017-06-19',
                    logContext: '是不是很好'
                },
                {
                    createdAt: '2017-06-19',
                    logContext: '是不是很好'
                },
                {
                    createdAt: '2017-06-19',
                    logContext: '是不是很好'
                },
                {
                    createdAt: '2017-06-19',
                    logContext: '是不是很好'
                }
            ],
            waterData: [],
            openoffBtn: [
                {
                    name: '开/关机',
                    value: 'onoff'
                },
                {
                    name: '一键冲洗',
                    value: 'flush'
                },
                {
                    name: '恢复出厂设置',
                    value: 'restore'
                }
            ],
            dataTypes: [
                {
                    label: '周',
                    type: '周'
                },
                {
                    label: '月',
                    type: '月'
                },
                {
                    label: '年',
                    type: '年'
                }
            ],
            pageArray: {
                total: 0,
                current: 1,
                size: 10,
                fisrtPage: 0,
                lastPage: 0
            },
            iotpStatuseImg: {
                lixian: false,
                off: false,
                noWater: false,
                leak: false,
                sensor: false
            },
            equipDetail: [
                {
                    text: '总用水量',
                    key: '',
                    style: 'blueBox',
                    unit: 'L'
                },
                {
                    text: '日均用水量',
                    key: '',
                    style: 'cambridgeBlueBox',
                    unit: 'L'
                },
                {
                    text: 'TDS值',
                    key: '',
                    style: 'greenBox',
                    unit: ''
                }
            ],
            equipInfoDetail: [
                {
                    title: '租赁产品名称',
                    name: '',
                    href: 'javascript:',
                    style: 'blueColor'
                },
                {
                    title: '产品设备型号',
                    name: '',
                    href: 'javascript:',
                    style: 'blueColor'
                },
                {
                    title: '设备编号',
                    name: '',
                    href: 'javascript:'
                },
                {
                    title: '客户姓名',
                    name: '',
                    href: 'javascript:'
                },
                {
                    title: '安装地址',
                    name: '',
                    href: 'javascript:'
                },
                {
                    title: '租赁单号',
                    name: '',
                    href: '',
                    style: 'blueColor'
                },
                {
                    title: '起租日期',
                    name: '',
                    href: 'javascript:'
                },
                {
                    title: '到期日期',
                    name: '',
                    style: 'redColor',
                    href: 'javascript:'
                }
            ],
            equipDetailTab: [
                {
                    label: '支出明细',
                    name: '支出明细'
                },{
                    label: '待审批支出',
                    name: '待审批支出'
                },
                {
                    label: '服务商提现申请',
                    name: '服务商提现申请'
                }
            ],
            equipData: [
                {
                    label: '基本信息',
                    name: '基本信息'
                },
                {
                    label: '服务记录',
                    name: '服务记录'
                },
                {
                    label: '充值记录',
                    name: '充值记录'
                },
                {
                    label: '数据报表',
                    name: '数据报表'
                }
            ],
            rechargeTit: [
                // {
                //     prop: 'select',
                //     width: '50',
                //     type: 'selection',
                //     label: ''
                // },
                {
                    prop: 'orderId',
                    width: '',
                    type: '',
                    label: '充值单号'
                },
                {
                    prop: 'summary',
                    width: '',
                    type: '',
                    label: '充值套餐'
                },
                {
                    prop: 'totalAmount',
                    width: '',
                    type: '',
                    label: '充值金额(元)'
                },
                {
                    prop: 'leaseEndDate',
                    width: '',
                    type: '',
                    label: '租赁到期日期'
                },
                {
                    prop: 'paidAt',
                    width: '',
                    type: '',
                    label: '充值时间'
                }
            ],
            seviceTit: [
                {
                    prop: 'select',
                    width: '50',
                    type: 'selection',
                    label: ''
                },
                {
                    prop: 'status',
                    width: '',
                    type: '',
                    label: '进度状态'
                },
                {
                    prop: 'shopServiceName',
                    width: '',
                    type: '',
                    label: '服务类型'
                },
                {
                    prop: 'orderId',
                    width: '',
                    type: '',
                    label: '服务单号'
                },
                {
                    prop: 'serviceVendor',
                    width: '',
                    type: '',
                    label: '服务商'
                },
                {
                    prop: 'contactName',
                    width: '',
                    type: '',
                    label: '客户姓名'
                },
                {
                    prop: 'serviceDate',
                    width: '',
                    type: '',
                    label: '预约时间'
                },
                {
                    prop: 'created',
                    width: '',
                    type: '',
                    label: '创建时间'
                }
            ],
            filterData: [],
            serviceData: [],
            rechargeData: [],
            statement: [],
            waterRecord: []
        }
    },
    created(){
        this.$nextTick(() => {
            this.detail()
        })
    },
    mounted () {
        this.$nextTick(() => {
            this.serviceTableTit()
            this.rechargeTableTit()
        })
    },
    methods: {
        //数据初始化
        detail () {
            let self = this
            let id = sessionStorage.getItem('id')
            let leaseProductId = sessionStorage.getItem('leaseProductId')
            let productId = sessionStorage.getItem('productId')
            axios.get('/api/v1/lease/devices/'+id)
                .then(function(res){
                    if(res.data.data.iotDevice.online === false){
                        self.equipInfoShade = true
                    }else{
                        self.equipInfoShade = false
                    }
                    if(res.data.data.alertInfo === ''){
                        self.equipStateToggle = false
                    }else{
                        self.equipStateToggle = true
                    }
                    self.iotpStatus = res.data.data.alertInfo
                    //基本资料
                    self.imgSrc = res.data.data.productImage
                    self.equipInfoDetail[0].name = res.data.data.leaseProductName
                    self.equipInfoDetail[1].name = res.data.data.itemCode
                    if(res.data.data.iotDevice.deviceMac !== null){
                        self.equipInfoDetail[2].name = res.data.data.deviceId + '(' + res.data.data.iotDevice.deviceMac.toUpperCase() + ')'
                    }else{
                        self.equipInfoDetail[2].name = '(未知)'
                    }
                    // self.equipInfoDetail[2].name = res.data.data.deviceId + '(' + res.data.data.iotDevice.deviceMac.toUpperCase() + ')'
                    if(res.data.data.lessee === '' || null){
                        self.equipInfoDetail[3].name = res.data.data.lesseeMobile
                    }else{
                        self.equipInfoDetail[3].name = res.data.data.lessee
                    }
                    if(res.data.data.address === null){
                        self.equipInfoDetail[4].name = '(未知)'
                    }else{
                        self.equipInfoDetail[4].name = res.data.data.address.fullAddress
                    }
                    if(res.data.data.leaseOrderId === null){
                        self.equipInfoDetail[5].name = '(未知)'
                    }else{
                        self.equipInfoDetail[5].name = res.data.data.leaseOrderId
                    }
                    // self.equipInfoDetail[4].name = res.data.data.address.fullAddress
                    // self.equipInfoDetail[5].name = res.data.data.leaseOrderId
                    self.equipInfoDetail[5].href = res.data.data.leaseOrderUrl
                    self.equipInfoDetail[6].name = new Date(res.data.data.leaseStartDate).Format("yyyy-MM-dd")
                    self.equipInfoDetail[7].name = new Date(res.data.data.leaseDueDate).Format("yyyy-MM-dd")
                    self.equipInfoDetail[0].href = '/lease/product/' + leaseProductId
                    self.equipInfoDetail[1].href = '/shop/product/' + productId
                    //用水量
                    self.equipDetail[0].key = res.data.data.iotDevice.outWaterTotalFlow/1000
                    self.equipDetail[1].key = res.data.data.iotDevice.outWaterTodayFlow/1000
                    self.equipDetail[2].key = res.data.data.iotDevice.outWaterTds
                    //滤芯详情
                    if(res.data.data.iotDevice.parts !== null){
                        self.noFilterDetails = false
                        self.filterDetails = true
                        res.data.data.iotDevice.parts.forEach((value,index,array) => {
                            self.filterData.push(array[index])
                            //console.log(self.filterData[i].partProductId)
                            self.filterTotal = self.filterData.length
                            if(array[index].estimatedDays === -1){
                                self.filterData[index].estimatedDays = '估算中...'
                            }
                            let filters = self.filterData.filter(function(r){
                                return r.availablePercentage <= 5
                            })
                            self.filterReplace = filters.length
                        })
                    }else{
                        self.noFilterDetails = true
                        self.filterDetails = false
                    }
                })
            //获取服务订单数据
            axios.get('/api/v1/lease/devices/' + id + '/serviceOrders')
                .then(function(res){
                    self.serviceData = res.data.data
                    for(let i in res.data.data){
                        if(isNaN(parseInt(i))){
                            return false
                        }else{
                            // console.log(self.serviceData[i].id)
                            let serviceDate = new Date(res.data.data[i].serviceDate)
                            let created = new Date(res.data.data[i].created)
                            //console.log(self.serviceData[i].serviceDate)
                            if(self.serviceData[i].serviceDate !== '' || self.serviceData[i].created !== ''){
                                self.serviceData[i].serviceDate = serviceDate.Format('yyyy-MM-dd')
                                self.serviceData[i].created = created.Format('yyyy-MM-dd')
                            }
                            switch (res.data.data[i].status){
                                case 0:
                                    self.serviceData[i].status = '待分派'
                                    break
                                case 10:
                                    self.serviceData[i].status = '待分派'
                                    break
                                case 15:
                                    self.serviceData[i].status = '待分派'
                                    break
                                case 20:
                                    self.serviceData[i].status = '已分派'
                                    break
                                case 40:
                                    self.serviceData[i].status = '待计价'
                                    break
                                case 41:
                                    self.serviceData[i].status = '进行中'
                                    break
                                case 45:
                                    self.serviceData[i].status = '已付款'
                                    break
                                case 60:
                                    self.serviceData[i].status = '待付款'
                                    break
                                case 65:
                                    self.serviceData[i].status = '已付款，待确认'
                                    break
                                case 70:
                                    self.serviceData[i].status = '待评价'
                                    break
                                case 80:
                                    self.serviceData[i].status = '已完成'
                                    break
                                case 85:
                                    self.serviceData[i].status = '已取消，待确认'
                                    break
                                case 90:
                                    self.serviceData[i].status = '已取消'
                                    break
                                case 92:
                                    self.serviceData[i].status = '待退款'
                                    break
                                case 95:
                                    self.serviceData[i].status = '厂商已拒绝'
                                    break
                            }
                        }
                    }
                })
            // 获取充值记录数据
            axios.get('/api/v1/lease/devices/' + id + '/rechargeOrders')
                .then(function(res){
                    self.rechargeData = res.data.data
                    for(let i in res.data.data){
                        if(isNaN(parseInt(i))){
                            return
                        }else {
                            let paidAt = new Date(res.data.data[i].paidAt)
                            let leaseEndDate = new Date(res.data.data[i].leaseEndDate)
                            if(self.rechargeData[i].paidAt !== '' || self.rechargeData[i].leaseEndDate !== ''){
                                self.rechargeData[i].paidAt = paidAt.Format('yyyy-MM-dd hh:mm:ss')
                                self.rechargeData[i].leaseEndDate = leaseEndDate.Format('yyyy-MM-dd')
                            }
                            self.rechargeData[i].totalAmount
                            let totalAmount = self.rechargeData[i].totalAmount / 100
                            self.rechargeData[i].totalAmount = totalAmount.toFixed(2)
                        }
                    }
                })
            if(screen.width < 1199){
                self.dateSize = 'small'
            }
            //净水记录数据获取
            this.getWaterRecord()
            // 获取设备日志数据
            this.deviceLogs()
            // 获取用水统计数据
            this.charts()
        },
        //获取滤芯图片链接的产品id
        getPartProductId(index){
            if(sessionStorage){
                sessionStorage.setItem('partProductId',this.filterData[index].partProductId)
                sessionStorage.getItem('partProductId')
                //this.filterData[index].href = this.filterData[index].partProductId
            }
        },
        //滤芯详情复位
        restoration(index){
            let self = this
            let id = sessionStorage.getItem('id')
            sessionStorage.setItem('filterIndex',index)
            let filterIndex = sessionStorage.getItem('filterIndex')
            axios.post('/api/v1/lease/devices/' + id + '/resetFilter/' + filterIndex)
                .then(function(res){
                    self.filterData[index].availablePercentage = res.data.data.availablePercentage
                    self.filterData[index].partProductImage = res.data.data.partProductImage
                    self.filterData[index].name = res.data.data.name
                    self.filterData[index].installedAt = res.data.data.installedAt
                    self.filterTotal = self.filterData.length
                    if(res.data.data.estimatedDays === -1){
                        self.filterData[index].estimatedDays = '估算中...'
                    }
                    self.$message({
                        message: '复位成功',
                        type: 'success'
                    })
                })
                .catch(function(error){
                    console.log(error)
                })
        },
        //净水记录数据获取
        getWaterRecord () {
            let self = this
            let id = sessionStorage.getItem('id')
            axios.get('/api/v1/lease/devices/' + id + '/datanodes',{
                params: {
                    current: this.pageArray.current,
                    rowCount: this.pageArray.size,
                    'sort[startTime]': 'desc'
                }
            })
                .then(function(res){
                    self.pageArray.total = res.data.data.totalElements
                    self.waterRecord = res.data.data.fileList
                    if(self.waterRecord.length < self.pageArray.size){
                        if(self.waterRecord.length === 0){
                            self.pageArray.fisrtPage = 0
                            self.pageArray.lastPage = 0
                        }else{
                            self.pageArray.fisrtPage = self.pageArray.total - self.waterRecord.length + 1
                            self.pageArray.lastPage = self.pageArray.total
                        }
                    }else if(self.waterRecord.length === self.pageArray.size){
                        self.pageArray.fisrtPage = self.pageArray.current * self.waterRecord.length - (self.pageArray.size - 1)
                        self.pageArray.lastPage = self.pageArray.current * self.waterRecord.length
                    }
                    self.waterRecord.forEach((value,index,array) => {
                        let startTime = new Date(array[index].startTime)
                        let endTime = new Date(array[index].endTime)
                        array[index].startTime = startTime.Format('yyyy-MM-dd hh:mm:ss')
                        array[index].endTime = endTime.Format('yyyy-MM-dd hh:mm:ss')
                    })
                    if(self.waterRecord.length === 0){
                        self.noData = true
                    }else{
                        self.noData = false
                    }
                })
                .catch(function(error){
                    console.log(error)
                })
        },
        openOnoff(index){
            let self = this
            let id = sessionStorage.getItem('id')
            if(sessionStorage){
                sessionStorage.setItem('instructionCode',self.openoffBtn[index].value)
            }
            let instructionCode = sessionStorage.getItem('instructionCode')
            axios.post('/api/v1/lease/devices/' + id + '/executeInstruction/' + instructionCode)
                .then(function(res){
                    sessionStorage.setItem('instructionId',res.data.data)
                    let instructionId = sessionStorage.getItem('instructionId')
                    self.maintain = false
                    self.$message({
                        message: '开始执行',
                        type: 'success'
                    })
                    setTimeout(()=>{
                        self.onoff = true
                    },500)
                    const count = (function() {
                        let timer
                        let i = 0

                        function change(tar) {
                            i++
                            // console.log(i)
                            axios.get('/api/v1/iot/instructions/' + instructionId + '/result')
                                .then(function (res) {
                                    if (i < tar) {
                                        if (res.data.data !== null) {
                                            clearTimeout(timer)
                                            self.onoff = false
                                            self.$message({
                                                message: '执行成功',
                                                type: 'success'
                                            })
                                            return false
                                        }
                                    }else if(i === tar){
                                        if (res.data.data === null) {
                                            clearTimeout(timer)
                                            self.onoff = false
                                            self.$message({
                                                message: '执行失败',
                                                type: 'error'
                                            })
                                            return false
                                        } else {
                                            if (res.data.data.errorMsg === null) {
                                                clearTimeout(timer)
                                                self.onoff = false
                                                self.$message({
                                                    message: '执行成功',
                                                    type: 'success'
                                                })
                                                return false
                                            } else {
                                                clearTimeout(timer)
                                                self.onoff = false
                                                self.$message({
                                                    message: '执行失败',
                                                    type: 'error'
                                                })
                                            }
                                        }
                                    }
                                })
                                .catch(function (err) {
                                    console.log(err)
                                })

                            timer = setTimeout(function () {
                                change(tar)
                            }, 1000)
                        }
                        return change
                    })()
                    count(15)
                })
                .catch(function(err){
                    console.log(err)
                })
        },
// 一页展示多少条
        handleSizeChange(val){
            this.pageArray.size = val
            this.getWaterRecord()
        },
// 转到某一页
        handleCurrentChange(val){
            this.pageArray.current = val
            this.getWaterRecord()
        },
        getRechargeId(row){
            if(sessionStorage){
                sessionStorage.setItem('rechargeId',row.id)
            }
            window.location.href = '/lease/rechargeOrder/' + row.id
        },
        // 设备日志初始化
        deviceLogs(){
            let self = this
            let deviceId = sessionStorage.getItem('deviceId')
            axios.get('/api/v1/devices/' + deviceId + '/deviceLogs',{
                params: {
                    current: self.deviceLogPage.current,
                    rowCount: self.deviceLogPage.size
                }
            })
                .then((res) => {
                    self.totalElements = res.data.data.totalElements
                    self.deviceLog = res.data.data.fileList
                    res.data.data.fileList.forEach((value,index,array) => {
                        if(array[index].createdAt === null || array[index].createdAt === ''){
                            self.deviceLog[index].createdAt = '(未知时间)'
                        }else{
                            self.deviceLog[index].createdAt = new Date(array[index].createdAt).Format('yyyy-MM-dd hh:mm:ss')
                        }
                        if(array[index].sourceUri === null || array[index].sourceUri === ''){
                            self.deviceLog[index].sourceUri = 'javascript:;'
                        }else{
                            self.deviceLog[index].sourceUri = array[index].sourceUri
                        }
                    })
                    if(self.totalElements === 0){
                        self.moreText = '暂无数据'
                        self.moreIcon = false
                        self.isBlue = true
                        self.isNull = true
                    }else if(self.deviceLog.length <= 4){
                        self.moreText = '没有了'
                        self.moreIcon = false
                        self.isBlue = true
                        self.isNull = true
                    }
                })
                .catch((err) => {
                    console.log(err)
                })
        },
        // 设备日志异步加载
        loadMore(){
            let self = this
            if(self.deviceLog.length < self.totalElements && self.totalElements !== 0){
                self.deviceLogPage.current ++
                axios.get('/api/v1/devices/' + deviceId + '/deviceLogs',{
                    params: {
                        current: self.deviceLogPage.current,
                        rowCount: self.deviceLogPage.size
                    }
                })
                    .then((res) => {
                        res.data.data.fileList.forEach((value,index,array) => {
                            if(array[index].createdAt === null || array[index].createdAt === ''){
                                array[index].createdAt = '(未知时间)'
                            }else{
                                array[index].createdAt = new Date(array[index].createdAt).Format('yyyy-MM-dd hh:mm:ss')
                            }
                            self.deviceLog.push(array[index])
                        })
                        if(self.deviceLog.length >= self.totalElements){
                            self.moreText = '没有了'
                            self.moreIcon = false
                            self.isBlue = true
                        }
                    })
                    .catch((err) => {
                        console.log(err)
                    })
            }else if(self.deviceLog.length >= self.totalElements){
                return false
            }
        },
        //用水统计表
        waterFetch(dateScopeType,dates) {
            let id = sessionStorage.getItem('id')
            let self = this
            let categories = []
            let step = 1
            axios.get('/api/v1/lease/devices/' + id + '/report', {
                params: {
                    dateScopeType: dateScopeType,
                    startDate: dates
                }
            })
                .then((resp)=> {
                    self.waterData = resp.data.data
                    if(dateScopeType === "周"){
                        step = 1
                        self.waterData.forEach((value,index,array) => {
                            categories.push(moment(array[index].date).format('dddd'))
                        })
                    }
                    else if(dateScopeType === "月"){
                        step = 1
                        self.waterData.forEach((value,index,array) => {
                            categories.push(moment(array[index].date).format('MM-DD'))
                        })
                    }
                    else if(dateScopeType === "年"){
                        self.waterData.forEach((value,index,array) => {
                            categories.push(moment(array[index].date).format('YYYY-MM'))
                        })
                    }

                    // 更新x坐标
                    let xAxis = self._chart.xAxis[0]
                    xAxis.update({
                        labels: {
                            step: step,
                            rotation: -45
                        },
                        categories: categories
                    })
                    // 更新对应数据列
                    let series = self._chart.series[0],
                        series1 = self._chart.series[1],
                        series2 = self._chart.series[2],
                        series3 = self._chart.series[3]

                    let seriesData = [],
                        seriesData1 = [],
                        seriesData2 = [],
                        seriesData3 = []
                    self.waterData.forEach((value,index,array) => {
                        seriesData.push(array[index].inWaterTds)
                        seriesData1.push(array[index].inWaterFlow)
                        seriesData2.push(array[index].outWaterTds)
                        seriesData3.push(array[index].outWaterFlow)
                    })
                    series.update({
                        data: seriesData
                    })
                    series1.update({
                        data: seriesData1
                    })
                    series2.update({
                        data: seriesData2
                    })
                    series3.update({
                        data: seriesData3
                    })
                })
                .catch((err) => {
                    console.log(err)
                })

        },
        //用水统计表
        charts(){
            let self = this
            $(() =>{
                self._chart = new Highcharts.Chart({
                    chart: {
                        renderTo: 'container',
                        events: {
                            load: self.waterFetch("周",new Date())
                        }
                    },
                    title: {
                        text: ''
                    },
                    legend: {
                        enabled: false
                    },
                    xAxis: {
                        crosshair: true
                    },
                    yAxis: {
                        min: 0,
                        title: {
                            text: ''
                        }
                    },
                    series: [
                        {
                            name: '净水TDS值',
                            type: 'spline',
                            data: []
                        },
                        {
                            name: '净水量(ml)',
                            type: 'column',
                            data: []
                        },
                        {
                            name: '出水TDS值',
                            type: 'spline',
                            data: []
                        },
                        {
                            name: '出水量(ml)',
                            type: 'column',
                            data: []
                        }
                    ],
                    credits:{
                        enabled: false
                    }
                })
            })
        },
        changeWater(value){
            let self = this
            if(self.waterDate === ''){
                self.waterFetch(value,new Date())
            }else{
                self.waterFetch(value,self.waterDate)
            }
        },
        changeWaterDate(value){
            let self = this
            self.waterFetch(self.waterRecordBtn,value)
        },
        serviceDatas(row){
          return 'serviceData'
        },
        rechargeDatas(row){
          return 'rechargeData'
        },
        serviceTableTit(){
            let body = document.getElementsByClassName('serviceData')
            console.log(body)
            for(let i = 0;i<body.length;i++){
                let cell = body[i].getElementsByClassName('cell')
                // for(let j = 0;j<cell.length;j++){
                //     cell[j].innerHTML += '<div class="cellTit">' + this.iotDeviceTit[j].label + '</div>'
                // }
                console.log(cell)
                cell[0].innerHTML += '<div class="cellTit">进度状态</div>'
                cell[1].innerHTML += '<div class="cellTit">服务类型</div>'
                cell[2].innerHTML += '<div class="cellTit">服务单号</div>'
                cell[3].innerHTML += '<div class="cellTit">服务商</div>'
                cell[4].innerHTML += '<div class="cellTit">客户姓名</div>'
                cell[5].innerHTML += '<div class="cellTit">预约时间</div>'
                cell[6].innerHTML += '<div class="cellTit">创建时间</div>'
            }
        },
        rechargeTableTit(){
            let body = document.getElementsByClassName('rechargeData')
            for(let i = 0;i<body.length;i++){
                let cell = body[i].getElementsByClassName('cell')
                for(let j = 0;j<cell.length;j++){
                    cell[j].innerHTML += '<div class="cellTit">' + this.rechargeTit[j].label + '</div>'
                }
            }
        }
    }
})
