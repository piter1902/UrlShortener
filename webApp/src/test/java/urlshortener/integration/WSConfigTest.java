package urlshortener.integration;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Consumer;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Websocket configuration test
 * Source: https://rafaelhz.github.io/testing-websockets/
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class WSConfigTest {
    private static final Logger log = LoggerFactory.getLogger(WSConfigTest.class);

    @Value("${local.server.port}")
    private int port;

    private WebSocketStompClient stompClient1;
    private StompSession stompSession1;

    private WebSocketStompClient stompClient2;
    private StompSession stompSession2;

    static final String WEBSOCKET_TOPIC = "/user/topic/getCSV";
    static final String ENDPOINT_REGISTER = "/app/uploadCSV";

    @Before
    public void setUp() throws Exception {
        String wsUrl = String.format("ws://localhost:%d/ws-uploadCSV", port);

        stompClient1 = createWebSocketClient();
//        stompSession1 = stompClient1.connect(wsUrl, new ClientSessionHandler()).get();
        stompSession1 = stompClient1.connect(wsUrl, new StompSessionHandlerAdapter() {
        }).get(4, SECONDS);


        stompClient2 = createWebSocketClient();
//        stompSession2 = stompClient2.connect(wsUrl, new ClientSessionHandler()).get();
        stompSession2 = stompClient2.connect(wsUrl, new StompSessionHandlerAdapter() {
        }).get(4, SECONDS);
    }

    @After
    public void tearDown() throws Exception {
        stompSession1.disconnect();
        stompClient1.stop();

        stompSession2.disconnect();
        stompClient2.stop();
    }

    @Test
    public void receivesMessageFromSubscribedQueue() throws Exception {

        log.info("### client1 subscribes");

        BlockingQueue<String> userQueue1 = new LinkedBlockingDeque<>();

        stompSession1.subscribe(WEBSOCKET_TOPIC,
                new ClientFrameHandler((payload) -> {
                    log.info("--> " + WEBSOCKET_TOPIC + " (cli1) : " + payload);
                    userQueue1.offer(payload.toString());
                }));
        Thread.sleep(100);

        log.info("### client2 subscribes");

        BlockingQueue<String> userQueue2 = new LinkedBlockingDeque<>();

        stompSession2.subscribe(WEBSOCKET_TOPIC,
                new ClientFrameHandler((payload) -> {
                    log.info("--> " + WEBSOCKET_TOPIC + " (cli2) : " + payload);
                    userQueue2.offer(payload.toString());
                }));

        Thread.sleep(100);

        log.info("### client1 registers");
        stompSession1.send(ENDPOINT_REGISTER, "Test cli1");
        Thread.sleep(100);
        Assert.assertEquals("Test cli1,,debe ser una URI http o https", userQueue1.poll());

        Thread.sleep(100);

        log.info("### client2 registers");
        stompSession2.send(ENDPOINT_REGISTER, "Test cli2");
        Thread.sleep(100);
        Assert.assertEquals("Test cli2,,debe ser una URI http o https", userQueue2.poll());
    }

    public WebSocketStompClient createWebSocketClient() {
        WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(
                Collections.singletonList(new WebSocketTransport(new StandardWebSocketClient()))));
        stompClient.setMessageConverter(new StringMessageConverter());
        return stompClient;
    }

    private static class ClientFrameHandler implements StompFrameHandler {
        private final Consumer<String> frameHandler;

        public ClientFrameHandler(Consumer<String> frameHandler) {
            this.frameHandler = frameHandler;
        }

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return String.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            assert payload != null;
            frameHandler.accept(payload.toString());
        }
    }
}
