package cn.hamm.spms.module.channel.customer;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.enums.Api;
import cn.hamm.airpower.annotation.Extends;
import cn.hamm.spms.base.BaseController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@RestController
@RequestMapping("customer")
@Description("客户")
@Extends(exclude = Api.Delete)
public class CustomerController extends BaseController<CustomerEntity, CustomerService, CustomerRepository> {
}
