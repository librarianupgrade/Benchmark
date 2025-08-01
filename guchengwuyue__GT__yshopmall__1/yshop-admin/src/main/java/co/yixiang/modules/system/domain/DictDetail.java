/**
* Copyright (C) 2018-2020
* All rights reserved, Designed By www.yixiang.co
* 注意：
* 本软件为www.yixiang.co开发研制
*/
package co.yixiang.modules.system.domain;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

/**
* @author hupeng
* @date 2020-05-14
*/
@Data
@TableName("dict_detail")
public class DictDetail implements Serializable {

	/** 字典详细 */
	@TableId
	private Long id;

	/** 字典标签 */
	private String label;

	/** 字典值 */
	private String value;

	/** 排序 */
	private String sort;

	/** 字典id */
	private Long dictId;

	@TableField(exist = false)
	private Dict dict;

	/** 创建日期 */
	@TableField(fill = FieldFill.INSERT)
	private Timestamp createTime;

	public void copy(DictDetail source) {
		BeanUtil.copyProperties(source, this, CopyOptions.create().setIgnoreNullValue(true));
	}
}
