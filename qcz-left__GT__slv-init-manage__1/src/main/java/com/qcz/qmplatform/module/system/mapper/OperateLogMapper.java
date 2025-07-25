package com.qcz.qmplatform.module.system.mapper;

import com.qcz.qmplatform.module.system.domain.OperateLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qcz.qmplatform.module.system.vo.OperateLogTimeVO;
import com.qcz.qmplatform.module.system.vo.OperateLogVO;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author quchangzhong
 * @since 2020-12-06
 */
public interface OperateLogMapper extends BaseMapper<OperateLog> {
	List<OperateLogVO> queryOperateLogList(OperateLogTimeVO operateLog);
}
