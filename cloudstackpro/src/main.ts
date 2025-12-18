import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import type { Environment } from 'monaco-editor'

declare global {
    interface Window {
        MonacoEnvironment?: Environment
    }
}

// Monaco Editor 配置
(window as any).MonacoEnvironment = {
    getWorkerUrl: function (moduleId: string, label: string) {
        if (label === 'json') {
            return './json.worker.js'
        }
        if (label === 'css' || label === 'scss' || label === 'less') {
            return './css.worker.js'
        }
        if (label === 'html' || label === 'handlebars' || label === 'razor') {
            return './html.worker.js'
        }
        if (label === 'typescript' || label === 'javascript') {
            return './ts.worker.js'
        }
        return './editor.worker.js'
    }
}

// 导入博客系统样式
import './assets/blog/styles/main.css'

createApp(App).use(store).use(router).mount('#app')
