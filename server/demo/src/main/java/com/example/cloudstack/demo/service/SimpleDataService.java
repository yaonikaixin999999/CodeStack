package com.example.cloudstack.demo.service;

import com.example.cloudstack.demo.model.SimpleData;
import com.example.cloudstack.demo.model.repository.SimpleDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Service
public class SimpleDataService {

    private static final Logger log = LoggerFactory.getLogger(SimpleDataService.class);

    private final SimpleDataRepository simpleDataRepository;

    @Autowired
    public SimpleDataService(SimpleDataRepository simpleDataRepository) {
        this.simpleDataRepository = simpleDataRepository;
    }

    public List<SimpleData> getAllData() {
        List<SimpleData> data = simpleDataRepository.findAll();
        log.info(">>>> 从数据库获取到所有 SimpleData: {}", data); // **关键：日志打印**
        return data;
    }

    public Optional<SimpleData> getDataById(String id) {
        Optional<SimpleData> data = simpleDataRepository.findById(id);
        data.ifPresent(d -> log.info(">>>> 从数据库获取到 ID 为 {} 的 SimpleData: {}", id, d)); // **关键：日志打印**
        return data;
    }

    public SimpleData saveData(SimpleData simpleData) {
        SimpleData savedData = simpleDataRepository.save(simpleData);
        log.info(">>>> 保存或更新了 SimpleData: {}", savedData); // **关键：日志打印**
        return savedData;
    }

    public void deleteData(String id) {
        simpleDataRepository.deleteById(id);
        log.info(">>>> 删除了 ID 为 {} 的 SimpleData", id); // **关键：日志打印**
    }
}
