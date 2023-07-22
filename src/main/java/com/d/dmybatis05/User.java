package com.d.dmybatis05;

import com.d.dmybatis05.annotation.TableField;
import org.junit.Test;

import java.time.LocalDateTime;

/**
 * @description:
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-05-19 22:29:22
 * @modify:
 */

public class User {

    @TableField("id")
    private String id;

    @TableField("username")
    private String username;

    @TableField("password")
    private String password;

    @TableField("create_date_time")
    private LocalDateTime createDateTime;

    @TableField("update_date_time")
    private LocalDateTime updateDateTime;

    @TableField("deleted")
    private Integer deleted;

    public User() {
    }

    public User(String id, String username, String password, LocalDateTime createDateTime, LocalDateTime updateDateTime,
                Integer deleted) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.createDateTime = createDateTime;
        this.updateDateTime = updateDateTime;
        this.deleted = deleted;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(LocalDateTime createDateTime) {
        this.createDateTime = createDateTime;
    }

    public LocalDateTime getUpdateDateTime() {
        return updateDateTime;
    }

    public void setUpdateDateTime(LocalDateTime updateDateTime) {
        this.updateDateTime = updateDateTime;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    @Override
    public String toString() {
        return "User{" +
               "id='" + id + '\'' +
               ", username='" + username + '\'' +
               ", password='" + password + '\'' +
               ", createDateTime=" + createDateTime +
               ", updateDateTime=" + updateDateTime +
               ", deleted=" + deleted +
               '}';
    }
}
