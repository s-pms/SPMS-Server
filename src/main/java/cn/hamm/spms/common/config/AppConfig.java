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
     * <h2>项目名称</h2>
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
     * <h2>默认房间ID {@code 不是房间号}</h2>
     */
    private long defaultRoomId = 1L;

    /**
     * <h2>上传文件目录</h2>
     */
    private String uploadDirectory = "upload";

    /**
     * <h2>上传文件最大大小</h2>
     */
    private long uploadMaxSize = FILE_SCALE * FILE_SCALE * 10;

    /**
     * <h2>上传文件允许的扩展名</h2>
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
     * <h2>上传平台</h2>
     */
    private UploadPlatform uploadPlatform = LOCAL;

    /**
     * <h3>Influx配置</h3>
     */
    private InfluxConfig influxdb = new InfluxConfig();
}
