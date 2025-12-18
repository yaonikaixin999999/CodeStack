// @ts-ignore
import { io, Socket } from 'socket.io-client';

export interface CompilationEvent {
    compilationKey: string;
    filePath: string;
    command: string;
    userId: string;
    startTime: number;
    endTime?: number;
    result?: any;
    status?: 'running' | 'completed' | 'failed';
}

export interface CompilationResult {
    stdout?: string;
    stderr?: string;
    code?: number;
    error?: string;
}

export interface CollaborationUser {
    userId: string;
    userName: string;
}

export interface FileChange {
    filePath: string;
    content: string;
    userId: string;
    timestamp: number;
    changes?: any;
}

export interface CursorPosition {
    filePath: string;
    userId: string;
    userName?: string;
    position: any;
    selection?: any;
}

class CollaborationService {
    private socket: any = null;
    private currentFilePath: string | null = null;
    private userId: string;
    private userName: string;
    private reconnectAttempts: number = 0;
    private maxReconnectAttempts: number = 5;
    private reconnectInterval: number = 3000;

    // 协作状态
    private collaborators: CollaborationUser[] = [];
    private eventListeners: Map<string, Function[]> = new Map();

    constructor() {
        this.userId = this.generateUserId();
        this.userName = this.generateUserName();
        this.connect();
    }

    private generateUserId(): string {
        return Math.random().toString(36).substring(2, 15);
    }

    private generateUserName(): string {
        // 生成更友好的用户名
        const adjectives = ['Smart', 'Quick', 'Clever', 'Bright', 'Fast', 'Sharp', 'Swift'];
        const animals = ['Fox', 'Cat', 'Owl', 'Eagle', 'Tiger', 'Lion', 'Wolf'];
        const adjective = adjectives[Math.floor(Math.random() * adjectives.length)];
        const animal = animals[Math.floor(Math.random() * animals.length)];
        const number = Math.floor(Math.random() * 1000);
        return `${adjective}${animal}${number}`;
    }

    private connect(): void {
        try {
            this.socket = io('http://192.168.22.16:3001', {
                reconnection: true,
                reconnectionAttempts: this.maxReconnectAttempts,
                reconnectionDelay: this.reconnectInterval,
                timeout: 5000
            });

            this.socket.on('connect', () => {
                console.log(`✅ 已连接到协作服务器 (用户: ${this.userName})`);
                this.reconnectAttempts = 0;

                // 重新加入之前的房间
                if (this.currentFilePath) {
                    this.joinFileCollaboration(this.currentFilePath);
                }
            });

            this.socket.on('disconnect', (reason: any) => {
                console.log(`⚠️ 已断开与协作服务器的连接: ${reason}`);
            });

            this.socket.on('connect_error', (error: any) => {
                console.error('❌ 连接协作服务器失败:', error);
                this.reconnectAttempts++;

                if (this.reconnectAttempts >= this.maxReconnectAttempts) {
                    console.error('🚫 达到最大重连次数，停止重连');
                }
            });

            this.socket.on('reconnect', (attemptNumber: any) => {
                console.log(`🔄 重新连接成功 (尝试 ${attemptNumber} 次)`);
            });

            this.socket.on('reconnect_failed', () => {
                console.error('🚫 重连失败，请检查网络连接');
            });

            // 设置文件协作事件监听器
            this.setupFileCollaborationListeners();

        } catch (error) {
            console.error('初始化协作服务失败:', error);
        }
    }

    private setupFileCollaborationListeners(): void {
        // 文件内容更新
        this.socket.on('file-content-updated', (data: FileChange) => {
            this.emitEvent('content-change', data);
        });

        // 协作者列表更新
        this.socket.on('collaborators-updated', (data: { filePath: string; collaborators: CollaborationUser[] }) => {
            this.collaborators = data.collaborators;
            this.emitEvent('collaborators-update', this.collaborators);
        });

        // 用户加入
        this.socket.on('user-joined', (data: { userId: string; userName: string; filePath: string }) => {
            console.log(`👋 ${data.userName} 加入了协作`);
            this.emitEvent('user-joined', { userId: data.userId, userName: data.userName });
        });

        // 用户离开
        this.socket.on('user-left', (data: { userId: string; filePath: string }) => {
            console.log(`👋 用户 ${data.userId} 离开了协作`);
            this.emitEvent('user-left', { userId: data.userId, userName: data.userId });
        });

        // 光标位置更新
        this.socket.on('cursor-position-updated', (data: CursorPosition) => {
            if (data.userId !== this.userId) {
                this.emitEvent('cursor-update', data);
            }
        });
    }

