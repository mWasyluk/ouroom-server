package pl.mwasyluk.ouroom_server.domain.media;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import pl.mwasyluk.ouroom_server.controllers.MediaController;
import pl.mwasyluk.ouroom_server.services.media.MediaService;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static pl.mwasyluk.ouroom_server.domain.media.source.DataSourceTestUtil.IN_JPEG_SOURCE;
import static pl.mwasyluk.ouroom_server.domain.media.source.DataSourceTestUtil.IN_PNG_SOURCE;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureWebMvc
@ActiveProfiles(profiles = "test")
//@ComponentScan(basePackages = {"pl.mwasyluk.ouroom_server"})
//@WebMvcTest(MediaController.class)
//@WebAppConfiguration
//@ContextConfiguration(classes = {TestConfiguration.class})
//@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
//@TestPropertySource("classpath:application-test.properties")
public class MediaIntegrationTest {

    @Autowired
    Environment environment;

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Nested
    @DisplayName("getInternalUrl method")
    class GetInternalUrlMethodTest {

        @Test
        @DisplayName("returns internal url for internal source")
        void returnsInternalUrlWhenInternalSource() throws UnknownHostException {
            Media i1 = new Image(IN_PNG_SOURCE);
            Media i2 = new Image(IN_JPEG_SOURCE);
//            Media i3 = new Image(IN_MP4_SOURCE);

            String protocol = "http";
            String hostname = InetAddress.getLoopbackAddress().getHostName();
            String serverAddress = protocol + "://" + hostname + ":" + port;

            assertAll(() -> {
                assertNotNull(i1.getInternalUrl());
                assertNotNull(i2.getInternalUrl());

                // FIXME: assert correct server address and media id presence after fixing
                //  https://github.com/mWasyluk/ouroom-server/issues/6
                System.out.println(serverAddress);
                System.out.println(i1.getInternalUrl());
                System.out.println(environment.getProperty("server.api.prefix"));
//                assertTrue(i1.getInternalUrl().startsWith(serverAddress));
//                assertTrue(i2.getInternalUrl().startsWith(serverAddress));
            });
        }
    }
}
