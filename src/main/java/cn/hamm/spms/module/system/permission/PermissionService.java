package cn.hamm.spms.module.system.permission;

import cn.hamm.airpower.annotation.Extends;
import cn.hamm.airpower.config.Constant;
import cn.hamm.airpower.enums.Api;
import cn.hamm.airpower.enums.SystemError;
import cn.hamm.airpower.model.Access;
import cn.hamm.airpower.model.query.QueryRequest;
import cn.hamm.airpower.root.RootEntity;
import cn.hamm.airpower.root.RootEntityController;
import cn.hamm.airpower.util.AirUtil;
import cn.hamm.spms.Application;
import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.common.config.AppConstant;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Service
@Slf4j
public class PermissionService extends BaseService<PermissionEntity, PermissionRepository> {

    /**
     * <h2>通过标识获取一个权限</h2>
     *
     * @param identity 权限标识
     * @return 权限
     */
    public PermissionEntity getPermissionByIdentity(String identity) {
        return repository.getByIdentity(identity);
    }

    @Override
    protected void beforeDelete(long id) {
        PermissionEntity permission = get(id);
        SystemError.FORBIDDEN_DELETE.when(permission.getIsSystem(), "系统内置权限无法被删除!");
        QueryRequest<PermissionEntity> queryRequest = new QueryRequest<>();
        queryRequest.setFilter(new PermissionEntity().setParentId(id));
        List<PermissionEntity> children = getList(queryRequest);
        SystemError.FORBIDDEN_DELETE.when(!children.isEmpty(), "含有子权限,无法删除!");
    }

    @Override
    protected @NotNull List<PermissionEntity> afterGetList(@NotNull List<PermissionEntity> list) {
        list.forEach(RootEntity::excludeBaseData);
        return list;
    }

    @SuppressWarnings("AlibabaMethodTooLong")
    public void loadPermission() {
        try {
            ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
            String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                    ClassUtils.convertClassNameToResourcePath(Application.class.getPackageName()) + AppConstant.CONTROLLER_CLASS_PATH;
            Resource[] resources = resourcePatternResolver.getResources(pattern);
            MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);

            for (Resource resource : resources) {
                // 用于读取类信息
                MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                // 扫描到的class
                String className = metadataReader.getClassMetadata().getClassName();
                Class<?> clazz = Class.forName(className);

                RestController restController = AirUtil.getReflectUtil().getAnnotation(RestController.class, clazz);
                if (Objects.isNull(restController) || RootEntityController.class.getSimpleName().equals(clazz.getSimpleName())) {
                    // 不是rest控制器或者是指定的几个白名单控制器
                    continue;
                }

                String customClassName = AirUtil.getReflectUtil().getDescription(clazz);
                String identity = clazz.getSimpleName().replaceAll(Constant.CONTROLLER_SUFFIX, Constant.EMPTY_STRING);
                PermissionEntity permissionEntity = getPermissionByIdentity(identity);
                if (Objects.isNull(permissionEntity)) {
                    permissionEntity = new PermissionEntity()
                            .setName(customClassName)
                            .setIdentity(identity)
                            .setIsSystem(true);
                    permissionEntity.setId(add(permissionEntity));
                } else {
                    permissionEntity.setName(customClassName)
                            .setIdentity(identity)
                            .setIsSystem(true);
                    update(permissionEntity);
                }
                permissionEntity = get(permissionEntity.getId());

                // 读取类的RequestMapping
                RequestMapping requestMappingClass = AirUtil.getReflectUtil().getAnnotation(RequestMapping.class, clazz);
                String pathClass = Constant.EMPTY_STRING;
                if (Objects.nonNull(requestMappingClass) && requestMappingClass.value().length > 0) {
                    // 标了RequestMapping
                    pathClass = requestMappingClass.value()[0];
                }
                // 取出所有控制器方法
                Method[] methods = clazz.getMethods();

                // 取出控制器类上的Extends注解 如自己没标 则使用父类的
                Extends extendsApi = AirUtil.getReflectUtil().getAnnotation(Extends.class, clazz);
                for (Method method : methods) {
                    if (Objects.nonNull(extendsApi)) {
                        try {
                            Api current = Api.valueOf(method.getName());
                            if (checkApiExcluded(current, extendsApi)) {
                                continue;
                            }
                        } catch (Exception ignored) {
                        }
                    }
                    String customMethodName = AirUtil.getReflectUtil().getDescription(method);

                    String subIdentity = (!Constant.EMPTY_STRING.equalsIgnoreCase(pathClass) ? (pathClass + Constant.UNDERLINE) : Constant.EMPTY_STRING);

                    RequestMapping requestMapping = AirUtil.getReflectUtil().getAnnotation(RequestMapping.class, method);
                    if (Objects.nonNull(requestMapping) && requestMapping.value().length > 0) {
                        subIdentity += requestMapping.value()[0];
                    }

                    PostMapping postMapping = AirUtil.getReflectUtil().getAnnotation(PostMapping.class, method);
                    if (Objects.nonNull(postMapping) && postMapping.value().length > 0) {
                        subIdentity += postMapping.value()[0];
                    }

                    if (!StringUtils.hasText(subIdentity) || (pathClass + Constant.UNDERLINE).equals(subIdentity)) {
                        continue;
                    }

                    Access accessConfig = AirUtil.getAccessUtil().getWhatNeedAccess(clazz, method);
                    if (!accessConfig.isLogin() || !accessConfig.isAuthorize()) {
                        // 这里可以选择是否不读取这些接口的权限，但前端可能需要
                        continue;
                    }
                    PermissionEntity subPermission = getPermissionByIdentity(subIdentity);
                    if (Objects.isNull(subPermission)) {
                        subPermission = new PermissionEntity()
                                .setName(customClassName + Constant.LINE + customMethodName)
                                .setIdentity(subIdentity)
                                .setIsSystem(true)
                                .setParentId(permissionEntity.getId());
                        add(subPermission);
                    } else {
                        subPermission.setName(customClassName + Constant.LINE + customMethodName)
                                .setIdentity(subIdentity)
                                .setIsSystem(true)
                                .setParentId(permissionEntity.getId());
                        update(subPermission);
                    }
                }
            }
        } catch (Exception e) {
            log.error("初始化权限失败: {}", e.getMessage());
        }
    }

    private boolean checkApiExcluded(Api api, @NotNull Extends extend) {
        List<Api> excludeList = Arrays.asList(extend.exclude());
        List<Api> includeList = Arrays.asList(extend.value());
        if (excludeList.contains(api)) {
            return true;
        }
        if (includeList.isEmpty()) {
            return false;
        }
        return !includeList.contains(api);
    }
}