    // 事件发射器
    private emitEvent(eventName: string, data: any): void {
        const listeners = this.eventListeners.get(eventName) || [];
        listeners.forEach(listener => {
            try {
                listener(data);
            } catch (error) {
                console.error(`Error in ${eventName} listener:`, error);
            }
        });
    }

    // 添加事件监听器
    private addEventListener(eventName: string, callback: Function): void {
        if (!this.eventListeners.has(eventName)) {
            this.eventListeners.set(eventName, []);
        }
        this.eventListeners.get(eventName)!.push(callback);
    }

    // 移除事件监听器
    private removeEventListener(eventName: string, callback: Function): void {
        const listeners = this.eventListeners.get(eventName) || [];
        const index = listeners.indexOf(callback);
        if (index > -1) {
            listeners.splice(index, 1);
        }
    }

    // 检查连接状态
    isConnected(): boolean {
        return this.socket?.connected || false;
    }

    // 手动重连
    reconnect(): void {
        if (this.socket) {
            this.socket.disconnect();
        }
        this.reconnectAttempts = 0;
        this.connect();
    }

    // ==================== 文件协作功能 ====================

    // 加入文件协作
    joinFileCollaboration(filePath: string): void {
        if (!this.isConnected()) {
            console.warn('⚠️ 未连接到协作服务器，无法加入文件协作');
            return;
        }

        // 离开之前的文件
        if (this.currentFilePath && this.currentFilePath !== filePath) {
            this.leaveFileCollaboration();
        }

        this.currentFilePath = filePath;
        this.socket?.emit('join-collaboration', {
            userId: this.userId,
            userName: this.userName,
            filePath
        });

        // 同时加入编译房间
        this.socket?.emit('join-compilation-room', { filePath, userId: this.userId });

        console.log(`📁 加入文件协作: ${filePath}`);
    }

    // 离开文件协作
    leaveFileCollaboration(): void {
        if (!this.isConnected() || !this.currentFilePath) {
            return;
        }

        const filePath = this.currentFilePath;

        this.socket?.emit('leave-file', {
            filePath,
            userId: this.userId
        });

        // 同时离开编译房间
        this.socket?.emit('leave-compilation-room', { filePath, userId: this.userId });

        this.currentFilePath = null;
        this.collaborators = [];

        console.log(`📤 离开文件协作: ${filePath}`);
    }

    // 发送文件内容变更
    sendContentChange(content: string, changes?: any): void {
        if (!this.isConnected() || !this.currentFilePath) {
            return;
        }

        this.socket?.emit('file-content-change', {
            filePath: this.currentFilePath,
            content,
            userId: this.userId,
            changes,
            timestamp: Date.now()
        });
    }

    // 发送光标位置
    sendCursorPosition(position: any, selection?: any): void {
        if (!this.isConnected() || !this.currentFilePath) {
            return;
        }

        this.socket?.emit('cursor-position-change', {
            filePath: this.currentFilePath,
            userId: this.userId,
            userName: this.userName,
            position,
            selection
        });
    }

    // ==================== 编译协作功能 ====================

    // 加入文件编译房间（已集成到 joinFileCollaboration 中）
    joinCompilationRoom(filePath: string): void {
        if (!this.isConnected()) {
            console.warn('⚠️ 未连接到协作服务器，无法加入编译房间');
            return;
        }

        this.socket?.emit('join-compilation-room', { filePath, userId: this.userId });
        console.log(`🔧 加入编译房间: ${filePath}`);
    }

    // 离开文件编译房间（已集成到 leaveFileCollaboration 中）
    leaveCompilationRoom(filePath: string): void {
        if (!this.isConnected()) {
            return;
        }

        this.socket?.emit('leave-compilation-room', { filePath, userId: this.userId });
        console.log(`� 离开编译房间: ${filePath}`);
    }

    // 监听编译开始事件
    onCompilationStarted(callback: (event: CompilationEvent) => void): void {
        this.socket?.on('compilation-started', (event: CompilationEvent) => {
            console.log('🔄 编译开始:', event);
            callback(event);
        });
    }

    // 监听编译完成事件
    onCompilationCompleted(callback: (event: CompilationEvent) => void): void {
        this.socket?.on('compilation-completed', (event: CompilationEvent) => {
            console.log('✅ 编译完成:', event);
            callback(event);
        });
    }

