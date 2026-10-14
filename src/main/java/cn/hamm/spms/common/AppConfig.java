package cn.hamm.spms.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * <h1>应用配置</h1>
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
     * 未配置房间时用户默认进入的房间 ID，不是房间号
     */
    private long defaultRoomId = 1L;

    /**
     * 是否开发者模式，决定是否初始化演示数据
     */
    private Boolean isDevMode = false;

    /**
     * 登录 URL
     */
    private String loginUrl;

    /**
     * 设备上报静默告警阈值，单位秒
     *
     * @apiNote 超过该时长没有收到任何设备上报，{@code /actuator/health} 的 report 项会置为 DOWN。
     * 设为 0 表示关闭该检查。注意这是「有没有数据进来」的判断，
     * 不区分是没设备上报还是订阅已经断开
     */
    private long iotReportSilenceSeconds = 300L;
}
