package com.qqlab.spms.module.mes.bom;

import cn.hamm.airpower.annotation.Description;
import com.qqlab.spms.base.bill.BaseBillController;
import com.qqlab.spms.module.mes.bom.detail.BomDetailEntity;
import com.qqlab.spms.module.mes.bom.detail.BomDetailRepository;
import com.qqlab.spms.module.mes.bom.detail.BomDetailService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm
 */
@RestController
@RequestMapping("bom")
@Description("BOM")
public class BomController extends BaseBillController<BomEntity, BomService, BomRepository, BomDetailEntity, BomDetailService, BomDetailRepository> {
}
