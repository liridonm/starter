package com.boilerplate.starter.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity<T> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private T id;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    /**
     * Model create date.
     */
    @JsonIgnore
    @Column(name = "create_date", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    /**
     * Model update date.
     */
    @JsonIgnore
    @Column(name = "updated_date", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedDate;

    /**
     * Model create user Id.
     */
    @JsonIgnore
    @Column(name = "user_created", nullable = false,  updatable = false)
    private Integer createUserId;

    /**
     * Model create user Id.
     */
    @JsonIgnore
    @Column(name = "user_updated", nullable = false)
    private Integer updateUserId;

    @PrePersist
    private void onPrePersist() {
        this.createdDate = new Date();
        this.updatedDate = new Date();
        //TODO: need to dynamic.
        this.createUserId = 1;
        this.updateUserId = 1;
    }

    @PreUpdate
    private void onPreUpdate() {
        this.updatedDate = new Date();
        //TODO: need to dynamic.
        this.updateUserId = 1;
    }
}

