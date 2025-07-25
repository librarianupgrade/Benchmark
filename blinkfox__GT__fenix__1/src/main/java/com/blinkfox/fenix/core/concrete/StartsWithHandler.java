package com.blinkfox.fenix.core.concrete;

import com.blinkfox.fenix.bean.BuildSource;
import com.blinkfox.fenix.consts.Const;
import com.blinkfox.fenix.consts.LikeTypeEnum;
import com.blinkfox.fenix.core.FenixHandler;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

/**
 * 用于生成 'LIKE' 前缀匹配查询的动态 JPQL 或者 SQL 片段的 {@link FenixHandler} 接口的实现类，
 * 该类是 {@link LikeHandler} 的子类.
 *
 * <p>XML 标签示例如：</p>
 * <ul>
 *     <li>{@code <startsWith match="" field="" name="" value="" />}</li>
 *     <li>{@code <andStartsWith match="" field="" name="" value="" />}</li>
 *     <li>{@code <orStartsWith match="" field="" name="" value="" />}</li>
 * </ul>
 * <p>注：</p>
 * <ul>
 *     <li>获取到 match 字段的值，如果为空或者为 true，就生成此 SQL 片段；</li>
 *     <li>field 和 value 的值必填，match 和 name 的值非必填；</li>
 *     <li>生成的 SQL 片段默认是按前缀来匹配的，即：'abc%'.</li>
 * </ul>
 *
 * @author blinkfox on 2019-08-06.
 * @see LikeHandler
 * @see EndsWithHandler
 * @since v1.0.0
 */
public class StartsWithHandler extends LikeHandler {

	/**
	 * 用于后续生成 LIKE 前缀匹配 SQL 片段的额外参数 Map.
	 */
	@Getter
	private static final Map<String, Object> startMap = new HashMap<>(2);

	static {
		startMap.put(Const.TYPE, LikeTypeEnum.STARTS_WITH);
	}

	/**
	 * 重写了 {@link LikeHandler#buildSqlInfo(BuildSource)} 中的方法，
	 * 在 {@link BuildSource} 变量中设置一个 map 参数，用来标记是前缀匹配的情况，便于后续的获取、判断和处理.
	 *
	 * @param source {@link BuildSource} 构建资源参数
	 */
	@Override
	public void buildSqlInfo(BuildSource source) {
		source.setOthers(startMap);
		super.buildSqlInfo(source);
	}

}
