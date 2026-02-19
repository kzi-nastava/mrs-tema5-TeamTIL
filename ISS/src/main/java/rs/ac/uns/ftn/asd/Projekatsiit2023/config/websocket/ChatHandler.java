package rs.ac.uns.ftn.asd.Projekatsiit2023.config.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request.ChatMessageRequestDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.MessageResponseDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.service.ChatService;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatHandler extends TextWebSocketHandler {
    private final Map<Integer, Set<WebSocketSession>> chatSessions = new ConcurrentHashMap<>();

    private final Set<WebSocketSession> adminSessions = ConcurrentHashMap.newKeySet();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ChatService chatService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        if (isAdminGlobalSession(session)) {
            adminSessions.add(session);
        } else {
            Integer chatId = extractChatId(session);
            if (chatId == null) return;
            chatSessions
                    .computeIfAbsent(chatId, k -> ConcurrentHashMap.newKeySet())
                    .add(session);
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage rawMessage) throws IOException {
        Integer chatId = extractChatId(session);
        if (chatId == null) return;

        ChatMessageRequestDTO request = objectMapper.readValue(
                rawMessage.getPayload(), ChatMessageRequestDTO.class);

        MessageResponseDTO saved = chatService.sendMessage(chatId, request);
        String json = objectMapper.writeValueAsString(saved);

        // 1. Broadcast to everyone viewing this specific chat
        broadcastToChat(chatId, json);

        // 2. Notify all admin global sessions so the sidebar updates
        notifyAdminsPublic(saved);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        if (isAdminGlobalSession(session)) {
            adminSessions.remove(session);
        } else {
            Integer chatId = extractChatId(session);
            if (chatId == null) return;
            Set<WebSocketSession> sessions = chatSessions.get(chatId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) chatSessions.remove(chatId);
            }
        }
    }

    private void broadcastToChat(Integer chatId, String json) {
        Set<WebSocketSession> sessions = chatSessions.get(chatId);
        if (sessions == null) return;
        TextMessage msg = new TextMessage(json);
        for (WebSocketSession s : sessions) {
            if (s.isOpen()) {
                try { s.sendMessage(msg); } catch (IOException e) { e.printStackTrace(); }
            }
        }
    }

    public void notifyAdminsPublic(MessageResponseDTO msg) {
        if (adminSessions.isEmpty()) return;
        try {
            String notification = objectMapper.writeValueAsString(
                    Map.of(
                            "type",      "NEW_MESSAGE",
                            "chatId",    msg.getChatId(),
                            "content",   msg.getContent(),
                            "timestamp", msg.getTimestamp() != null ? msg.getTimestamp() : ""
                    )
            );
            TextMessage textMsg = new TextMessage(notification);
            for (WebSocketSession s : adminSessions) {
                if (s.isOpen()) {
                    try { s.sendMessage(textMsg); } catch (IOException e) { e.printStackTrace(); }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isAdminGlobalSession(WebSocketSession session) {
        if (session.getUri() == null) return false;
        // Admin connects to /ws/chat/admin (path ends with /admin)
        return session.getUri().getPath().endsWith("/admin");
    }

    private Integer extractChatId(WebSocketSession session) {
        if (session.getUri() == null) return null;
        String query = session.getUri().getQuery();
        if (query == null) return null;
        for (String param : query.split("&")) {
            if (param.startsWith("chatId=")) {
                try { return Integer.parseInt(param.substring(7)); }
                catch (NumberFormatException e) { return null; }
            }
        }
        return null;
    }
}
