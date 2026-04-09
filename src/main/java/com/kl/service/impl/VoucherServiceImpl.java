package com.kl.service.impl;

import com.kl.dto.Result;
import com.kl.entity.SeckillVoucher;
import com.kl.entity.Voucher;
import com.kl.repository.VoucherRepository;
import com.kl.service.ISeckillVoucherService;
import com.kl.service.IVoucherService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static com.kl.utils.RedisConstants.SECKILL_STOCK_KEY;


@Service
public class VoucherServiceImpl implements IVoucherService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryVoucherOfShop(Long shopId) {
        // 查询优惠券信息
        List<Object[]> rows = voucherRepository.queryVoucherOfShop(shopId);

        List<Voucher> vouchers = rows.stream().map(row -> {
            Voucher voucher = new Voucher();
            voucher.setId(row[0] == null ? null : ((Number) row[0]).longValue());
            voucher.setShopId(row[1] == null ? null : ((Number) row[1]).longValue());
            voucher.setTitle((String) row[2]);
            voucher.setSubTitle((String) row[3]);
            voucher.setRules((String) row[4]);
            voucher.setPayValue(row[5] == null ? null : ((Number) row[5]).longValue());
            voucher.setActualValue(row[6] == null ? null : ((Number) row[6]).longValue());
            voucher.setType(row[7] == null ? null : ((Number) row[7]).byteValue());
            voucher.setStatus(row[8] == null ? null : ((Number) row[8]).byteValue());
            voucher.setBeginTime(toLocalDateTime(row[12]));
            voucher.setEndTime(toLocalDateTime(row[13]));
            voucher.setStock(row[11] == null ? null : ((Number) row[11]).intValue());
            voucher.setBeginTime(toLocalDateTime(row[12]));
            voucher.setEndTime(toLocalDateTime(row[13]));
            return voucher;
        }).toList();
        // 返回结果
        return Result.ok(vouchers);
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime ldt) {
            return ldt;
        }
        if (value instanceof Timestamp ts) {
            return ts.toLocalDateTime();
        }
        throw new IllegalArgumentException("Unsupported datetime value type: " + value.getClass());
    }

    @Override
    @Transactional
    public void addSeckillVoucher(Voucher voucher) {
        // 保存优惠券
        voucherRepository.save(voucher);
        // 保存秒杀信息
        SeckillVoucher seckillVoucher = new SeckillVoucher();
        seckillVoucher.setVoucherId(voucher.getId());
        seckillVoucher.setStock(voucher.getStock());
        seckillVoucher.setBeginTime(voucher.getBeginTime());
        seckillVoucher.setEndTime(voucher.getEndTime());
        seckillVoucherService.save(seckillVoucher);
        //保存秒杀优惠券到redis中
        stringRedisTemplate.opsForValue().set(SECKILL_STOCK_KEY + voucher.getId(), voucher.getStock().toString());

    }

    @Override
    public void addVoucher(Voucher voucher) {
        voucherRepository.save(voucher);
    }
}
