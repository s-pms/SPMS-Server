package cn.hamm.spms.module.open.app;

import cn.hamm.airpower.exception.ServiceException;
import cn.hamm.airpower.open.IOpenAppService;
import cn.hamm.airpower.util.RandomUtil;
import cn.hamm.airpower.util.RsaUtil;
import cn.hamm.spms.base.BaseService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Objects;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@SuppressWarnings("AlibabaServiceOrDaoClassShouldEndWithImpl")
@Service
public class OpenAppService extends BaseService<OpenAppEntity, OpenAppRepository> implements IOpenAppService {
    /**
     * <h3>通过AppKey获取一个应用</h3>
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
            resetKeyPare(openApp);
        }
        return openApp;
    }

    /**
     * <h3>创建AppSecret</h3>
     *
     * @return AppSecret
     */
    public final String createAppSecret() {
        return Base64.getEncoder().encodeToString(RandomUtil.randomBytes());
    }

    /**
     * <h3>重置密钥对</h3>
     *
     * @param openApp 应用
     */
    public final void resetKeyPare(@NotNull OpenAppEntity openApp) {
        try {
            KeyPair keyPair = RsaUtil.generateKeyPair();
            openApp.setPrivateKey(RsaUtil.convertPrivateKeyToPem(keyPair.getPrivate()));
            openApp.setPublicKey(RsaUtil.convertPublicKeyToPem(keyPair.getPublic()));
        } catch (NoSuchAlgorithmException e) {
            throw new ServiceException(e);
        }
    }

    /**
     * <h3>创建AppKey</h3>
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
