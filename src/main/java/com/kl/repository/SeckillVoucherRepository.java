package com.kl.repository;

import com.kl.entity.SeckillVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SeckillVoucherRepository extends JpaRepository<SeckillVoucher, Long> {
    @Modifying
    @Query("""
        update SeckillVoucher sv
        set sv.stock = sv.stock - 1
        where sv.voucherId = :voucherId and sv.stock > 0
        """)
    int deductStock(@Param("voucherId") Long voucherId);
}
