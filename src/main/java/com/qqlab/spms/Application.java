package com.qqlab.spms;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

/**
 * <h1>入口类</h1>
 *
 * @author Hamm
 */
@EnableAutoConfiguration
@ComponentScan({"cn.hamm.airpower", "com.qqlab.spms"})
@EnableWebSocket
@EnableScheduling
public class Application {
    private static Initializer initializer;

    public static void main(String[] args) throws MqttException {
        SpringApplication.run(Application.class, args);
        initializer.run();
        System.out.println("---------------------------------");
        System.out.println("   Hi Guy, Service is running!   ");
        System.out.println("   URL:  http://127.0.0.1:8080   ");
        System.out.println("---------------------------------");
    }

    @Autowired
    public void autorun(Initializer initializer) {
        Application.initializer = initializer;
    }

}
