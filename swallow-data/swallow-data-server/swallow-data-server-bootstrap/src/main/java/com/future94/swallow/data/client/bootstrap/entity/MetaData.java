package com.future94.swallow.data.client.bootstrap.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

/**
 * @author weilai
 */
@Data
@Entity
@Table(name = "swallow_meta_data")
public class MetaData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String appName;

    private String contextPath;

    private String path;

    private String pathDesc;

    private String serviceName;

    private String methodName;

    private String parameterTypes;

    private String rpcExt;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
