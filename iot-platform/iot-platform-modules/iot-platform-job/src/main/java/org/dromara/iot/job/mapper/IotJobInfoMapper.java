package org.dromara.iot.job.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.iot.job.domain.IotJobInfo;
import org.dromara.iot.job.domain.vo.IotJobInfoVo;

/**
 * 任务信息 Mapper
 *
 * @author iot team
 */
@Mapper
public interface IotJobInfoMapper extends BaseMapperPlus<IotJobInfo, IotJobInfoVo> {

}
