package com.brpc.client;

import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.AnnotationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;

import io.swagger.annotations.Api;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.lang.annotation.Annotation;
import java.util.Map;

@Configuration
@EnableSwagger2
public class Swagger2 implements InitializingBean, ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(Swagger2.class);

    @Value("${server.port}")
    private int port;

    @Value("${swagger.basePath:/}")
    private String basePath;

    private ApplicationContext ctx;

    @Autowired
    RequestMappingHandlerMapping requestMappingHandlerMapping;


    @Bean
    public Docket createRestApi() {

        return new Docket(DocumentationType.SWAGGER_2)//
                //
                .groupName("RestfulApi")
                //
                .apiInfo(apiInfo())
                //
                .genericModelSubstitutes(ResponseEntity.class)
                //
                .useDefaultResponseMessages(true).forCodeGeneration(true)
                //
                .pathMapping(basePath).select()
                //
                .apis(RequestHandlerSelectors.withClassAnnotation(Api.class)).paths(PathSelectors.any())

                // .paths(or(regex("/api.*")))
                //
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder().title("欢迎使用Swagger2，请在需要进行API管理的地方添加 @RestController 和 @Api").description("").termsOfServiceUrl("").contact("").version("1.0").build();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("init swagger2 ui http://" + Utils.getLocal() + ":" + port + "/swagger-ui.html");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.ctx = applicationContext;
    }
}
