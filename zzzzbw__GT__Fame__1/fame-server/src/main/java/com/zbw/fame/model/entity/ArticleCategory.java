package com.zbw.fame.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author by zzzzbw
 * @since 2021/03/19 11:06
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ArticleCategory extends BaseEntity {
	/**
	 * 文章id
	 */
	private Integer articleId;
	/**
	 * 分类id
	 */
	private Integer categoryId;

	public Integer getArticleId() {
		return articleId;
	}

	public Integer getCategoryId() {
		return categoryId;
	}

	public void setArticleId(Integer articleId) {
		this.articleId = articleId;
	}

	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}
}
