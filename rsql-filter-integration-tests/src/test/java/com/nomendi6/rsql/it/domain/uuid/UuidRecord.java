package com.nomendi6.rsql.it.domain.uuid;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

/** A UUID behind an AttributeConverter on a VARCHAR column, and a UUID mapped natively. */
@Entity
@Table(name = "uuid_record")
public class UuidRecord {

    @Id
    private Long id;

    @Column(name = "name")
    private String name;

    @Convert(converter = UuidToStringConverter.class)
    @Column(name = "converted", length = 36)
    private UUID converted;

    @Column(name = "native_uuid")
    private UUID nativeUuid;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private UuidRecord parent;

    public UuidRecord() {}

    public UuidRecord(Long id, String name, UUID uuid) {
        this.id = id;
        this.name = name;
        this.converted = uuid;
        this.nativeUuid = uuid;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public UUID getConverted() { return converted; }
    public UUID getNativeUuid() { return nativeUuid; }
    public UuidRecord getParent() { return parent; }
    public void setParent(UuidRecord parent) { this.parent = parent; }
}
