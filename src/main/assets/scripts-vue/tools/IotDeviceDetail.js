/**
 * Created by lyc on 2017/6/2.
 */
const vm = new Vue({
    el: '#IotDeviceDetail',
    data () {
        return {
            cloak: true,
            back: false,
            searchInput: '',
            onoff: false,
            searchResult: '',
            searchResults: [],
            searchDetailDia: false,
            IotDeviceDetailSearch: true,
            IotDeviceDetail: false,
            IotDeviceDetails: [
                {
                    key: '测试模式',
                    value: ''
                },
                {
                    key: '是否已删除',
                    value: ''
                },
                {
                    key: 'Mac地址',
                    value: ''
                },
                {
                    key: '设备编号',
                    value: ''
                },
                {
                    key: '远程地址',
                    value: ''
                },
                {
                    key: '是否已锁定',
                    value: ''
                },
                {
                    key: '是否在线',
                    value: ''
                },
                {
                    key: '设备异常',
                    value: ''
                },
                {
                    key: '激活日期',
                    value: ''
                },
                {
                    key: '租赁到期日',
                    value: ''
                },
                {
                    key: '首次到达时间',
                    value: ''
                },
                {
                    key: '最后到达时间',
                    value: ''
                },
                {
                    key: '开关状态',
                    value: ''
                },
                {
                    key: '水位状态',
                    value: ''
                },
                {
                    key: '水泵状态',
                    value: ''
                },
                {
                    key: '冲洗状态',
                    value: ''
                },
                {
                    key: '出水阀状态',
                    value: ''
                },
                {
                    key: '进水阀状态',
                    value: ''
                },
                {
                    key: '高压开关状态',
                    value: ''
                },
                {
                    key: '低压开关状态',
                    value: ''
                },
                {
                    key: '水温值',
                    value: ''
                },
                {
                    key: '原水TDS',
                    value: ''
                },
                {
                    key: '总处理水量',
                    value: ''
                },
                {
                    key: '出水TDS值',
                    value: ''
                },
                {
                    key: '总出水量',
                    value: ''
                },
                {
                    key: '上一阶段总出水量(ml)',
                    value: ''
                }
            ],
            IotDeviceParts: {
                num: '配件编号',
                created: '安装日期',
                total: '总处理水量(ml)',
                percent: '可用百分比'
            },
            IotDevicePart: []
        }
    },
    // created(){
    //   this.getSearchDetail()
    // },
    methods: {
        getResult(value){
            const self = this
            if(value.length >= 3){
                axios.get('/api/v1/admin/iot/devices',{
                    params: {
                        searchPhrase: value,
                        online: self.onoff
                    }
                })
                    .then((res) => {
                        self.searchResults = res.data.data
                        // console.log(self.searchResults)
                    })
                    .catch((err) => {
                        console.log(err)
                    })
            }
        },
        getSearchResult(val){
            const self = this
            val = val.replace(/\(.*?\)/,'')
            if(sessionStorage){
                sessionStorage.setItem('deviceMac',val)
            }
            let deviceMac = sessionStorage.getItem('deviceMac')
            if(self.searchResult === ''){
                self.searchDetailDia = true
            }else{
                // setTimeout(() => {
                    self.IotDeviceDetailSearch = false
                    self.IotDeviceDetail = true
                    self.back = true
                // },1000)
                axios.get('/api/v1/admin/iot/devices/' + deviceMac)
                    .then((res) => {
                        self.IotDeviceDetails[2].value = res.data.data.deviceMac
                        self.IotDeviceDetails[3].value = parseInt(res.data.data.deviceId,16)
                        self.IotDeviceDetails[20].value = res.data.data.waterTemperature
                        self.IotDeviceDetails[21].value = res.data.data.inWaterTds
                        self.IotDeviceDetails[22].value = res.data.data.inWaterTotalFlow
                        self.IotDeviceDetails[23].value = res.data.data.outWaterTds
                        self.IotDeviceDetails[24].value = res.data.data.outWaterTotalFlow
                        self.IotDeviceDetails[25].value = res.data.data.outWaterPreFlow
                        self.IotDevicePart = res.data.data.parts
                        if(res.data.data.testMode === false){
                            self.IotDeviceDetails[0].value = '否'
                        }else{
                            self.IotDeviceDetails[0].value = '是'
                        }
                        if(res.data.data.deleted === false){
                            self.IotDeviceDetails[1].value = '否'
                        }else{
                            self.IotDeviceDetails[1].value = '是'
                        }
                        if(res.data.data.remoteIp === null || res.data.data.remotePort === null){
                            self.IotDeviceDetails[4].value = '无'
                        }else{
                            self.IotDeviceDetails[4].value = res.data.data.remoteIp + ':' + res.data.data.remotePort
                        }
                        if(res.data.data.locked === false){
                            self.IotDeviceDetails[5].value = '否'
                        }else{
                            self.IotDeviceDetails[5].value = '是'
                        }
                        if(res.data.data.online === false){
                            self.IotDeviceDetails[6].value = '否'
                        }else{
                            self.IotDeviceDetails[6].value = '是'
                        }
                        switch (res.data.data.exceptionCode){
                            case 0:
                                self.IotDeviceDetails[7].value = '0:无异常'
                                break
                            case 1:
                                self.IotDeviceDetails[7].value = '1:无水'
                                break
                            case 2:
                                self.IotDeviceDetails[7].value = '2:漏水'
                                break
                            case 3:
                                self.IotDeviceDetails[7].value = '3:传感器异常'
                                break
                        }
                        if(res.data.data.activedAt === 0 || res.data.data.activedAt === '' || res.data.data.activedAt === null){
                            self.IotDeviceDetails[8].value = '(未知)'
                        }else{
                            self.IotDeviceDetails[8].value = res.data.data.activedAt
                        }
                        if(res.data.data.leaseDueDate === 0 || res.data.data.leaseDueDate === '' || res.data.data.leaseDueDate === null){
                            self.IotDeviceDetails[9].value = '(未知)'
                        }else{
                            self.IotDeviceDetails[9].value = new Date(res.data.data.leaseDueDate).Format('yyyy-MM-dd')
                        }
                        if(res.data.data.firstTouchTime === 0 || res.data.data.firstTouchTime === '' || res.data.data.firstTouchTime === null){
                            self.IotDeviceDetails[10].value = '(未知)'
                        }else{
                            self.IotDeviceDetails[10].value = new Date(res.data.data.firstTouchTime).Format('yyyy-MM-dd hh:mm:ss')
                        }
                        if(res.data.data.lastTouchTime === 0 || res.data.data.lastTouchTime === '' || res.data.data.lastTouchTime === null){
                            self.IotDeviceDetails[11].value = '(未知)'
                        }else{
                            self.IotDeviceDetails[11].value = new Date(res.data.data.lastTouchTime).Format('yyyy-MM-dd hh:mm:ss')
                        }
                        if(res.data.data.powerOn === false){
                            self.IotDeviceDetails[12].value = '关机'
                        }else{
                            self.IotDeviceDetails[12].value = '开机'
                        }
                        switch (res.data.data.waterLevel){
                            case 0:
                                self.IotDeviceDetails[13].value = '水满'
                                break
                            case 1:
                                self.IotDeviceDetails[13].value = '无水'
                                break
                        }
                        switch (res.data.data.waterPump){
                            case 0:
                                self.IotDeviceDetails[14].value = '停止'
                                break
                            case 1:
                                self.IotDeviceDetails[14].value = '运行中'
                                break
                        }
                        switch (res.data.data.waterFlush){
                            case 0:
                                self.IotDeviceDetails[15].value = '停止'
                                break
                            case 1:
                                self.IotDeviceDetails[15].value = '冲洗中'
                                break
                        }
                        switch (res.data.data.outValve){
                            case 0:
                                self.IotDeviceDetails[16].value = '停止'
                                break
                            case 1:
                                self.IotDeviceDetails[16].value = '运行中'
                                break
                        }
                        switch (res.data.data.inValve){
                            case 0:
                                self.IotDeviceDetails[17].value = '停止'
                                break
                            case 1:
                                self.IotDeviceDetails[17].value = '运行中'
                                break
                        }
                        switch (res.data.data.highVoltageSwitch){
                            case 0:
                                self.IotDeviceDetails[18].value = '关闭'
                                break
                            case 1:
                                self.IotDeviceDetails[18].value = '开启'
                                break
                        }
                        switch (res.data.data.lowVoltageSwitch){
                            case 0:
                                self.IotDeviceDetails[19].value = '关闭'
                                break
                            case 1:
                                self.IotDeviceDetails[19].value = '开启'
                                break
                        }
                        self.IotDevicePart.forEach((value,index,array) => {
                            array[index].installedAt = new Date(array[index].installedAt).Format('yyyy-MM-dd')
                            array[index].availablePercentage = array[index].availablePercentage + '%'
                        })
                    })
                    .catch((err) => {
                        console.log(err)
                    })
            }
        },
        getOnoff(){
            const self = this
            console.log(self.searchInput === '')
            if(self.searchInput !== ''){
                axios.get('/api/v1/admin/iot/devices',{
                    params: {
                        searchPhrase: self.searchInput,
                        online: self.onoff
                    }
                })
                    .then((res) => {
                        self.searchResults = res.data.data
                    })
                    .catch((err) => {
                        console.log(err)
                    })
            }else{
                return false
            }
        },
        goback(){
            const self = this
            self.IotDeviceDetailSearch = true
            self.IotDeviceDetail = false
            self.back = false
            self.searchInput = ''
            self.searchResults = ''
            self.onoff = false
            self.searchResult = ''
            // window.location.reload()
        }
    }
})
