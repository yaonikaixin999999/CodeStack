const express = require('express');
const router = express.Router();
const fs = require('fs').promises;
const path = require('path');
const { spawn } = require('child_process');
const sshService = require('../services/sshService');

// 远程服务器配置 - 支持环境变量覆盖
const REMOTE_SERVER_CONFIG = {
    host: process.env.REMOTE_HOST || '8.137.125.47',  // 你的Linux服务器IP
    username: process.env.REMOTE_USERNAME || 'root',      // SSH用户名
    password: process.env.REMOTE_PASSWORD || 'Yaonikaixin999999', // SSH密码
    port: parseInt(process.env.REMOTE_PORT) || 22,              // SSH端口
    baseDir: process.env.REMOTE_BASE_DIR || '/Desktop/User_Coding' // 代码文件根目录
};

// 获取或创建SSH连接
let globalConnectionId = null;

async function ensureSSHConnection() {
    try {
        // 检查现有连接是否有效
        if (globalConnectionId && sshService.getConnectionStatus(globalConnectionId)) {
            return globalConnectionId;
        }

        // 创建新连接
        globalConnectionId = `files_ssh_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
        await sshService.createConnection(globalConnectionId, REMOTE_SERVER_CONFIG);

        console.log('文件系统SSH连接已建立:', globalConnectionId);
        return globalConnectionId;
    } catch (error) {
        console.error('建立SSH连接失败:', error);
        throw new Error('无法连接到远程服务器');
    }
}

// 执行远程命令的辅助函数
async function executeRemoteCommand(command) {
    const connectionId = await ensureSSHConnection();
    const cleanCommand = buildCleanCommand(command);
    const result = await sshService.executeCommand(connectionId, cleanCommand);

    return {
        stdout: cleanOutput(result.stdout || ''),
        stderr: cleanOutput(result.stderr || ''),
        code: result.code || 0
    };
}

// 清理输出函数
function cleanOutput(output) {
    if (!output) return '';

    return output
        // 移除ANSI转义序列
        .replace(/\x1B\[[0-9;]*[a-zA-Z]/g, '')
        // 移除控制字符
        .replace(/\x1B\[\?[0-9]+[a-zA-Z]/g, '')
        // 移除其他控制字符
        .replace(/[\x00-\x08\x0B\x0C\x0E-\x1F\x7F]/g, '')
        // 移除多余的空白字符
        .replace(/\s+$/gm, '')
        // 清理多余空白
        .trim();
}

// 构建清理的命令
function buildCleanCommand(command) {
    // 设置环境变量并禁用回显
    return `export LANG=C.UTF-8; export LC_ALL=C.UTF-8; ${command}`;
}

// 设置UTF-8编码中间件
router.use((req, res, next) => {
    res.setHeader('Content-Type', 'application/json; charset=utf-8');
    next();
});

// 执行命令的API
router.post('/execute', async (req, res) => {
    try {
        const { command, cwd, filePath } = req.body;

        const workingDir = cwd || path.dirname(filePath || '') || process.cwd();

        const child = spawn('cmd', ['/c', command], {
            cwd: workingDir,
            stdio: 'pipe',
            encoding: 'utf8'
        });

        let stdout = '';
        let stderr = '';

        child.stdout.on('data', (data) => {
            stdout += data.toString();
        });

        child.stderr.on('data', (data) => {
            stderr += data.toString();
        });

        child.on('close', (code) => {
            res.json({
                stdout: stdout,
                stderr: stderr,
                code: code
            });
        });

    } catch (error) {
        console.error('执行命令失败:', error);
        res.status(500).json({
            error: error.message,
            stdout: '',
            stderr: error.message,
            code: 1
        });
    }
});

// SSH连接
router.post('/ssh/connect', async (req, res) => {
    try {
        const { host, username, password, port } = req.body;
        const connectionId = `ssh_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;

        console.log(`尝试连接SSH: ${username}@${host}:${port}`);

        const config = {
            host: host || '8.137.125.47',
            username: username || 'root',
            password: password || 'Yaonikaixin999999',
            port: port || 22
        };

        await sshService.createConnection(connectionId, config);

        console.log('SSH连接成功，connectionId:', connectionId);

        res.json({
            success: true,
            connectionId: connectionId,
            message: '成功连接到远程服务器'
        });
    } catch (error) {
        console.error('SSH连接失败:', error);
        res.status(500).json({
            success: false,
            message: error.message || 'SSH连接失败'
        });
    }
});

