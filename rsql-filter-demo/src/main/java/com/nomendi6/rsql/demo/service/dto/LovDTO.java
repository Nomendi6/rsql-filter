package com.nomendi6.rsql.demo.service.dto;

public class LovDTO extends rsql.dto.LovDTO {

    public LovDTO() {}

    public LovDTO(Long id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }

    public LovDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
