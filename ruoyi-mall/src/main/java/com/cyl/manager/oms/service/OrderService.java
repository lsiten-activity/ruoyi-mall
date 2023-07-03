package com.cyl.manager.oms.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cyl.h5.pojo.dto.OrderCreateDTO;
import com.cyl.h5.pojo.dto.OrderProductListDTO;
import com.cyl.h5.pojo.vo.OrderCalcVO;
import com.cyl.h5.pojo.vo.SkuViewDTO;
import com.cyl.h5.pojo.vo.form.OrderSubmitForm;
import com.cyl.h5.pojo.vo.query.OrderH5Query;
import com.cyl.manager.oms.convert.OrderConvert;
import com.cyl.manager.oms.domain.OrderDeliveryHistory;
import com.cyl.manager.oms.domain.OrderItem;
import com.cyl.manager.oms.domain.OrderOperateHistory;
import com.cyl.manager.oms.mapper.OrderDeliveryHistoryMapper;
import com.cyl.manager.oms.mapper.OrderItemMapper;
import com.cyl.manager.oms.mapper.OrderOperateHistoryMapper;
import com.cyl.manager.oms.pojo.request.DeliverProductRequest;
import com.cyl.manager.oms.pojo.request.ManagerOrderQueryRequest;
import com.cyl.manager.oms.pojo.vo.*;
import com.cyl.manager.pms.convert.SkuConvert;
import com.cyl.manager.pms.domain.Product;
import com.cyl.manager.pms.domain.Sku;
import com.cyl.manager.pms.mapper.ProductMapper;
import com.cyl.manager.pms.mapper.SkuMapper;
import com.cyl.manager.ums.domain.Member;
import com.cyl.manager.ums.domain.MemberAddress;
import com.cyl.manager.ums.domain.MemberCart;
import com.cyl.manager.ums.mapper.MemberAddressMapper;
import com.cyl.manager.ums.mapper.MemberCartMapper;
import com.cyl.manager.ums.mapper.MemberMapper;
import com.github.pagehelper.PageHelper;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.utils.AesCryptoUtils;
import com.ruoyi.common.utils.IDGenerator;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.framework.config.LocalDataUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.cyl.manager.oms.mapper.OrderMapper;
import com.cyl.manager.oms.domain.Order;
import com.cyl.manager.oms.pojo.query.OrderQuery;
import org.springframework.transaction.annotation.Transactional;

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
    @Autowired
    private SkuConvert skuConvert;
    @Autowired
    private OrderItemService orderItemService;
    @Autowired
    private OrderOperateHistoryMapper orderOperateHistoryMapper;
    @Autowired
    private MemberCartMapper memberCartMapper;
    @Autowired
    private MemberMapper memberMapper;
    @Value("${aes.key}")
    private String aesKey;
    @Autowired
    private OrderDeliveryHistoryMapper orderDeliveryHistoryMapper;

    /**
     * 查询订单表
     *
     * @param id 订单表主键
     * @return 订单表
     */
    public ManagerOrderDetailVO selectById(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null){
            throw new RuntimeException("查不到订单信息");
        }
        ManagerOrderDetailVO managerOrderDetailVO = new ManagerOrderDetailVO();
        //封装订单信息
        managerOrderDetailVO.setOrderId(id);
        managerOrderDetailVO.setOrderSn(order.getOrderSn());
        managerOrderDetailVO.setOrderStatus(order.getStatus());
        managerOrderDetailVO.setCreateTime(order.getCreateTime());
        managerOrderDetailVO.setDeliveryTime(order.getDeliveryTime());
        managerOrderDetailVO.setExpressName(order.getDeliveryCompany());
        managerOrderDetailVO.setExpressNo(order.getDeliverySn());
        managerOrderDetailVO.setPayAmount(order.getPayAmount());
        managerOrderDetailVO.setPayTime(order.getPaymentTime());
        managerOrderDetailVO.setPayType(order.getPayType());
        managerOrderDetailVO.setTotalAmount(order.getTotalAmount());
        managerOrderDetailVO.setPayAmount(order.getPayAmount());
        //封装订单地址信息
        ManagerOrderAddressVo managerOrderAddressVo = new ManagerOrderAddressVo();
        managerOrderAddressVo.setUserPhone(order.getReceiverPhone());
        managerOrderAddressVo.setAddress(order.getReceiverDetailAddress());
        managerOrderAddressVo.setArea(
                order.getReceiverProvince() +
                order.getReceiverCity() +
                order.getReceiverDistrict());
        managerOrderAddressVo.setName(order.getReceiverName());
        managerOrderDetailVO.setAddressInfo(managerOrderAddressVo);
        //查询会员信息
        Member member = memberMapper.selectById(order.getMemberId());
        managerOrderDetailVO.setUserName(member.getNickname());
        managerOrderDetailVO.setUserPhone(member.getPhoneHidden());
        //查询购买商品信息
        QueryWrapper<OrderItem> qw = new QueryWrapper<>();
        qw.eq("order_id", order.getId());
        List<OrderItem> orderItemList = orderItemMapper.selectList(qw);
        List<ManagerOrderProductVO> productList = new ArrayList<>();
        orderItemList.forEach(item -> {
            ManagerOrderProductVO productVO = new ManagerOrderProductVO();
            productVO.setProductId(item.getProductId());
            productVO.setBuyNum(item.getQuantity());
            productVO.setPic(item.getPic());
            productVO.setProductName(item.getProductName());
            productVO.setSalePrice(item.getSalePrice());
            productVO.setSpData(item.getSpData());
            productList.add(productVO);
        });
        managerOrderDetailVO.setProductInfo(productList);
        return managerOrderDetailVO;
    }

    /**
     * 查询订单表列表
     *
     * @param query 查询条件
     * @param page 分页条件
     * @return 订单表
     */
    public PageImpl<ManagerOrderVO> selectList(ManagerOrderQueryRequest query, Pageable page) {
        if (page != null) {
            PageHelper.startPage(page.getPageNumber() + 1, page.getPageSize());
        }
        if (!StringUtils.isEmpty(query.getUserPhone())){
            query.setUserPhone(AesCryptoUtils.encrypt(aesKey, query.getUserPhone()));
        }
        List<ManagerOrderVO> managerOrderVOList = orderMapper.selectManagerOrderPage(query);
        if (CollectionUtil.isEmpty(managerOrderVOList)){
            return new PageImpl<>(managerOrderVOList, page, 0);
        }
        long total = ((com.github.pagehelper.Page) managerOrderVOList).getTotal();
        Map<Long, ManagerOrderVO> orderMap = managerOrderVOList.stream().collect(Collectors.toMap(ManagerOrderVO::getId, it -> it));
        //查orderItem
        QueryWrapper<OrderItem> qw = new QueryWrapper<>();
        qw.in("order_id", orderMap.keySet());
        Map<Long, List<OrderItem>> groupedOrderItemMap = orderItemMapper.selectList(qw)
                .stream().collect(Collectors.groupingBy(OrderItem::getOrderId));
        groupedOrderItemMap.keySet().forEach(key -> {
            ManagerOrderVO managerOrderVO = orderMap.get(key);
            managerOrderVO.setBuyNum(0);
            List<OrderItem> orderItemList = groupedOrderItemMap.get(key);
            List<ManagerOrderProductVO> addProductList = new ArrayList<>();
            orderItemList.forEach(item -> {
                ManagerOrderProductVO vo = new ManagerOrderProductVO();
                vo.setProductName(item.getProductName());
                vo.setSalePrice(item.getSalePrice());
                vo.setPic(item.getPic());
                vo.setBuyNum(item.getQuantity());
                vo.setProductId(item.getProductId());
                vo.setSpData(item.getSpData());
                addProductList.add(vo);
                managerOrderVO.setBuyNum(managerOrderVO.getBuyNum() + item.getQuantity());
            });
            managerOrderVO.setProductList(addProductList);
        });
        return new PageImpl<>(new ArrayList<>(orderMap.values()), page, total);
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

    public Page<OrderVO> queryOrderPage(OrderH5Query query, Pageable pageReq) {
        QueryWrapper<Order> qw = new QueryWrapper<>();
        qw.eq("member_id", SecurityUtils.getUserId());
        IPage<Order> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>();
        page.setCurrent(pageReq.getPageNumber())
                .setSize(pageReq.getPageSize());
        if (CollUtil.isEmpty(pageReq.getSort())) {
            pageReq.getSort().forEach(it -> {
                qw.orderBy(true, it.getDirection().isAscending(), it.getProperty());
            });
        }
        Integer tab = query.getTab();
        if (tab != null) {
            qw.eq("delete_status", 0);
            if (tab == 1) {
                qw.eq("status", 0);
            } else if (tab == 2) {
                qw.eq("status", 1);
                qw.eq("aftersale_status", 1);
            } else if (tab == 3) {
                qw.eq("status", 2);
                qw.eq("confirm_status", 0);
            } else if (tab == 4) {
                qw.eq("status", 2);
                qw.eq("confirm_status", 1);
            }
        }
        orderMapper.selectPage(page, qw);
        List<Order> orders = page.getRecords();
        long total = page.getPages();
        if (CollUtil.isEmpty(orders)) {
            return new PageImpl<>(Collections.emptyList(), pageReq, total);
        }
        LambdaQueryWrapper<OrderItem> qw1 = new LambdaQueryWrapper<>();
        qw1.in(OrderItem::getOrderId, orders.stream().map(Order::getId).collect(Collectors.toList()));
        Map<Long, List<OrderItem>> oid2items = orderItemMapper.selectList(qw1)
                .stream().collect(Collectors.groupingBy(OrderItem::getOrderId));
        List<OrderVO> res = orderConvert.dos2vos(orders);
        res.forEach(it -> {
            it.setItems(oid2items.get(it.getId()));
        });
        return new PageImpl<>(res, pageReq, total);
    }


    public Integer saveMerchantNote(Order order) {
        Order orderInDb = orderMapper.selectById(order.getId());
        if (orderInDb == null){
            throw new RuntimeException("订单不存在");
        }
        UpdateWrapper<Order> qw = new UpdateWrapper<>();
        qw.eq("id", order.getId());
        qw.set("merchant_note", order.getMerchantNote());
        return orderMapper.update(null, qw);
    }

    /**
     * 管理后台发货
     * 目前发货是这样的：待发货、已发货、已完成都能执行发货，每次都会创建一条新的发货记录且修改订单发货信息
     * @param request 发货请求
     * @param userId 操作人
     * @return 结果
     */
    @Transactional
    public String deliverProduct(DeliverProductRequest request, Long userId) {
        //查询订单
        Order order = orderMapper.selectById(request.getOrderId());
        QueryWrapper<OrderItem> qw = new QueryWrapper<>();
        qw.eq("order_id", request.getOrderId());
        OrderItem orderItem = orderItemMapper.selectOne(qw);
        if (order == null || orderItem == null){
            throw new RuntimeException("未找到该订单信息");
        }
        // 是否为待发货、已发货 、已完成
        if (!(Constants.OrderStatus.SEND.equals(order.getStatus())
                || Constants.OrderStatus.GET.equals(order.getStatus())
                || Constants.OrderStatus.CONFIRM.equals(order.getStatus()))){
            throw new RuntimeException("订单状态错误");
        }
        Integer orderStatus =
                Constants.OrderStatus.SEND.equals(order.getStatus()) ? Constants.OrderStatus.GET : order.getStatus();
        //更新订单
        LocalDateTime optDate = LocalDateTime.now();
        UpdateWrapper<Order> orderQw = new UpdateWrapper();
        orderQw.eq("id", order.getId())
                .set("status", orderStatus)
                .set("delivery_company", request.getExpressName())
                .set("delivery_sn", request.getExpressSn())
                .set("update_time", optDate)
                .set("update_by", userId)
                .set("delivery_time", optDate);
        int rows = orderMapper.update(null, orderQw);
        if (rows < 1){
            throw new RuntimeException("更新订单发货信息失败");
        }
        //创建新的发货记录
        this.createDeliveryHistory(request, userId, optDate);
        //创建订单操作记录
        this.createOrderOptHistory(order.getId(), orderStatus, userId, optDate);
        return "发货成功";
    }

    /**
     * 创建发货记录
     * @param request 发货请求
     * @param userId 操作人
     * @param optDate 操作时间
     */
    private void createDeliveryHistory(DeliverProductRequest request, Long userId, LocalDateTime optDate){
        OrderDeliveryHistory orderDeliveryHistory = new OrderDeliveryHistory();
        orderDeliveryHistory.setOrderId(request.getOrderId());
        orderDeliveryHistory.setDeliveryCompany(request.getExpressName());
        orderDeliveryHistory.setDeliverySn(request.getExpressSn());
        orderDeliveryHistory.setCreateTime(optDate);
        orderDeliveryHistory.setCreateBy(userId);
        int rows = orderDeliveryHistoryMapper.insert(orderDeliveryHistory);
        if (rows < 1) {
            throw new RuntimeException("新增订单发货记录失败");
        }
    }

    /**
     * 创建订单操作历史
     * @param orderId 订单id
     * @param orderStatus 订单状态
     * @param userId 操作人
     * @param optDate 操作时间
     */
    private void createOrderOptHistory(Long orderId, Integer orderStatus, Long userId, LocalDateTime optDate){
        OrderOperateHistory optHistory = new OrderOperateHistory();
        optHistory.setOrderId(orderId);
        optHistory.setOperateMan(userId + "");
        optHistory.setOrderStatus(orderStatus);
        optHistory.setCreateTime(optDate);
        optHistory.setCreateBy(userId);
        optHistory.setUpdateBy(userId);
        optHistory.setUpdateTime(optDate);
        int rows = orderOperateHistoryMapper.insert(optHistory);
        if (rows < 1) {
            throw new RuntimeException("新增订单操作记录失败");
        }
    }
}