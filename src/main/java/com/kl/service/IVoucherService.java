package com.kl.service;

import com.kl.dto.Result;
import com.kl.entity.Voucher;


public interface IVoucherService{

    Result queryVoucherOfShop(Long shopId);

    void addSeckillVoucher(Voucher voucher);

    void addVoucher(Voucher voucher);
}
