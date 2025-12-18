/* eslint-disable */
declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}


// src/types.d.ts
declare module 'amoayun/monaco-editor-vue3' {
  import { defineComponent } from 'vue'
  const MonacoEditor: ReturnType<typeof defineComponent>
  export default MonacoEditor
}