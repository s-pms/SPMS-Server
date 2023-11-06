package com.qqlab.spms.module.system.mail;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.result.json.Json;
import cn.hamm.airpower.root.RootController;
import cn.hamm.airpower.security.Permission;
import com.qqlab.spms.module.system.user.UserEntity;
import com.qqlab.spms.module.system.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm
 */
@RestController
@RequestMapping("mail")
@Description("邮件")
public class MailController extends RootController {
    @Autowired
    private UserService userService;

    @Description("发送邮件")
    @Permission(login = false)
    @PostMapping("send")
    public Json send(@RequestBody @Validated({UserEntity.WhenSendEmail.class}) UserEntity userEntity) {
        userService.sendMail(userEntity.getEmail());
        return json("发送成功");
    }
}
