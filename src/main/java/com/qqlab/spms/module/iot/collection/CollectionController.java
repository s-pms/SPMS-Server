package com.qqlab.spms.module.iot.collection;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.api.Api;
import cn.hamm.airpower.api.Extends;
import com.qqlab.spms.base.BaseController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm
 */
@RestController
@RequestMapping("collection")
@Description("采集数据")
@Extends({Api.GetList, Api.GetPage})
public class CollectionController extends BaseController<CollectionEntity, CollectionService, CollectionRepository> {

}
