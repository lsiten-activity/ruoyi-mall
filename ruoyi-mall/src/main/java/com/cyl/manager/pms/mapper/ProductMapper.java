package com.cyl.manager.pms.mapper;

import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyl.manager.pms.domain.Product;

/**
 * 商品信息Mapper接口
 * 
 * @author zcc
 */
public interface ProductMapper extends BaseMapper<Product> {
    /**
     * 查询商品信息列表
     *
     * @param product 商品信息
     * @return 商品信息集合
     */
    List<Product> selectByEntity(Product product);
}
