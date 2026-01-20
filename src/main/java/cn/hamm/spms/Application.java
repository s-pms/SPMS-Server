package cn.hamm.spms;

import cn.hamm.spms.common.config.AppConfig;
import cn.hamm.spms.module.iot.report.ReportEventListener;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

/**
 * <h1>入口类</h1>
 *
 * @author Hamm.cn
 */
@SpringBootApplication
@EnableWebSocket
@EnableScheduling
public class Application {
    /**
     * 服务器上下文对象
     */
    private static ServletWebServerApplicationContext serverApplicationContext;

    private static AppConfig appConfig;

    private static ReportEventListener reportEventListener;

    public static void main(String[] args) throws MqttException {
        SpringApplication.run(Application.class, args);
        if (serverApplicationContext != null) {
            int port = serverApplicationContext.getWebServer().getPort();
            System.out.println("------------------------------------------");
            System.out.println("   Hi Guy, " + appConfig.getProjectName() + " is running at [" + port + "] !");
            System.out.println("------------------------------------------");
            reportEventListener.listen();
        }
    }

    @Autowired(required = false)
    public void autorun(ServletWebServerApplicationContext serverApplicationContext, AppConfig appConfig, ReportEventListener reportEventListener) {
        Application.serverApplicationContext = serverApplicationContext;
        Application.appConfig = appConfig;
        Application.reportEventListener = reportEventListener;
    }
}
                