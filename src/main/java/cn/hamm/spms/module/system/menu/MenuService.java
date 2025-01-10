package cn.hamm.spms.module.system.menu;

import cn.hamm.airpower.config.Constant;
import cn.hamm.airpower.model.Sort;
import cn.hamm.airpower.model.query.QueryListRequest;
import cn.hamm.airpower.root.RootEntity;
import cn.hamm.airpower.root.delegate.TreeServiceDelegate;
import cn.hamm.spms.base.BaseService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Service
public class MenuService extends BaseService<MenuEntity, MenuRepository> {
    /**
     * <h3>排序字段</h3>
     */
    private static final String ORDER_FIELD_NAME = "orderNo";

    @Override
    protected void beforeDelete(long id) {
        TreeServiceDelegate.ensureNoChildrenBeforeDelete(this, id);
    }

    @Override
    protected @NotNull QueryListRequest<MenuEntity> beforeGetList(@NotNull QueryListRequest<MenuEntity> sourceRequestData) {
        MenuEntity filter = sourceRequestData.getFilter();
        sourceRequestData.setSort(Objects.requireNonNullElse(
                sourceRequestData.getSort(),
                new Sort().setField(ORDER_FIELD_NAME)
        ));
        sourceRequestData.setFilter(filter);
        return sourceRequestData;
    }

    @Override
    protected @NotNull List<MenuEntity> afterGetList(@NotNull List<MenuEntity> list) {
        list.forEach(RootEntity::excludeBaseData);
        return list;
    }

