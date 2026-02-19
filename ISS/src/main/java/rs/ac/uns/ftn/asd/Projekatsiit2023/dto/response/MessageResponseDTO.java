package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponseDTO {
    private Integer id;
    private String content;
    private String timestamp; // "HH:mm"
    private String date; // "yyyy-MM-dd"
    private String userType;  // ADMINISTRATOR | REGISTERED_USER | DRIVER
    private Integer chatId;
}
