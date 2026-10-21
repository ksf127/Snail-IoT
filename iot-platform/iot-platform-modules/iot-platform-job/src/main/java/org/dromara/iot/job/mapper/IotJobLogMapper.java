package org.dromara.iot.job.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.iot.job.domain.IotJobLog;
import org.dromara.iot.job.domain.vo.IotJobLogVo;

/**
 * 任务执行日志 Mapper
 *
 * @author iot team
 */
@Mapper
public interface IotJobLogMapper extends BaseMapperPlus<IotJobLog, IotJobLogVo> {

}
