package com.kl.repository;

import com.kl.entity.VoucherOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoucherOrderRepository extends JpaRepository<VoucherOrder, Long> {
    int countByUserIdAndVoucherId(Long userId, Long voucherId);
}
