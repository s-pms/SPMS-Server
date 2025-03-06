package cn.hamm.spms.module.system.file;

import cn.hamm.airpower.exception.ServiceException;
import cn.hamm.airpower.util.DateTimeUtil;
import cn.hamm.airpower.util.FileUtil;
import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.common.config.AppConfig;
import cn.hamm.spms.common.third.aliyun.oss.AliyunOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;

import static cn.hamm.airpower.config.Constant.*;
import static cn.hamm.airpower.enums.DateTimeFormatter.FULL_DATE;
import static cn.hamm.airpower.exception.ServiceError.FORBIDDEN_UPLOAD_MAX_SIZE;
import static cn.hamm.airpower.exception.ServiceError.PARAM_INVALID;

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
    private AliyunOssUtil aliyunOssUtil;

    /**
     * <h3>上传到阿里云OSS</h3>
     *
     * @param multipartFile 文件
     * @param savedFilePath 文件路径
     * @throws IOException 文件写入失败
     */
    private void saveToAliyunOss(@NotNull MultipartFile multipartFile, @NotNull String savedFilePath) throws IOException {
        aliyunOssUtil.upload(savedFilePath, multipartFile.getInputStream().readAllBytes());
    }

    /**
     * <h3>创建文件夹</h3>
     *
     * @param pathString 文件夹路径
     */
    private void createDirectory(String pathString) {
        Path path = Paths.get(pathString);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException("自动创建文件夹失败，请确认权限是否正常");
            }
        }
    }

    /**
     * <h3>保存到本地文件</h3>
     *
     * @param multipartFile 文件
     * @param savedFilePath 文件路径
     * @throws IOException 文件写入失败
     */
    private void saveToLocalFile(@NotNull MultipartFile multipartFile, @NotNull String savedFilePath) throws IOException {
        Path path = Paths.get(savedFilePath);
        Files.write(path, multipartFile.getBytes());
    }

    /**
     * <h3>文件上传</h3>
     *
     * @param multipartFile 文件
     * @param fileCategory  文件类别
     * @return 存储的文件信息
     */
    public FileEntity upload(@NotNull MultipartFile multipartFile, FileCategory fileCategory) {
        // 判断文件大小和类型
        FORBIDDEN_UPLOAD_MAX_SIZE.when(multipartFile.getSize() > appConfig.getUploadMaxSize());
        String originalFilename = multipartFile.getOriginalFilename();
        PARAM_INVALID.whenNull(originalFilename, "文件名不能为空");
        String extension = FileUtil.getExtension(originalFilename);
        PARAM_INVALID.whenEmpty(extension, "文件类型不能为空");
        PARAM_INVALID.when(!Arrays.stream(appConfig.getUploadAllowExtensions()).toList().contains(extension), "文件类型不允许上传");

        String uploadDirectory = appConfig.getUploadDirectory();
        if (!uploadDirectory.endsWith(File.separator)) {
            uploadDirectory += File.separator;
        }
        uploadDirectory += fileCategory.name().toLowerCase() + File.separator;

        long milliSecond = System.currentTimeMillis();

        // 追加今日文件夹 定时任务将按存储文件夹进行删除过时文件
        String todayDir = DateTimeUtil.format(milliSecond,
                FULL_DATE.getValue().replaceAll(STRING_LINE, STRING_EMPTY)
        );
        String absoluteDirectory = uploadDirectory + todayDir + File.separator;

        try {
            // 获取文件的MD5
            String hashMd5 = DigestUtils.md5DigestAsHex(multipartFile.getBytes());
            FileEntity file = repository.getByCategoryAndHashMd5(fileCategory.getKey(), hashMd5);
            if (Objects.nonNull(file)) {
                return file;
            }

            // 文件名
            String fileName = hashMd5 + STRING_DOT + extension;

            // 保存的相对文件路径
            String savedFilePath = fileCategory.name().toLowerCase() + File.separator + todayDir + File.separator + fileName;

            switch (appConfig.getUploadPlatform()) {
                case LOCAL -> {
                    createDirectory(absoluteDirectory);
                    saveToLocalFile(multipartFile, absoluteDirectory + File.separator + fileName);
                }
                case ALIYUN_OSS -> saveToAliyunOss(multipartFile, savedFilePath);
                default -> throw new ServiceException("暂不支持该平台");
            }

            file = new FileEntity().setExtension(extension)
                    .setSize(multipartFile.getSize())
                    .setPlatform(appConfig.getUploadPlatform().getKey())
                    .setCategory(fileCategory.getKey())
                    .setName(multipartFile.getOriginalFilename())
                    .setHashMd5(hashMd5)
                    .setUrl(savedFilePath);
            long fileId = add(file);
            return get(fileId);
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
            throw new ServiceException(exception);
        }
    }
}
