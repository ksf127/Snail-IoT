package org.dromara.iot.cloud.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * 云账号对象 iot_cloud_account
 * <p>
 * 一个云账号对应一个云平台的接入凭证；阿里云使用 AccessKey，Flink 使用 JobManager 地址。
 *
 * @author iot team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("iot_cloud_account")
public class IotCloudAccount extends BaseEntity {

    /**
     * 云账号ID
     */
    @TableId(value = "account_id", type = IdType.ASSIGN_ID)
    private Long accountId;

    /**
     * 账号名称
     */
    private String accountName;

    /**
     * 云平台（mock/aliyun/flink）
     */
    private String provider;

    /**
     * 默认地域ID
     */
    private String regionId;

    /**
     * 访问密钥ID（阿里云 AccessKeyId）
     */
    private String accessKey;

    /**
     * 访问密钥（阿里云 AccessKeySecret，Flink 可留空）
     */
    private String accessSecret;

    /**
     * 接入端点（Flink JobManager 地址，如 http://flink-jobmanager:8081）
     */
    private String endpoint;

    /**
     * 状态（0启用 1停用）
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
