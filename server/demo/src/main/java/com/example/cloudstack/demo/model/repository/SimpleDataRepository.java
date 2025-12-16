package com.example.cloudstack.demo.model.repository;

import com.example.cloudstack.demo.model.SimpleData; // 导入你的实体类
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SimpleDataRepository extends JpaRepository<SimpleData, String> {
    // JpaRepository 提供了基本的 CRUD 方法 (save, findById, findAll, delete 等)
    // 你可以添加自定义查询方法，例如：
    List<SimpleData> findByNameContainingIgnoreCase(String name);
}