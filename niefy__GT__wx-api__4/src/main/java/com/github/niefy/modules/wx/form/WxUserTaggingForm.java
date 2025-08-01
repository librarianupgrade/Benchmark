package com.github.niefy.modules.wx.form;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

@Data
public class WxUserTaggingForm {
	@NotNull(message = "标签ID不得为空")
	private Long tagid;
}
