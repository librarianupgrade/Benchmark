package com.macro.mall.dao;

import com.macro.mall.dto.PmsProductResult;
import org.apache.ibatis.annotations.Param;

/**
 * 自定义商品管理Dao
 * Created by macro on 2018/4/26.
 */
public interface PmsProductDao {
	/**
	 * 获取商品编辑信息
	 */
	PmsProductResult getUpdateInfo(@Param("id") Long id);
}
