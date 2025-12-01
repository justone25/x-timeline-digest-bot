package life.ljs.digest.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "life.ljs.digest")
@EnableScheduling
public class DigestApplication {
    public static void main(String[] args) {
        SpringApplication.run(DigestApplication.class, args);
    }
}
