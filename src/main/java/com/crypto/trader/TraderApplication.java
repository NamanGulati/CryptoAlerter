package com.crypto.trader;

import com.crypto.trader.service.TraderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication
public class TraderApplication  {

    @Autowired
    TraderService service;

    public static void main(String[] args) {
        SpringApplication.run(TraderApplication.class, args);
    }

    @Scheduled(fixedDelay = 600000, initialDelay = 0)
    public void scheduledRunner(){
        service.trade();
    }

}
