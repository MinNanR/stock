import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import locale from 'element-plus/lib/locale/lang/zh-cn'
import router from './router'
import request from './utils/request'
import App from './App.vue'
import 'element-plus/theme-chalk/index.css'

const app = createApp(App)

app.use(ElementPlus, { locale })
app.use(router)
app.config.globalProperties.request = request
app.mount("#app")


export default app