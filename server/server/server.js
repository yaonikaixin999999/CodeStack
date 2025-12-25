const express = require('express');
const cors = require('cors');
const path = require('path');
const http = require('http');
const CollaborationService = require('./services/collaborationService');

const app = express();
const server = http.createServer(app);
const PORT = process.env.PORT || 3001;

// 初始化协作服务
const collaborationService = new CollaborationService(server);

// 设置默认编码
process.env.LANG = 'zh_CN.UTF-8';
process.env.LC_ALL = 'zh_CN.UTF-8';

// 中间件
app.use(cors({
    origin: true, // 允许所有来源
    credentials: true
}));

// 静态文件服务（为生产环境提供前端文件）
if (process.env.NODE_ENV === 'production') {
    app.use(express.static(path.join(__dirname, 'public')));
}

app.use(express.json({
    limit: '50mb',
    type: 'application/json'
}));

app.use(express.text({
    type: 'text/plain',
    limit: '50mb',
    defaultCharset: 'utf-8'
}));

// 设置默认编码为UTF-8
app.use((req, res, next) => {
    res.setHeader('Content-Type', 'application/json; charset=utf-8');
    next();
});

// 路由
const filesRouter = require('./routes/files');
app.use('/api/files', filesRouter);

// 健康检查
app.get('/health', (req, res) => {
    res.json({
        status: 'ok',
        message: 'CloudStack Pro Server is running',
        encoding: 'UTF-8',
        timestamp: new Date().toISOString(),
        environment: process.env.NODE_ENV || 'development'
    });
});

app.get('/api/health', (req, res) => {
    res.json({
        status: 'ok',
        message: 'CloudStack Pro Server is running',
        encoding: 'UTF-8',
        timestamp: new Date().toISOString(),
        environment: process.env.NODE_ENV || 'development'
    });
});

// 生产环境下，所有其他路由都返回前端应用
if (process.env.NODE_ENV === 'production') {
    app.get('*', (req, res) => {
        res.sendFile(path.join(__dirname, 'public', 'index.html'));
    });
}

// 启动服务器
server.listen(PORT, '0.0.0.0', () => {
    console.log(`CloudStack Pro Server running on port ${PORT}`);
    console.log(`Server encoding: UTF-8`);
    console.log(`Server accessible at:`);
    console.log(`  Local:   http://localhost:${PORT}/api/health`);
    console.log(`  Network: http://192.168.31.1:${PORT}/api/health`);
    console.log(`Collaboration service initialized`);
});

module.exports = app;