package org.dromara.iot.cloud.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.iot.cloud.domain.IotFlinkJob;
import org.dromara.iot.cloud.domain.vo.IotFlinkJobVo;

/**
 * Flink 作业 Mapper
 *
 * @author iot team
 */
@Mapper
public interface IotFlinkJobMapper extends BaseMapperPlus<IotFlinkJob, IotFlinkJobVo> {

}
