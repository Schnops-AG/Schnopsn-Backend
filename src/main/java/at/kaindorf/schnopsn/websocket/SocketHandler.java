package at.kaindorf.schnopsn.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SocketHandler extends TextWebSocketHandler {
    List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    /**
     * handler for all INCOMING messages
     */
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {

        System.out.println(message);
        System.out.println(message.getPayload());
        session.sendMessage(new TextMessage("a response")); // send Message to client
    }

    /**
     * will be called as soon as the connection has been established successfully
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {

        System.out.println("here??");
        sessions.add(session);
        session.sendMessage(new TextMessage("Hello from Spring!")); // send Message to client
    }
}
