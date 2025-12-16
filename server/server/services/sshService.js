const { Client } = require('ssh2');

class SSHService {
    constructor() {
        this.connections = new Map();
    }

    async createConnection(connectionId, config) {
        return new Promise((resolve, reject) => {
            const conn = new Client();

            conn.on('ready', () => {
                console.log('SSH连接已建立');

                // 使用exec而不是shell，避免初始化输出
                this.connections.set(connectionId, {
                    client: conn,
                    config: config
                });

                resolve(connectionId);
            });

            conn.on('error', (err) => {
                console.error('SSH连接错误:', err);
                reject(err);
            });

            conn.on('end', () => {
                console.log('SSH连接已关闭');
                this.connections.delete(connectionId);
            });

            conn.connect({
                host: config.host,
                port: config.port || 22,
                username: config.username,
                password: config.password,
                keepaliveInterval: 30000,
                readyTimeout: 20000
            });
        });
    }

    async executeCommand(connectionId, command) {
        const connection = this.connections.get(connectionId);
        if (!connection) {
            throw new Error('SSH连接不存在');
        }

        return new Promise((resolve, reject) => {
            const { client } = connection;

            // 使用exec执行单个命令，避免shell的初始化输出
            client.exec(command, (err, stream) => {
                if (err) {
                    reject(err);
                    return;
                }

                let stdout = '';
                let stderr = '';

                stream.on('close', (code, signal) => {
                    resolve({
                        stdout: stdout,
                        stderr: stderr,
                        code: code || 0
                    });
                });

                stream.on('data', (data) => {
                    stdout += data.toString();
                });

                stream.stderr.on('data', (data) => {
                    stderr += data.toString();
                });

                // 设置超时
                const timeout = setTimeout(() => {
                    stream.close();
                    reject(new Error('命令执行超时'));
                }, 30000);

                stream.on('close', () => {
                    clearTimeout(timeout);
                });
            });
        });
    }

    closeConnection(connectionId) {
        const connection = this.connections.get(connectionId);
        if (connection) {
            connection.client.end();
            this.connections.delete(connectionId);
        }
    }

    getConnectionStatus(connectionId) {
        return this.connections.has(connectionId);
    }

    closeAllConnections() {
        for (const [id, connection] of this.connections) {
            connection.client.end();
        }
        this.connections.clear();
    }
}

module.exports = new SSHService();