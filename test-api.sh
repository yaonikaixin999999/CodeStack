#!/bin/bash

# 测试CloudStack Pro后端API

echo "🧪 测试CloudStack Pro后端API..."

API_BASE="http://localhost:3001/api"

echo "1. 测试健康检查..."
curl -s "$API_BASE/health" | jq '.' || echo "健康检查失败"

echo -e "\n2. 测试文件树获取..."
curl -s "$API_BASE/files/tree" | jq '.' || echo "文件树获取失败"

echo -e "\n3. 测试SSH连接..."
curl -s -X POST "$API_BASE/files/ssh/connect" \
  -H "Content-Type: application/json" \
  -d '{
    "host": "120.46.182.160",
    "username": "root", 
    "password": "Yaonikaixin999999",
    "port": 22
  }' | jq '.' || echo "SSH连接失败"

echo -e "\n✅ API测试完成"
