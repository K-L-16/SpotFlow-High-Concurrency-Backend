package com.kl.service.impl;


import com.kl.entity.SeckillVoucher;
import com.kl.repository.SeckillVoucherRepository;
import com.kl.service.ISeckillVoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class SeckillVoucherServiceImpl implements ISeckillVoucherService {

    @Autowired
    private SeckillVoucherRepository seckillVoucherRepository;

    @Override
    public void save(SeckillVoucher seckillVoucher) {
        seckillVoucherRepository.save(seckillVoucher);
    }
}