    // 监听编译失败事件
    onCompilationFailed(callback: (event: { filePath: string; error: string; userId: string }) => void): void {
        this.socket?.on('compilation-failed', (event: { filePath: string; error: string; userId: string }) => {
            console.log('❌ 编译失败:', event);
            callback(event);
        });
    }

    // 开始编译
    startCompilation(filePath: string, command: string): void {
        if (!this.isConnected()) {
            console.warn('⚠️ 未连接到协作服务器，无法开始编译');
            return;
        }

        const compilationData = {
            filePath,
            command,
            userId: this.userId,
            timestamp: Date.now()
        };

        this.socket?.emit('start-compilation', compilationData);
        console.log('🚀 开始编译:', compilationData);
    }

    // 通知编译完成
    notifyCompilationComplete(compilationKey: string, result: CompilationResult): void {
        if (!this.isConnected()) {
            return;
        }

        this.socket?.emit('compilation-complete', {
            compilationKey,
            result,
            userId: this.userId,
            endTime: Date.now()
        });
    }

    // ==================== 回调函数设置 ====================

    // 设置内容变更回调
    onContentChange(callback: (change: FileChange) => void): void {
        this.addEventListener('content-change', callback);
    }

    // 设置协作者更新回调
    onCollaboratorsUpdate(callback: (collaborators: CollaborationUser[]) => void): void {
        this.addEventListener('collaborators-update', callback);
    }

    // 设置光标更新回调
    onCursorUpdate(callback: (data: CursorPosition) => void): void {
        this.addEventListener('cursor-update', callback);
    }

    // 设置用户加入回调
    onUserJoined(callback: (user: CollaborationUser) => void): void {
        this.addEventListener('user-joined', callback);
    }

    // 设置用户离开回调
    onUserLeft(callback: (user: CollaborationUser) => void): void {
        this.addEventListener('user-left', callback);
    }


    // ==================== 获取信息方法 ====================

    // 获取当前用户信息
    getCurrentUser(): CollaborationUser {
        return {
            userId: this.userId,
            userName: this.userName
        };
    }

    // 获取用户ID
    getUserId(): string {
        return this.userId;
    }

    // 获取用户名
    getUserName(): string {
        return this.userName;
    }

    // 设置用户名
    setUserName(userName: string): void {
        this.userName = userName;
    }

    // 获取协作者列表
    getCollaborators(): CollaborationUser[] {
        return this.collaborators;
    }

    // 获取当前文件路径
    getCurrentFilePath(): string | null {
        return this.currentFilePath;
    }

    // 获取当前编译房间（保持向后兼容）
    getCurrentRoom(): string | null {
        return this.currentFilePath;
    }

    // 检查是否正在协作
    isCollaborating(): boolean {
        return this.collaborators.length > 1;
    }

    // ==================== 清理和断开连接 ====================

    // 移除事件监听器
    removeAllListeners(): void {
        this.eventListeners.clear();

        // 移除socket事件监听器
        this.socket?.removeAllListeners('compilation-started');
        this.socket?.removeAllListeners('compilation-completed');
        this.socket?.removeAllListeners('compilation-failed');
        this.socket?.removeAllListeners('file-content-updated');
        this.socket?.removeAllListeners('collaborators-updated');
        this.socket?.removeAllListeners('user-joined');
        this.socket?.removeAllListeners('user-left');
        this.socket?.removeAllListeners('cursor-position-updated');
    }

    // 断开连接
    disconnect(): void {
        if (this.currentFilePath) {
            this.leaveFileCollaboration();
        }

        this.removeAllListeners();
        this.socket?.disconnect();
        console.log('🔌 已断开协作服务连接');
    }

    // 获取连接状态信息
    getConnectionInfo(): {
        connected: boolean;
        userId: string;
        userName: string;
        currentFilePath: string | null;
        reconnectAttempts: number;
        collaborators: CollaborationUser[];
        isCollaborating: boolean;
    } {
        return {
            connected: this.isConnected(),
            userId: this.userId,
            userName: this.userName,
            currentFilePath: this.currentFilePath,
            reconnectAttempts: this.reconnectAttempts,
            collaborators: this.collaborators,
            isCollaborating: this.isCollaborating()
        };
    }
}

// 创建单例实例
export const collaborationService = new CollaborationService();

// 页面卸载时自动断开连接
if (typeof window !== 'undefined') {
    window.addEventListener('beforeunload', () => {
        collaborationService.disconnect();
    });
}