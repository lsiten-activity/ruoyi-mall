package com.cyl.manager.oms.mapper;

import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyl.manager.oms.domain.Order;
import com.cyl.manager.oms.pojo.request.ManagerOrderQueryRequest;
import com.cyl.manager.oms.pojo.vo.ManagerOrderVO;

/**
 * 订单表Mapper接口
 * 
 * @author zcc
 */
public interface OrderMapper extends BaseMapper<Order> {
    /**
     * 查询订单表列表
     *
     * @param order 订单表
     * @return 订单表集合
     */
    List<Order> selectByEntity(Order order);

    List<ManagerOrderVO> selectManagerOrderPage(ManagerOrderQueryRequest request);
}