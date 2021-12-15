import axios from 'axios'
import config from './config.js'
import router from '../router/index.js'
console.log(config.baseUrl)

const request = axios.create({
    baseURL: config.baseUrl,
    timeout: 30000,
    headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    },
    // transformRequest: [
    //     (data) => {
    //         return JSON.stringify(data)
    //     }
    // ]
})

request.interceptors.request.use(
    config => {
        let token = localStorage.getItem('stock-token')
        config.headers.authorization = token
        return config;
    },
    error => {
        return Promise.reject(error)
    }
)

request.interceptors.response.use(
    response => {
        if (response.status === 200) {
            let data = response.data
            if (data.code === '000') {
                if (response.headers['newtoken'] != null) {
                    localStorage.setItem("stock-token", `Bearer ${response.headers.newtoken}`);
                }
                return { data: data.data, message: data.message }
            } else {
                return Promise.reject(data.message)
            }
        } else {
            return Promise.reject()
        }
    },
    error => {
        if (error.response != null) {
            console.log(error.response.status)
            if (error.response.status != null) {
                if (error.response.status === 401) {
                    localStorage.removeItem("stock-token")
                    alert("登录信息过期")
                    router.push("/login")
                } else if (error.response.status === 403) {
                    localStorage.removeItem("stock-token")
                    alert("无权限")
                    router.push('/login')
                }
                return Promise.reject(error)
            } else {
                return Promise.reject(error)
            }
        } else {
            return Promise.reject(error)
        }
    }
)

export default request