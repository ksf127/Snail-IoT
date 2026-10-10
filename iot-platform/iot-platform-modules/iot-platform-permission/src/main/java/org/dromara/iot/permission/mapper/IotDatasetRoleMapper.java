package org.dromara.iot.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.iot.permission.domain.IotDatasetRole;
import org.dromara.iot.permission.domain.vo.IotDatasetRoleVo;

import java.util.List;

/**
 * 数据集-角色数据权限 Mapper
 *
 * @author iot team
 */
@Mapper
public interface IotDatasetRoleMapper extends BaseMapper<IotDatasetRole> {

    /**
     * 查询数据集已配置的角色数据权限（含角色名称）
     *
     * @param datasetId 数据集ID
     * @return 角色数据权限列表
     */
    @Select("""
        SELECT dr.dataset_id, dr.role_id, dr.data_scope, r.role_name, r.role_key
        FROM iot_dataset_role dr
        LEFT JOIN sys_role r ON r.role_id = dr.role_id
        WHERE dr.dataset_id = #{datasetId}
        ORDER BY dr.role_id
        """)
    List<IotDatasetRoleVo> selectRoleScopesByDatasetId(@Param("datasetId") Long datasetId);

}
