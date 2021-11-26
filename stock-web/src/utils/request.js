import axios from 'axios'
import config from './config.js'
import app from '../main.js'
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
                    localStorage.setItem("rental-token", `Bearer ${response.headers.newtoken}`);
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
            if (error.response.status != null) {
                if (error.response.status === 401) {
                    localStorage.removeItem("rental-token")
                    alert("登录信息过期")
                    app.router.push("/login")
                } else if (error.response.status === 403) {
                    localStorage.removeItem("rental-token")
                    alert("无权限")
                    app.router.push('/login')
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