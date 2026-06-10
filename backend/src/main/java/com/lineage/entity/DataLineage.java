package com.lineage.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

/**
 * 数据血缘关系实体类
 */
@Data
@Entity
@Table(name = "data_lineage")
public class DataLineage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long sourceTableId; // 源表ID

    @Column(nullable = false)
    private Long targetTableId; // 目标表ID

    private Long sourceColumnId; // 源列ID（可选）

    private Long targetColumnId; // 目标列ID（可选）

    @Column(length = 50)
    private String lineageType; // 血缘类型: TABLE(表级), COLUMN(列级)

    @Column(length = 1000)
    private String transformRule; // 转换规则

    @Column(length = 500)
    private String description;

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(updatable = false)
    private Date createTime;

    private Long createBy;
}
