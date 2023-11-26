package com.qqlab.spms.module.system.permission;

import cn.hamm.airpower.query.QueryRequest;
import cn.hamm.airpower.result.Result;
import cn.hamm.airpower.root.RootEntityController;
import cn.hamm.airpower.security.AccessConfig;
import cn.hamm.airpower.security.AccessUtil;
import cn.hamm.airpower.util.ReflectUtil;
import com.qqlab.spms.base.BaseService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <h1>Service</h1>
 *
 * @author Hamm
 */
@Service
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

    /**
     * <h2>强制重载所有权限</h2>
     */
    @SuppressWarnings("UnusedReturnValue")
    public List<PermissionEntity> forceReloadAllPermissions() {
        String packageName = "com.qqlab.spms";
        // 遍历所有接口
        List<PermissionEntity> permissionList = new ArrayList<>();
        try {
            ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
            String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                    ClassUtils.convertClassNameToResourcePath(packageName) + "/**/*.class";
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
                    permissionEntity = add(permissionEntity);
                } else {
                    permissionEntity.setName(customClassName)
                            .setIsSystem(true);
                    permissionEntity = update(permissionEntity);
                }

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
                    String customMethodName = ReflectUtil.getDescription(method);
                    PostMapping postMappingMethod = method.getAnnotation(PostMapping.class);

                    AccessConfig accessConfig = AccessUtil.getWhatNeedAccess(clazz, method);
                    if (!accessConfig.login || !accessConfig.authorize) {
                        // 这里可以选择是否不读取这些接口的权限，但前端可能需要
                        System.out.println("不需要的权限");
                    }
                    System.out.println(clazz.getSimpleName() + ":" + method.getName());

                    if (Objects.nonNull(postMappingMethod) && postMappingMethod.value().length > 0) {
                        String subIdentity = (!"".equalsIgnoreCase(pathClass) ? (pathClass + "_") : "") + postMappingMethod.value()[0];
                        PermissionEntity subPermission = getPermissionByIdentity(subIdentity);
                        if (Objects.isNull(subPermission)) {
                            subPermission = new PermissionEntity()
                                    .setName(customMethodName)
                                    .setIdentity(subIdentity)
                                    .setIsSystem(true)
                                    .setParentId(permissionEntity.getId());
                            add(subPermission);
                        } else {
                            subPermission.setName(customMethodName)
                                    .setIsSystem(true)
                                    .setParentId(permissionEntity.getId());
                            update(subPermission);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return permissionList;
    }

    @Override
    protected void beforeDelete(Long id) {
        PermissionEntity entity = getById(id);
        Result.FORBIDDEN_DELETE.when(entity.getIsSystem(), "系统内置权限无法被删除!");
        QueryRequest<PermissionEntity> queryRequest = new QueryRequest<>();
        queryRequest.setFilter(new PermissionEntity().setParentId(id));
        List<PermissionEntity> children = getList(queryRequest);
        Result.FORBIDDEN_DELETE.when(children.size() > 0, "含有子权限,无法删除!");
    }

    @Override
    protected QueryRequest<PermissionEntity> beforeGetList(QueryRequest<PermissionEntity> queryRequest) {
        PermissionEntity filter = queryRequest.getFilter();
        if (Objects.isNull(filter.getParentId())) {
            filter.setParentId(0L);
        }
        return queryRequest.setFilter(filter);
    }

    @Override
    protected List<PermissionEntity> afterGetList(List<PermissionEntity> list) {
        for (PermissionEntity item : list) {
            QueryRequest<PermissionEntity> queryRequest = new QueryRequest<>();
            queryRequest.setFilter(new PermissionEntity().setParentId(item.getId()));
            item.setChildren(this.getList(queryRequest));
            item.excludeBaseData();
        }
        return list;
    }
}
