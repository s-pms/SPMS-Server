package cn.hamm.spms.module.system;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.root.RootController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@RestController
@RequestMapping("/")
@Description("首页")
public class IndexController extends RootController {
    @GetMapping("")
    public String index() {
        return "<h1>Server running!</h1>";
    }
}
