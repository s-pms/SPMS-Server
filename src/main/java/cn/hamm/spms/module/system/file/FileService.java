package cn.hamm.spms.module.system.file;

import cn.hamm.airpower.core.exception.ServiceException;
import cn.hamm.airpower.file.FileConfig;
import cn.hamm.airpower.file.FileHelper;
import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.module.system.file.enums.FileCategory;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Service
@Slf4j
public class FileService extends BaseService<FileEntity, FileRepository> {
    @Autowired
    private FileConfig fileConfig;

    @Autowired
    private FileHelper fileHelper;

    /**
     * 文件上传
     *
     * @param multipartFile 文件
     * @param fileCategory  文件类别
     * @return 存储的文件信息
     */
    public FileEntity upload(@NotNull MultipartFile multipartFile, @NotNull FileCategory fileCategory) {
        // 获取文件的MD5
        String fileHash = FileHelper.getFileHash(multipartFile);
        FileEntity file = repository.getByCategoryAndHashMd5(fileCategory.getKey(), fileHash);
        if (Objects.nonNull(file)) {
            return file;
        }

        // 验证文件类型
        FileHelper.validateFileExtension(multipartFile, fileCategory.getExtensions());

        // 存储的相对路径目录
        String category = fileCategory.name().toLowerCase();
        try {
            // 文件名
            String fileUrl = fileHelper.upload(multipartFile, category);

            file = new FileEntity()
                    .setExtension(FileHelper.getFileExtension(multipartFile))
                    .setSize(multipartFile.getSize())
                    .setPlatform(fileConfig.getFilePlatform().getKey())
                    .setCategory(fileCategory.getKey())
                    .setName(multipartFile.getOriginalFilename())
                    .setHashMd5(fileHash)
                    .setUrl(fileUrl);
            return addAndGet(file);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ServiceException("上传文件失败，" + e.getMessage());
        }
    }

    /**
     * 获取文件URL
     *
     * @param url 文件URL
     * @return 文件URL
     */
    public String getUrl(String url) {
        return fileHelper.getUrl(url);
    }
}
