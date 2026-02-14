package cn.hamm.spms.module.open.app;

import cn.hamm.airpower.ai.mcp.method.McpMethod;
import cn.hamm.airpower.core.RandomUtil;
import cn.hamm.airpower.core.RsaUtil;
import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.core.exception.ServiceException;
import cn.hamm.airpower.open.IOpenAppService;
import cn.hamm.spms.base.BaseService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import static cn.hamm.airpower.exception.ServiceError.SERVICE_ERROR;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@SuppressWarnings("AlibabaServiceOrDaoClassShouldEndWithImpl")
@Service
public class OpenAppService extends BaseService<OpenAppEntity, OpenAppRepository> implements IOpenAppService {
    /**
     * 通过 AppKey 获取一个应用
     *
     * @param appKey AppKey
     * @return 应用
     */
    @Override
    public OpenAppEntity getByAppKey(String appKey) {
        return repository.getByAppKey(appKey);
    }

    @Override
    protected @NotNull OpenAppEntity beforeAdd(@NotNull OpenAppEntity openApp) {
        if (!StringUtils.hasText(openApp.getAppKey())) {
            openApp.setAppKey(createAppKey());
        }
        if (!StringUtils.hasText(openApp.getAppSecret())) {
            openApp.setAppSecret(createAppSecret());
        }
        if (!StringUtils.hasText(openApp.getPrivateKey()) || !StringUtils.hasText(openApp.getPublicKey())) {
            resetKeyPair(openApp);
        }
        return openApp;
    }

    /**
     * 创建 AppSecret
     *
     * @return AppSecret
     */
    public final String createAppSecret() {
        return Base64.getEncoder().encodeToString(RandomUtil.randomBytes());
    }

    /**
     * 重置密钥对
     *
     * @param openApp 应用
     */
    public final void resetKeyPair(@NotNull OpenAppEntity openApp) {
        try {
            KeyPair keyPair = RsaUtil.generateKeyPair();
            openApp.setPrivateKey(RsaUtil.convertPrivateKeyToPem(keyPair.getPrivate()));
            openApp.setPublicKey(RsaUtil.convertPublicKeyToPem(keyPair.getPublic()));
        } catch (NoSuchAlgorithmException e) {
            throw new ServiceException("重置密钥对失败，" + e.getMessage());
        }
    }

    @McpMethod
    @Description("disable the open app by app name")
    public final void disableAppByName(@Description("the app name to disable") String appName) {
        List<OpenAppEntity> openAppEntities = filter(new OpenAppEntity().setAppName(appName));
        SERVICE_ERROR.when(openAppEntities.size() > 1, "应用名重复，我无法为你禁用");
        SERVICE_ERROR.when(openAppEntities.isEmpty(), "应用不存在，我无法为你禁用");
        OpenAppEntity openAppEntity = openAppEntities.get(0);
        openAppEntity.setIsDisabled(true);
        update(openAppEntity);
    }

    /**
     * 创建 AppKey
     *
     * @return AppKey
     */
    public final String createAppKey() {
        String appKey = RandomUtil.randomString();
        OpenAppEntity openApp = getByAppKey(appKey);
        if (Objects.isNull(openApp)) {
            return appKey;
        }
        return createAppKey();
    }
}
