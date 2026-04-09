package com.kl.repository;

import com.kl.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    @Query(value = """
        SELECT
            v.id,
            v.shop_id,
            v.title,
            v.sub_title,
            v.rules,
            v.pay_value,
            v.actual_value,
            v.type,
            v.status,
            v.create_time,
            v.update_time,
            sv.stock,
            sv.begin_time,
            sv.end_time
        FROM tb_voucher v
        LEFT JOIN tb_seckill_voucher sv ON v.id = sv.voucher_id
        WHERE v.shop_id = :shopId AND v.status = 1
        """, nativeQuery = true)
    List<Object[]> queryVoucherOfShop(@Param("shopId") Long shopId);
}
