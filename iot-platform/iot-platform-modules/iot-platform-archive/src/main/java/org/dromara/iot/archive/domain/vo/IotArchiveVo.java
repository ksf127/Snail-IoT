package org.dromara.iot.archive.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.iot.archive.domain.IotArchive;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 档案视图对象 iot_archive
 *
 * @author iot team
 */
@Data
@AutoMapper(target = IotArchive.class)
public class IotArchiveVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 档案ID
     */
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
     * 对象存储文件ID
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
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
