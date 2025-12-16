const { Server } = require('socket.io');

class CollaborationService {
    constructor(server) {
        this.io = new Server(server, {
            cors: {
                origin: "*",
                methods: ["GET", "POST"]
            }
        });

        this.fileRooms = new Map(); // 文件房间管理
        this.userSessions = new Map(); // 用户会话管理

        this.setupEventHandlers();
        console.log('🚀 协作服务已初始化');
    }

    setupEventHandlers() {
        this.io.on('connection', (socket) => {
            console.log(`👤 用户连接: ${socket.id}`);

            // 加入文件协作
            socket.on('join-collaboration', (data) => {
                const { userId, userName, filePath } = data;

                console.log(`📁 ${userName}(${userId}) 加入文件协作: ${filePath}`);

                socket.join(filePath);
                socket.userId = userId;
                socket.userName = userName;
                socket.currentFile = filePath;

                // 更新房间信息
                this.updateFileRoom(filePath, socket, 'join');

                // 通知其他用户
                socket.to(filePath).emit('user-joined', {
                    userId,
                    userName,
                    filePath
                });

                // 发送当前协作者列表
                this.sendCollaboratorsList(filePath);
            });

            // 文件内容变更
            socket.on('file-content-change', (data) => {
                const { filePath, content, userId, changes, timestamp } = data;

                console.log(`✏️ 文件内容变更: ${filePath} by ${userId}`);

                // 转发给同一文件的其他用户
                socket.to(filePath).emit('file-content-updated', {
                    filePath,
                    content,
                    userId,
                    changes,
                    timestamp
                });
            });

            // 光标位置变更
            socket.on('cursor-position-change', (data) => {
                const { filePath, userId, userName, position, selection } = data;

                // 转发给同一文件的其他用户
                socket.to(filePath).emit('cursor-position-updated', {
                    filePath,
                    userId,
                    userName,
                    position,
                    selection
                });
            });

            // 加入编译房间
            socket.on('join-compilation-room', (data) => {
                const { filePath, userId } = data;
                const compilationRoom = `compilation:${filePath}`;

                socket.join(compilationRoom);
                console.log(`🔧 ${userId} 加入编译房间: ${filePath}`);
            });

            // 开始编译
            socket.on('start-compilation', (data) => {
                const { filePath, command, userId, timestamp } = data;
                const compilationRoom = `compilation:${filePath}`;
                const compilationKey = `${filePath}-${timestamp}`;

                console.log(`🚀 开始编译: ${filePath} - ${command}`);

                // 通知同一文件的所有用户编译开始
                this.io.to(compilationRoom).emit('compilation-started', {
                    compilationKey,
                    filePath,
                    command,
                    userId,
                    startTime: timestamp,
                    status: 'running'
                });
            });

            // 编译完成
            socket.on('compilation-complete', (data) => {
                const { compilationKey, result, userId, endTime } = data;
                const filePath = compilationKey.split('-')[0];
                const compilationRoom = `compilation:${filePath}`;

                console.log(`✅ 编译完成: ${compilationKey}`);

                // 通知编译结果
                this.io.to(compilationRoom).emit('compilation-completed', {
                    compilationKey,
                    filePath,
                    userId,
                    endTime,
                    result,
                    status: 'completed'
                });
            });

            // 离开文件协作
            socket.on('leave-file', (data) => {
                const { filePath, userId } = data;
                this.handleUserLeave(socket, filePath, userId);
            });

            // 离开编译房间
            socket.on('leave-compilation-room', (data) => {
                const { filePath, userId } = data;
                const compilationRoom = `compilation:${filePath}`;

                socket.leave(compilationRoom);
                console.log(`🔧 ${userId} 离开编译房间: ${filePath}`);
            });

            // 断开连接
            socket.on('disconnect', () => {
                console.log(`👋 用户断开连接: ${socket.id}`);
                if (socket.currentFile) {
                    this.handleUserLeave(socket, socket.currentFile, socket.userId);
                }
            });

            // 错误处理
            socket.on('error', (error) => {
                console.error(`❌ Socket错误 (${socket.id}):`, error);
            });
        });

        // 全局错误处理
        this.io.on('error', (error) => {
            console.error('❌ Socket.IO错误:', error);
        });
    }

    updateFileRoom(filePath, socket, action) {
        if (!this.fileRooms.has(filePath)) {
            this.fileRooms.set(filePath, new Set());
        }

        const room = this.fileRooms.get(filePath);

        if (action === 'join') {
            room.add(socket.id);
        } else if (action === 'leave') {
            room.delete(socket.id);
            if (room.size === 0) {
                this.fileRooms.delete(filePath);
                console.log(`🗂️ 文件房间已清空: ${filePath}`);
            }
        }
    }

    sendCollaboratorsList(filePath) {
        const sockets = this.io.sockets.adapter.rooms.get(filePath);
        if (!sockets) return;

        const collaborators = [];
        sockets.forEach(socketId => {
            const socket = this.io.sockets.sockets.get(socketId);
            if (socket && socket.userId) {
                collaborators.push({
                    userId: socket.userId,
                    userName: socket.userName || socket.userId
                });
            }
        });

        console.log(`👥 发送协作者列表 (${filePath}):`, collaborators.map(c => c.userName));

        this.io.to(filePath).emit('collaborators-updated', {
            filePath,
            collaborators
        });
    }

    handleUserLeave(socket, filePath, userId) {
        console.log(`👋 ${userId} 离开文件协作: ${filePath}`);

        socket.leave(filePath);
        this.updateFileRoom(filePath, socket, 'leave');

        // 同时离开编译房间
        const compilationRoom = `compilation:${filePath}`;
        socket.leave(compilationRoom);

        // 通知其他用户
        socket.to(filePath).emit('user-left', {
            userId,
            filePath
        });

        // 更新协作者列表
        this.sendCollaboratorsList(filePath);
    }

    // 获取统计信息
    getStats() {
        return {
            connectedUsers: this.io.sockets.sockets.size,
            activeFiles: this.fileRooms.size,
            fileRooms: Array.from(this.fileRooms.entries()).map(([filePath, sockets]) => ({
                filePath,
                userCount: sockets.size
            }))
        };
    }

    // 关闭服务
    close() {
        console.log('🔌 关闭协作服务...');
        this.io.close();
    }
}

module.exports = CollaborationService;