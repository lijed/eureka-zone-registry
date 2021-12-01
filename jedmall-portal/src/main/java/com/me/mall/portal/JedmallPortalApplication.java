package com.me.mall.portal;

        import org.springframework.boot.SpringApplication;
        import org.springframework.boot.autoconfigure.SpringBootApplication;
        import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@SpringBootApplication
public class JedmallPortalApplication {

    public static void main(String[] args) {
        SpringApplication.run(JedmallPortalApplication.class, args);
    }

}
