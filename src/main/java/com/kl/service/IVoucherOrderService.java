package com.kl.service;

import com.kl.dto.Result;
import com.kl.entity.VoucherOrder;
import com.kl.utils.VoucherOrderProcessResult;


public interface IVoucherOrderService{

    Result seckillVoucher(Long voucherId);

    VoucherOrderProcessResult createVoucherOrder(VoucherOrder voucherOrder);
}
