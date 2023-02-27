package com.cyl.oms.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cyl.h5.pojo.vo.form.OrderSubmitForm;
import com.cyl.oms.convert.OrderConvert;
import com.cyl.oms.domain.OrderItem;
import com.cyl.oms.mapper.OrderItemMapper;
import com.cyl.oms.pojo.vo.OrderVO;
import com.cyl.pms.domain.Product;
import com.cyl.pms.domain.Sku;
import com.cyl.pms.mapper.ProductMapper;
import com.cyl.pms.mapper.SkuMapper;
import com.cyl.ums.domain.MemberAddress;
import com.cyl.ums.mapper.MemberAddressMapper;
import com.github.pagehelper.PageHelper;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.base.BaseException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import com.cyl.oms.mapper.OrderMapper;
import com.cyl.oms.domain.Order;
import com.cyl.oms.pojo.query.OrderQuery;

/**
 * 订单表Service业务层处理
 *
 *
 * @author zcc
 */
@Service
public class OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderConvert orderConvert;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private MemberAddressMapper memberAddressMapper;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private ProductMapper productMapper;

    /**
     * 查询订单表
     *
     * @param id 订单表主键
     * @return 订单表
     */
    public Order selectById(Long id) {
        return orderMapper.selectById(id);
    }

    /**
     * 查询订单表列表
     *
     * @param query 查询条件
     * @param page 分页条件
     * @return 订单表
     */
    public List<Order> selectList(OrderQuery query, Pageable page) {
        if (page != null) {
            PageHelper.startPage(page.getPageNumber() + 1, page.getPageSize());
        }
        QueryWrapper<Order> qw = new QueryWrapper<>();
        Long memberId = query.getMemberId();
        if (memberId != null) {
            qw.eq("member_id", memberId);
        }
        String memberUsernameLike = query.getMemberUsernameLike();
        if (!StringUtils.isEmpty(memberUsernameLike)) {
            qw.like("member_username", memberUsernameLike);
        }
        BigDecimal totalAmount = query.getTotalAmount();
        if (totalAmount != null) {
            qw.eq("total_amount", totalAmount);
        }
        BigDecimal purchasePrice = query.getPurchasePrice();
        if (purchasePrice != null) {
            qw.eq("purchase_price", purchasePrice);
        }
        BigDecimal payAmount = query.getPayAmount();
        if (payAmount != null) {
            qw.eq("pay_amount", payAmount);
        }
        BigDecimal freightAmount = query.getFreightAmount();
        if (freightAmount != null) {
            qw.eq("freight_amount", freightAmount);
        }
        Integer payType = query.getPayType();
        if (payType != null) {
            qw.eq("pay_type", payType);
        }
        Integer status = query.getStatus();
        if (status != null) {
            qw.eq("status", status);
        }
        Integer aftersaleStatus = query.getAftersaleStatus();
        if (aftersaleStatus != null) {
            qw.eq("aftersale_status", aftersaleStatus);
        }
        String deliveryCompany = query.getDeliveryCompany();
        if (!StringUtils.isEmpty(deliveryCompany)) {
            qw.eq("delivery_company", deliveryCompany);
        }
        String deliverySn = query.getDeliverySn();
        if (!StringUtils.isEmpty(deliverySn)) {
            qw.eq("delivery_sn", deliverySn);
        }
        Integer autoConfirmDay = query.getAutoConfirmDay();
        if (autoConfirmDay != null) {
            qw.eq("auto_confirm_day", autoConfirmDay);
        }
        String receiverNameLike = query.getReceiverNameLike();
        if (!StringUtils.isEmpty(receiverNameLike)) {
            qw.like("receiver_name", receiverNameLike);
        }
        String receiverPhone = query.getReceiverPhone();
        if (!StringUtils.isEmpty(receiverPhone)) {
            qw.eq("receiver_phone", receiverPhone);
        }
        String receiverPostCode = query.getReceiverPostCode();
        if (!StringUtils.isEmpty(receiverPostCode)) {
            qw.eq("receiver_post_code", receiverPostCode);
        }
        String receiverProvince = query.getReceiverProvince();
        if (!StringUtils.isEmpty(receiverProvince)) {
            qw.eq("receiver_province", receiverProvince);
        }
        String receiverCity = query.getReceiverCity();
        if (!StringUtils.isEmpty(receiverCity)) {
            qw.eq("receiver_city", receiverCity);
        }
        String receiverDistrict = query.getReceiverDistrict();
        if (!StringUtils.isEmpty(receiverDistrict)) {
            qw.eq("receiver_district", receiverDistrict);
        }
        Long receiverProvinceId = query.getReceiverProvinceId();
        if (receiverProvinceId != null) {
            qw.eq("receiver_province_id", receiverProvinceId);
        }
        Long receiverCityId = query.getReceiverCityId();
        if (receiverCityId != null) {
            qw.eq("receiver_city_id", receiverCityId);
        }
        Long receiverDistrictId = query.getReceiverDistrictId();
        if (receiverDistrictId != null) {
            qw.eq("receiver_district_id", receiverDistrictId);
        }
        String receiverDetailAddress = query.getReceiverDetailAddress();
        if (!StringUtils.isEmpty(receiverDetailAddress)) {
            qw.eq("receiver_detail_address", receiverDetailAddress);
        }
        String note = query.getNote();
        if (!StringUtils.isEmpty(note)) {
            qw.eq("note", note);
        }
        Integer confirmStatus = query.getConfirmStatus();
        if (confirmStatus != null) {
            qw.eq("confirm_status", confirmStatus);
        }
        Integer deleteStatus = query.getDeleteStatus();
        if (deleteStatus != null) {
            qw.eq("delete_status", deleteStatus);
        }
        LocalDateTime paymentTime = query.getPaymentTime();
        if (paymentTime != null) {
            qw.eq("payment_time", paymentTime);
        }
        LocalDateTime deliveryTime = query.getDeliveryTime();
        if (deliveryTime != null) {
            qw.eq("delivery_time", deliveryTime);
        }
        LocalDateTime receiveTime = query.getReceiveTime();
        if (receiveTime != null) {
            qw.eq("receive_time", receiveTime);
        }
        return orderMapper.selectList(qw);
    }

    /**
     * 新增订单表
     *
     * @param order 订单表
     * @return 结果
     */
    public int insert(Order order) {
        order.setCreateTime(LocalDateTime.now());
        return orderMapper.insert(order);
    }

    /**
     * 修改订单表
     *
     * @param order 订单表
     * @return 结果
     */
    public int update(Order order) {
        return orderMapper.updateById(order);
    }

    /**
     * 删除订单表信息
     *
     * @param id 订单表主键
     * @return 结果
     */
    public int deleteById(Long id) {
        return orderMapper.deleteById(id);
    }

    public OrderVO submit(OrderSubmitForm form) {
        MemberAddress addr = memberAddressMapper.selectById(form.getAddressId());
        if (addr == null) {
            throw new BaseException("参数错误");
        }
        List<Long> skuIds = form.getSkus().stream().map(OrderSubmitForm.SkuParam::getSkuId).distinct().collect(Collectors.toList());
        List<Sku> skus = skuMapper.selectBatchIds(skuIds);
        if (skuIds.size() != skus.size()) {
            throw new BaseException("参数错误");
        }
        Map<Long, Sku> id2sku = skus.stream().collect(Collectors.toMap(Sku::getId, it -> it));

        Map<Long, Product> id2Product = productMapper.selectBatchIds(
                skus.stream().map(Sku::getProductId).distinct().collect(Collectors.toList())
        ).stream().collect(Collectors.toMap(Product::getId, it -> it));

        // 1. 生成订单商品快照
        Order order = new Order();
        order.setTotalAmount(BigDecimal.ZERO);
        List<OrderItem> items = new ArrayList<>();
        form.getSkus().forEach(it -> {
            Sku s = id2sku.get(it.getSkuId());
            Product p = id2Product.get(s.getProductId());

            OrderItem item = new OrderItem();
            item.setProductId(s.getProductId());
            item.setOutProductId(p.getOutProductId());
            item.setSkuId(it.getSkuId());
            item.setOutSkuId(s.getOutSkuId());
            item.setProductSnapshotId(p.getId());
            item.setSkuSnapshotId(s.getId());
            item.setPic(StrUtil.isNotEmpty(s.getPic()) ? s.getPic() : p.getPic());
            item.setProductName(p.getName());
            item.setSalePrice(s.getPrice());
            item.setQuantity(it.getQuantity());
            item.setProductCategoryId(p.getCategoryId());
            item.setSpData(s.getSpData());
            items.add(item);
            order.setTotalAmount(order.getTotalAmount().add(s.getPrice().multiply(BigDecimal.valueOf(it.getQuantity()))));
        });
        LoginUser user = SecurityUtils.getLoginUser();

        // 2. 生成订单
        order.setMemberId(user.getUserId());
        order.setMemberUsername(user.getUsername());
        order.setStatus(0);
        order.setAftersaleStatus(1);
        order.setAutoConfirmDay(7);
        order.setReceiverName(addr.getName());
        order.setReceiverPhone(addr.getPhone());
        order.setReceiverProvince(addr.getProvince());
        order.setReceiverDistrict(addr.getDistrict());
        order.setReceiverDetailAddress(addr.getDetailAddress());
        order.setNote(form.getNote());
        order.setConfirmStatus(0);
        order.setDeleteStatus(0);
        orderMapper.insert(order);
        items.forEach(it -> it.setOrderId(order.getId()));
        items.forEach(orderItemMapper::insert);

        // 3. 判断是否付费，生成支付订单
        if (BigDecimal.ZERO.compareTo(order.getTotalAmount()) > 0) {
            // todo 生成支付订单
        }
        OrderVO vo = orderConvert.do2vo(order);
        vo.setItems(items);
        return vo;
    }
}
