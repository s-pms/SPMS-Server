package cn.hamm.spms.module.system.menu;

import cn.hamm.airpower.model.Sort;
import cn.hamm.airpower.model.query.QueryListRequest;
import cn.hamm.airpower.root.RootEntity;
import cn.hamm.airpower.util.TreeUtil;
import cn.hamm.spms.base.BaseService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static cn.hamm.airpower.util.TreeUtil.ROOT_ID;

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
        TreeUtil.ensureNoChildrenBeforeDelete(this, id);
    }

    @Override
    protected @NotNull QueryListRequest<MenuEntity> beforeGetList(@NotNull QueryListRequest<MenuEntity> queryListRequest) {
        MenuEntity filter = queryListRequest.getFilter();
        queryListRequest.setSort(Objects.requireNonNullElse(
                queryListRequest.getSort(),
                new Sort().setField(ORDER_FIELD_NAME)
        ));
        queryListRequest.setFilter(filter);
        return queryListRequest;
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

        firstMenu = new MenuEntity().setName("首页").setOrderNo(99).setPath("/console").setComponent("/console/index/index").setParentId(ROOT_ID);
        add(firstMenu);

        // 基础数据
        firstMenu = new MenuEntity().setName("资产管理").setOrderNo(88).setParentId(ROOT_ID);
        firstMenu = get(add(firstMenu));

        secondMenu = new MenuEntity().setName("物料管理").setPath("/console/asset/material/list").setParentId(firstMenu.getId());
        add(secondMenu);
        secondMenu = new MenuEntity().setName("设备管理").setPath("/console/asset/device/list").setParentId(firstMenu.getId());
        add(secondMenu);

        // 渠道管理
        firstMenu = new MenuEntity().setName("渠道管理").setOrderNo(77).setParentId(ROOT_ID);
        firstMenu = get(add(firstMenu));

        secondMenu = new MenuEntity().setName("供应商管理").setPath("/console/channel/supplier/list").setParentId(firstMenu.getId());
        add(secondMenu);
        secondMenu = new MenuEntity().setName("采购单价").setPath("/console/channel/purchasePrice/list").setParentId(firstMenu.getId());
        add(secondMenu);
        secondMenu = new MenuEntity().setName("采购管理").setPath("/console/channel/purchase/list").setParentId(firstMenu.getId());
        add(secondMenu);
        secondMenu = new MenuEntity().setName("客户管理").setPath("/console/channel/customer/list").setParentId(firstMenu.getId());
        add(secondMenu);
        secondMenu = new MenuEntity().setName("销售单价").setPath("/console/channel/salePrice/list").setParentId(firstMenu.getId());
        add(secondMenu);
        secondMenu = new MenuEntity().setName("销售管理").setPath("/console/channel/sale/list").setParentId(firstMenu.getId());
        add(secondMenu);


        // 仓储管理 - WMS
        firstMenu = new MenuEntity().setName("仓储管理").setOrderNo(66).setParentId(ROOT_ID);
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
        firstMenu = new MenuEntity().setName("生产管理").setOrderNo(55).setParentId(ROOT_ID);
        firstMenu = get(add(firstMenu));

        secondMenu = new MenuEntity().setName("物料领取").setPath("/console/mes/picking/list").setParentId(firstMenu.getId());
        add(secondMenu);
        secondMenu = new MenuEntity().setName("计划管理").setPath("/console/mes/plan/list").setParentId(firstMenu.getId());
        add(secondMenu);
        secondMenu = new MenuEntity().setName("订单管理").setPath("/console/mes/order/list").setParentId(firstMenu.getId());
        add(secondMenu);
        secondMenu = new MenuEntity().setName("生产工艺").setPath("/console/mes/routing/list").setParentId(firstMenu.getId());
        add(secondMenu);
        secondMenu = new MenuEntity().setName("生产工序").setPath("/console/mes/operation/list").setParentId(firstMenu.getId());
        add(secondMenu);
        secondMenu = new MenuEntity().setName("生产配方").setPath("/console/mes/bom/list").setParentId(firstMenu.getId());
        add(secondMenu);


        // 物联网
//        firstMenu = new MenuEntity().setName("设备物联").setOrderNo(44).setParentId(TreeUtil.ROOT_ID);
//        firstMenu = get(add(firstMenu));
//
//        secondMenu = new MenuEntity().setName("设备概览").setPath("/console/iot/monitor/preview").setParentId(firstMenu.getId());
//        add(secondMenu);
//        secondMenu = new MenuEntity().setName("参数管理").setPath("/console/iot/parameter/list").setParentId(firstMenu.getId());
//        add(secondMenu);

        // 系统管理
        firstMenu = new MenuEntity().setName("系统管理").setOrderNo(3).setParentId(ROOT_ID);
        firstMenu = get(add(firstMenu));

        secondMenu = new MenuEntity().setName("仓库管理").setPath("/console/factory/storage/list").setParentId(firstMenu.getId());
        add(secondMenu);
        secondMenu = new MenuEntity().setName("生产单元").setPath("/console/factory/structure/list").setParentId(firstMenu.getId());
        add(secondMenu);
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
        firstMenu = new MenuEntity().setName("超管配置").setOrderNo(2).setParentId(ROOT_ID);
        firstMenu = get(add(firstMenu));

        secondMenu = new MenuEntity().setName("权限管理").setPath("/console/system/permission/list").setParentId(firstMenu.getId());
        add(secondMenu);
        secondMenu = new MenuEntity().setName("菜单管理").setPath("/console/system/menu/list").setParentId(firstMenu.getId());
        add(secondMenu);
        secondMenu = new MenuEntity().setName("系统配置").setPath("/console/system/config/list").setParentId(firstMenu.getId());
        add(secondMenu);
        secondMenu = new MenuEntity().setName("应用管理").setPath("/console/open/app/list").setParentId(firstMenu.getId());
        add(secondMenu);
        secondMenu = new MenuEntity().setName("通知管理").setPath("/console/open/notify/list").setParentId(firstMenu.getId());
        add(secondMenu);
        secondMenu = new MenuEntity().setName("MCP工具").setPath("/console/system/mcp/tools").setParentId(firstMenu.getId());
        add(secondMenu);
    }
}
