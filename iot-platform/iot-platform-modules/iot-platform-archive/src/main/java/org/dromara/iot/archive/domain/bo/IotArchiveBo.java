package org.dromara.iot.archive.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.iot.archive.domain.IotArchive;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 档案业务对象 iot_archive
 *
 * @author iot team
 */
@Data
@AutoMapper(target = IotArchive.class, reverseConvertGenerate = false)
public class IotArchiveBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 档案ID
     */
    private Long archiveId;

    /**
     * 档案类型
     */
    @Size(max = 64, message = "档案类型长度不能超过 64 个字符")
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
     * 状态（0正常 1停用）
     */
    private String status;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过 500 个字符")
    private String remark;

    /**
     * 请求参数
     */
    private Map<String, Object> params = new HashMap<>();

}
