package cn.hamm.spms.module.system;

import cn.hamm.airpower.api.ApiController;
import cn.hamm.airpower.api.RequestUtil;
import cn.hamm.airpower.api.annotation.Api;
import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.redis.RedisHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@Slf4j
@Api("/")
@Description("首页")
public class IndexController extends ApiController {
    @Autowired
    private RedisHelper redisHelper;

    @GetMapping("")
    public String index() {
        long index = redisHelper.increment("index");
        return "<h1>Server running! " + index + "</h1>";
    }

    @GetMapping("ip")
    public String ip() {
        return "<h1>" + RequestUtil.getIpAddress(request) + "</h1>";
    }
}
