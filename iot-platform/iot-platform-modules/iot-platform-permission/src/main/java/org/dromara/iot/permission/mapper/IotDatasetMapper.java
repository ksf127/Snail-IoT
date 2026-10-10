package org.dromara.iot.permission.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.iot.permission.domain.IotDataset;
import org.dromara.iot.permission.domain.vo.IotDatasetVo;

/**
 * 数据集 Mapper
 *
 * @author iot team
 */
@Mapper
public interface IotDatasetMapper extends BaseMapperPlus<IotDataset, IotDatasetVo> {

}
