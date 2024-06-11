package cn.hamm.spms.module.open.app;

import cn.hamm.airpower.exception.ServiceException;
import cn.hamm.airpower.open.IOpenAppService;
import cn.hamm.airpower.util.Utils;
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
@Service
public class OpenAppService extends BaseService<OpenAppEntity, OpenAppRepository> implements IOpenAppService {
    /**
     * <h2>通过AppKey获取一个应用</h2>
     *
     * @param appKey AppKey
     * @return 应用
     */
    public OpenAppEntity getByAppKey(String appKey) {
        return repository.getByAppKey(appKey);
    }

    @Override
    protected @NotNull OpenAppEntity beforeAdd(@NotNull OpenAppEntity openApp) {
        openApp.setAppKey(createAppKey());
        openApp.setAppSecret(Base64.getEncoder().encodeToString(Utils.getRandomUtil().randomBytes()));
        try {
            KeyPair keyPair = Utils.getRsaUtil().generateKeyPair();
            openApp.setPrivateKey(Utils.getRsaUtil().convertPrivateKeyToPEM(keyPair.getPrivate()));
            openApp.setPublicKey(Utils.getRsaUtil().convertPublicKeyToPEM(keyPair.getPublic()));
        } catch (NoSuchAlgorithmException e) {
            throw new ServiceException(e);
        }
        return openApp;
    }

    /**
     * <h2>创建AppKey</h2>
     *
     * @return AppKey
     */
    private String createAppKey() {
        String appKey = Utils.getRandomUtil().randomString();
        OpenAppEntity openApp = getByAppKey(appKey);
        if (Objects.isNull(openApp)) {
            return appKey;
        }
        return createAppKey();
    }
}
