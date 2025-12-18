import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import BlogHome from '../views/BlogHome.vue'
import BlogPost from '../views/BlogPost.vue'
import BlogWrite from '../views/BlogWrite.vue'
import BlogCategory from '../views/BlogCategory.vue'
import BlogSearch from '../views/BlogSearch.vue'
import BlogProfile from '../views/BlogProfile.vue'

const routes: Array<RouteRecordRaw> = [
    {
        path: '/',
        name: 'Home',
        component: BlogHome
    },
    {
        path: '/post/:id',
        name: 'Post',
        component: BlogPost
    },
    {
        path: '/write',
        name: 'Write',
        component: BlogWrite
    },
    {
        path: '/category/:name',
        name: 'Category',
        component: BlogCategory
    },
    {
        path: '/search',
        name: 'Search',
        component: BlogSearch
    },
    {
        path: '/profile',
        name: 'Profile',
        component: BlogProfile
    }
]

const router = createRouter({
    history: createWebHistory(process.env.BASE_URL),
    routes
})

export default router
