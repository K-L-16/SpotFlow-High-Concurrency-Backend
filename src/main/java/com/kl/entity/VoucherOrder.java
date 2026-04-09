package com.kl.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tb_voucher_order")
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class VoucherOrder {

    @Id
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "voucher_id")
    private Long voucherId;

    @Column(name = "pay_type")
//    private Integer payType;
    private Byte payType;

//    private Integer status;
    private Byte status;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "pay_time")
    private LocalDateTime payTime;

    @Column(name = "use_time")
    private LocalDateTime useTime;

    @Column(name = "refund_time")
    private LocalDateTime refundTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;
    @PrePersist
    public void prePersist() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}