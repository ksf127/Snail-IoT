package org.dromara.iot.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.dromara.iot.permission.domain.IotAppRole;

/**
 * 应用-角色授权关系 Mapper
 *
 * @author iot team
 */
@Mapper
public interface IotAppRoleMapper extends BaseMapper<IotAppRole> {

}
