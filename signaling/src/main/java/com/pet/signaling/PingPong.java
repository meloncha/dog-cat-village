package com.pet.signaling;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PingPong implements Runnable {

    @Override
    public void run() {
        while (true) {
            try {
                log.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
