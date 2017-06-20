/**
 * Created by lyc on 2017/4/21.
 */
new Vue({
    el: '#expendApproval',
    data () {
    return {
        approveToggle: false,
        rejectToggle: false,
        disabled: true,
        comment: '',
        disable: false,
        appDisable: false,
        rejectDisable: false,
        nextActorDisabled: false,
        uploadFile: false,
        uploaderFile: [],
        comments: true,
        nextActors: true,
        submit: true,
        approveDia: false,
        rejectDia: false,
        submitDia: false,
        avatar: '',
        remand: 300,
        readonly: true,
        expendList: {
            expenseType: '',
            expenseReason: '',
            totalAmount: '',
            creator: '',
            expenseMemos: '',
            comment: '',
            nextActor: '',
            processStateContext: {
                todoer: '',
                nextActorId: '',
                nextActorName: '',
                functionIds: ''
            }
        },
        adminInfo: [],
        expend: [],
        fileList2: [],
        rule: {
            comment: [
                {
                    required: true,message: '请输入审批说明',trigger: 'blur'
                }
            ],
            nextActor: [
                {
                    required: true,message: '请选择下一步审批人',trigger: 'change'
                }
            ]
        }
    }
},
    mounted(){
      this.$nextTick(() => {
          this.fetchData()
      })
    },
    methods: {
        fetchData(){
            let expenseId = sessionStorage.getItem('expenseId')
            let self = this
            self.uploaderFile = JSON.parse(localStorage.getItem('file'))
            // if(self.uploaderFile === null){
            //     self.uploadFile = false
            // }else{
            //     self.uploadFile = true
            // }
            if(self.submitDia === false){
                self.rule.nextActor[0].required = false
                // console.log(self.rule)
            }
            axios.get('/api/v1/user')
                .then(function(res){
                    if(res.data.data.avatar === null){
                        self.avatar = '/dist/images/logo/logo.png'
                    }else{
                        self.avatar = res.data.data.avatar
                    }
                })
                .catch(function(err){
                    console.log(err)
                })
            axios.get('/api/v1/funds/expenseform/' + expenseId,{
                params: {
                    entityId: expenseId
                }
            })
                .then(function(res){
                    self.expendList.expenseType = res.data.data.expenseType
                    self.expendList.expenseReason = res.data.data.expenseReason
                    self.expendList.totalAmount = (res.data.data.totalAmount / 100).toFixed(2)
                    self.expendList.expenseMemos = res.data.data.expenseMemos
                    self.expendList.creator = res.data.data.creator
                    self.expendList.processStateContext.todoer = res.data.data.processStateContext.todoer
                    self.expendList.processStateContext.functionIds = res.data.data.processStateContext.functionIds
                    self.adminInfo = res.data.data.formComments
                    res.data.data.formComments.forEach((value,index,array) => {
                        // self.adminInfo[index].created = jQuery.timeago(array[index].created)
                        self.adminInfo[index].created = new Date(array[index].created).Format('yyyy-MM-dd hh:mm')
                    })
                    if(res.data.data.processStateContext.nextActorId !== null){
                        self.expendList.nextActor = res.data.data.processStateContext.nextActorId
                        self.nextActorDisabled = true
                    }else{
                        self.expendList.nextActor = ''
                        self.nextActorDisabled = false
                    }
                    if(res.data.data.processStateContext.todoer === true){
                        if(res.data.data.processStateContext.endActivity === true){
                            self.nextActors = false
                            self.submit = false
                        }else{
                            self.nextActors = true
                            self.submit = true
                        }
                        self.comments = true
                        if(self.expendList.processStateContext.functionIds === null){
                            self.approveToggle = false
                            self.rejectToggle = false
                        }else{
                            if(self.expendList.processStateContext.functionIds.indexOf('reject') > -1){
                                self.rejectToggle = true
                            }
                            if(self.expendList.processStateContext.functionIds.indexOf('approval') > -1){
                                self.approveToggle = true
                            }
                        }
                    }else{
                        self.nextActors = false
                        self.comments = false
                        self.approveToggle = false
                        self.rejectToggle = false
                        self.submit = false
                    }
                })
                .catch(function(err){
                    console.log(err)
                })

            axios.get('/api/v1/shop/employees')
                .then(function(res){
                    self.expend = res.data.data
                })
                .catch(function(err){
                    console.log(err)
                })
        },
        saveExpenseList(formName){
            let self = this
            self.$refs[formName].validate(function(valid){
                if(valid){
                    self.submitDia = true
                }
            })
        },
        approve(formName){
            let self = this
            self.$refs[formName].validate(function(valid){
                if (valid) {
                    self.approveDia = true
                }
            })
        },
        reject(formName){
            let self = this

            self.$refs[formName].validate(function(valid){
                if(valid){
                    self.rejectDia = true
                }
            })
        },
        approveSure(){
            let self = this
            let expenseId = sessionStorage.getItem('expenseId')
            if(sessionStorage){
                sessionStorage.setItem('comment',self.expendList.comment)
            }
            let comment = sessionStorage.getItem('comment')
            axios({
                method: 'post',
                url: '/api/v1/funds/expenseform/' + expenseId + '/approve',
                params: {
                    comment: comment
                }
            })
                .then(function (res) {
                    self.$message({
                        message: '已批准',
                        type: 'success'
                    })
                    self.appDisable = true
                    self.disable = true
                    self.rejectDisable = true
                    setTimeout(() => {
                        window.location.href = '/shop/expenseProcessForm'
                },3000)
                    self.approveDia = false
                })
                .catch(function (err) {
                    console.log(err)
                })
        },
        rejectSure(){
            let self = this
            let expenseId = sessionStorage.getItem('expenseId')
            if(sessionStorage){
                sessionStorage.setItem('comment',self.expendList.comment)
            }
            let comment = sessionStorage.getItem('comment')
            axios({
                method: 'post',
                url: '/api/v1/funds/expenseform/' + expenseId + '/reject',
                params: {
                    comment: comment
                }
            })
                .then(function(res){
                    self.$message({
                        message: '已驳回',
                        type: 'success'
                    })
                    self.disable = true
                    self.appDisable = true
                    self.rejectDisable = true
                    setTimeout(() => {
                        window.location.href = '/shop/expenseProcessForm'
                },3000)
                    self.rejectDia = false
                })
                .catch(function(err){
                    console.log(err)
                })
        },
        submitSure(){
            let self = this
            let expenseId = sessionStorage.getItem('expenseId')
            if(sessionStorage){
                sessionStorage.setItem('nextActorIds',self.expendList.nextActor)
                sessionStorage.setItem('comment',self.expendList.comment)
            }
            let nextActorIds = sessionStorage.getItem('nextActorIds')
            let comment = sessionStorage.getItem('comment')
            axios({
                method: 'post',
                url: '/api/v1/funds/expenseform/' + expenseId + '/signal',
                params: {
                    // entityId: expenseId,
                    nextActorId: nextActorIds,
                    comment: comment
                }
            })
                .then(function(res){
                    self.$message({
                        message: '提交成功',
                        type: 'success'
                    })
                    self.disable = true
                    self.appDisable = true
                    self.rejectDisable = true
                    setTimeout(() => {
                        window.location.href = '/shop/expenseProcessForm'
                },3000)
                    self.submitDia = false
                })
                .catch(function(err){
                    console.log(err)
                })
        },
        maxLen(){
            let len = this.expendList.comment.length
            this.remand = 300 - len
            if(len > 300){
                this.remand = 0
                return false
            }
        }
    }
})