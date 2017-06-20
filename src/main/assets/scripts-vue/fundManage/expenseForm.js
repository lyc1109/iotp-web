/**
 * Created by lyc on 2017/5/3.
 */
// 深拷贝
function clone(oldObj,newObj){
    let obj = newObj || {}
    for(let i in oldObj){
        let prop = oldObj[i]
        if(prop === obj){
            continue
        }
        if(typeof prop === 'object'){
            obj[i] =(prop.constructor === Array) ? [] : {}
            arguments.callee(prop,obj[i])
        }else{
            obj[i] = prop
        }
    }
    return obj
}
// json去重
// Array.prototype.removeRepeatAttr=function(){
//     let tmp={},b=[],a=this
//     for(let i=0;i<a.length;i++){
//         if(!tmp[a[i].name]){
//             b.push(a[i].name)
//             tmp[a[i].name]=!0
//         }
//     }
//     return b
// }
let vm = new Vue({
    el: '#expenseForm',
    data () {
        return {
            expenseTitle: '',
            filterable: true,
            agree: '',
            disagree: '',
            saveDialog: false,
            selectArr: [],
            newProcessArr: [],
            listProcessArr: [],
            trueLabels: [],
            falseLabels: [],
            trueModel: [],
            falseModel: [],
            newLists: [],
            newList: {
                key: '',
                actors: '',
                functionIds: ''
            },
            exData: {
                step: '',
                transactor: '',
                operation: '',
                del: ''
            },
            transactorOption: [
                {
                    userDto: {
                        name: '不选择审批人',
                        id: ''
                    }
                }
            ],
            expenseFormData: [],
            expenseFormData1: [
                {
                    step: '',
                    transactor: '',
                    operation: '',
                    del: ''
                }
            ]
        }
    },
    created(){
        let id = sessionStorage.getItem('id')
        if(id === '-1' || id === '') {
            this.newFetchData()
        }else{
            this.listFetchData()
        }
    },
    mounted(){
        let id = sessionStorage.getItem('id')
        if(id === '-1' || id === ''){
            this.newData()
        }else{
            this.listData()
        }
    },
    methods: {
        // 步骤初始化
        listFetchData(){
            let self = this
            let id = sessionStorage.getItem('id')
            axios.get('/api/v1/workflow/definitions/' + id,{
                params: {
                    entityId: id
                }
            })
                .then(function(res){
                        for(let i in res.data.data.activityList){
                            if(isNaN(parseInt(i))){
                                return false
                            }else {
                                self.expenseFormData.push(self.exData)
                            }
                        }
                })
                .catch(function(err){
                    console.log(err)
                })
        },
        newFetchData(){
            let self = this
            self.expenseFormData = self.expenseFormData1
            for(let i in self.expenseFormData){
                if(isNaN(parseInt(i))){
                    return false
                }else{
                    // 渲染步骤
                    self.selectArr.push({ name: '' })
                    self.trueLabels.push({ trueLabel: '' })
                    self.falseLabels.push({ falseLabel: '' })
                    self.trueModel.push({ model: false })
                    self.falseModel.push({ model: false })
                }
            }

        },
        // 新建审批流程表
        newData(){
            let self = this
            let key = sessionStorage.getItem('key')
            axios.get('/api/v1/workflow/definitions/loadBlank',{
                params: {
                    pdTplKey: key
                }
            })
                .then(function(res){
                    self.newProcessArr = res.data.data
                    self.expenseTitle = res.data.data.name
                    self.disagree = res.data.data.processDefinitionTpl.functions[0].name
                    self.agree = res.data.data.processDefinitionTpl.functions[1].name
                    for(let i in self.expenseFormData){
                        if(isNaN(parseInt(i))){
                            return false
                        }else{
                            self.newProcessArr.activityList = self.newLists
                            self.newProcessArr.activityList.push(self.newList)
                        }
                    }

                })
                .catch(function(err){
                    console.log(err)
                })
            // 获取店铺员工信息
            axios.get('/api/v1/shop/employees')
                .then(function(res){
                    res.data.data.forEach((value,index,array) => {
                        self.transactorOption.push(array[index])
                    })
                })
                .catch(function(err){
                    console.log(err)
                })
        },
        // 审批流程的详细数据
        listData(){
            let self = this
            let id = sessionStorage.getItem('id')
            axios.get('/api/v1/workflow/definitions/' + id,{
                params: {
                    entityId: id
                }
            })
                .then(function(res){
                    self.listProcessArr = res.data.data
                    self.expenseTitle = res.data.data.name
                    self.disagree = res.data.data.processDefinitionTpl.functions[0].name
                    self.agree = res.data.data.processDefinitionTpl.functions[1].name
                    // 渲染步骤
                    self.expenseFormData.forEach((value,index,array) => {
                            self.selectArr.push({ name: self.listProcessArr.activityList[index].actors })
                            if(self.listProcessArr.activityList[index].functionIds.indexOf('approval') > -1){
                                self.trueModel.push({ model: true })
                                self.trueLabels.push({ trueLabel: 'approval' })
                            }else{
                                self.trueModel.push({ model: false })
                                self.trueLabels.push({ trueLabel: '' })
                            }
                            if(self.listProcessArr.activityList[index].functionIds.indexOf('reject') > -1){
                                self.falseModel.push({ model: true })
                                self.falseLabels.push({ falseLabel: 'reject' })
                            }else{
                                self.falseModel.push({ model: false })
                                self.falseLabels.push({ falseLabel: '' })
                            }
                })
                })
                .catch(function(err){
                    console.log(err)
                })
            // 获取店铺员工信息
            axios.get('/api/v1/shop/employees')
                .then(function(res){
                    // self.transactorOption = res.data.data
                    res.data.data.forEach((value,index,array) => {
                        self.transactorOption.push(array[index])
                    })
                })
                .catch(function(err){
                    console.log(err)
                })
        },
        // 提交保存审批表
        saveExpense(){
            let self = this
            let id = sessionStorage.getItem('id')
            axios({
                method: 'post',
                url: '/api/v1/workflow/definitions/' + id,
                data: id === '-1' ? self.newProcessArr : self.listProcessArr,
                params: {
                    entityId: id
                }
            })
                .then(function(res){
                    self.$message({
                        message: '提交成功',
                        type: 'success'
                    })
                    setTimeout(() => {
                        window.location.href = '/workflow/'
                    },3000)
                })
                .catch(function(err){
                    console.log(err)
                })
        },
        // 添加步骤
        addStep() {
            let self = this
            let expenseFormData = self.expenseFormData[0]
            let newObj = {}
            clone(expenseFormData,newObj)
            self.expenseFormData.push(newObj)
            self.selectArr.push({ name: '' })
            self.trueLabels.push({ trueLabel: '' })
            self.falseLabels.push({ falseLabel: '' })
            self.trueModel.push({ model: false })
            self.falseModel.push({ model: false })
            if(self.newProcessArr !== {} && self.listProcessArr.length === 0){
                self.newProcessArr.activityList = self.newLists
                self.newProcessArr.activityList.push(self.newList)
            }
            if(self.listProcessArr !== {} && self.newProcessArr.length === 0){
                self.listProcessArr.activityList.push(self.newList)
            }
        },
        // 删除步骤
        deleteStep(index) {
            let self = this
            if(self.expenseFormData.length <= 1){
                self.saveDialog = true
                return false
            }else{
                self.expenseFormData[index] = ''
                self.expenseFormData.splice(index,1)
                self.selectArr.splice(index,1)
                self.trueLabels.splice(index,1)
                self.falseLabels.splice(index,1)
                self.trueModel.splice(index,1)
                self.falseModel.splice(index,1)

                if(self.listProcessArr !== {} && self.newProcessArr.length === 0){
                    self.listProcessArr.activityList.splice(index,1)
                }
                if(self.newProcessArr !== {} && self.listProcessArr.length === 0){
                    self.newProcessArr.activityList.splice(index,1)
                }
            }
        },
        // 同意按钮
        trueChange(index){
            let self = this
            let trueModel = self.trueModel
            let trueLabels = self.trueLabels
            let newProcessArr = self.newProcessArr
            let listProcessArr = self.listProcessArr
            if(trueModel[index].model === true){
                trueLabels[index].trueLabel = 'approval'
                if(listProcessArr !== {} && newProcessArr.length === 0){
                    if(listProcessArr.activityList[index].functionIds !== '' && listProcessArr.activityList[index].functionIds.indexOf(trueLabels[index].trueLabel) === -1){
                        listProcessArr.activityList[index].functionIds = listProcessArr.activityList[index].functionIds + ',' + trueLabels[index].trueLabel
                    }else if(listProcessArr.activityList[index].functionIds !== '' && listProcessArr.activityList[index].functionIds.indexOf(trueLabels[index].trueLabel) > -1){
                        return false
                    }else{
                        listProcessArr.activityList[index].functionIds = trueLabels[index].trueLabel
                    }
                }
                if(newProcessArr !== {} && listProcessArr.length === 0){
                    self.newProcessArr.activityList = self.newLists
                    if(newProcessArr.activityList[index].functionIds !== '' && newProcessArr.activityList[index].functionIds.indexOf(trueLabels[index].trueLabel) === -1){
                        newProcessArr.activityList[index].functionIds = newProcessArr.activityList[index].functionIds + ',' + trueLabels[index].trueLabel
                    }else if(newProcessArr.activityList[index].functionIds !== '' && newProcessArr.activityList[index].functionIds.indexOf(trueLabels[index].trueLabel) > -1){
                        return false
                    }else{
                        newProcessArr.activityList[index].functionIds = trueLabels[index].trueLabel
                    }
                }
            }else{
                if(newProcessArr !== {} && listProcessArr.length === 0){
                    self.newProcessArr.activityList = self.newLists
                    newProcessArr.activityList[index].functionIds = newProcessArr.activityList[index].functionIds.replace(trueLabels[index].trueLabel,'').replace(',','')
                }
                if(listProcessArr !== {} && newProcessArr.length === 0){
                    listProcessArr.activityList[index].functionIds = listProcessArr.activityList[index].functionIds.replace(trueLabels[index].trueLabel,'').replace(',','')
                }
            }
        },
        // 驳回按钮
        falseChange(index){
            let self = this
            let falseModel = self.falseModel
            let falseLabels = self.falseLabels
            let newProcessArr = self.newProcessArr
            let listProcessArr = self.listProcessArr
            if(falseModel[index].model === true){
                falseLabels[index].falseLabel = 'reject'
                if(listProcessArr !== {} && newProcessArr.length === 0){
                    if(listProcessArr.activityList[index].functionIds !== '' && listProcessArr.activityList[index].functionIds.indexOf(falseLabels[index].falseLabel) === -1){
                        listProcessArr.activityList[index].functionIds = self.listProcessArr.activityList[index].functionIds + ',' + falseLabels[index].falseLabel
                    }else if(listProcessArr.activityList[index].functionIds !== '' && listProcessArr.activityList[index].functionIds.indexOf(falseLabels[index].falseLabel) > -1){
                        return false
                    }else{
                        self.listProcessArr.activityList[index].functionIds = self.falseLabels[index].falseLabel
                    }
                }
                if(newProcessArr !== {} && listProcessArr.length === 0){
                    self.newProcessArr.activityList = self.newLists
                    if(newProcessArr.activityList[index].functionIds !== '' && newProcessArr.activityList[index].functionIds.indexOf(falseLabels[index].falseLabel) === -1){
                        newProcessArr.activityList[index].functionIds = newProcessArr.activityList[index].functionIds + ',' + falseLabels[index].trueLabel
                    }else if(newProcessArr.activityList[index].functionIds !== '' && newProcessArr.activityList[index].functionIds.indexOf(falseLabels[index].falseLabel) > -1){
                        return false
                    }else{
                        newProcessArr.activityList[index].functionIds = falseLabels[index].falseLabel
                    }
                }
            }else{
                if(newProcessArr !== {} && listProcessArr.length === 0){
                    self.newProcessArr.activityList = self.newLists
                    newProcessArr.activityList[index].functionIds = newProcessArr.activityList[index].functionIds.replace(falseLabels[index].falseLabel,'').replace(',','')
                }
                if(listProcessArr !== {} && newProcessArr.length === 0){
                    listProcessArr.activityList[index].functionIds = listProcessArr.activityList[index].functionIds.replace(falseLabels[index].falseLabel,'').replace(',','')
                }
            }
        },
        // 办理人选中赋值
        selectActor(index){
            let self = this
            if(self.listProcessArr !== {} && self.newProcessArr.length === 0){
                self.listProcessArr.activityList[index].actors = self.selectArr[index].name
            }
            if(self.newProcessArr !== {} && self.listProcessArr.length === 0){
                self.newProcessArr.activityList.push(self.newList)
                self.newProcessArr.activityList[index].actors = self.selectArr[index].name
            }
        }
    }
})