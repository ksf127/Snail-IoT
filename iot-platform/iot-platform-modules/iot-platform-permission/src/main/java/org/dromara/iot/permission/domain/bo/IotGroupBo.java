package org.dromara.iot.permission.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.iot.permission.domain.IotGroup;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据分组业务对象 iot_group
 *
 * @author iot team
 */
@Data
@AutoMapper(target = IotGroup.class, reverseConvertGenerate = false)
public class IotGroupBo implements Serializable {

    /**
     * 分组ID
     */
    private Long groupId;

    /**
     * 分组编码
     */
    @NotBlank(message = "分组编码不能为空")
    @Size(max = 64, message = "分组编码长度不能超过 64 个字符")
    private String groupCode;

    /**
     * 分组名称
     */
    @NotBlank(message = "分组名称不能为空")
    @Size(max = 128, message = "分组名称长度不能超过 128 个字符")
    private String groupName;

    /**
     * 父分组ID（0 为顶级）
     */
    private Long parentId;

    /**
     * 显示顺序
     */
    private Integer orderNum;

    /**
     * 状态（0正常 1停用）
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 请求参数
     */
    private Map<String, Object> params = new HashMap<>();

}
