const { defineConfig } = require('@vue/cli-service')
const MonacoWebpackPlugin = require('monaco-editor-webpack-plugin')

module.exports = defineConfig({
  transpileDependencies: ['monaco-editor'],
  configureWebpack: {
    plugins: [
      new MonacoWebpackPlugin({
        languages: ['javascript', 'typescript', 'json', 'html', 'css'],
        filename: '[name].worker.js' // Worker 文件命名规则
      })
    ]
  },
  chainWebpack: config => {
    config.module
      .rule('js')
      .include
      .add(/node_modules[\\/]monaco-editor/)
      .end()
  }
})
