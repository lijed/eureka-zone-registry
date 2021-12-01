# Eureka分区注册配置

随着业务全球化，或者业务在一个国家的多个地区同步进行，需要在各个地区（或者国家）的数据中心部署服务，如 北京， 上海 和广州的机房， 要实现部署在广州的微服务注册到广州的Eureka server上，且要求各个数据中心的Eureka server 是集群且相互注册，为了实现HA， 当广州的数据中心的Eureka server不可用了，可以从其他的数据中心拉取服务列表。 



Eureka server的整体架构图

region：us-east

defaultZone：us-east-1c

![image-20211201144455029](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20211201144455029.png)



# 微服务简介

Jed-Mall 微服务，包含订单， 产品， 市场， mall-portal 微服务和两个eureka server。 



我们要实现在北京地区，有两个数据中心 zone-1 和zone-2。当调用zone-1的微服务时，它优先调用zone-1的微服务，如果被依赖的服务提供者不可用，则调用部署在同地区的其他机房里微服务。 



![image-20211201142104296](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20211201142104296.png)

# 代码实现

## Eureka server

### Java 代码

```java
package com.me.mall.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }

}
```

### property 配置

####  eureka server1的配置

```java
server.port=8761
spring.application.name=eureka-server

eureka.client.register-with-eureka=true

eureka.client.service-url.defaultZone=http://${application.ipaddress}:8762/eureka

#eureka默认是按照hostname来比较集群里node是否是同一个，
#因为我配置eureka.server.hostname=ip 地址，所有eureka 认为他们就是同一个server
eureka.server.my-url=http://${application.ipaddress}:${server.port}/eureka

eureka.client.region=beijing
eureka.client.availability-zones.beijing=zone-1,zone-2

eureka.client.service-url.zone-1=http://${application.ipaddress}:8761/eureka
eureka.client.service-url.zone-2=http://${application.ipaddress}:8762/eureka

```

####  eureka server2的配置

```properties
server.port=8762
spring.application.name=eureka-server-replicator
eureka.client.register-with-eureka=true

eureka.client.service-url.defaultZone=http://${application.ipaddress}:8761/eureka

#eureka默认是按照hostname来比较集群里node是否是同一个，
#因为我配置eureka.server.hostname=ip 地址，所有eureka 认为他们就是同一个server
eureka.server.my-url=http://${application.ipaddress}:${server.port}/eureka

### 地区
eureka.client.region=beijing
eureka.client.availability-zones.beijing=zone-1,zone-2

eureka.client.service-url.zone-1=http://${application.ipaddress}:8761/eureka
eureka.client.service-url.zone-2=http://${application.ipaddress}:8762/eureka
```

### Maven 依赖

```xml
  <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
```

## Goods 微服务

### Java代码

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@SpringBootApplication
public class GoodsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GoodsServiceApplication.class, args);
    }

}
```

### Property 配置

#### Goods MS 1 的配置

```properties
server.port=9090

#eureka.client.service-url.defaultZone=http://localhost:8761/eureka

eureka.client.region=beijing

eureka.client.availability-zones.beijing=zone-1,zone-2
eureka.client.service-url.zone-1=http://localhost:8761/eureka
eureka.client.service-url.zone-2=http://localhost:8762/eureka

#默认的zone-1
eureka.instance.metadata-map.zone=zone-1

eureka.client.prefer-same-zone-eureka=true

eureka.client.refresh.enable=false
```

#### Goods MS 2 的配置

```properties
server.port=9091

eureka.client.region=beijing
eureka.client.availability-zones.beijing=zone-1,zone-2
eureka.client.service-url.zone-1=http://localhost:8761/eureka
eureka.client.service-url.zone-2=http://localhost:8762/eureka

#默认的zone-2
eureka.instance.metadata-map.zone=zone-2

eureka.client.prefer-same-zone-eureka=true

#By default, the EurekaClient bean is refreshable, meaning the Eureka client properties can be changed and refreshed.
# When a refresh occurs clients will be unregistered from the Eureka server and there might be a brief moment of time where
# all instance of a given service are not available.
# One way to eliminate this from happening is to disable the ability to refresh Eureka clients. To do this set eureka.client.refresh.enable=false.
eureka.client.healthcheck.enabled=true
```

### Maven 依赖

```xml
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
    </dependencies>
```



## Order 微服务

### Java 代码

​	

```java
package com.me.mall.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@SpringBootApplication
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }

}
```

### Property 配置

```properties
spring.application.name=order-service

server.port=9093

#
application.ipaddress=${spring.cloud.client.ip-address}

eureka.instance.prefer-ip-address=true
eureka.instance.instance-id=${spring.application.name}-${spring.cloud.client.ip-address}-${server.port}
eureka.instance.hostname= ${application.ipaddress}


eureka.client.service-url.defaultZone=http://${application.ipaddress}:8761/eureka,http://${application.ipaddress}:8762/eureka
management.endpoints.web.exposure.include=*
```

### Maven 依赖

```xml
   <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
    </dependencies>
```



## Marketing微服务

### Java 代码

```java
package com.me.mall.marketingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@SpringBootApplication
public class MarketingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketingServiceApplication.class, args);
    }

}
```

### Property 配置

```properties
spring.application.name=marketing-service

server.port=9092

#
application.ipaddress=${spring.cloud.client.ip-address}

eureka.instance.prefer-ip-address=true
eureka.instance.instance-id=${spring.application.name}-${spring.cloud.client.ip-address}-${server.port}
eureka.instance.hostname= ${application.ipaddress}


eureka.client.service-url.defaultZone=http://${application.ipaddress}:8761/eureka,http://${application.ipaddress}:8762/eureka

management.endpoints.web.exposure.include=*
```

### Maven 依赖



```xml
   <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>   
<dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
</dependencies>
```

## JedMall 微服务

### java 代码

```java
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
```

### Property配置

```properties
spring.application.name=mall-portal
server.port=8080


#goods-service.ribbon.listOfServers=\
#  http://localhost:9090/goods,http://localhost:9093/goods


#goods-service.ribbon.NFLoadBalancerRuleClassName=com.gupaoedu.malll.gpmallportal.GpDefinieIpHashRule
#goods-service.ribbon.NFLoadBalancerPingClassName=com.gupaoedu.malll.gpmallportal.HealthChecker
#goods-service.ribbon.NFLoadBalancerPingInterval=3


#
application.ipaddress=${spring.cloud.client.ip-address}

eureka.instance.prefer-ip-address=true
eureka.instance.instance-id=${spring.application.name}-${spring.cloud.client.ip-address}-${server.port}

eureka.instance.hostname= ${application.ipaddress}


##EUreak 默认的
#eureka.client.service-url.defaultZone=http://localhost:8761/eureka,http://localhost:8762/eureka
#management.endpoints.web.exposure.include=*


###eurea configuration with datacenter region and zone
eureka.client.region=beijing
eureka.client.availability-zones.beijing=zone-1,zone-2
eureka.client.service-url.zone-1=http://localhost:8761/eureka/
eureka.client.service-url.zone-2=http://localhost:8762/eureka/

eureka.instance.metadata-map.zone=zone-2
eureka.client.prefer-same-zone-eureka=true
```

### Maven依赖

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.me.mall</groupId>
        <artifactId>jedmall-pc</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>

    <artifactId>jedmall-portal</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>jedmall-portal</name>
    <description>Demo project for Spring Boot</description>
    <properties>
        <java.version>1.8</java.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
    </dependencies>


    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
```

## 参考文档

https://docs.spring.io/spring-cloud-netflix/docs/3.0.3/reference/html/#spring-cloud-eureka-server-zones-and-regions