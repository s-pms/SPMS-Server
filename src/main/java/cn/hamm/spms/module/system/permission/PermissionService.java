package cn.hamm.spms.module.system.permission;

import cn.hamm.airpower.api.Api;
import cn.hamm.airpower.api.Extends;
import cn.hamm.airpower.query.QueryRequest;
import cn.hamm.airpower.result.Result;
import cn.hamm.airpower.root.RootEntityController;
import cn.hamm.airpower.security.AccessConfig;
import cn.hamm.airpower.security.AccessUtil;
import cn.hamm.airpower.util.ReflectUtil;
import cn.hamm.spms.base.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;
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
 * @author Hamm
 */
@Service
@Slf4j
public class PermissionService extends BaseService<PermissionEntity, PermissionRepository> {
    /**
     * 通过标识获取一个权限
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
        Result.FORBIDDEN_DELETE.when(permission.getIsSystem(), "系统内置权限无法被删除!");
        QueryRequest<PermissionEntity> queryRequest = new QueryRequest<>();
        queryRequest.setFilter(new PermissionEntity().setParentId(id));
        List<PermissionEntity> children = getList(queryRequest);
        Result.FORBIDDEN_DELETE.when(!children.isEmpty(), "含有子权限,无法删除!");
    }

    @Override
    protected List<PermissionEntity> afterGetList(List<PermissionEntity> list) {
        for (PermissionEntity item : list) {
            item.excludeBaseData();
        }
        return list;
    }

    @SuppressWarnings("AlibabaMethodTooLong")
    public void initPermission(String packageName) {
        try {
            ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
            String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                    ClassUtils.convertClassNameToResourcePath(packageName) + "/**/*Controller.class";
            Resource[] resources = resourcePatternResolver.getResources(pattern);
            MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);

            for (Resource resource : resources) {
                // 用于读取类信息
                MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                // 扫描到的class
                String className = metadataReader.getClassMetadata().getClassName();
                Class<?> clazz = Class.forName(className);

                RestController restController = clazz.getAnnotation(RestController.class);
                if (Objects.isNull(restController) || RootEntityController.class.getSimpleName().equals(clazz.getSimpleName())) {
                    // 不是rest控制器或者是指定的几个白名单控制器
                    continue;
                }

                String customClassName = ReflectUtil.getDescription(clazz);
                String identity = clazz.getSimpleName().replaceAll("Controller", "");
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
                RequestMapping requestMappingClass = clazz.getAnnotation(RequestMapping.class);
                String pathClass = "";
                if (Objects.nonNull(requestMappingClass) && requestMappingClass.value().length > 0) {
                    // 标了RequestMapping
                    pathClass = requestMappingClass.value()[0];
                }
                // 取出所有控制器方法
                Method[] methods = clazz.getMethods();
                for (Method method : methods) {
                    Extends extendsApi = clazz.getAnnotation(Extends.class);
                    if (Objects.nonNull(extendsApi)) {
                        switch (method.getName()) {
                            case "add":
                                if (checkApiBand(Api.Add, extendsApi)) {
                                    continue;
                                }
                                break;
                            case "delete":
                                if (checkApiBand(Api.Delete, extendsApi)) {
                                    continue;
                                }
                                break;
                            case "disable":
                                if (checkApiBand(Api.Disable, extendsApi)) {
                                    continue;
                                }
                                break;
                            case "enable":
                                if (checkApiBand(Api.Enable, extendsApi)) {
                                    continue;
                                }
                                break;
                            case "getDetail":
                                if (checkApiBand(Api.GetDetail, extendsApi)) {
                                    continue;
                                }
                                break;
                            case "getList":
                                if (checkApiBand(Api.GetList, extendsApi)) {
                                    continue;
                                }
                                break;
                            case "getPage":
                                if (checkApiBand(Api.GetPage, extendsApi)) {
                                    continue;
                                }
                                break;
                            case "update":
                                if (checkApiBand(Api.Update, extendsApi)) {
                                    continue;
                                }
                                break;
                            default:
                        }
                    }
                    String customMethodName = ReflectUtil.getDescription(method);

                    String subIdentity = (!"".equalsIgnoreCase(pathClass) ? (pathClass + "_") : "");

                    RequestMapping requestMapping = ReflectUtil.getAnnotation(RequestMapping.class, method);
                    if (Objects.nonNull(requestMapping) && requestMapping.value().length > 0) {
                        subIdentity += requestMapping.value()[0];
                    }

                    PostMapping postMapping = ReflectUtil.getAnnotation(PostMapping.class, method);
                    if (Objects.nonNull(postMapping) && postMapping.value().length > 0) {
                        subIdentity += postMapping.value()[0];
                    }

                    AccessConfig accessConfig = AccessUtil.getWhatNeedAccess(clazz, method);
                    if (!accessConfig.login || !accessConfig.authorize) {
                        // 这里可以选择是否不读取这些接口的权限，但前端可能需要
                        continue;
                    }
                    PermissionEntity subPermission = getPermissionByIdentity(subIdentity);
                    if (Objects.isNull(subPermission)) {
                        subPermission = new PermissionEntity()
                                .setName(customClassName + "-" + customMethodName)
                                .setIdentity(subIdentity)
                                .setIsSystem(true)
                                .setParentId(permissionEntity.getId());
                        add(subPermission);
                    } else {
                        subPermission.setName(customClassName + "-" + customMethodName)
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

    private boolean checkApiBand(Api api, Extends extend) {
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
