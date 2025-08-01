package com.nomendi6.rsql.it.service.dto;

import com.nomendi6.rsql.it.domain.AppObjectType;
import com.nomendi6.rsql.it.domain.StandardRecordStatus;

import java.io.Serializable;
import java.time.Instant;

public class AppObjectDTO implements Serializable {

    private Long id;
    private String code;
    private String name;
    private AppObjectType appObjectType;
    private Instant createdDate;
    private Instant lastModifiedDate;
    private StandardRecordStatus status;
    private Long parentId;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AppObjectType getAppObjectType() {
        return appObjectType;
    }

    public void setAppObjectType(AppObjectType appObjectType) {
        this.appObjectType = appObjectType;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public StandardRecordStatus getStatus() {
        return status;
    }

    public void setStatus(StandardRecordStatus status) {
        this.status = status;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
}