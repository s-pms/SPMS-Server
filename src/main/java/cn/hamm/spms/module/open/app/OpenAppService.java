package cn.hamm.spms.module.open.app;

import cn.hamm.airpower.exception.ServiceException;
import cn.hamm.airpower.open.IOpenAppService;
import cn.hamm.airpower.util.RandomUtil;
import cn.hamm.airpower.util.RsaUtil;
import cn.hamm.spms.base.BaseService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

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
        openApp.setAppKey(createAppKey());
        openApp.setAppSecret(Base64.getEncoder().encodeToString(RandomUtil.randomBytes()));
        resetKeyPare(openApp);
        return openApp;
    }

    /**
     * <h3>重置密钥对</h3>
     *
     * @param openApp 待重置的应用
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
    private String createAppKey() {
        String appKey = RandomUtil.randomString();
        OpenAppEntity openApp = getByAppKey(appKey);
        if (Objects.isNull(openApp)) {
            return appKey;
        }
        return createAppKey();
    }
}
