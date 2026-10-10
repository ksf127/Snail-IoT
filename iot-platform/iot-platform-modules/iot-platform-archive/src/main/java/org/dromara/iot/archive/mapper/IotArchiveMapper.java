package org.dromara.iot.archive.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.iot.archive.domain.IotArchive;
import org.dromara.iot.archive.domain.vo.IotArchiveVo;

/**
 * 档案 Mapper
 *
 * @author iot team
 */
@Mapper
public interface IotArchiveMapper extends BaseMapperPlus<IotArchive, IotArchiveVo> {

}
