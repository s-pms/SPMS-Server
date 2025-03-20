package cn.hamm.spms.module.system.file;

import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.core.dictionary.DictionaryUtil;
import cn.hamm.airpower.core.model.Json;
import cn.hamm.airpower.web.annotation.ApiController;
import cn.hamm.airpower.web.annotation.Permission;
import cn.hamm.spms.base.BaseController;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@ApiController("file")
@Description("文件")
public class FileController extends BaseController<FileEntity, FileService, FileRepository> {
    @PostMapping("upload")
    @Description("文件上传")
    @Permission(authorize = false)
    public Json upload(@NotNull(message = "文件不能为空") @RequestParam("file") MultipartFile file, @RequestParam("category") Integer category) {
        category = Objects.requireNonNullElse(category, FileCategory.NORMAL.getKey());
        FileCategory fileCategory = DictionaryUtil.getDictionary(FileCategory.class, category);
        return Json.data(service.upload(file, fileCategory), "文件上传成功");
    }
}
