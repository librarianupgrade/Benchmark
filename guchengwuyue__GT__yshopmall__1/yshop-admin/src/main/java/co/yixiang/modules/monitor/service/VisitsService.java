package co.yixiang.modules.monitor.service;

import co.yixiang.common.service.BaseService;
import co.yixiang.modules.monitor.domain.Visits;
import org.springframework.scheduling.annotation.Async;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Zheng Jie
 * @date 2018-12-13
 */
public interface VisitsService extends BaseService<Visits> {

	/**
	 * 提供给定时任务，每天0点执行
	 */
	void save();

	/**
	 * 新增记录
	 * @param request /
	 */
	@Async
	void count(HttpServletRequest request);

	/**
	 * 获取数据
	 * @return /
	 */
	Object get();

	/**
	 * getChartData
	 * @return /
	 */
	Object getChartData();
}
