package com.blinkfox.fenix.core.concrete;

import com.blinkfox.fenix.bean.BuildSource;
import com.blinkfox.fenix.consts.XpathConst;
import com.blinkfox.fenix.core.FenixHandler;
import com.blinkfox.fenix.core.builder.XmlSqlInfoBuilder;
import com.blinkfox.fenix.helper.ParseHelper;
import com.blinkfox.fenix.helper.XmlNodeHelper;
import org.dom4j.Node;

/**
 * 用于生成 'LIKE' 模糊查询的动态 JPQL 或者 SQL 片段的 {@link FenixHandler} 接口的实现类.
 *
 * <p>LIKE 包括：前后模糊，前缀匹配，后缀匹配等.</p>
 * <p>XML 标签示例如：</p>
 * <ul>
 *     <li>{@code <like match="" field="" name="" value="" pattern="" />}</li>
 *     <li>{@code <andLike match="" field="" name="" value="" pattern="" />}</li>
 *     <li>{@code <orLike match="" field="" name="" value="" pattern="" />}</li>
 * </ul>
 * <p>注：</p>
 * <ul>
 *     <li>获取到 match 字段的值，如果没有或者为 true，就生成此 SQL 片段；</li>
 *     <li>pattern 和 value 只能存在一个，且存在的这一个不能为空；</li>
 *     <li>pattern 是用来指定自定义的匹配模式，里面的值是死值，不能被动态解析；</li>
 *     <li>value 生成的 SQL 片段默认是前后模糊的，即：'%abc%'.</li>
 * </ul>
 *
 * @author blinkfox on 2019-08-06.
 * @see NormalHandler
 * @since v1.0.0
 */
public class LikeHandler implements FenixHandler {

	/**
	 * 根据 {@link BuildSource} 参数构建"模糊查询"的 JPQL 或者 SQL 语句片段的信息.
	 *
	 * <p>如果 match 属性为空或者 match 属性中的表达式的值是 true，则生成此 JPQL 或者 SQL 的语句和参数.</p>
	 *
	 * @param source {@link BuildSource} 构建资源参数
	 */
	@Override
	public void buildSqlInfo(BuildSource source) {
		Node node = source.getNode();
		if (ParseHelper.isMatch(XmlNodeHelper.getNodeAttrText(node, XpathConst.ATTR_MATCH), source.getContext())) {
			new XmlSqlInfoBuilder(source).buildLikeSql(XmlNodeHelper.getAndCheckNodeText(node, XpathConst.ATTR_FIELD),
					XmlNodeHelper.getNodeAttrText(node, XpathConst.ATTR_NAME),
					XmlNodeHelper.getNodeAttrText(node, XpathConst.ATTR_VALUE),
					XmlNodeHelper.getNodeAttrText(node, XpathConst.ATTR_PATTERN));
		}
	}

}
