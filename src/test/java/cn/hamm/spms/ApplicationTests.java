package cn.hamm.spms;

import cn.hamm.airpower.response.Filter;
import cn.hamm.airpower.util.ReflectUtil;
import cn.hamm.spms.base.BaseController;
import cn.hamm.spms.base.BaseEntity;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

class ApplicationTests {
    @Test
    void initTest() throws NoSuchMethodException {
        System.out.println("Hello AirPower!");
        Method method = BaseController.class.getMethod("getDetail", BaseEntity.class);
        Filter filter = ReflectUtil.getAnnotation(Filter.class, method);
    }
}
