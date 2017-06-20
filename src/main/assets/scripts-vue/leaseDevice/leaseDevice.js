/**
 * Created by lyc on 2017/4/20.
 */
//function down(x,y){
//    return (x.leaseStartDate < y.leaseStartDate)?1:-1
//}
new Vue({
    el: '#equip',
    data () {
        return {
            activeName: '支出明细',
            city: '所有地区',
            sorts: 'custom',
            searchText: '搜索',
            isIcon: false,
            operateBtn: true,
            equipSearches: {
                product: '',
                equipSearch: ''
            },
            typeSearch: {
                type: '',
                vals: ''
            },
            pageArray: {
                total: 0,
                current: 1,
                size: 10,
                fisrtPage: 0,
                lastPage: 0
            },
            equip: [
                {
                    style: 'blueBox',
                    name: '已租设备',
                    text: '0',
                    val: 'all'
                },
                {
                    style: 'grayBox',
                    name: '离线',
                    text: '0',
                    val: 'offline'
                },
                {
                    style: 'redBox',
                    name: '异常',
                    text: '0',
                    val: 'error'
                },
                {
                    style: 'yellowBox',
                    name: '即将到期',
                    text: '0',
                    val: 'soon'
                },
                {
                    style: 'blackBox',
                    name: '逾期',
                    text: '0',
                    val: 'overdue'
                }
            ],
            equipTab: [
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
            equipType: [
                {
                    name: '所有状态',
                    value: 'all'
                },
                {
                    name: '在线',
                    value: 'online'
                },
                {
                    name: '离线',
                    value: 'offline'
                },
                {
                    name: '异常',
                    value: 'error'
                },
                {
                    name: '即将到期',
                    value: 'soon'
                },
                {
                    name: '已逾期',
                    value: 'overdue'
                }
            ],
            equipProduct: [
                {
                    name: '所有产品',
                    value: ''
                }
            ],
            equipCity: [
                {
                    text: '北京'
                },
                {
                    text: '广州'
                }
            ],
            dataTit: [
                // {
                //     pro: 'select',
                //     width: '50',
                //     label: '',
                //     type: 'selection'
                // },
                {
                    pro: 'online',
                    label: '状态',
                    width: '100'
                },
                {
                    pro: 'leaseProductName',
                    width: '',
                    label: '产品名称',
                    type: ''
                },
                {
                    pro: 'lessee',
                    width: '150',
                    label: '客户名称',
                    type: ''
                },
                {
                    pro: 'address.fullAddress',
                    width: '',
                    label: '安装地址',
                    type: ''
                },
                {
                    pro: 'leaseStartDate',
                    width: '120',
                    label: '起租日期',
                    type: ''
                },
                {
                    pro: 'leaseDueDate',
                    width: '120',
                    label: '到期日期',
                    type: ''
                }
            ],
            equipData: [],
            returnEquipData: []
        }
    },
    created (){
        this.fetchData()
        this.$nextTick(() => {
            if(screen.width < 1199){
                this.tableTit()
            }
        })
    },
    methods: {
        //数据初始化
        fetchData(){
            let self = this
            if(sessionStorage.getItem('devicePage') !== null){
                self.pageArray.current = parseInt(sessionStorage.getItem('devicePage'))
            }else{
                self.pageArray.current = 1
            }
            self.typeSearch.vals = sessionStorage.getItem('deviceType')
            self.equipSearches.product = sessionStorage.getItem('deviceProduct')
            self.equipSearches.equipSearch = sessionStorage.getItem('deviceSearch')
            //获取租赁设备统计信息
            axios.get('/api/v1/lease/devices/dashboard')
                .then(function(rsp){
                    self.equip[0].text = rsp.data.data.totalDeviceCount
                    self.equip[1].text = rsp.data.data.offlineDeviceCount
                    self.equip[2].text = rsp.data.data.errorDeviceCount
                    self.equip[3].text = rsp.data.data.dueSoonDeviceCount
                    self.equip[4].text = rsp.data.data.overdueDeviceCount
                })
            //获取租赁设备分页数据
            axios.get('/api/v1/lease/devices', {
                params: {
                    current: self.pageArray.current,
                    rowCount: self.pageArray.size,
                    viewType: self.typeSearch.vals,
                    leaseProductId: self.equipSearches.product,
                    searchPhrase: self.equipSearches.equipSearch,
                    'sort[purchasedDate]': 'desc'
                }
            })
                .then(function (res) {
                    if (res.data.data === 'Error') {
                        self.$message.error(res.data.errMsg)
                    } else {
                        self.pageArray.total = res.data.data.totalElements
                        self.equipData = res.data.data.fileList
                        self.equipData.forEach((value,index,array) => {
                            if(array[index].iotDevice.online === false){
                                array[index].online = '离线'
                            }else{
                                array[index].online = '在线'
                            }
                        })
                        if (self.equipData.length < self.pageArray.size) {
                            if (self.equipData.length === 0) {
                                self.pageArray.fisrtPage = 0
                                self.pageArray.lastPage = 0
                            } else {
                                self.pageArray.fisrtPage = self.pageArray.total - self.equipData.length + 1
                                self.pageArray.lastPage = self.pageArray.total
                            }
                        } else if (self.equipData.length === self.pageArray.size) {
                            self.pageArray.fisrtPage = self.pageArray.current * self.equipData.length - (self.pageArray.size - 1)
                            self.pageArray.lastPage = self.pageArray.current * self.equipData.length
                        }
                        for (let i in self.equipData) {
                            let leaseStartDate = new Date(self.equipData[i].leaseStartDate)
                            let leaseDueDate = new Date(self.equipData[i].leaseDueDate)
                            if (self.equipData[i].leaseStartDate !== '' || self.equipData[i].leaseDueDate !== '') {
                                self.equipData[i].leaseStartDate = leaseStartDate.Format("yyyy-MM-dd")
                                self.equipData[i].leaseDueDate = leaseDueDate.Format("yyyy-MM-dd")
                            }
                        }
                        // if(screen.width < 1200){
                        //     self.equipData.forEach((value,index,array) => {
                        //         array[index].online = self.dataTit[0].label + ':' + array[index].online
                        //         array[index].leaseProductName = self.dataTit[1].label + ':' + array[index].leaseProductName
                        //         array[index].lessee = self.dataTit[2].label + ':' + array[index].online
                        //         array[index].leaseStartDate = self.dataTit[4].label + ':' + array[index].leaseStartDate
                        //         array[index].leaseDueDate = self.dataTit[5].label + ':' + array[index].leaseDueDate
                        //
                        //         if(array[index].address !== null){
                        //             array[index].address.fullAddress = self.dataTit[3].label + ':' + array[index].address.fullAddress
                        //         }else{
                        //             array[index].address.fullAddress = ''
                        //             // console.log(array[index].address.fullAddress)
                        //         }
                        //     })
                        // }
                    }
                })
                .catch(function (error) {
                    console.log(error)
                })
            //获取产品列表
            axios.get('/api/v1/lease/products/listOptions')
                .then(function(res){
                    var data = res.data.data.unique()
                    for(let i in data){
                        if(isNaN(parseInt(i))){
                            return false
                        }else{
                            self.equipProduct.push(data[i])
                        }
                    }

                })
                .catch(function (error) {
                    console.log(error)
                })
            if(screen.width < 1200){
                this.searchText = ''
                this.isIcon = true
                this.operateBtn = false
            }else{
                this.searchText = '搜索'
                this.isIcon = false
                this.operateBtn = true
            }
        },
        // 一页展示多少条
        handleSizeChange(val){
            let self = this
            self.pageArray.size = val
            //获取租赁设备分页数据
            axios.get('/api/v1/lease/devices', {
                params: {
                    current: self.pageArray.current,
                    rowCount: self.pageArray.size,
                    viewType: self.typeSearch.vals,
                    leaseProductId: self.equipSearches.product,
                    searchPhrase: self.equipSearches.equipSearch,
                    'sort[purchasedDate]': 'desc'
                }
            })
                .then(function (res) {
                    if (res.data.data === 'Error') {
                        self.$message.error(res.data.errMsg)
                    } else {
                        self.pageArray.total = res.data.data.totalElements
                        self.equipData = res.data.data.fileList
                        self.equipData.forEach((value,index,array) => {
                            if(array[index].iotDevice.online === false){
                                array[index].online = '离线'
                            }else{
                                array[index].online = '在线'
                            }
                        })
                        if (self.equipData.length < self.pageArray.size) {
                            if (self.equipData.length === 0) {
                                self.pageArray.fisrtPage = 0
                                self.pageArray.lastPage = 0
                            } else {
                                self.pageArray.fisrtPage = self.pageArray.total - self.equipData.length + 1
                                self.pageArray.lastPage = self.pageArray.total
                            }
                        } else if (self.equipData.length === self.pageArray.size) {
                            self.pageArray.fisrtPage = self.pageArray.current * self.equipData.length - (self.pageArray.size - 1)
                            self.pageArray.lastPage = self.pageArray.current * self.equipData.length
                        }
                        for (let i in self.equipData) {
                            let leaseStartDate = new Date(self.equipData[i].leaseStartDate)
                            let leaseDueDate = new Date(self.equipData[i].leaseDueDate)
                            if (self.equipData[i].leaseStartDate !== '' || self.equipData[i].leaseDueDate !== '') {
                                self.equipData[i].leaseStartDate = leaseStartDate.Format("yyyy-MM-dd")
                                self.equipData[i].leaseDueDate = leaseDueDate.Format("yyyy-MM-dd")
                            }
                        }
                    }
                })
                .catch(function (error) {
                    console.log(error)
                })
        },
        // 转到某一页
        handleCurrentChange(val){
            let self = this
            self.pageArray.current = val
            sessionStorage.setItem('devicePage',val)
            //获取租赁设备分页数据
            axios.get('/api/v1/lease/devices', {
                params: {
                    current: self.pageArray.current,
                    rowCount: self.pageArray.size,
                    viewType: self.typeSearch.vals,
                    leaseProductId: self.equipSearches.product,
                    searchPhrase: self.equipSearches.equipSearch,
                    'sort[purchasedDate]': 'desc'
                }
            })
                .then(function (res) {
                    if (res.data.data === 'Error') {
                        self.$message.error(res.data.errMsg)
                    } else {
                        self.pageArray.total = res.data.data.totalElements
                        self.equipData = res.data.data.fileList
                        self.equipData.forEach((value,index,array) => {
                            if(array[index].iotDevice.online === false){
                                array[index].online = '离线'
                            }else{
                                array[index].online = '在线'
                            }
                        })
                        if (self.equipData.length < self.pageArray.size) {
                            if (self.equipData.length === 0) {
                                self.pageArray.fisrtPage = 0
                                self.pageArray.lastPage = 0
                            } else {
                                self.pageArray.fisrtPage = self.pageArray.total - self.equipData.length + 1
                                self.pageArray.lastPage = self.pageArray.total
                            }
                        } else if (self.equipData.length === self.pageArray.size) {
                            self.pageArray.fisrtPage = self.pageArray.current * self.equipData.length - (self.pageArray.size - 1)
                            self.pageArray.lastPage = self.pageArray.current * self.equipData.length
                        }
                        for (let i in self.equipData) {
                            let leaseStartDate = new Date(self.equipData[i].leaseStartDate)
                            let leaseDueDate = new Date(self.equipData[i].leaseDueDate)
                            if (self.equipData[i].leaseStartDate !== '' || self.equipData[i].leaseDueDate !== '') {
                                self.equipData[i].leaseStartDate = leaseStartDate.Format("yyyy-MM-dd")
                                self.equipData[i].leaseDueDate = leaseDueDate.Format("yyyy-MM-dd")
                            }
                        }
                    }
                })
                .catch(function (error) {
                    console.log(error)
                })
        },
        getValue(index){
            if(sessionStorage){
                sessionStorage.setItem('value',this.equip[index].val)
                let val = sessionStorage.getItem('value')
                this.typeSearch.vals = val
            }
        },
        //保存当前选中ID
        gainID(row){
            console.log(row)
            sessionStorage.setItem('id',row.id)
            sessionStorage.setItem('leaseProductId',row.leaseProductId)
            sessionStorage.setItem('productId',row.productId)
            sessionStorage.setItem('deviceId',row.deviceId)
            window.location.href = '/lease/devices/' + row.id
        },
        sortChange(column){
            console.log(column)
        },
        searchChange(){
            let self = this
            sessionStorage.setItem('deviceType',self.typeSearch.vals)
            sessionStorage.setItem('deviceProduct',self.equipSearches.product)
            sessionStorage.setItem('deviceSearch',self.equipSearches.equipSearch)
            axios.get('/api/v1/lease/devices', {
                params: {
                    current: self.pageArray.current,
                    rowCount: self.pageArray.size,
                    viewType: self.typeSearch.vals,
                    leaseProductId: self.equipSearches.product,
                    searchPhrase: self.equipSearches.equipSearch,
                    'sort[purchasedDate]': 'desc'
                }
            })
                .then(function (res) {
                    if (res.data.data === 'Error') {
                        self.$message.error(res.data.errMsg)
                    } else {
                        self.pageArray.total = res.data.data.totalElements
                        self.equipData = res.data.data.fileList
                        self.equipData.forEach((value,index,array) => {
                            if(array[index].iotDevice.online === false){
                                array[index].online = '离线'
                            }else{
                                array[index].online = '在线'
                            }
                        })
                        if (self.equipData.length < self.pageArray.size) {
                            if (self.equipData.length === 0) {
                                self.pageArray.fisrtPage = 0
                                self.pageArray.lastPage = 0
                            } else {
                                self.pageArray.fisrtPage = self.pageArray.total - self.equipData.length + 1
                                self.pageArray.lastPage = self.pageArray.total
                            }
                        } else if (self.equipData.length === self.pageArray.size) {
                            self.pageArray.fisrtPage = self.pageArray.current * self.equipData.length - (self.pageArray.size - 1)
                            self.pageArray.lastPage = self.pageArray.current * self.equipData.length
                        }
                        if (self.equipSearches.product === 'all') {
                            return self.equipData
                        }
                        for (let i in self.equipData) {
                            let leaseStartDate = new Date(self.equipData[i].leaseStartDate)
                            let leaseDueDate = new Date(self.equipData[i].leaseDueDate)
                            if (self.equipData[i].leaseStartDate !== '' || self.equipData[i].leaseDueDate !== '') {
                                self.equipData[i].leaseStartDate = leaseStartDate.Format("yyyy-MM-dd")
                                self.equipData[i].leaseDueDate = leaseDueDate.Format("yyyy-MM-dd")
                            }
                        }
                    }
                })
                .catch(function (error) {
                    console.log(error)
                })
        },
        rowClass(row,index){
            if(new Date() > new Date(row.leaseDueDate)){
                return 'overdue equipRecords'
            }else{
                return 'equipRecords'
            }
        },
        deviceFresh(){
            sessionStorage.setItem('devicePage',1)
            sessionStorage.setItem('deviceType','')
            sessionStorage.setItem('deviceProduct','')
            sessionStorage.setItem('deviceSearch','')
        },
        tableTit(){
            // let body = document.getElementsByClassName('equipRecords')
            // for(let i = 0;i<body.length;i++){
            //     let cell = body[i].getElementsByClassName('cell')
            //     for(let j = 0;j<cell.length;j++){
            //         console.log(this.dataTit[j].label)
            //         cell[j].innerHTML += '<div class="cellTit">' + this.dataTit[j].label + '</div>'
            //     }
            // }
        }
    }
//computed: {
//    equipDataSearch: function(){
//        let self = this
//        let equipSearch = this.equipSearches.equipSearch
//        let type = this.equipSearches.type
//        let product = this.equipSearches.product
//        //console.log(type)
//
//                ////三个选项
//                //if (product !== 'all' && type === 'all' && equipSearch !== '') {
//                //    return this.equipData.filter(function (data) {
//                //        return data['leaseProductId'].indexOf(product) > -1 && Object.keys(data).some(function (key) {
//                //                return String(data[key]).toLowerCase().indexOf(equipSearch) > -1
//                //            })
//                //    })
//                //}
//                //if (product === 'all' && type !== 'all' && equipSearch !== '') {
//                //    return this.equipData.filter(function (data) {
//                //        return data['status'].indexOf(type) > -1 && Object.keys(data).some(function (key) {
//                //                return String(data[key]).toLowerCase().indexOf(equipSearch) > -1
//                //            })
//                //    })
//                //}
//                //
//                ////两个选项
//                //if (product !== 'all' && type === 'all') {
//                //    return this.equipData.filter(function (data) {
//                //        return data['leaseProductId'].indexOf(product) > -1
//                //    })
//                //}
//                //if (product === 'all' && type !== 'all') {
//                //    return this.equipData.filter(function (data) {
//                //        return data['status'].indexOf(type) > -1
//                //    })
//                //}
//
//                //“所有”选项
//                //if (type == 'all') {
//                //    return this.equipData
//                //}
//                if (product == 'all') {
//                    return this.equipData
//                }
//                //默认筛选
//                //return this.equipData.filter(function (data) {
//                //    return data['leaseProductId'].indexOf(product) > -1 && Object.keys(data).some(function (key) {
//                //            return String(data[key]).toLowerCase().indexOf(equipSearch) > -1
//                //        })
//                //})
//        return this.equipData
//}
})