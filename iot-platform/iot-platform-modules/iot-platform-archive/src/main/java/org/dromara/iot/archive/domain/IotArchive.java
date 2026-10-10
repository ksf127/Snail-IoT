package org.dromara.iot.archive.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.time.LocalDateTime;

/**
 * 档案对象 iot_archive
 * <p>
 * 档案承载设备档案、运维记录与归档附件，附件通过 ossId 关联对象存储。
 *
 * @author iot team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("iot_archive")
public class IotArchive extends BaseEntity {

    /**
     * 档案ID
     */
    @TableId(value = "archive_id", type = IdType.ASSIGN_ID)
    private Long archiveId;

    /**
     * 档案类型
     */
    private String archiveType;

    /**
     * 业务主键（如设备ID）
     */
    private Long bizId;

    /**
     * 对象存储文件ID（sys_oss.oss_id）
     */
    private Long ossId;

    /**
     * 归档时间
     */
    private LocalDateTime archiveTime;

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
