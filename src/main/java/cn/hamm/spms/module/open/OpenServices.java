package cn.hamm.spms.module.open;

import cn.hamm.spms.module.open.app.OpenAppService;
import cn.hamm.spms.module.open.notify.NotifyService;
import cn.hamm.spms.module.open.oauth.OauthService;
import cn.hamm.spms.module.open.thirdlogin.UserThirdLoginService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <h1>服务整合助手类</h1>
 *
 * @author Hamm.cn
 */
@Component
public class OpenServices {
    @Getter
    private static OpenAppService openAppService;

    @Getter
    private static NotifyService notifyService;

    @Getter
    private static OauthService oauthService;

    @Getter
    private static UserThirdLoginService userThirdLoginService;

    @Autowired
    private void initService(
            OpenAppService openAppService,
            NotifyService notifyService,
            OauthService oauthService,
            UserThirdLoginService userThirdLoginService
    ) {
        OpenServices.openAppService = openAppService;
        OpenServices.notifyService = notifyService;
        OpenServices.oauthService = oauthService;
        OpenServices.userThirdLoginService = userThirdLoginService;
    }
}
