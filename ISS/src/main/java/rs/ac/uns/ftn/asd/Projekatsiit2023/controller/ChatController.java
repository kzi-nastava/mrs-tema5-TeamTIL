package rs.ac.uns.ftn.asd.Projekatsiit2023.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.Projekatsiit2023.config.websocket.ChatHandler;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request.ChatMessageRequestDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.ChatResponseDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.MessageResponseDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.service.ChatService;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    @Autowired
    private ChatService chatService;
    @Autowired
    private ChatHandler chatHandler;

    @GetMapping("/my/exists")
    @PreAuthorize("hasAnyRole('REGISTERED_USER', 'DRIVER')")
    public ResponseEntity<ChatResponseDTO> getExistingChat(@RequestParam String email) {
        return ResponseEntity.ok(
                chatService.getExistingChat(email).orElse(null)
        );
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<List<ChatResponseDTO>> getAllChats() {
        return ResponseEntity.ok(chatService.getAllChats());
    }

    @GetMapping("/{chatId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'REGISTERED_USER', 'DRIVER')")
    public ResponseEntity<ChatResponseDTO> getChatById(@PathVariable Integer chatId) {
        return ResponseEntity.ok(chatService.getChatById(chatId));
    }

    @PostMapping("/my/start")
    @PreAuthorize("hasAnyRole('REGISTERED_USER', 'DRIVER')")
    public ResponseEntity<ChatResponseDTO> startChatWithMessage(
            @RequestParam String email,
            @RequestBody ChatMessageRequestDTO request) {

        ChatResponseDTO chat = chatService.getOrCreateChat(email);

        request.setSenderEmail(email);
        MessageResponseDTO savedMsg = chatService.sendMessage(chat.getId(), request);

        // Broadcast to anyone viewing this chat via WebSocket
        chatHandler.broadcastToChat(chat.getId(), savedMsg);
        // Notify admin global sessions (sidebar update)
        chatHandler.notifyAdminsPublic(savedMsg);

        return ResponseEntity.ok(chatService.getChatById(chat.getId()));
    }

    @PostMapping("/{chatId}/messages")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'REGISTERED_USER', 'DRIVER')")
    public ResponseEntity<MessageResponseDTO> sendMessageRest(
            @PathVariable Integer chatId,
            @RequestBody ChatMessageRequestDTO request) {

        MessageResponseDTO saved = chatService.sendMessage(chatId, request);

        // Broadcast to anyone viewing this chat via WebSocket
        chatHandler.broadcastToChat(chatId, saved);
        // Notify admin global sessions (sidebar update)
        chatHandler.notifyAdminsPublic(saved);

        return ResponseEntity.ok(saved);
    }
}