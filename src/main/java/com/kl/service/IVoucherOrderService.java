package com.kl.service;

import com.kl.dto.Result;
import com.kl.entity.VoucherOrder;


public interface IVoucherOrderService{

    Result seckillVoucher(Long voucherId);

    void createVoucherOrder(VoucherOrder voucherOrder);
}
