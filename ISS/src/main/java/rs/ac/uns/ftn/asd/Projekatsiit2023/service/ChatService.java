package rs.ac.uns.ftn.asd.Projekatsiit2023.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request.ChatMessageRequestDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.ChatResponseDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.MessageResponseDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Account;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Chat;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Message;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.AccountRepository;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.ChatRepository;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.MessageRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChatService {
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Transactional
    public Optional<ChatResponseDTO> getExistingChat(String userEmail) {
        return chatRepository.findByUser_Email(userEmail)
                .map(this::mapChatToDTO);
    }

    @Transactional
    public ChatResponseDTO getOrCreateChat(String userEmail) {
        Chat chat = chatRepository.findByUser_Email(userEmail)
                .orElseGet(() -> {
                    Account user = accountRepository.findByEmail(userEmail)
                            .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
                    Chat newChat = new Chat();
                    newChat.setUser(user);
                    newChat.setMessages(new ArrayList<>());
                    return chatRepository.save(newChat);
                });

        return mapChatToDTO(chat);
    }

    @Transactional
    public ChatResponseDTO getChatById(Integer chatId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found: " + chatId));
        return mapChatToDTO(chat);
    }

    @Transactional
    public List<ChatResponseDTO> getAllChats() {
        return chatRepository.findAllWithUsers()
                .stream()
                .map(this::mapChatToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public MessageResponseDTO sendMessage(Integer chatId, ChatMessageRequestDTO request) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found: " + chatId));

        Account sender = accountRepository.findByEmail(request.getSenderEmail())
                .orElseThrow(() -> new RuntimeException("Sender not found: " + request.getSenderEmail()));

        Message message = new Message();
        message.setContent(request.getContent());
        message.setTimestamp(LocalDateTime.now());
        message.setUserType(sender.getUserType());
        message.setChat(chat);

        Message saved = messageRepository.save(message);

        return mapMessageToDTO(saved);
    }

    public ChatResponseDTO mapChatToDTO(Chat chat) {
        List<Message> messages = messageRepository.findByChatIdOrderByTimestampAsc(chat.getId());

        List<MessageResponseDTO> messageDTOs = messages.stream()
                .map(this::mapMessageToDTO)
                .collect(Collectors.toList());

        String lastContent = null;
        String lastTime = null;
        String lastUserType = null;
        if (!messages.isEmpty()) {
            Message last = messages.get(messages.size() - 1);
            lastContent = last.getContent();
            lastTime = last.getTimestamp() != null ? last.getTimestamp().format(TIME_FMT) : null;
            lastUserType = last.getUserType() != null ? last.getUserType().name() : null;
        }

        return new ChatResponseDTO(
                chat.getId(),
                chat.getUser().getEmail(),
                chat.getUser().getFirstName(),
                chat.getUser().getLastName(),
                chat.getUser().getUserType() != null ? chat.getUser().getUserType().name() : null,
                messageDTOs,
                lastContent,
                lastTime,
                lastUserType
        );
    }

    public MessageResponseDTO mapMessageToDTO(Message message) {
        return new MessageResponseDTO(
                message.getId(),
                message.getContent(),
                message.getTimestamp() != null ? message.getTimestamp().format(TIME_FMT) : null,
                message.getTimestamp() != null ? message.getTimestamp().toLocalDate().toString() : null,
                message.getUserType() != null ? message.getUserType().name() : null,
                message.getChat().getId()
        );
    }
}
