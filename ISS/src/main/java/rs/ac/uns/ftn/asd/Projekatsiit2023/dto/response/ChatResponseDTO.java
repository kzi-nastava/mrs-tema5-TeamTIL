package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponseDTO {
    private Integer id;
    private String userEmail;
    private String userFirstName;
    private String userLastName;
    private String userType;        // REGISTERED_USER | DRIVER | ADMINISTRATOR
    private List<MessageResponseDTO> messages;
    private String lastMessageContent;
    private String lastMessageTime;
    private String lastMessageUserType;
}
