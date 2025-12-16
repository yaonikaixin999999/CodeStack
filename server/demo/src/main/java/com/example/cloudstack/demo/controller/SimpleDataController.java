package com.example.cloudstack.demo.controller;

import com.example.cloudstack.demo.model.SimpleData; // 导入你的实体类
import com.example.cloudstack.demo.service.SimpleDataService; // 导入你的 Service
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test") // API 基础路径
public class SimpleDataController {

    private final SimpleDataService simpleDataService;

    @Autowired
    public SimpleDataController(SimpleDataService simpleDataService) {
        this.simpleDataService = simpleDataService;
    }

    // 获取所有数据
    // GET http://localhost:8081/api/simple-data
    @GetMapping
    public ResponseEntity<List<SimpleData>> getAllSimpleData() {
        List<SimpleData> data = simpleDataService.getAllData(); // 调用 Service 获取数据
        return ResponseEntity.ok(data); // 返回 200 OK 和数据
    }

    // 根据ID获取特定数据
    // GET http://localhost:8081/api/simple-data/{id}
    @GetMapping("/{id}")
    public ResponseEntity<SimpleData> getSimpleDataById(@PathVariable String id) {
        return simpleDataService.getDataById(id) // 调用 Service 获取数据
                .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // 保存新数据
    // POST http://localhost:8081/api/simple-data
    // 请求体: {"name": "新数据", "valueContent": "这是新数据的内容"}
    @PostMapping
    public ResponseEntity<SimpleData> createSimpleData(@RequestBody SimpleData simpleData) {
        SimpleData savedData = simpleDataService.saveData(simpleData);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedData);
    }
    // ... 可以省略其他 PUT 和 DELETE 方法，如果你只关注查询
}
