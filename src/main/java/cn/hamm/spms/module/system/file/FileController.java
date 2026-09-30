package cn.hamm.spms.module.system.file;

import cn.hamm.airpower.api.annotation.Api;
import cn.hamm.airpower.core.DictionaryUtil;
import cn.hamm.airpower.core.Json;
import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.curd.permission.Permission;
import cn.hamm.airpower.file.FileHelper;
import cn.hamm.spms.base.BaseController;
import cn.hamm.spms.common.AppConstant;
import cn.hamm.spms.module.personnel.user.UserEntity;
import cn.hamm.spms.module.system.file.enums.FileCategory;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

import static cn.hamm.airpower.exception.Errors.*;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@Api("file")
@Description("文件")
public class FileController extends BaseController<FileEntity, FileService, FileRepository> {
    @Autowired
    private FileHelper fileHelper;

    @PostMapping("upload")
    @Description("文件上传")
    @Permission(authorize = false)
    public Json upload(@NotNull(message = "文件不能为空") @RequestParam("file") MultipartFile file, @RequestParam("category") Integer category) {
        category = Objects.requireNonNullElse(category, FileCategory.NORMAL.getKey());
        FileCategory fileCategory = DictionaryUtil.getDictionary(FileCategory.class, category);
        return Json.data(service.upload(file, fileCategory, getCurrentUserId()), "文件上传成功");
    }

    /**
     * 获取文件
     * <p>
     * 文件主键是自增 ID，若接口免登录且无归属校验，
     * 任何人遍历 {@code ?id=1,2,3...} 即可下载全站所有附件。
     * </p>
     *
     * @param id       文件 ID
     * @param response 响应对象
     * @throws IOException 重定向失败
     */
    @RequestMapping("")
    @Description("获取文件")
    @Permission(authorize = false)
    public void getFileUrl(@NotNull(message = "文件ID不能为空") @RequestParam("id") String id, HttpServletResponse response) throws IOException {
        long fileId;
        try {
            fileId = Long.parseLong(id.trim());
        } catch (NumberFormatException e) {
            PARAM_INVALID.show("文件ID格式不正确");
            return;
        }
        FileEntity file = service.get(fileId);
        DATA_NOT_FOUND.whenNull(file, "文件不存在");
        FileCategory fileCategory = DictionaryUtil.getDictionary(FileCategory.class, file.getCategory());
        if (Boolean.TRUE.equals(fileCategory.getIsProtected())) {
            long currentUserId = getCurrentUserId();
            boolean isRoot = new UserEntity().setId(currentUserId).isRootUser();
            boolean isOwner = Objects.nonNull(file.getUploader())
                    && Objects.equals(file.getUploader().getId(), currentUserId);
            FORBIDDEN.when(!isRoot && !isOwner, "无权访问该文件");
        }
        // 平台必须取记录中保存的那个，不能一律使用全局默认平台
        response.sendRedirect(fileHelper.getPlatform(file.getPlatform()).getUrl(file.getUrl(), 3000));
    }
}
