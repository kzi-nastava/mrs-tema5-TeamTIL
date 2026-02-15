package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BlockUserRequestDTO {
    private Integer userId;
    private Boolean block; // true je block, false je unblock
    private String reason;
}