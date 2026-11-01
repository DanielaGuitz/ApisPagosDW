package api_bancos_spring_cloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "gt.edu.umg.desarrolloweb.pagos")
@EnableFeignClients
@EnableScheduling   
public class ApiBancosSpringCloudApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiBancosSpringCloudApplication.class, args);
    }


    
}