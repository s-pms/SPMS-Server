package cn.hamm.spms.common.config;

import cn.hamm.spms.common.enums.UploadPlatform;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import static cn.hamm.airpower.util.FileUtil.FILE_SCALE;
import static cn.hamm.spms.common.enums.UploadPlatform.LOCAL;

/**
 * <h1>应用配置文件</h1>
 *
 * @author Hamm.cn
 */
@Component
@Data
@Accessors(chain = true)
@Configuration
@ConfigurationProperties("app")
public class AppConfig {
    /**
     * <h3>项目名称</h3>
     */
    private String projectName = "SPMS";

    /**
     * <h3>登录地址 用于 {@code OAuth2}</h3>
     */
    private String loginUrl;

    /**
     * <h3>授权地址 用于 {@code OAuth2}</h3>
     */
    private String authorizeUrl;

    /**
     * <h3>默认房间ID {@code 不是房间号}</h3>
     */
    private long defaultRoomId = 1L;

    /**
     * <h3>上传文件目录</h3>
     */
    private String uploadDirectory = "upload";
    /**
     * <h3>上传文件最大大小</h3>
     */
    private long uploadMaxSize = FILE_SCALE * FILE_SCALE * 10;
    /**
     * <h3>上传文件允许的扩展名</h3>
     */
    private String[] uploadAllowExtensions = new String[]{
            "jpg", "jpeg", "png", "gif", "bmp",
            "mp4",
            "mp3", "wav", "wma",
            "zip", "rar", "7z", "tar", "gz",
            "pdf", "doc", "docx", "xls", "xlsx",
            "markdown"
    };
    /**
     * <h3>上传平台</h3>
     */
    private UploadPlatform uploadPlatform = LOCAL;
}