// SSH命令执行 - 修改这个函数
router.post('/ssh/execute', async (req, res) => {
    try {
        const { connectionId, command } = req.body;

        console.log(`执行SSH命令: ${command}, connectionId: ${connectionId}`);

        if (!connectionId) {
            return res.status(400).json({
                success: false,
                message: '缺少连接ID'
            });
        }

        if (!sshService.getConnectionStatus(connectionId)) {
            return res.status(400).json({
                success: false,
                message: 'SSH连接不存在，请重新连接'
            });
        }

        // 构建清理的命令
        const cleanCommand = buildCleanCommand(command);

        // 执行命令
        const result = await sshService.executeCommand(connectionId, cleanCommand);

        console.log('原始命令执行结果:', result);

        // 清理输出
        const cleanedResult = {
            stdout: cleanOutput(result.stdout || ''),
            stderr: cleanOutput(result.stderr || ''),
            code: result.code || 0
        };

        console.log('清理后的命令执行结果:', cleanedResult);

        res.json({
            success: true,
            ...cleanedResult
        });
    } catch (error) {
        console.error('SSH命令执行失败:', error);
        res.status(500).json({
            success: false,
            message: error.message || 'SSH命令执行失败'
        });
    }
});

// SSH断开连接
router.post('/ssh/disconnect', (req, res) => {
    try {
        const { connectionId } = req.body;
        sshService.closeConnection(connectionId);

        res.json({
            success: true,
            message: '已断开SSH连接'
        });
    } catch (error) {
        res.status(500).json({
            success: false,
            message: error.message || '断开连接失败'
        });
    }
});

// 检查SSH连接状态
router.get('/ssh/status/:connectionId', (req, res) => {
    const { connectionId } = req.params;
    const isConnected = sshService.getConnectionStatus(connectionId);

    res.json({
        success: true,
        connected: isConnected
    });
});

// 获取远程文件树
router.get('/tree', async (req, res) => {
    try {
        const { path: requestPath, loadChildren = 'true' } = req.query;
        const targetPath = requestPath || REMOTE_SERVER_CONFIG.baseDir;

        // 确保路径在允许的目录内
        if (!targetPath.startsWith(REMOTE_SERVER_CONFIG.baseDir)) {
            return res.status(403).json({ error: '拒绝访问该目录' });
        }

        console.log('正在获取文件树:', targetPath, '加载子目录:', loadChildren);

        // 首先检查目录是否存在
        const checkCommand = `test -d "${targetPath}" && echo "exists" || echo "not_found"`;
        const checkResult = await executeRemoteCommand(checkCommand);

        if (checkResult.stdout.trim() !== 'exists') {
            // 如果目录不存在，尝试创建
            const createCommand = `mkdir -p "${targetPath}"`;
            await executeRemoteCommand(createCommand);
            console.log('已创建目录:', targetPath);
        }

        // 使用ls命令获取目录内容（更可靠）
        const command = `ls -la "${targetPath}" 2>/dev/null | tail -n +2`;
        const result = await executeRemoteCommand(command);

        if (result.code !== 0) {
            throw new Error(result.stderr || '获取目录列表失败');
        }

        console.log('ls命令输出:', result.stdout);

        // 解析ls -la的输出，支持递归加载子目录
        const fileTree = await parseLsOutputWithChildren(result.stdout, targetPath, loadChildren === 'true');

        res.json(fileTree);
    } catch (error) {
        console.error('获取文件树失败:', error);
        res.status(500).json({ error: error.message || '获取文件树失败' });
    }
});

