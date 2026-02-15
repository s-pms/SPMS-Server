package cn.hamm.spms.module.chat.member;

import cn.hamm.airpower.api.annotation.Api;
import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.spms.base.BaseController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@Api("member")
@Description("成员")
public class MemberController extends BaseController<MemberEntity, MemberService, MemberRepository> implements IMemberAction {
}
