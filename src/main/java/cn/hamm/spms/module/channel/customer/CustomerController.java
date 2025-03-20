package cn.hamm.spms.module.channel.customer;

import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.web.annotation.ApiController;
import cn.hamm.spms.base.BaseController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@ApiController("customer")
@Description("客户")
public class CustomerController extends BaseController<CustomerEntity, CustomerService, CustomerRepository> {
}
