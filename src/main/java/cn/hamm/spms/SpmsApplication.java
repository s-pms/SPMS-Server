package cn.hamm.spms;

import cn.hamm.spms.common.Configs;
import cn.hamm.spms.module.iot.report.ReportEventListener;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

/**
 * <h1>SPMS 启动入口</h1>
 *
 * @author Hamm.cn
 */
@SpringBootApplication
@EnableWebSocket
@EnableScheduling
public class SpmsApplication {
    private static ServletWebServerApplicationContext serverApplicationContext;

    private static ReportEventListener reportEventListener;

    public static void main(String[] args) throws MqttException {
        SpringApplication.run(SpmsApplication.class, args);
        if (serverApplicationContext != null) {
            int port = serverApplicationContext.getWebServer().getPort();
            System.out.println("------------------------------------------");
            System.out.println("   Hi Guy, " + Configs.getAppConfig().getProjectName() + " is running at [" + port + "] !");
            System.out.println("------------------------------------------");
            reportEventListener.listen();
        }
    }

    /**
     * 把容器与 MQTT 监听器交给静态的 {@link #main(String[])} 使用
     *
     * @param serverApplicationContext Web 容器上下文
     * @param reportEventListener      MQTT 采集监听器
     * @apiNote 两个 Bean 都是 {@code required = false}，所以 {@code main} 里必须判空：
     * 拿不到监听器时直接跳过 {@code listen()}，否则启动会直接失败
     */
    @Autowired(required = false)
    public void autorun(
            ServletWebServerApplicationContext serverApplicationContext,
            ReportEventListener reportEventListener
    ) {
        SpmsApplication.serverApplicationContext = serverApplicationContext;
        SpmsApplication.reportEventListener = reportEventListener;
    }
}
                