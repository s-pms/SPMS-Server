package cn.hamm.spms.common.config;

import cn.hamm.spms.common.enums.UploadPlatform;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import static cn.hamm.airpower.file.FileUtil.FILE_SCALE;
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
     * 项目名称
     */
    private String projectName = "SPMS";

    /**
     * 登录地址 用于 {@code OAuth2}
     */
    private String loginUrl;

    /**
     * 授权地址 用于 {@code OAuth2}
     */
    private String authorizeUrl;

    /**
     * 默认房间ID {@code 不是房间号}
     */
    private long defaultRoomId = 1L;

    /**
     * 上传文件目录
     */
    private String uploadDirectory = "upload";
    /**
     * 上传文件最大大小
     */
    private long uploadMaxSize = FILE_SCALE * FILE_SCALE * 10;
    /**
     * 上传平台
     */
    private UploadPlatform uploadPlatform = LOCAL;
}
