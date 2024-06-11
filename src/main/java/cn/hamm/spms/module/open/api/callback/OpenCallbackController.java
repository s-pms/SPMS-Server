package cn.hamm.spms.module.open.api.callback;

import cn.hamm.airpower.annotation.ApiController;
import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Permission;
import cn.hamm.airpower.root.RootController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@ApiController("open/callback")
@Description("回调通知")
@Permission(login = false)
public class OpenCallbackController extends RootController {
}
