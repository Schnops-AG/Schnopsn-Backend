package at.kaindorf.schnopsn.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebsocketConfigurer implements WebSocketConfigurer {

    /**
     * used to set all the websocket handlers with their URL
     */
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new SocketHandler(), "/schnopsn").setAllowedOrigins("*"); // URL --> ws://localhost:8080/schnopsn
    }
}
