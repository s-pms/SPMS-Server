package cn.hamm.spms.common.config;

import cn.hamm.airpower.core.FileUtil;
import cn.hamm.spms.module.system.file.enums.FileUploadPlatform;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import static cn.hamm.airpower.core.FileUtil.FILE_SCALE;
import static cn.hamm.spms.module.system.file.enums.FileUploadPlatform.LOCAL;

/**
 * <h1>应用配置文件</h1>
 *
 * @author Hamm.cn
 */
@Data
@Configuration
@ConfigurationProperties("app")
public class AppConfig {
    /**
     * 项目名称
     */
    private String projectName = "SPMS";

    /**
     * 默认房间 ID {@code 不是房间号}
     */
    private long defaultRoomId = 1L;

    /**
     * 上传文件最大大小
     */
    private long uploadMaxSize = FILE_SCALE * FILE_SCALE * 10;

    /**
     * 上传平台
     */
    private FileUploadPlatform uploadPlatform = LOCAL;

    /**
     * 是否是开发模式
     */
    private Boolean isDevMode = false;

    /**
     * 登录 URL
     */
    private String loginUrl;

    /**
     * 上传文件目录
     */
    private String fileDirectory = "/home/static/";

    /**
     * 上传文件目录
     */
    private String uploadDirectory = "upload";

    /**
     * 获取文件目录
     *
     * @return 文件目录
     */
    public String getFileDirectory() {
        return FileUtil.formatDirectory(fileDirectory);
    }

    /**
     * 获取上传文件目录
     *
     * @return 上传文件目录
     */
    public String getUploadDirectory() {
        return FileUtil.formatDirectory(uploadDirectory);
    }
}
