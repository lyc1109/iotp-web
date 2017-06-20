/**
 * Created by lyc on 2017/5/3.
 */
let vm = new Vue({
    el: '#workflow',
    data () {
        return {
            workflowBox: [
                {
                    name: '',
                    time: '',
                    iconfont: 'iconfont',
                    icon_expense: '',
                    imgBgcolor: '',
                    times: true
                }
            ]
        }
    },
    created () {
        this.fetchData()
    },
    methods: {
        fetchData() {
            let self = this
            axios.get('/api/v1/workflow/definitions')
                .then(function(res){
                    res.data.data.forEach(function(value,index,array){
                        self.workflowBox[index].name = array[index].name
                        let modified = new Date(array[index].modified)
                        self.workflowBox[index].time = modified.Format('yyyy-MM-dd hh:mm')
                        self.workflowBox[index].icon_expense = array[index].processDefinitionTpl.bgIcon
                        self.workflowBox[index].imgBgcolor = array[index].processDefinitionTpl.bgColor
                        if(array[index].id === '-1' || array[index].id === '' || array[index].id === null){
                            self.workflowBox[index].times = false
                        }else{
                            self.workflowBox[index].times = true
                        }
                    })
                })
                .catch(function(err){
                    console.log(err)
                })
        },
        getKey(index){
            axios.get('/api/v1/workflow/definitions')
                .then(function(res){
                    if(sessionStorage){
                        sessionStorage.setItem('key',res.data.data[index].processDefinitionTpl.key)
                        sessionStorage.setItem('id',res.data.data[index].id)
                    }
                })
                .catch(function(err){
                    console.log(err)
                })
            setTimeout(() => {
                window.location.href = '/workflow/expenseForm'
            },500)
        }
    }
})