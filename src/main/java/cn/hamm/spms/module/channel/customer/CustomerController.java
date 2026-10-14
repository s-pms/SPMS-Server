package cn.hamm.spms.module.channel.customer;

import cn.hamm.airpower.api.annotation.Api;
import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.spms.base.BaseController;

/**
 * <h1>客户</h1>
 *
 * @author Hamm.cn
 */
@Api("customer")
@Description("客户")
public class CustomerController extends BaseController<CustomerEntity, CustomerService, CustomerRepository> {
}
