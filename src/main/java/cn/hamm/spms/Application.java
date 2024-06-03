package cn.hamm.spms;

import cn.hamm.spms.common.config.AppConfig;
import cn.hamm.spms.module.iot.report.ReportEvent;
import lombok.Getter;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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
    private static ReportEvent reportEvent;

    @Getter
    private static AppConfig appConfig;

    public static void main(String[] args) throws MqttException {
        SpringApplication.run(Application.class, args);
        reportEvent.listen();
        System.out.println("---------------------------------");
        System.out.println("   Hi Guy, Service is running!   ");
        System.out.println("   URL:  http://127.0.0.1:8080   ");
        System.out.println("---------------------------------");
    }

    @Autowired
    public void autorun(AppConfig appConfig, ReportEvent reportEvent) {
        Application.reportEvent = reportEvent;
        Application.appConfig = appConfig;
    }

}
