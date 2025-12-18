declare module '*.vue' {
    import type { DefineComponent } from 'vue'
    const component: DefineComponent<{}, {}, any>
    export default component
}

declare module '*.png' {
    const value: string
    export default value
}

declare module '*.svg' {
    const value: string
    export default value
}

declare module 'vue-router' {
    import { Router, RouteLocationNormalized, RouteRecordRaw, NavigationGuardNext } from 'vue-router'
    export function createRouter(options: any): Router
    export function createWebHistory(base?: string): any
    export function useRouter(): Router
    export function useRoute(): RouteLocationNormalized
    export type { RouteRecordRaw, Router, RouteLocationNormalized, NavigationGuardNext }
}
