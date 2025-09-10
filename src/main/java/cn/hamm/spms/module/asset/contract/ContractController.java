package cn.hamm.spms.module.asset.contract;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.api.Api;
import cn.hamm.airpower.api.Json;
import cn.hamm.spms.base.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@Api("contract")
@Description("合同")
public class ContractController extends BaseController<ContractEntity, ContractService, ContractRepository> {
    @Description("生效合同")
    @PostMapping("enforce")
    public Json enforce(@RequestBody @Validated(WhenIdRequired.class) ContractEntity contract) {
        service.enforce(contract.getId());
        return Json.success("生效合同成功");
    }

    @Description("终止合同")
    @PostMapping("stop")
    public Json stop(@RequestBody @Validated(WhenIdRequired.class) ContractEntity contract) {
        service.stop(contract.getId());
        return Json.success("终止合同成功");
    }
}
