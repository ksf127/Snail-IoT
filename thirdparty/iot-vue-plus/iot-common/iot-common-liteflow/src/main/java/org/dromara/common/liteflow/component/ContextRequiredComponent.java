package org.dromara.common.liteflow.component;

import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import org.dromara.common.core.exception.ServiceException;

/**
 * LiteFlow 上下文必填校验节点。
 *
 * @author iot team
 */
@LiteflowComponent("contextRequired")
public class ContextRequiredComponent extends NodeComponent {

    @Override
    public void process() {
        if (getFirstContextBean() == null) {
            throw new ServiceException("LiteFlow 上下文不能为空");
        }
    }

}
