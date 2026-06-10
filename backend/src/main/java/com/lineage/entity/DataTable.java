package com.lineage.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

/**
 * 数据表实体类
 */
@Data
@Entity
@Table(name = "data_table")
public class DataTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long sourceId; // 关联数据源ID

    @Column(nullable = false, length = 100)
    private String tableName;

    @Column(length = 500)
    private String tableComment;

    private Integer columnCount; // 列数量

    private Long rowCount; // 行数量

    @Column(length = 50)
    private String tableType; // TABLE, VIEW

    @Column(length = 500)
    private String description;

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(updatable = false)
    private Date createTime;

    @UpdateTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
