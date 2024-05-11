package cn.hamm.spms;

import cn.hamm.airpower.annotation.Filter;
import cn.hamm.airpower.util.Utils;
import cn.hamm.spms.base.BaseController;
import cn.hamm.spms.base.BaseEntity;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

class ApplicationTests {
    @Test
    void initTest() throws NoSuchMethodException {
        System.out.println("Hello AirPower!");
        Method method = BaseController.class.getMethod("getDetail", BaseEntity.class);
        Filter filter = Utils.getReflectUtil().getAnnotation(Filter.class, method);
    }
}
