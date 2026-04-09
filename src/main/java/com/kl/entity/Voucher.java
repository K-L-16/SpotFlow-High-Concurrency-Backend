package com.kl.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tb_voucher")
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id")
    private Long shopId;

    private String title;

    @Column(name = "sub_title")
    private String subTitle;

    private String rules;

    @Column(name = "pay_value")
    private Long payValue;

    @Column(name = "actual_value")
    private Long actualValue;

    private Integer type;

    private Integer status;

    @Transient
    private Integer stock;

    @Transient
    private LocalDateTime beginTime;

    @Transient
    private LocalDateTime endTime;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;
}