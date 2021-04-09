package com.pet.signaling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@SpringBootApplication
@EnableWebSocket
public class SignalingApplication {

    public static void main(String[] args) {
        PingPong pingPong = new PingPong();
        Thread t = new Thread(pingPong);
        t.start();
        SpringApplication.run(SignalingApplication.class, args);
    }
}
