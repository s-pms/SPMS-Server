package cn.hamm.spms.module.asset.contract.participant;

import cn.hamm.airpower.api.annotation.Api;
import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.spms.base.BaseController;

/**
 * <h1>合同参与方</h1>
 *
 * @author Hamm.cn
 */
@Api("participant")
@Description("参与方")
public class ParticipantController extends BaseController<ParticipantEntity, ParticipantService, ParticipantRepository> {
}
