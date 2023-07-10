package com.cyl.h5.controller;

import com.cyl.h5.pojo.dto.OrderCreateDTO;
import com.cyl.h5.pojo.vo.CountOrderVO;
import com.cyl.h5.pojo.vo.H5OrderVO;
import com.cyl.h5.pojo.vo.OrderCalcVO;
import com.cyl.h5.pojo.vo.form.OrderSubmitForm;
import com.cyl.h5.service.H5OrderService;
import com.cyl.manager.ums.domain.Member;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.redis.RedisService;
import com.ruoyi.framework.config.LocalDataUtil;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/h5/order")
@Slf4j
public class H5OrderController {
    @Autowired
    private RedisService redisService;
    @Autowired
    private H5OrderService service;

    @ApiOperation("下单")
    @PostMapping("/add")
    public ResponseEntity<Long> submit(@RequestBody OrderSubmitForm form) {
        Member member = (Member) LocalDataUtil.getVar(Constants.MEMBER_INFO);
        Long memberId = member.getId();
        String redisKey = "h5_order_add" + memberId;
        String redisValue = memberId + "_" + System.currentTimeMillis();
        try{
            redisService.lock(redisKey, redisValue, 60);
            return ResponseEntity.ok(service.submit(form));
        }catch (Exception e){
            log.info("创建订单方法异常", e);
            throw new RuntimeException("服务繁忙，稍后再试");
        }finally {
            try {
                redisService.unLock(redisKey, redisValue);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
//    @PostMapping("orders")
//    public ResponseEntity<Page<OrderVO>> queryOrderPage(@RequestBody OrderH5Query query, Pageable pageReq) {
//        return ResponseEntity.ok(service.queryOrderPage(query, pageReq));
//    }

    @ApiOperation("下单前校验")
    @PostMapping("/addOrderCheck")
    public ResponseEntity<OrderCalcVO> addOrderCheck(@RequestBody OrderCreateDTO orderCreateDTO){
        return ResponseEntity.ok(service.addOrderCheck(orderCreateDTO));
    }

    @ApiOperation("订单列表")
    @GetMapping("/page")
    public ResponseEntity<PageImpl<H5OrderVO>> orderPage(Integer status, Pageable pageable){
        Member member = (Member) LocalDataUtil.getVar(Constants.MEMBER_INFO);
        return ResponseEntity.ok(service.orderPage(status, member.getId(), pageable));
    }

    @ApiOperation("订单详情")
    @GetMapping("/orderDetail")
    public ResponseEntity<H5OrderVO> orderDetail(@RequestParam(required = false) Long orderId){
        if (orderId == null){
            throw new RuntimeException("系统繁忙");
        }
        return ResponseEntity.ok(service.orderDetail(orderId));
    }

    @ApiOperation("确认收货")
    @GetMapping("/orderComplete")
    public ResponseEntity<String> orderComplete(Long orderId) {
        log.info("确认收货，订单id："+orderId);
        String redisKey = "h5_oms_order_complete_"+orderId;
        String redisValue = orderId+"_"+System.currentTimeMillis();
        try{
            redisService.lock(redisKey,redisValue,60);
            return ResponseEntity.ok(service.orderComplete(orderId));
        }catch (Exception e){
            log.error("确认收货异常",e);
            throw new RuntimeException("确认收货失败");
        }finally {
            try{
                redisService.unLock(redisKey,redisValue);
            }catch (Exception e){
                log.error("",e);
            }
        }
    }

    @ApiOperation("订单数量统计")
    @GetMapping("/countOrder")
    public ResponseEntity<CountOrderVO> orderNumCount(){
        Member member = (Member) LocalDataUtil.getVar(Constants.MEMBER_INFO);
        Long memberId = member.getId();
        return ResponseEntity.ok(service.orderNumCount(memberId));
    }
}
