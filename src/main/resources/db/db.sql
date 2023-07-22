DROP DATABASE IF EXISTS dmybatis05;
CREATE DATABASE IF NOT EXISTS dmybatis05;

USE dmybatis05;

CREATE TABLE `user`
(
    `id` CHAR(32) PRIMARY KEY COMMENT '主键 ID',
    `username` VARCHAR(32) NOT NULL COMMENT '用户名',
    `password` VARCHAR(16) NOT NULL COMMENT '密码',
    `create_date_time` DATETIME NOT NULL COMMENT '创建时间',
    `update_date_time` DATETIME NOT NULL COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除字段，0 表示未删除'
) COMMENT '用户表';