package cn.hamm.spms.module.asset.contract.participant;

import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.web.api.Api;
import cn.hamm.spms.base.BaseController;
import cn.hamm.spms.module.asset.contract.ContractEntity;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@Api("participant")
@Description("参与方")
public class ParticipantController extends BaseController<ContractEntity, ParticipantService, ParticipantRepository> {
}
