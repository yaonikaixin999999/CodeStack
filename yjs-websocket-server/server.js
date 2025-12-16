const http = require('http')
const WebSocket = require('ws')
const { setupWSConnection } = require('y-websocket/bin/utils')

const port = 1234

const server = http.createServer((req, res) => {
    res.writeHead(200, { 'Content-Type': 'text/plain' })
    res.end('Yjs WebSocket server is running')
})

const wss = new WebSocket.Server({
    server,
    // 添加CORS支持
    perMessageDeflate: false
})

console.log('🚀 Yjs WebSocket 服务器启动中...')

wss.on('connection', (conn, req) => {
    console.log(`📡 新的WebSocket连接: ${req.url}`)
    setupWSConnection(conn, req, {
        // 可以添加持久化配置
        // gc: true 
    })
})

wss.on('error', (error) => {
    console.error('❌ WebSocket服务器错误:', error)
})

server.listen(port, '0.0.0.0', () => {
    console.log(`✅ Yjs WebSocket服务器运行在: ws://0.0.0.0:${port}`)
    console.log(`🌐 HTTP状态页面: http://localhost:${port}`)
})

// // 优雅关闭
// process.on('SIGINT', () => {
//     console.log('\n⏹️  正在关闭Yjs WebSocket服务器...')
//     wss.close(() => {
//         console.log('✅ Yjs WebSocket服务器已关闭')
//         process.exit(0)
//     })
// })