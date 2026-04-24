package cn.hamm.spms.module.personnel.user.token;

import cn.hamm.airpower.api.annotation.Api;
import cn.hamm.airpower.core.Json;
import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.core.annotation.ExposeAll;
import cn.hamm.airpower.curd.annotation.Extends;
import cn.hamm.airpower.curd.base.Curd;
import cn.hamm.airpower.curd.model.query.QueryListRequest;
import cn.hamm.airpower.curd.model.query.QueryPageRequest;
import cn.hamm.airpower.curd.permission.Permission;
import cn.hamm.spms.base.BaseController;
import cn.hamm.spms.module.personnel.user.UserEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import static cn.hamm.airpower.exception.Errors.FORBIDDEN_EDIT;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@Api("personalToken")
@Description("私人令牌")
@Extends({Curd.Disable, Curd.Enable})
public class PersonalTokenController extends BaseController<PersonalTokenEntity, PersonalTokenService, PersonalTokenRepository> {
    @Description("获取我的私人令牌列表")
    @Permission(authorize = false)
    @PostMapping("getMyList")
    @ExposeAll(PersonalTokenEntity.class)
    public Json getMyList() {
        UserEntity user = new UserEntity().setId(getCurrentUserId());
        List<PersonalTokenEntity> list = service.filter(new PersonalTokenEntity().setUser(user));
        return Json.data(list);
    }

    @Description("禁用我的令牌")
    @Permission(authorize = false)
    @PostMapping("disableMy")
    public Json disableMy(@RequestBody @Validated(WhenIdRequired.class) PersonalTokenEntity personalToken) {
        PersonalTokenEntity exist = service.get(personalToken.getId());
        FORBIDDEN_EDIT.whenNotEquals(exist.getUser().getId(), getCurrentUserId(), "你没有权限禁用此令牌");
        service.disable(personalToken.getId());
        return Json.success("禁用成功");
    }

    @Override
    protected PersonalTokenEntity beforeAdd(@NotNull PersonalTokenEntity entity) {
        throw new IllegalStateException("请使用createMy");
    }

    @Override
    protected void beforeDisable(@NotNull PersonalTokenEntity entity) {
        throw new IllegalStateException("请使用disableMy");
    }

    @Override
    protected QueryPageRequest<PersonalTokenEntity> beforeGetPage(QueryPageRequest<PersonalTokenEntity> queryPageRequest) {
        throw new IllegalStateException("请使用getMyList");
    }

    @Override
    protected QueryListRequest<PersonalTokenEntity> beforeGetList(QueryListRequest<PersonalTokenEntity> queryListRequest) {
        throw new IllegalStateException("请使用getMyList");
    }

    @Override
    protected void beforeEnable(@NotNull PersonalTokenEntity entity) {
        throw new IllegalStateException("请使用enableMy");
    }

    @Override
    protected void beforeAppDelete(@NotNull PersonalTokenEntity entity) {
        throw new IllegalStateException("请使用deleteMy");
    }

    @Description("启用我的令牌")
    @Permission(authorize = false)
    @PostMapping("enableMy")
    public Json enableMy(@RequestBody @Validated(WhenIdRequired.class) PersonalTokenEntity personalToken) {
        PersonalTokenEntity exist = service.get(personalToken.getId());
        FORBIDDEN_EDIT.whenNotEquals(exist.getUser().getId(), getCurrentUserId(), "你没有权限启用此令牌");
        service.enable(personalToken.getId());
        return Json.success("启用成功");
    }

    @Description("删除我的令牌")
    @Permission(authorize = false)
    @PostMapping("deleteMy")
    public Json deleteMy(@RequestBody @Validated(WhenIdRequired.class) PersonalTokenEntity personal) {
        PersonalTokenEntity exist = service.get(personal.getId());
        FORBIDDEN_EDIT.whenNotEquals(exist.getUser().getId(), getCurrentUserId(), "你没有权限删除此令牌");
        service.delete(personal.getId());
        return Json.success("删除成功");
    }

    @Description("创建我的令牌")
    @Permission(authorize = false)
    @PostMapping("createMy")
    public Json createMy(@RequestBody @Validated(WhenAdd.class) PersonalTokenEntity personal) {
        PersonalTokenEntity personalToken = service.addAndGet(personal.setUser(new UserEntity().setId(getCurrentUserId())));
        String token = personalToken.getToken();
        return Json.data(token, "创建成功");
    }
}