// 解析ls -la命令的输出 - 支持递归加载子目录
async function parseLsOutputWithChildren(output, basePath, loadChildren = true) {
    const lines = output.split('\n').filter(line => line.trim());
    const files = [];

    for (const line of lines) {
        const parts = line.trim().split(/\s+/);
        if (parts.length < 9) continue;

        const permissions = parts[0];
        const size = parts[4];
        const month = parts[5];
        const day = parts[6];
        const timeOrYear = parts[7];
        const fileName = parts.slice(8).join(' ');

        // 跳过 . 和 .. 目录
        if (fileName === '.' || fileName === '..') continue;

        const isDirectory = permissions.startsWith('d');
        const filePath = `${basePath}/${fileName}`.replace('//', '/');

        const fileNode = {
            name: fileName,
            path: filePath,
            type: isDirectory ? 'directory' : 'file',
            size: isDirectory ? undefined : parseInt(size) || 0,
            modifyTime: `${month} ${day} ${timeOrYear}`
        };

        // 如果是目录且需要加载子目录，递归获取子内容
        if (isDirectory && loadChildren) {
            try {
                const childCommand = `ls -la "${filePath}" 2>/dev/null | tail -n +2`;
                const childResult = await executeRemoteCommand(childCommand);

                if (childResult.code === 0 && childResult.stdout.trim()) {
                    fileNode.children = await parseLsOutputWithChildren(childResult.stdout, filePath, false); // 只加载一层
                } else {
                    fileNode.children = []; // 空目录
                }
            } catch (error) {
                console.error(`获取子目录失败 ${filePath}:`, error);
                fileNode.children = [];
            }
        }

        files.push(fileNode);
    }

    // 排序：文件夹在前，然后按名称排序
    files.sort((a, b) => {
        if (a.type !== b.type) {
            return a.type === 'directory' ? -1 : 1;
        }
        return a.name.localeCompare(b.name);
    });

    return files;
}

// 解析ls -la命令的输出（原始版本，保留向后兼容）
function parseLsOutput(output, basePath) {
    const lines = output.split('\n').filter(line => line.trim());
    const files = [];

    for (const line of lines) {
        const parts = line.trim().split(/\s+/);
        if (parts.length < 9) continue;

        const permissions = parts[0];
        const size = parts[4];
        const month = parts[5];
        const day = parts[6];
        const timeOrYear = parts[7];
        const fileName = parts.slice(8).join(' ');

        // 跳过 . 和 .. 目录
        if (fileName === '.' || fileName === '..') continue;

        const isDirectory = permissions.startsWith('d');
        const filePath = `${basePath}/${fileName}`.replace('//', '/');

        files.push({
            name: fileName,
            path: filePath,
            type: isDirectory ? 'directory' : 'file',
            size: isDirectory ? undefined : parseInt(size) || 0,
            modifyTime: `${month} ${day} ${timeOrYear}`
        });
    }

    // 排序：文件夹在前，然后按名称排序
    files.sort((a, b) => {
        if (a.type !== b.type) {
            return a.type === 'directory' ? -1 : 1;
        }
        return a.name.localeCompare(b.name);
    });

    return files;
}

// 创建文件或文件夹
router.post('/create', async (req, res) => {
    try {
        const { path: filePath, type, content = '' } = req.body;

        if (!filePath || !type) {
            return res.status(400).json({ error: '缺少必要参数' });
        }

        // 确保路径在允许的目录内
        if (!filePath.startsWith(REMOTE_SERVER_CONFIG.baseDir)) {
            return res.status(403).json({ error: '拒绝在该目录创建文件' });
        }

        let command;
        if (type === 'directory') {
            command = `mkdir -p "${filePath}"`;
        } else {
            // 确保父目录存在
            const parentDir = path.dirname(filePath);
            command = `mkdir -p "${parentDir}" && echo '${content.replace(/'/g, "\\'")}' > "${filePath}"`;
        }

        const result = await executeRemoteCommand(command);

        if (result.code !== 0) {
            throw new Error(result.stderr || `创建${type === 'directory' ? '文件夹' : '文件'}失败`);
        }

        res.json({
            success: true,
            message: `${type === 'directory' ? '文件夹' : '文件'}创建成功`,
            path: filePath
        });
    } catch (error) {
        console.error('创建文件/文件夹失败:', error);
        res.status(500).json({ error: error.message || '创建失败' });
    }
});

