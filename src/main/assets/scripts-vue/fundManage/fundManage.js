/**
 * Created by lyc on 2017/4/21.
 */
let vm = new Vue({
    el: '#fundManage',
    data () {
        return {
            addExpendsDia: false,
            modalBody: true,
            modal: true,
            activeName: '交易记录',
            type: '',
            dateSelect: [],
            getDay: '',
            expenseTit: '',
            sortable: true,
            createdArr: [],
            created: '',
            defaultSort: {
                prop: 'accountingDate',
                order: 'descending'
            },
            pageArray: {
                total: 0,
                size: 10,
                current: parseInt(sessionStorage.getItem('fundSize')),
                lastPage: '',
                firstPage: ''
            },
            appPageArray: {
                total: 0,
                size: 10,
                current: parseInt(sessionStorage.getItem('appCurrent')),
                lastPage: '',
                firstPage: ''
            },
            fundManageSearch: '',
            dateOption: {
                shortcuts: [
                    {
                        text: '最近一周',
                        onClick(picker){
                            const end = new Date()
                            const start = new Date()
                            start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
                            picker.$emit('pick',[start,end])
                        }
                    },
                    {
                        text: '最近一月',
                        onClick(picker){
                            const end = new Date()
                            const start = new Date()
                            start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
                            picker.$emit('pick',[start,end])

                        }
                    }
                ]
            },
            fundManage: [
                {
                    text: '已收用户押金',
                    money: '',
                    style: 'greenBox',
                    iconClass: 'icon-img_x'
                },
                {
                    text: '近7日收入',
                    money: '',
                    style: 'yellowBox',
                    iconClass: 'icon-img_x1'
                },
                {
                    text: '近7日支出',
                    money: '',
                    style: 'CambridgeBlueBox',
                    iconClass: 'icon-icon_expense'
                },
                {
                    text: '可用余额',
                    money: '',
                    style: 'purpleBox',
                    iconClass: 'icon-yue'
                },
                {
                    text: '资金池余额',
                    money: '',
                    style: 'blueBox',
                    iconClass: 'icon-zijin'
                }
            ],
            fundManageTab: [
                {
                    label: '交易记录',
                    name: '交易记录'
                },
                {
                    label: '待审批支出',
                    name: '待审批支出'
                }
                //{
                //    label: '服务商提现申请',
                //    name: '服务商提现申请'
                //}
            ],
            fundManageType: [
                {
                    name: '押金',
                    value: 'deposit'
                },
                {
                    name: '收入',
                    value: 'income'
                },
                {
                    name: '支出',
                    value: 'expense'
                },
                {
                    name: '租金',
                    value: 'rental'
                },
                {
                    name: '所有类型',
                    value: ''
                }
            ],
            dataTit: [
                {
                    pro: 'select',
                    width: '50',
                    label: '',
                    type: 'selection',
                },
                {
                    pro: 'type',
                    width: '100',
                    label: '交易类型',
                    type: ''
                },
                {
                    pro: 'accountingDate',
                    width: '150',
                    label: '交易时间',
                    type: ''
                },
                {
                    pro: 'summary',
                    width: '450',
                    label: '交易摘要',
                    type: '',
                    fit: false
                },
                {
                    pro: 'amount',
                    width: '',
                    label: '交易金额',
                    type: ''
                },
                {
                    pro: 'customerName',
                    width: '',
                    label: '经办人',
                    type: ''
                }
            ],
            approvalTit: [
                {
                    pro: 'expenseType',
                    width: '',
                    label: '支出类型',
                    type: ''
                },
                {
                    pro: 'expenseReason',
                    width: '450',
                    label: '支出事由',
                    type: ''
                },
                {
                    pro: 'totalAmount',
                    width: '',
                    label: '支出金额',
                    type: ''
                },
                {
                    pro: 'creator',
                    width: '',
                    label: '申请人',
                    type: ''
                },
                {
                    pro: 'created',
                    width: '',
                    label: '申请时间',
                    type: ''
                },
                {
                    pro: 'toders',
                    width: '',
                    label: '当前处理人',
                    type: ''
                }
            ],
            fundManageData: [],
            approvalData: []
        }
    },
    created(){
        this.fetchData()
    },
    methods: {
        //初始化数据
        fetchData(){
            let self = this
            // 获取存储过滤和分页选项
            if(sessionStorage.getItem('appCurrent') !== null){
                self.appPageArray.current = parseInt(sessionStorage.getItem('appCurrent'))
            }else{
                self.appPageArray.current = 1
            }
            if(sessionStorage.getItem('fundSize') !== null){
                self.pageArray.current = parseInt(sessionStorage.getItem('fundSize'))
            }else{
                self.pageArray.current = 1
            }
            self.type = sessionStorage.getItem('fundType')
            self.dateSelect = [sessionStorage.getItem('fundStartDate'),sessionStorage.getItem('fundEndDate')]
            self.fundManageSearch = sessionStorage.getItem('fundSearch')
            if(sessionStorage.getItem('tabName') !== null){
                self.activeName = sessionStorage.getItem('tabName')
            }else{
                self.activeName = '交易记录'
            }

            //账户资金信息
            axios.get("/api/v1/funds/dashboard")
                .then(function (response) {
                    self.fundManage[0].money = response.data.data.totalDeposit / 100
                    self.fundManage[1].money = response.data.data.incomeFor7d / 100
                    self.fundManage[2].money = response.data.data.expenseFor7d / 100
                    self.fundManage[3].money = response.data.data.availableBalance / 100
                    self.fundManage[4].money = response.data.data.totalIncome / 100
                })
                .catch(function (error) {
                    console.log(error)
                })

            //资金分页数据
            axios.get("/api/v1/funds/account/details", {
                params: {
                    current: self.pageArray.current,
                    rowCount: self.pageArray.size,
                    'sort[accountingDate]': 'desc',
                    type:  self.type,
                    startDate: self.dateSelect[0] === '' ? '' : sessionStorage.getItem('fundStartDate'),
                    endDate: self.dateSelect[1] === '' ? '' : sessionStorage.getItem('fundEndDate'),
                    searchPhrase: self.fundManageSearch
                }
            })
                .then(function (res) {
                    self.fundManageData = res.data.data.fileList
                    self.pageArray.total = res.data.data.totalElements
                    // console.log(self.dateSelect)
                    if(self.fundManageData.length < self.pageArray.size){
                        if(self.fundManageData.length === 0){
                            self.pageArray.fisrtPage = 0
                            self.pageArray.lastPage = 0
                        }else{
                            self.pageArray.fisrtPage = self.pageArray.total - self.fundManageData.length + 1
                            self.pageArray.lastPage = self.pageArray.total
                        }
                    }else if(self.fundManageData.length === self.pageArray.size){
                        self.pageArray.fisrtPage = self.pageArray.current * self.fundManageData.length - (self.pageArray.size - 1)
                        self.pageArray.lastPage = self.pageArray.current * self.fundManageData.length
                    }
                    self.fundManageData.forEach((value,index,array) => {
                        let accountingDate = new Date(array[index].accountingDate)
                        array[index].accountingDate = accountingDate.Format('yyyy-MM-dd')
                        array[index].amount = (array[index].amount / 100).toFixed(2)
                        if(array[index].type === 'expense'){
                            array[index].customerName = res.data.data.fileList[index].operator
                        }else{
                            array[index].customerName = res.data.data.fileList[index].customerName
                        }
                        switch (array[index].type) {
                            case 'deposit':
                                array[index].type = '押金'
                                break
                            case 'income':
                                array[index].type = '收入'
                                break
                            case 'expense':
                                array[index].type = '支出'
                                break
                            case 'rental':
                                array[index].type = '租金'
                                break
                        }
                    })
                })
                .catch(function (error) {
                    console.log(error)
                })
            // window.history.replaceState({}, 0, self.buildListUrl())
            // self.reloadGird()

            //审批分页数据初始化
            axios('/api/v1/funds/expenseform', {
                params: {
                    current: self.appPageArray.current,
                    rowCount: self.appPageArray.size,
                    'sort[created]': 'desc',
                    viewType: 'processing'
                }
            })
                .then(function (res) {
                    self.approvalData = res.data.data.fileList
                    self.appPageArray.total = res.data.data.totalElements
                    if(self.approvalData.length < self.appPageArray.size){
                        self.appPageArray.fisrtPage = self.appPageArray.total - self.approvalData.length + 1
                        self.appPageArray.lastPage = self.appPageArray.total
                    }else if(self.approvalData.length === self.appPageArray.size){
                        self.appPageArray.fisrtPage = self.appPageArray.current * self.approvalData.length - (self.appPageArray.size - 1)
                        self.appPageArray.lastPage = self.appPageArray.current * self.approvalData.length
                    }
                    res.data.data.fileList.forEach((value,index,array) => {
                        let created = new Date(array[index].created)
                        array[index].created = created.Format('yyyy-MM-dd')
                        array[index].totalAmount = (array[index].totalAmount/100).toFixed(2)
                })

                })
                .catch(function (err) {
                    console.log(err)
                })

        },
        //转到某一页(资金)
        currentChange(val)
        {
            let self = this
            self.pageArray.current = val
            sessionStorage.setItem('fundSize',val)
            axios.get("/api/v1/funds/account/details", {
                params: {
                    current: self.pageArray.current,
                    rowCount: self.pageArray.size,
                    'sort[accountingDate]': 'desc',
                    type: self.type,
                    startDate: self.dateSelect[0] === '' ? '' : new Date(self.dateSelect[0]).Format('yyyy-MM-dd'),
                    endDate: self.dateSelect[1] === '' ? '' : new Date(self.dateSelect[1]).Format('yyyy-MM-dd'),
                    searchPhrase: self.fundManageSearch
                }
            })
                .then(function (res) {
                    self.fundManageData = res.data.data.fileList
                    self.pageArray.total = res.data.data.totalElements
                    if(self.fundManageData.length < self.pageArray.size){
                        if(self.fundManageData.length === 0){
                            self.pageArray.fisrtPage = 0
                            self.pageArray.lastPage = 0
                        }else{
                            self.pageArray.fisrtPage = self.pageArray.total - self.fundManageData.length + 1
                            self.pageArray.lastPage = self.pageArray.total
                        }
                    }else if(self.fundManageData.length === self.pageArray.size){
                        self.pageArray.fisrtPage = self.pageArray.current * self.fundManageData.length - (self.pageArray.size - 1)
                        self.pageArray.lastPage = self.pageArray.current * self.fundManageData.length
                    }
                    self.fundManageData.forEach((value,index,array) => {
                        let accountingDate = new Date(array[index].accountingDate)
                        array[index].accountingDate = accountingDate.Format('yyyy-MM-dd')
                        array[index].amount = (array[index].amount / 100).toFixed(2)
                        if(array[index].type === 'expense'){
                            array[index].customerName = res.data.data.fileList[index].operator
                        }else{
                            array[index].customerName = res.data.data.fileList[index].customerName
                        }
                        switch (array[index].type) {
                            case 'deposit':
                                array[index].type = '押金'
                                break
                            case 'income':
                                array[index].type = '收入'
                                break
                            case 'expense':
                                array[index].type = '支出'
                                break
                            case 'rental':
                                array[index].type = '租金'
                                break
                        }
                })
                })
                .catch(function (error) {
                    console.log(error)
                })
        },
        //一页显示多少条(资金)
        sizeChange(val)
        {
            let self = this
            self.pageArray.size = val
            axios.get("/api/v1/funds/account/details", {
                params: {
                    current: self.pageArray.current,
                    rowCount: self.pageArray.size,
                    'sort[accountingDate]': 'desc',
                    type: self.type,
                    startDate: self.dateSelect[0] === '' ? '' : new Date(self.dateSelect[0]).Format('yyyy-MM-dd'),
                    endDate: self.dateSelect[1] === '' ? '' : new Date(self.dateSelect[1]).Format('yyyy-MM-dd'),
                    searchPhrase: self.fundManageSearch
                }
            })
                .then(function (res) {
                    self.pageArray.current = sessionStorage.getItem('fundSize')
                    self.fundManageData = res.data.data.fileList
                    self.pageArray.total = res.data.data.totalElements
                    if(self.fundManageData.length < self.pageArray.size){
                        if(self.fundManageData.length === 0){
                            self.pageArray.fisrtPage = 0
                            self.pageArray.lastPage = 0
                        }else{
                            self.pageArray.fisrtPage = self.pageArray.total - self.fundManageData.length + 1
                            self.pageArray.lastPage = self.pageArray.total
                        }
                    }else if(self.fundManageData.length === self.pageArray.size){
                        self.pageArray.fisrtPage = self.pageArray.current * self.fundManageData.length - (self.pageArray.size - 1)
                        self.pageArray.lastPage = self.pageArray.current * self.fundManageData.length
                    }
                    self.fundManageData.forEach((value,index,array) => {
                        let accountingDate = new Date(array[index].accountingDate)
                        array[index].accountingDate = accountingDate.Format('yyyy-MM-dd')
                        array[index].amount = (array[index].amount / 100).toFixed(2)
                        if(array[index].type === 'expense'){
                            array[index].customerName = res.data.data.fileList[index].operator
                        }else{
                            array[index].customerName = res.data.data.fileList[index].customerName
                        }
                        switch (array[index].type) {
                            case 'deposit':
                                array[index].type = '押金'
                                break
                            case 'income':
                                array[index].type = '收入'
                                break
                            case 'expense':
                                array[index].type = '支出'
                                break
                            case 'rental':
                                array[index].type = '租金'
                                break
                        }
                    })
                })
                .catch(function (error) {
                    console.log(error)
                })
        },
        //转到某一页(审批)
        appCurrentChange(val)
        {
            let self = this
            self.appPageArray.current = val
            sessionStorage.setItem('appCurrent',val)
            axios('/api/v1/funds/expenseform', {
                params: {
                    current: self.appPageArray.current,
                    rowCount: self.appPageArray.size,
                    'sort[created]': 'desc',
                    viewType: 'processing'
                }
            })
                .then(function (res) {
                    self.approvalData = res.data.data.fileList
                    self.appPageArray.total = res.data.data.totalElements
                    if(self.approvalData.length < self.appPageArray.size){
                        self.appPageArray.fisrtPage = self.appPageArray.total - self.approvalData.length + 1
                        self.appPageArray.lastPage = self.appPageArray.total
                    }else if(self.approvalData.length === self.appPageArray.size){
                        self.appPageArray.fisrtPage = self.appPageArray.current * self.approvalData.length - (self.appPageArray.size - 1)
                        self.appPageArray.lastPage = self.appPageArray.current * self.approvalData.length
                    }
                    res.data.data.fileList.forEach((value,index,array) => {
                        let created = new Date(array[index].created)
                        array[index].created = created.Format('yyyy-MM-dd')
                        array[index].totalAmount = (array[index].totalAmount/100).toFixed(2)
                    })
                })
                .catch(function (err) {
                    console.log(err)
                })
        },
        //一页显示多少条(审批)
        appSizeChange(val)
        {
            let self = this
            self.appPageArray.size = val
            axios('/api/v1/funds/expenseform', {
                params: {
                    current: self.appPageArray.current,
                    rowCount: self.appPageArray.size,
                    'sort[created]': 'desc',
                    viewType: 'processing'
                }
            })
                .then(function (res) {
                    self.approvalData = res.data.data.fileList
                    self.appPageArray.total = res.data.data.totalElements
                    if(self.approvalData.length < self.appPageArray.size){
                        self.appPageArray.fisrtPage = self.appPageArray.total - self.approvalData.length + 1
                        self.appPageArray.lastPage = self.appPageArray.total
                    }else if(self.approvalData.length === self.appPageArray.size){
                        self.appPageArray.fisrtPage = self.appPageArray.current * self.approvalData.length - (self.appPageArray.size - 1)
                        self.appPageArray.lastPage = self.appPageArray.current * self.approvalData.length
                    }
                    res.data.data.fileList.forEach((value,index,array) => {
                        let created = new Date(array[index].created)
                        array[index].created = created.Format('yyyy-MM-dd')
                        array[index].totalAmount = (array[index].totalAmount/100).toFixed(2)
                    })
                })
                .catch(function (err) {
                    console.log(err)
                })
        },
        //搜索功能初始化(资金)
        searchValue()
        {
            let self = this
            sessionStorage.setItem('fundType',self.type)
            sessionStorage.setItem('fundStartDate',self.dateSelect[0])
            sessionStorage.setItem('fundEndDate',self.dateSelect[1])
            sessionStorage.setItem('fundSearch',self.fundManageSearch)
            axios.get("/api/v1/funds/account/details", {
                params: {
                    current: self.pageArray.current,
                    rowCount: self.pageArray.size,
                    'sort[accountingDate]': 'desc',
                    type: self.type,
                    startDate: self.dateSelect[0] === '' ? '' : new Date(self.dateSelect[0]).Format('yyyy-MM-dd'),
                    endDate: self.dateSelect[1] === '' ? '' : new Date(self.dateSelect[1]).Format('yyyy-MM-dd'),
                    searchPhrase: self.fundManageSearch
                }
            })
                .then(function (res) {
                    self.fundManageData = res.data.data.fileList
                    self.pageArray.total = res.data.data.totalElements
                    if(self.fundManageData.length < self.pageArray.size){
                        if(self.fundManageData.length === 0){
                            self.pageArray.fisrtPage = 0
                            self.pageArray.lastPage = 0
                        }else{
                            self.pageArray.fisrtPage = self.pageArray.total - self.fundManageData.length + 1
                            self.pageArray.lastPage = self.pageArray.total
                        }
                    }else if(self.fundManageData.length === self.pageArray.size){
                        self.pageArray.fisrtPage = self.pageArray.current * self.fundManageData.length - (self.pageArray.size - 1)
                        self.pageArray.lastPage = self.pageArray.current * self.fundManageData.length
                    }
                    self.fundManageData.forEach((value,index,array) => {
                        let accountingDate = new Date(array[index].accountingDate)
                        array[index].accountingDate = accountingDate.Format('yyyy-MM-dd')
                        array[index].amount = (array[index].amount / 100).toFixed(2)
                        if(array[index].type === 'expense'){
                            array[index].customerName = res.data.data.fileList[index].operator
                        }else{
                            array[index].customerName = res.data.data.fileList[index].customerName
                        }
                        switch (array[index].type) {
                            case 'deposit':
                                array[index].type = '押金'
                                break
                            case 'income':
                                array[index].type = '收入'
                                break
                            case 'expense':
                                array[index].type = '支出'
                                break
                            case 'rental':
                                array[index].type = '租金'
                                break
                        }
                    })
                })
                .catch(function (error) {
                    console.log(error)
                })
        },
        getID(row){
            if(sessionStorage){
                sessionStorage.setItem('expenseId',row.id)
            }
                window.location.href = '/shop/expenseProcessForm/' + row.id
        },
        addExpends(){
          let self = this
            // 判断是否进行过审批配置
            axios.get('/api/v1/workflow/definitions')
                .then(function(res){
                    res.data.data.forEach(function(value,index,array){
                        if(array[index].id === '-1'){
                            self.addExpendsDia = true
                            return false
                        }else{
                            window.location.href = '/shop/expenseProcessForm/addExpend'
                        }
                    })
                })
                .catch(function(err){
                    console.log(err)
                })
        },
        gotoWorkflow(){
            window.location.href = '/workflow'
        },
        detail(index){
            let self = this
            if(self.fundManageData[index].formUrl.indexOf('/lease/rechargeOrder/') > -1){
                let formurl = self.fundManageData[index].formUrl.replace('/lease/rechargeOrder/','')
                sessionStorage.setItem('rechargeId',formurl)
            }
            if(self.fundManageData[index].formUrl.indexOf('/shop/expenseProcessForm/') > -1) {
                let formId = self.fundManageData[index].formUrl.replace('/shop/expenseProcessForm/', '')
                sessionStorage.setItem('expenseId',formId)
            }
            setTimeout(() => {
                window.location.href = self.fundManageData[index].formUrl
            },300)
        },
        fundFresh(){
            sessionStorage.setItem('fundType','')
            sessionStorage.setItem('fundStartDate','')
            sessionStorage.setItem('fundEndDate','')
            sessionStorage.setItem('fundSearch','')
            sessionStorage.setItem('fundSize',1)
            sessionStorage.setItem('appCurrent',1)
            sessionStorage.setItem('tabName','交易记录')
        },
        fundClick(row){
            sessionStorage.setItem('tabName',row.name)
        }
    }
})