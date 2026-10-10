package org.dromara.iot.permission.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 应用-数据集引用关系 iot_app_dataset
 * <p>
 * 应用绑定数据集为「引用」而非「存储」。
 *
 * @author iot team
 */
@Data
@TableName("iot_app_dataset")
public class IotAppDataset implements Serializable {

    /**
     * 应用ID
     */
    private Long appId;

    /**
     * 数据集ID
     */
    private Long datasetId;

}
