package com.lineage.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

/**
 * 数据列实体类
 */
@Data
@Entity
@Table(name = "data_column")
public class DataColumn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tableId; // 关联表ID

    @Column(nullable = false, length = 100)
    private String columnName;

    @Column(length = 50)
    private String dataType;

    private Integer columnLength;

    @Column(length = 500)
    private String columnComment;

    @Column(nullable = false)
    private Boolean isPrimaryKey = false;

    @Column(nullable = false)
    private Boolean isNullable = true;

    @Column(length = 200)
    private String defaultValue;

    private Integer columnOrder; // 列顺序

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(updatable = false)
    private Date createTime;
}