    @SuppressWarnings("AlibabaMethodTooLong")
    public final void initMenu() {
        MenuEntity firstMenu;
        MenuEntity secondMenu;
        MenuEntity thirdMenu;

        firstMenu = new MenuEntity().setName("首页").setOrderNo(99).setPath("/console").setComponent("/console/index/index").setParentId(Constant.ZERO_LONG);
        add(firstMenu);

        // 基础数据
        firstMenu = new MenuEntity().setName("基础数据").setOrderNo(88).setParentId(Constant.ZERO_LONG);
        firstMenu = get(add(firstMenu));

        secondMenu = new MenuEntity().setName("物料管理").setPath("/console/asset/material/list").setParentId(firstMenu.getId());
        add(secondMenu);
//        secondMenu = new MenuEntity().setName("设备管理").setPath("/console/asset/device/list").setParentId(firstMenu.getId());
//        add(secondMenu);
        secondMenu = new MenuEntity().setName("存储资源").setPath("/console/factory/storage/list").setParentId(firstMenu.getId());
        add(secondMenu);
//        secondMenu = new MenuEntity().setName("工厂结构").setPath("/console/factory/structure/list").setParentId(firstMenu.getId());
//        add(secondMenu);


        // 渠道管理
//        firstMenu = new MenuEntity().setName("渠道管理").setOrderNo(77).setParentId(Constant.ZERO_LONG);
//        firstMenu = get(add(firstMenu));
//
//        secondMenu = new MenuEntity().setName("采购管理").setPath("/console/channel/purchase/list").setParentId(firstMenu.getId());
//        add(secondMenu);
//        secondMenu = new MenuEntity().setName("销售管理").setPath("/console/channel/sale/list").setParentId(firstMenu.getId());
//        add(secondMenu);
//        secondMenu = new MenuEntity().setName("供应商管理").setPath("/console/channel/supplier/list").setParentId(firstMenu.getId());
//        add(secondMenu);
//        secondMenu = new MenuEntity().setName("客户管理").setPath("/console/channel/customer/list").setParentId(firstMenu.getId());
//        add(secondMenu);
//        secondMenu = new MenuEntity().setName("采购价管理").setPath("/console/channel/purchasePrice/list").setParentId(firstMenu.getId());
//        add(secondMenu);
//        secondMenu = new MenuEntity().setName("销售价管理").setPath("/console/channel/salePrice/list").setParentId(firstMenu.getId());
//        add(secondMenu);


        // 仓储管理 - WMS
        firstMenu = new MenuEntity().setName("仓储管理").setOrderNo(66).setParentId(Constant.ZERO_LONG);
        firstMenu = get(add(firstMenu));

        secondMenu = new MenuEntity().setName("库存概览").setPath("/console/wms/inventory/list").setParentId(firstMenu.getId());
        add(secondMenu);
        secondMenu = new MenuEntity().setName("入库管理").setPath("/console/wms/input/list").setParentId(firstMenu.getId());
        add(secondMenu);
        secondMenu = new MenuEntity().setName("出库管理").setPath("/console/wms/output/list").setParentId(firstMenu.getId());
        add(secondMenu);
        secondMenu = new MenuEntity().setName("移库管理").setPath("/console/wms/move/list").setParentId(firstMenu.getId());
        add(secondMenu);


        // 生产管理 - MES
        firstMenu = new MenuEntity().setName("生产管理").setOrderNo(55).setParentId(Constant.ZERO_LONG);
        firstMenu = get(add(firstMenu));

//        secondMenu = new MenuEntity().setName("生产资源").setParentId(firstMenu.getId());
//        secondMenu = get(add(secondMenu));
//
//        thirdMenu = new MenuEntity().setName("物料领取").setPath("/console/mes/pickout/list").setParentId(secondMenu.getId());
//        add(thirdMenu);
//        thirdMenu = new MenuEntity().setName("物料退还").setPath("/console/mes/restore/list").setParentId(secondMenu.getId());
//        add(thirdMenu);

//        secondMenu = new MenuEntity().setName("工艺工序").setParentId(firstMenu.getId());
//        secondMenu = get(add(secondMenu));
//
//        thirdMenu = new MenuEntity().setName("工艺流程").setPath("/console/mes/process/list").setParentId(secondMenu.getId());
//        add(thirdMenu);
//        thirdMenu = new MenuEntity().setName("工序管理").setPath("/console/mes/operation/list").setParentId(secondMenu.getId());
//        add(thirdMenu);
//        thirdMenu = new MenuEntity().setName("BOM管理").setPath("/console/mes/bom/list").setParentId(secondMenu.getId());
//        add(thirdMenu);

        secondMenu = new MenuEntity().setName("生产计划").setPath("/console/mes/plan/list").setParentId(firstMenu.getId());
        add(secondMenu);
        secondMenu = new MenuEntity().setName("生产订单").setPath("/console/mes/order/list").setParentId(firstMenu.getId());
        add(secondMenu);


        // 物联网
//        firstMenu = new MenuEntity().setName("设备物联").setOrderNo(44).setParentId(Constant.ZERO_LONG);
//        firstMenu = get(add(firstMenu));
//
//        secondMenu = new MenuEntity().setName("设备概览").setPath("/console/iot/monitor/preview").setParentId(firstMenu.getId());
//        add(secondMenu);
//        secondMenu = new MenuEntity().setName("参数管理").setPath("/console/iot/parameter/list").setParentId(firstMenu.getId());
//        add(secondMenu);

        // 开放能力
//        firstMenu = new MenuEntity().setName("开放能力").setOrderNo(10).setParentId(0L);
//        firstMenu = get(add(firstMenu));
//
//        secondMenu = new MenuEntity().setName("我的应用").setPath("/console/open/app/list").setParentId(firstMenu.getId());
//        add(secondMenu);
//
//        secondMenu = new MenuEntity().setName("通知管理").setPath("/console/open/notify/list").setParentId(firstMenu.getId());
//        add(secondMenu);


        // 系统管理
        firstMenu = new MenuEntity().setName("系统管理").setOrderNo(3).setParentId(Constant.ZERO_LONG);
        firstMenu = get(add(firstMenu));

        secondMenu = new MenuEntity().setName("员工管理").setPath("/console/personnel/user/list").setParentId(firstMenu.getId());
        add(secondMenu);
        secondMenu = new MenuEntity().setName("角色管理").setPath("/console/personnel/role/list").setParentId(firstMenu.getId());
        add(secondMenu);
        secondMenu = new MenuEntity().setName("部门管理").setPath("/console/personnel/department/list").setParentId(firstMenu.getId());
        add(secondMenu);
        secondMenu = new MenuEntity().setName("计量单位").setPath("/console/system/unit/list").setParentId(firstMenu.getId());
        add(secondMenu);
        secondMenu = new MenuEntity().setName("编码规则").setPath("/console/system/coderule/list").setParentId(firstMenu.getId());
        add(secondMenu);

        // 超管配置
        firstMenu = new MenuEntity().setName("超管配置").setOrderNo(2).setParentId(Constant.ZERO_LONG);
        firstMenu = get(add(firstMenu));

        secondMenu = new MenuEntity().setName("权限管理").setPath("/console/system/permission/list").setParentId(firstMenu.getId());
        add(secondMenu);
        secondMenu = new MenuEntity().setName("菜单管理").setPath("/console/system/menu/list").setParentId(firstMenu.getId());
        add(secondMenu);
//        secondMenu = new MenuEntity().setName("请求日志").setPath("/console/system/log/list").setParentId(firstMenu.getId());
//        add(secondMenu);
    }
}
