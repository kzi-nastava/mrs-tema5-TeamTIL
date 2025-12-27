package rs.ac.uns.ftn.asd.Projekatsiit2023.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.RatingType;

import java.time.LocalTime;

@Setter
@Getter
@Data
public class Rating {
    private Integer id;
    private Double score;
    private RatingType ratingType;
    private Integer ratedEntityId;
    private String comment;
    private LocalTime createdAt;
    private RegisteredUser rater;
}
