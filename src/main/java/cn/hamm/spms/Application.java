package cn.hamm.spms;

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

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        if (serverApplicationContext != null) {
            int port = serverApplicationContext.getWebServer().getPort();
            System.out.println("------------------------------------------");
            System.out.println("   Hi Guy, Service is listen : [" + port + "] !");
            System.out.println("------------------------------------------");
        }
    }

    @Autowired(required = false)
    public void autorun(ServletWebServerApplicationContext serverApplicationContext) {
        Application.serverApplicationContext = serverApplicationContext;
        // ReportEventListener 注入后可开启MQTT监听
    }
}
                