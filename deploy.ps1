# CloudStack Pro Deploy Script
$SERVER_IP = "120.46.182.160"
$SERVER_USER = "root"

Write-Host "Starting CloudStack Pro deployment..." -ForegroundColor Green

# 1. Create server directory structure
Write-Host "1. Creating server directory structure..." -ForegroundColor Yellow
ssh $SERVER_USER@$SERVER_IP "mkdir -p /root/cloudstack-pro/server"

# 2. Transfer project files
Write-Host "2. Transferring project files..." -ForegroundColor Yellow
scp -r server\demo $SERVER_USER@${SERVER_IP}:/root/cloudstack-pro/server/
scp -r cloudstackpro $SERVER_USER@${SERVER_IP}:/root/cloudstack-pro/
scp -r yjs-websocket-server $SERVER_USER@${SERVER_IP}:/root/cloudstack-pro/
scp -r server\server $SERVER_USER@${SERVER_IP}:/root/cloudstack-pro/server/

# 3. Transfer configuration files
Write-Host "3. Transferring configuration files..." -ForegroundColor Yellow
scp docker-compose-simplified.yml $SERVER_USER@${SERVER_IP}:/root/cloudstack-pro/docker-compose.yml
scp nginx.conf $SERVER_USER@${SERVER_IP}:/root/cloudstack-pro/

# 4. Deploy application
Write-Host "4. Deploying application on server..." -ForegroundColor Yellow
ssh $SERVER_USER@$SERVER_IP "cd /root/cloudstack-pro && docker-compose down && docker-compose up -d --build"

Write-Host "Deployment completed!" -ForegroundColor Green
Write-Host "Access URL: http://$SERVER_IP" -ForegroundColor Cyan
