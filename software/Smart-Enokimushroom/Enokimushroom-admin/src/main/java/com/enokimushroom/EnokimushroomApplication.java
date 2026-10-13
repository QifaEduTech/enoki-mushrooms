package com.enokimushroom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 启动程序
 * 
 * @author ruoyi
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class EnokimushroomApplication
{
    public static void main(String[] args)
    {
        // System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(EnokimushroomApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  智慧金针菇启动成功   ლ(´ڡ`ლ)ﾞ  ");
    }
}