// 删除文件或文件夹
router.delete('/delete', async (req, res) => {
    try {
        const { path: filePath } = req.query;

        if (!filePath) {
            return res.status(400).json({ error: '缺少文件路径' });
        }

        // 确保路径在允许的目录内，且不是根目录
        if (!filePath.startsWith(REMOTE_SERVER_CONFIG.baseDir) ||
            filePath === REMOTE_SERVER_CONFIG.baseDir) {
            return res.status(403).json({ error: '拒绝删除该文件/目录' });
        }

        const command = `rm -rf "${filePath}"`;
        const result = await executeRemoteCommand(command);

        if (result.code !== 0) {
            throw new Error(result.stderr || '删除失败');
        }

        res.json({ success: true, message: '删除成功' });
    } catch (error) {
        console.error('删除文件/文件夹失败:', error);
        res.status(500).json({ error: error.message || '删除失败' });
    }
});

// 重命名文件或文件夹
router.post('/rename', async (req, res) => {
    try {
        const { oldPath, newPath } = req.body;

        if (!oldPath || !newPath) {
            return res.status(400).json({ error: '缺少必要参数' });
        }

        // 确保路径在允许的目录内
        if (!oldPath.startsWith(REMOTE_SERVER_CONFIG.baseDir) ||
            !newPath.startsWith(REMOTE_SERVER_CONFIG.baseDir)) {
            return res.status(403).json({ error: '拒绝访问该目录' });
        }

        const command = `mv "${oldPath}" "${newPath}"`;
        const result = await executeRemoteCommand(command);

        if (result.code !== 0) {
            throw new Error(result.stderr || '重命名失败');
        }

        res.json({ success: true, message: '重命名成功' });
    } catch (error) {
        console.error('重命名失败:', error);
        res.status(500).json({ error: error.message || '重命名失败' });
    }
});

// 获取文件内容 - 修改为支持远程文件
router.get('/content', async (req, res) => {
    try {
        const { path: filePath } = req.query;

        if (!filePath) {
            return res.status(400).json({ error: '缺少文件路径' });
        }

        // 确保路径在允许的目录内
        if (!filePath.startsWith(REMOTE_SERVER_CONFIG.baseDir)) {
            return res.status(403).json({ error: '拒绝访问该文件' });
        }

        // 检查文件是否存在且是普通文件
        const checkCommand = `test -f "${filePath}" && echo "exists" || echo "not_found"`;
        const checkResult = await executeRemoteCommand(checkCommand);

        if (checkResult.stdout.trim() !== 'exists') {
            return res.status(404).json({ error: '文件不存在' });
        }

        // 读取文件内容，限制大小避免内存问题
        const readCommand = `head -c 1048576 "${filePath}"`;  // 限制1MB
        const result = await executeRemoteCommand(readCommand);

        if (result.code !== 0) {
            throw new Error(result.stderr || '读取文件失败');
        }

        res.json({ content: result.stdout });
    } catch (error) {
        console.error('读取文件失败:', error);
        res.status(500).json({ error: error.message || '读取文件失败' });
    }
});

// 保存文件 - 修改为支持远程文件
router.post('/save', async (req, res) => {
    try {
        const { path: filePath } = req.query;
        const content = req.body;

        if (!filePath) {
            return res.status(400).json({ error: '缺少文件路径' });
        }

        // 确保路径在允许的目录内
        if (!filePath.startsWith(REMOTE_SERVER_CONFIG.baseDir)) {
            return res.status(403).json({ error: '拒绝保存到该位置' });
        }

        // 转义内容中的特殊字符，使用更安全的方式写入文件
        const escapedContent = Buffer.from(content).toString('base64');
        const command = `echo "${escapedContent}" | base64 -d > "${filePath}"`;

        const result = await executeRemoteCommand(command);

        if (result.code !== 0) {
            throw new Error(result.stderr || '保存文件失败');
        }

        res.json({ success: true, message: '文件保存成功' });
    } catch (error) {
        console.error('保存文件失败:', error);
        res.status(500).json({ error: error.message || '保存文件失败' });
    }
});

module.exports = router;