/**
 * Created by lyc on 2017/6/2.
 */
const vm = new Vue({
    el: '#IotDeviceLog',
    data () {
        return {
            cloak: true,
            refreshLoading: false,
            IotDeviceLogDetail: []
        }
    },
    created(){
      this.fetch()
    },
    methods: {
        fetch(){
            const self = this
            axios.get('/api/v1/admin/iot/devices/log')
                .then((res) => {
                    self.IotDeviceLogDetail = res.data.data
                    self.IotDeviceLogDetail.forEach((value,index,array) => {
                        array[index].loggedAt = new Date(array[index].loggedAt).Format('yyyy-MM-dd hh:mm:ss')
                        switch (array[index].type){
                            case 'inbound':
                                array[index].type = 'I'
                                break
                            case 'outbound':
                                array[index].type = 'O'
                                break
                        }
                    })
                })
                .catch((err) => {
                    console.log(err)
                })
        },
        refresh(){
            // window.location.reload()
            this.refreshLoading = true
            setTimeout(() => {
                this.refreshLoading = false
                this.fetch()
            },1000)
        }
    }
})