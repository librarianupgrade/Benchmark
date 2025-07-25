package com.blinkfox.fenix.repository.transformer;

import com.blinkfox.fenix.FenixTestApplication;
import com.blinkfox.fenix.config.FenixConfigManager;
import com.blinkfox.fenix.entity.transformer.UnderscoreEntity;
import com.blinkfox.fenix.vo.transformer.UnderscoreColumnVo;
import com.blinkfox.fenix.vo.transformer.UnderscoreVo;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * UnderscoreEntityRepository 的单元测试类.
 *
 * @author blinkfox on 2022-03-26.
 * @since v2.7.0
 */
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = FenixTestApplication.class)
public class UnderscoreEntityRepositoryTest {

	private static final String COLUMN_VALUE = "column-name-value, index is:";

	@Resource
	private UnderscoreEntityRepository underscoreEntityRepository;

	/**
	 * 是否加载过的标识.
	 */
	@Setter
	private static Boolean isLoad = false;

	private static final int COUNT = 5;

	/**
	 * 初始化.
	 */
	@PostConstruct
	public void init() {
		if (!isLoad) {
			// 初始化加载 Fenix 配置.
			FenixConfigManager.getInstance().initLoad();

			// 构造实体集合.
			List<UnderscoreEntity> underscoreEntities = new ArrayList<>();
			for (int i = 1; i <= COUNT; ++i) {
				underscoreEntities
						.add(new UnderscoreEntity().setColumnName(COLUMN_VALUE + i).setColumnLongName((long) i)
								.setColumnThreeName("This is column_three_name value, index is:" + i)
								.setColumnFourTestName("这是 column_four_test_name 的值, index is:" + i)
								.setColumnCreateTime(new Date()).setColumnLastUpdateTime(LocalDateTime.now()));
			}
			this.underscoreEntityRepository.saveAll(underscoreEntities);
			isLoad = true;
		}

		// 断言结果是否正确.
		Assert.assertEquals(COUNT, this.underscoreEntityRepository.count());
	}

	@Test
	public void queryFenixResultType() {
		int num = 2;
		List<UnderscoreVo> underscoreVoList = this.underscoreEntityRepository.queryFenixResultType(num);
		Assert.assertEquals(COUNT - num, underscoreVoList.size());
		for (UnderscoreVo underscoreVo : underscoreVoList) {
			Assert.assertNotNull(underscoreVo.getId());
			Assert.assertNotNull(underscoreVo.getColumnName());
			Assert.assertTrue(underscoreVo.getColumnName().startsWith(COLUMN_VALUE));
			Assert.assertNotNull(underscoreVo.getColumnLongName());
			Assert.assertTrue(underscoreVo.getColumnLongName() > num);
			Assert.assertNotNull(underscoreVo.getColumnThreeName());
			Assert.assertNotNull(underscoreVo.getColumnFourTestName());
			Assert.assertNotNull(underscoreVo.getColumnCreateTime());
			Assert.assertNotNull(underscoreVo.getColumnLastUpdateTime());
		}
	}

	@Test
	public void queryAtColumnVoList() {
		int num = 3;
		List<UnderscoreColumnVo> underscoreColumnVoList = this.underscoreEntityRepository.queryAtColumnVoList(num);
		Assert.assertEquals(COUNT - num, underscoreColumnVoList.size());
		for (UnderscoreColumnVo underscoreColumnVo : underscoreColumnVoList) {
			Assert.assertNotNull(underscoreColumnVo.getId());
			Assert.assertNotNull(underscoreColumnVo.getColumnName());
			Assert.assertTrue(underscoreColumnVo.getColumnName().startsWith(COLUMN_VALUE));
			Assert.assertNotNull(underscoreColumnVo.getColumnLongName());
			Assert.assertTrue(underscoreColumnVo.getColumnLongName() > num);
			Assert.assertNotNull(underscoreColumnVo.getColumnFourTestName());
			Assert.assertNotNull(underscoreColumnVo.getLastUpdateTime());
			Assert.assertNull(underscoreColumnVo.getCreateTime());
		}
	}

	/**
	 * 销毁.
	 */
	@PreDestroy
	public void destroy() {
		FenixConfigManager.getInstance().clear();
	}

}
