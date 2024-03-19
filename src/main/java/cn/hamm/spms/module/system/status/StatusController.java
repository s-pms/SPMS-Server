package cn.hamm.spms.module.system.status;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.result.Result;
import cn.hamm.airpower.result.json.JsonData;
import cn.hamm.airpower.root.RootController;
import cn.hamm.airpower.security.Permission;
import cn.hamm.airpower.util.ReflectUtil;
import cn.hamm.spms.common.exception.CustomResult;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm
 */
@Permission(login = false)
@RestController
@RequestMapping("status")
@Description("系统状态")
public class StatusController extends RootController {
    @Description("获取所有错误代码")
    @PostMapping("getErrorCodes")
    public JsonData getErrorCodes() {
        List<Map<String, Object>> resultList = new ArrayList<>();
        Result[] errorList = Result.class.getEnumConstants();
        for (Result error : errorList) {
            resultList.add(new HashMap<>(3) {
                {
                    put("name", error.name());
                    put("code", error.getCode());
                    put("message", error.getMessage());
                }
            });
        }
        CustomResult[] errorCustomList = CustomResult.class.getEnumConstants();
        for (CustomResult error : errorCustomList) {
            resultList.add(new HashMap<>(3) {
                {
                    put("name", error.name());
                    put("code", error.getCode());
                    put("message", error.getMessage());
                }
            });
        }
        return jsonData(resultList);
    }

    @Description("获取所有枚举数据")
    @PostMapping("getEnums")
    public JsonData getEnums() {
        List<Map<String, Object>> resultList = new ArrayList<>();
        try {
            ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
            String enumPackage = "cn.hamm";
            String resourcePattern = "/**/*.class";
            String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                    ClassUtils.convertClassNameToResourcePath(enumPackage) + resourcePattern;
            Resource[] resources = resourcePatternResolver.getResources(pattern);
            MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);

            for (Resource resource : resources) {
                //用于读取类信息
                MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                //扫描到的class
                String className = metadataReader.getClassMetadata().getClassName();
                Class<?> clazz = Class.forName(className);
                if (Void.class.getName().equalsIgnoreCase(clazz.getName())) {
                    continue;
                }
                try {
                    Method getKey = clazz.getMethod("getKey");
                    Method getLabel = clazz.getMethod("getLabel");

                    //取出所有枚举类型
                    Object[] objs = clazz.getEnumConstants();
                    List<Map<String, String>> enumTypeList = new ArrayList<>();
                    for (Object obj : objs) {
                        enumTypeList.add(new HashMap<>(2) {{
                            put("flag", getKey.invoke(obj).toString());
                            put("description", getLabel.invoke(obj).toString());
                        }});
                    }
                    resultList.add(new HashMap<>(3) {
                        {
                            put("name", ReflectUtil.getDescription(clazz));
                            put("class", clazz.getSimpleName());
                            put("enums", enumTypeList);
                        }
                    });
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            Result.ERROR.show("获取枚举数据异常");
        }
        return jsonData(resultList);
    }
}
