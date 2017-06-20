/**
 * Created by lyc on 2017/5/3.
 */
// function delFile(index){
//     // $(".filePath").each(function(){
//     //     let filePath = $(".filePath").index(this)
//     //     $(".filePath").eq(filePath).remove()
//     // })
//     vm.file.splice(index,1)
//     console.log(vm.file)
// }
let vm = new Vue({
    el: '#addExpend',
    data () {
        let checkCash = (rule,value,callback) => {
            if(!value){
                return callback(new Error('请输入支出金额'))
            }
            if(!/^\d{0,8}(\.\d{0,2})?$/.test(value)){
                callback(new Error('请输入金额'))
            }else{
                callback()
            }
            callback()
        }
        let chaeckReason = (rule,value,callback) => {
            if(!value){
                return callback(new Error('请输入支出事由'))
            }
            callback()
        }
        return {
            keep: true,
            actorName: '',
            drag: true,
            disabled: false,
            disabled1: false,
            successIcon: true,
            // delIcon: false,
            uploads: {
                img: false,
                audio: false,
                video: false,
            },
            file: [],
            accessory: false,
            restaurants: [],
            names: [],
            disable: false,
            newExpenseForm: {
                expenseType: '',
                expenseReason: '',
                totalAmount: '',
                expenseMemos: '',
                actorName: ''
            },
            rules: {
                expenseType: [
                    {
                        required: true,message: '请选择支出类型',trigger: 'change'
                    }
                ],
                expenseReason: [
                    {
                        required: true,validator: chaeckReason,trigger: 'blur'
                    }
                ],
                totalAmount: [
                    {
                        required: true,validator: checkCash,trigger: 'blur'
                    }
                ],
                actorName: [
                    {
                        required: true,message: '请选择下一步审批人',trigger: 'change'
                    }
                ]
            },
            saveApprovalData: {},
            expend: [],
            expendPro: [
                {
                    label: '提现'
                },
                {
                    label: '购买配件'
                },
                {
                    label: '购买设备'
                },
                {
                    label: '其它'
                }
            ]
        }
    },
    created() {
      this.fetchData()
    },
    mounted(){
        this.$nextTick(() => {
            this.upload()
        })
    },
    methods: {
        // 新增资金支出表单初始化
        fetchData() {
            let self = this
            axios.get('/api/v1/funds/expenseform/loadBlank')
                .then(function(res){
                    // self.newExpenseForm = res.data.data
                    // self.newExpenseForm.actorName = res.data.data.processStateContext.nextActorName
                    if(res.data.data.processStateContext.nextActorId === null || res.data.data.processStateContext.nextActorId === ''){
                        self.newExpenseForm.actorName = ''
                    }else{
                        self.newExpenseForm.actorName = res.data.data.processStateContext.nextActorId
                    }
                    if(res.data.data.processStateContext.todoer === false && res.data.data.processStateContext.nextActorId !== null){
                        self.disabled = true
                    }else{
                        self.disabled = false
                    }
                })
                .catch(function(err){
                    console.log(err)
                })
            axios.get('/api/v1/shop/employees')
                .then(function(res){
                    // console.log(res.data.data)
                    // res.data.data.forEach((value,index,array) => {
                    //     self.expend[index].name = array[index].userDto.name
                    // })
                    self.expend = res.data.data
                })
                .catch(function(err){
                    console.log(err)
                })
        },
        // 提交新增资金支出
        saveApproval(formName){
            let self = this
            let nextActorId = sessionStorage.getItem('nextActorId')
            self.saveApprovalData.expenseType = self.newExpenseForm.expenseType
            self.saveApprovalData.expenseReason = self.newExpenseForm.expenseReason
            self.saveApprovalData.expenseMemos = self.newExpenseForm.expenseMemos
            self.saveApprovalData.totalAmount = self.newExpenseForm.totalAmount * 100
            self.$refs[formName].validate(function(valid){
                if(valid){
                    axios({
                        method: 'post',
                        url: '/api/v1/funds/expenseform/submit',
                        data: self.saveApprovalData,
                        params: {
                            nextActorId: nextActorId
                        }
                    })
                        .then(function(res){
                            self.$message({
                                message: '提交成功',
                                type: 'success'
                            })
                            localStorage.setItem('file',JSON.stringify(self.file))
                            self.disable = true
                            setTimeout(() => {
                                window.location.href = '/shop/expenseProcessForm'
                        },3000)

                        })
                        .catch(function(err){
                            console.log(err)
                        })
                }
            })
        },
        // 附件上传调用方法
        upload (){
            let self = this
            new Uploader({
                id: "btnImageUploader",
                mediaType: 9,
                prefix:  "dist",
                onComplete: function (resp) {
                    let uploadImg = document.getElementById('uploadImg')
                    if(this.options.mediaType === 9){
                        let gen = resp.files[0]
                        self.$message({
                            type: 'success',
                            message: '上传成功'
                        })
                        // uploadImg.innerHTML +=  ''
                        self.file.push(gen)
                        // console.log(self.file)
                    }
                }
            })
        },
        getNextActorId(){
            let self = this
            if(sessionStorage){
                sessionStorage.setItem('nextActorId',self.newExpenseForm.actorName)
            }
        },
        delFile(index){
            let self = this
            self.file.splice(index,1)
        }
    }
})