package org.dromara.iot.permission.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * 应用对象 iot_app（服务线：功能窗口，三线交汇点）
 * <p>
 * 绑定数据集（引用非存储）+ 授权角色。
 *
 * @author iot team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("iot_app")
public class IotApp extends BaseEntity {

    /**
     * 应用ID
     */
    @TableId(value = "app_id", type = IdType.ASSIGN_ID)
    private Long appId;

    /**
     * 应用编码（SSO 接入唯一标识）
     */
    private String appCode;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 应用类型 WEB / APP / MINI / API
     */
    private String appType;

    /**
     * 访问地址
     */
    private String accessUrl;

    /**
     * 应用密钥（SSO 票据校验）
     */
    private String appSecret;

    /**
     * 状态（0正常 1停用）
     */
    private String status;

    /**
     * 删除标志（0代表存在 1代表删除）
     */
    @TableLogic
    private String delFlag;

    /**
     * 备注
     */
    private String remark;

}
