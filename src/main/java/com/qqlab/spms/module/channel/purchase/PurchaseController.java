package com.qqlab.spms.module.channel.purchase;

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
@RequestMapping("purchase")
@Description("采购单")
public class PurchaseController extends RootEntityController<PurchaseService, PurchaseEntity> {
}
