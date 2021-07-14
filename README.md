## Quick Start

### dubbo client

#### apache dubbo 

springboot 用户引入如下包
```xml
<groupId>com.future94</groupId>
<artifactId>swallow-client-apache-dubbo-starter</artifactId>
<version>${latest.version}</version>
```


#### alibaba dubbo

springboot 用户引入如下包
```xml
<groupId>com.future94</groupId>
<artifactId>swallow-client-alibaba-dubbo-starter</artifactId>
<version>${latest.version}</version>
```

#### 指定数据中心配置
```yaml
swallow:
    server:
        server-list: http://127.0.0.1:9507
        context-path: test
        app-name: test
```

#### 使用com.future94.swallow.client.dubbo.common.annotation.SwallowDubboClient注解同步数据

```java
@Service
@DubboService
public class TestDubboServiceImpl implements TestFacade {

    @SwallowDubboClient(path = "/name", pathDesc = "测试")
    @Override
    public String reg(String name) {
        throw new RuntimeException();
    }

    @SwallowDubboClient(path = "/ok", pathDesc = "测试")
    @Override
    public String ok(String name) {
        return name;
    }
}
```

### web 端

#### web
spring webflux 用户引入下面包
```xml
<groupId>com.future94</groupId>
<artifactId>swallow-webflux-spring-boot-starter</artifactId>
<version>${latest.version}</version>
```

#### dubbo generic
apache dubbo 用户引入下面包
```xml
<groupId>com.future94</groupId>
<artifactId>swallow-apache-dubbo-spring-boot-starter</artifactId>
<version>${latest.version}</version>
```

alibaba dubbo 用户引入下面包
```xml
<groupId>com.future94</groupId>
<artifactId>swallow-alibaba-dubbo-spring-boot-starter</artifactId>
<version>${latest.version}</version>
```

#### sync data

http 长轮训方式引入下面包
```xml
<groupId>com.future94</groupId>
<artifactId>swallow-sync-data-http-spring-boot-starter</artifactId>
<version>${latest.version}</version>
```


### 自定义返回
重写com.future94.swallow.common.dto.SwallowResponse接口