/**
* Copyright (C) 2018-2020
* All rights reserved, Designed By www.yixiang.co
* 注意：
* 本软件为www.yixiang.co开发研制
*/
package co.yixiang.modules.shop.domain;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

/**
* @author hupeng
* @date 2020-05-12
*/

@Data
@TableName("yx_material")
public class YxMaterial implements Serializable {

	/** PK */
	@TableId(type = IdType.UUID)
	private String id;

	/** 逻辑删除标记（0：显示；1：隐藏） */
	@TableLogic
	@TableField(fill = FieldFill.INSERT_UPDATE)
	private Boolean delFlag;

	/** 创建时间 */
	@TableField(fill = FieldFill.INSERT)
	private Timestamp createTime;

	/** 创建者ID */
	private String createId;

	/** 类型1、图片；2、视频 */
	private String type;

	/** 分组ID */
	private String groupId;

	/** 素材名 */
	private String name;

	/** 素材链接 */
	private String url;

	public void copy(YxMaterial source) {
		BeanUtil.copyProperties(source, this, CopyOptions.create().setIgnoreNullValue(true));
	}
}
