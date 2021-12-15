import { createRouter, createWebHashHistory } from "vue-router";

const routes = [
    {
        path:'/login',
        component: () => import('../components/page/login.vue'),
        meta: {
            title: '登录',
        }
    },
    {
    path: '/',
    component: () =>
        import ('../components/layout/layout.vue'),
    children: [{
        path: "/",
        component: () =>
            import ('../components/page/index.vue')
    }, {
        path: "/user",
        component: () =>
            import ('../components/page/user.vue')
    },{
        path: '/kline',
        component: () =>import ('../components/page/kline.vue')
    }]
}]

const router = createRouter({
    history: createWebHashHistory("stock"),
    routes: routes
})

router.beforeEach((to, from, next) => {
    if (to.path == '/unauthorized' || to.path == '/login') {
        next()
    } else {
        const token = localStorage.getItem("stock-token")
        if (token == null) {
            next('/login')
        } else {
            next()
        }
        document.title = to.meta.title || 'Stock'
    }
})

export default router