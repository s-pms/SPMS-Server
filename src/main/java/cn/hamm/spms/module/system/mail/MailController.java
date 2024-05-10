package cn.hamm.spms.module.system.mail;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Permission;
import cn.hamm.airpower.model.Json;
import cn.hamm.airpower.root.RootController;
import cn.hamm.spms.module.personnel.user.IUserAction;
import cn.hamm.spms.module.personnel.user.UserEntity;
import cn.hamm.spms.module.personnel.user.UserService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@RestController
@RequestMapping("mail")
@Description("邮件")
public class MailController extends RootController implements IUserAction {
    @Autowired
    private UserService userService;

    @Description("发送邮件")
    @Permission(login = false)
    @RequestMapping("send")
    public Json send(@RequestBody @Validated(WhenSendEmail.class) UserEntity userEntity) throws MessagingException {
        userService.sendMail(userEntity.getEmail());
        return Json.success("发送成功");
    }
}
