CREATE TABLE IF NOT EXISTS `swallow_meta_data` (
     `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
     `app_name` varchar(255) NOT NULL DEFAULT '' COMMENT '应用名称',
     `context_path` varchar(255) DEFAULT NULL COMMENT '请求uri前缀路径',
     `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
     `method_name` varchar(255) NOT NULL DEFAULT '' COMMENT '方法名称',
     `parameter_types` varchar(255) DEFAULT NULL COMMENT '参数类型',
     `path` varchar(255) NOT NULL DEFAULT '' COMMENT 'URI路径',
     `path_desc` varchar(255) NOT NULL DEFAULT '' COMMENT 'URI路径描述',
     `rpc_ext` varchar(255) NOT NULL DEFAULT '' COMMENT 'DUBBO扩展信息',
     `service_name` varchar(255) NOT NULL DEFAULT '' COMMENT 'DUBBO服务名称',
     `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
     PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='dubbo的metadata信息';