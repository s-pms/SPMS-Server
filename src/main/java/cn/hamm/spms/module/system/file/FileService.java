package cn.hamm.spms.module.system.file;

import cn.hamm.airpower.core.FileUtil;
import cn.hamm.airpower.core.exception.ServiceException;
import cn.hamm.airpower.web.file.FileConfig;
import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.common.aliyun.oss.AliyunOssUtil;
import cn.hamm.spms.common.config.AppConfig;
import cn.hamm.spms.module.system.file.enums.FileCategory;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import static cn.hamm.airpower.web.exception.ServiceError.FORBIDDEN_UPLOAD_MAX_SIZE;
import static cn.hamm.airpower.web.exception.ServiceError.PARAM_INVALID;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Service
@Slf4j
public class FileService extends BaseService<FileEntity, FileRepository> {
    @Autowired
    private AppConfig appConfig;

    @Autowired
    private FileConfig fileConfig;

    @Autowired
    private AliyunOssUtil aliyunOssUtil;

    /**
     * 上传到阿里云 OSS
     *
     * @param multipartFile 文件
     * @param savedFilePath 文件路径
     * @throws IOException 文件写入失败
     */
    private void saveToAliyunOss(@NotNull MultipartFile multipartFile, @NotNull String savedFilePath) throws IOException {
        aliyunOssUtil.upload(savedFilePath, multipartFile.getInputStream().readAllBytes());
    }

    /**
     * 文件上传
     *
     * @param multipartFile 文件
     * @param fileCategory  文件类别
     * @return 存储的文件信息
     */
    public FileEntity upload(@NotNull MultipartFile multipartFile, @NotNull FileCategory fileCategory) {
        // 上传文件的绝对路径
        final String absoluteDirectory = fileConfig.getFileDirectory();
        // 判断文件大小和类型
        FORBIDDEN_UPLOAD_MAX_SIZE.when(multipartFile.getSize() > appConfig.getUploadMaxSize());
        String originalFilename = multipartFile.getOriginalFilename();
        PARAM_INVALID.whenNull(originalFilename, "文件名不能为空");
        String extension = FileUtil.getExtension(originalFilename);
        PARAM_INVALID.whenEmpty(extension, "文件类型不能为空");
        PARAM_INVALID.when(!Arrays.stream(fileCategory.getExtensions()).toList().contains(extension), "文件类型不允许上传");

        // 存储的相对路径目录
        String relativeDirectory = fileConfig.getUploadDirectory() + fileCategory.name().toLowerCase() + "/" + FileUtil.getTodayDirectory();

        try {
            // 获取文件的MD5
            String hashMd5 = DigestUtils.md5DigestAsHex(multipartFile.getBytes());
            FileEntity file = repository.getByCategoryAndHashMd5(fileCategory.getKey(), hashMd5);
            if (Objects.nonNull(file)) {
                return file;
            }

            // 文件名
            String fileName = hashMd5 + "." + extension;

            // 保存的相对文件路径
            String relativeFilePath = relativeDirectory + fileName;

            switch (appConfig.getUploadPlatform()) {
                case LOCAL -> FileUtil.saveFile(absoluteDirectory + relativeDirectory,
                        fileName,
                        multipartFile.getBytes()
                );
                case ALIYUN_OSS -> saveToAliyunOss(multipartFile, relativeFilePath);
                default -> throw new ServiceException("暂不支持该平台");
            }

            file = new FileEntity().setExtension(extension)
                    .setSize(multipartFile.getSize())
                    .setPlatform(appConfig.getUploadPlatform().getKey())
                    .setCategory(fileCategory.getKey())
                    .setName(multipartFile.getOriginalFilename())
                    .setHashMd5(hashMd5)
                    .setUrl(relativeFilePath);
            return addAndGet(file);
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
            throw new ServiceException(exception);
        }
    }
}
