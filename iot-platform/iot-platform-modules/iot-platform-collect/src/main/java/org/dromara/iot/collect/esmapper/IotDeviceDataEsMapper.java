package org.dromara.iot.collect.esmapper;

import org.dromara.easyes.core.kernel.BaseEsMapper;
import org.dromara.iot.collect.domain.IotDeviceDataDoc;

/**
 * 设备上报数据 Elasticsearch Mapper
 * <p>
 * 注意：Easy-Es Mapper 必须位于 {@code org.dromara.**.esmapper} 包下，
 * 且与 MyBatis-Plus 的 Mapper 分属不同包，否则项目无法启动。
 *
 * @author iot team
 */
public interface IotDeviceDataEsMapper extends BaseEsMapper<IotDeviceDataDoc> {
}
