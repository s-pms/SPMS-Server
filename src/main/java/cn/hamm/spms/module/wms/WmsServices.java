package cn.hamm.spms.module.wms;

import cn.hamm.spms.module.wms.input.InputService;
import cn.hamm.spms.module.wms.input.detail.InputDetailService;
import cn.hamm.spms.module.wms.inventory.InventoryService;
import cn.hamm.spms.module.wms.move.MoveService;
import cn.hamm.spms.module.wms.move.detail.MoveDetailService;
import cn.hamm.spms.module.wms.output.OutputService;
import cn.hamm.spms.module.wms.output.detail.OutputDetailService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <h1>服务整合助手类</h1>
 *
 * @author Hamm.cn
 */
@Component
public class WmsServices {
    @Getter
    private static InputService inputService;

    @Getter
    private static InputDetailService inputDetailService;

    @Getter
    private static InventoryService inventoryService;

    @Getter
    private static MoveService moveService;

    @Getter
    private static MoveDetailService moveDetailService;

    @Getter
    private static OutputService outputService;

    @Getter
    private static OutputDetailService outputDetailService;

    @Autowired
    private void initService(
            InputService inputService,
            InputDetailService inputDetailService,
            InventoryService inventoryService,
            MoveService moveService,
            MoveDetailService moveDetailService,
            OutputService outputService,
            OutputDetailService outputDetailService
    ) {
        WmsServices.inputService = inputService;
        WmsServices.inputDetailService = inputDetailService;
        WmsServices.inventoryService = inventoryService;
        WmsServices.moveService = moveService;
        WmsServices.moveDetailService = moveDetailService;
        WmsServices.outputService = outputService;
        WmsServices.outputDetailService = outputDetailService;
    }
}
