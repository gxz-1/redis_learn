package com.gxz.service.impl;

import com.gxz.dto.Result;
import com.gxz.entity.SeckillVoucher;
import com.gxz.entity.VoucherOrder;
import com.gxz.mapper.VoucherOrderMapper;
import com.gxz.service.ISeckillVoucherService;
import com.gxz.service.IVoucherOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gxz.utils.RedisIdWorker;
import com.gxz.utils.UserHolder;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {
    @Autowired
    RedisIdWorker redisIdWorker;

    @Autowired
    ISeckillVoucherService seckillVoucherService;

    @Override
    public Result seckillVoucher(Long voucherId) {
        // 1.查询优惠券
        SeckillVoucher voucher = seckillVoucherService.getById(voucherId);
        // 2.判断秒杀是否开始
        if (voucher.getBeginTime().isAfter(LocalDateTime.now())) {
            // 尚未开始
            return Result.fail("秒杀尚未开始！");
        }
        // 3.判断秒杀是否已经结束
        if (voucher.getEndTime().isBefore(LocalDateTime.now())) {
            // 尚未开始
            return Result.fail("秒杀已经结束！");
        }
        // 4.1.判断库存是否充足
        Integer oldstock = voucher.getStock();
        if (voucher.getStock() < 1) {
            // 库存不足
            return Result.fail("库存不足！");
        }
        //4.2判断用户是否已经下过单
        Long userId = UserHolder.getUser().getId();
        synchronized (userId.toString().intern()){
        //TODO:自调用(实际上是目标对象内的方法调用目标对象的另一个方法)在运行时不会导致实际的事务
        //TODO:因此要拿到当前代理对象,并用代理对象调用事务方法
            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
            return proxy.createVoucherOrder(voucherId);
        }
    }

    @Override
    @Transactional
    public synchronized Result createVoucherOrder(Long voucherId) {

        //TODO:对userId加锁，用悲观锁保证同一用户不能同时访问
        //TODO:注意userId.toString()进行了new String，因此需要intern找池中的对象。
//        synchronized (userId.toString().intern()){
            Long userId = UserHolder.getUser().getId();
            int count = query().eq("user_id", userId).eq("voucher_id", voucherId).count();
            // 用户已经购买过了
            if (count > 0) {
                return Result.fail("用户已经购买过一次！");
            }

            //5，扣减库存

    //        //TODO:把库存数量字段stock当作版本号，通过乐观锁保证线程安全，解决超卖问题
    //       //TODO:即当更新时的stock与第4步查询的stock值一致时才成功扣减 where id = #{voucherId} and stock = #{old_stock}
    //        //TODO:但这种方案成功率太低，但是100个人中只有1个人能扣减成功
    //        boolean success = seckillVoucherService.update()
    //                .setSql("stock= stock -1")
    //                .eq("voucher_id", voucherId)
    //                .eq("stock",oldstock)
    //                .update();

            //TODO:因此，改成stock>0,增加乐观锁的成功率 where id = ? and stock > 0
            boolean success = seckillVoucherService.update()
                    .setSql("stock= stock -1")
                    .eq("voucher_id", voucherId).gt("stock",0).update();
            if (!success) {
                //扣减库存
                return Result.fail("库存不足！");
            }
            //6.创建订单
            VoucherOrder voucherOrder = new VoucherOrder();
            // 6.1.订单id
            long orderId = redisIdWorker.nextId("order");
            voucherOrder.setId(orderId);
            // 6.2.用户id
            voucherOrder.setUserId(userId);
            // 6.3.代金券id
            voucherOrder.setVoucherId(voucherId);
            save(voucherOrder);
            return Result.ok(orderId);
//        }
        //TODO:这里先释放锁，再提交事务->在事务提交前，仍有同个用户的线程进入方法的可能
        //TODO:因此要先提交事务再释放锁->即把锁放到这个方法外
    }
}
