package com.github.xiaoyao9184.eproject.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

import java.util.HashMap;

/**
 * Created by xy on 2021/2/25.
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(EurekaServerApplication.class);
        app.setDefaultProperties(new HashMap<String, Object>() {{
            put("spring.profiles.default", "dev");
        }});
        app.run(args);
    }
}
