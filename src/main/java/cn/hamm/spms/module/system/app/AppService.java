package cn.hamm.spms.module.system.app;

import cn.hamm.airpower.result.Result;
import cn.hutool.core.util.RandomUtil;
import cn.hamm.spms.base.BaseService;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * <h1>Service</h1>
 *
 * @author Hamm
 */
@Service
public class AppService extends BaseService<AppEntity, AppRepository> {
    /**
     * 通过AppKey获取一个应用
     *
     * @param appKey AppKey
     * @return AppEntity
     */
    public AppEntity getByAppKey(String appKey) {
        AppEntity appEntity = repository.getByAppKey(appKey);
        Result.DATA_NOT_FOUND.whenNull(appEntity, "没有查到指定AppKey的应用");
        return appEntity;
    }

    /**
     * 通过应用ID重置秘钥
     *
     * @param id 应用ID
     * @return 应用新秘钥
     */
    public String resetSecretById(Long id) {
        String newSecret = RandomUtil.randomString(32).toUpperCase();
        AppEntity entity = get(id);
        entity.setAppSecret(newSecret);
        update(entity);
        return newSecret;
    }

    @Override
    protected AppEntity beforeSaveToDatabase(AppEntity entity) {
        if (Objects.isNull(entity.getAppSecret())) {
            entity.setAppSecret(RandomUtil.randomString(32).toUpperCase());
        }
        return entity;
    }
}
