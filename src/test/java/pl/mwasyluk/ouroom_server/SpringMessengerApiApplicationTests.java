package pl.mwasyluk.ouroom_server;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.Test;

@SpringBootTest
@ActiveProfiles(profiles = "test")
class SpringMessengerApiApplicationTests {

    @Test
    void contextLoads() {
    }
}
