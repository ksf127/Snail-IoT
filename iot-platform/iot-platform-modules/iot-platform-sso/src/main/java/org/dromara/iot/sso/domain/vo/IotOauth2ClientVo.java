package org.dromara.iot.sso.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import org.dromara.iot.sso.domain.IotOauth2Client;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * OAuth2 客户端注册视图对象 iot_oauth2_client
 *
 * @author iot team
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = IotOauth2Client.class)
public class IotOauth2ClientVo implements Serializable {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 客户端标识
     */
    @ExcelProperty(value = "客户端标识")
    private String clientId;

    /**
     * 客户端密钥
     */
    @ExcelProperty(value = "客户端密钥")
    private String clientSecret;

    /**
     * 客户端名称
     */
    @ExcelProperty(value = "客户端名称")
    private String clientName;

    /**
     * 签约的 scope 列表（逗号分隔）
     */
    @ExcelProperty(value = "签约Scope")
    private String contractScopes;

    /**
     * 允许的重定向地址（逗号分隔）
     */
    @ExcelProperty(value = "允许重定向地址")
    private String allowRedirectUris;

    /**
     * 允许的授权类型（逗号分隔）
     */
    @ExcelProperty(value = "允许授权类型")
    private String allowGrantTypes;

    /**
     * 授权对象（为空时默认取当前登录用户ID）
     */
    @ExcelProperty(value = "授权对象")
    private String subjectId;

    /**
     * AccessToken 有效期（秒）
     */
    @ExcelProperty(value = "AccessToken有效期(秒)")
    private Long accessTokenTimeout;

    /**
     * RefreshToken 有效期（秒）
     */
    @ExcelProperty(value = "RefreshToken有效期(秒)")
    private Long refreshTokenTimeout;

    /**
     * ClientToken 有效期（秒）
     */
    @ExcelProperty(value = "ClientToken有效期(秒)")
    private Long clientTokenTimeout;

    /**
     * AccessToken 最大保留数量
     */
    private Integer maxAccessTokenCount;

    /**
     * RefreshToken 最大保留数量
     */
    private Integer maxRefreshTokenCount;

    /**
     * ClientToken 最大保留数量
     */
    private Integer maxClientTokenCount;

    /**
     * 每次刷新是否重新生成 RefreshToken（1是 0否）
     */
    @ExcelProperty(value = "刷新令牌轮换")
    private String newRefresh;

    /**
     * 是否自动授权（1是 0否）
     */
    @ExcelProperty(value = "自动授权")
    private String autoConfirm;

    /**
     * 适配设备类型（pc/android/ios/h5）
     */
    @ExcelProperty(value = "设备类型")
    private String deviceType;

    /**
     * 状态（0正常 1停用）
     */
    @ExcelProperty(value = "状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "sys_normal_disable")
    private String status;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    private String remark;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private LocalDateTime createTime;

}
