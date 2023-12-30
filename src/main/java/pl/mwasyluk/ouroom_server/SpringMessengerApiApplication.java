package pl.mwasyluk.ouroom_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class SpringMessengerApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringMessengerApiApplication.class, args);
    }
}
