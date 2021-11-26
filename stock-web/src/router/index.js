import { createRouter, createWebHashHistory } from "vue-router";

const routes = [{
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
    }]
}]

const router = createRouter({
    history: createWebHashHistory("stock"),
    routes: routes
})

// router.beforeEach((to, from, next) => {
//     if (to.path == '/unauthorized' || to.path == '/login') {
//         next()
//     } else {
//         const token = localStorage.getItem("rental-token")
//         if (token == null) {
//             next('/login')
//         } else {
//             next()
//         }
//         document.title = to.meta.title || '出租屋管理系统'
//     }
// })

export default router