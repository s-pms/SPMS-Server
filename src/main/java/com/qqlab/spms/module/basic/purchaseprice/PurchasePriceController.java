package com.qqlab.spms.module.basic.purchaseprice;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.root.RootEntityController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm
 */
@RestController
@RequestMapping("purchasePrice")
@Description("采购价格")
public class PurchasePriceController extends RootEntityController<PurchasePriceService, PurchasePriceEntity> {
}
