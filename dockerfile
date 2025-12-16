FROM node:22-alpine AS frontend-build

# 构建前端
WORKDIR /app/frontend
COPY cloudstackpro/package*.json ./
RUN npm install
COPY cloudstackpro/ ./
RUN npm run build

# 多阶段构建 - 生产环境
FROM node:22-bookworm AS production

WORKDIR /app

# 安装生产依赖
COPY server/server/package*.json ./
RUN npm install --only=production

# 复制服务器代码
COPY server/server/ ./

# 复制前端构建结果到服务器的public目录
COPY --from=frontend-build /app/frontend/dist ./public

# 复制WebSocket服务器
COPY yjs-websocket-server/ ./yjs-websocket-server/
WORKDIR /app/yjs-websocket-server
RUN npm install --only=production

WORKDIR /app

# 暴露端口
EXPOSE 3001 1234 8080

# 创建启动脚本
RUN echo '#!/bin/sh\nnode yjs-websocket-server/server.js &\nnode server.js' > start.sh
RUN chmod +x start.sh

CMD ["./start.sh"]