package com.example.cloudstack.demo.model;

import jakarta.persistence.*; // Spring Boot 3+ 使用 jakarta.persistence
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor; // 添加这个以便快速创建对象

@Entity
@Table(name = "test") // 对应数据库表名
@Data // Lombok: 自动生成 Getter, Setter, equals, hashCode, toString
@NoArgsConstructor // Lombok: 自动生成无参构造函数
@AllArgsConstructor // Lombok: 自动生成全参构造函数 (方便初始化)
public class SimpleData {

    @Id // 主键
    private String id;

    @Column(nullable = false) // 字段不可为空
    private String name;

    @Column(columnDefinition = "TEXT") // 存储长文本
    private String valueContent; // 对应你的代码内容或任何文本数据
}
