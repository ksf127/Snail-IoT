package org.dromara.iot.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.dromara.iot.permission.domain.IotAppDataset;

/**
 * 应用-数据集引用关系 Mapper
 *
 * @author iot team
 */
@Mapper
public interface IotAppDatasetMapper extends BaseMapper<IotAppDataset> {

}
