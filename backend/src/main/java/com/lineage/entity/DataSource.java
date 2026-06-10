package com.lineage.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

/**
 * 数据源实体类
 */
@Data
@Entity
@Table(name = "data_source")
public class DataSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String type; // MySQL, Oracle, PostgreSQL, Hive

    @Column(length = 200)
    private String host;

    private Integer port;

    @Column(name = "`database`", length = 100)
    private String database;

    @Column(length = 100)
    private String username;

    @Column(length = 100)
    private String password;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Integer status = 1; // 1:正常, 0:停用

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(updatable = false)
    private Date createTime;

    @UpdateTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    private Long createBy;
}
