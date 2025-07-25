package com.zbw.fame.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author by zzzzbw
 * @since 2021/03/15 13:49
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ArticleTag extends BaseEntity {
	/**
	 * 文章id
	 */
	private Integer articleId;
	/**
	 * 标签id
	 */
	private Integer tagId;

	public Integer getArticleId() {
		return articleId;
	}

	public Integer getTagId() {
		return tagId;
	}

	public void setArticleId(Integer articleId) {
		this.articleId = articleId;
	}

	public void setTagId(Integer tagId) {
		this.tagId = tagId;
	}
}
