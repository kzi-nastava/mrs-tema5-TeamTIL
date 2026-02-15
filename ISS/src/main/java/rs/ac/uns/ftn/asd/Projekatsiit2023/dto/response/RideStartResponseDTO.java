package rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response;

public class RideStartResponseDTO {
    private Integer rideId;
    private String status;
    private String message;
    private String startTime;

    public RideStartResponseDTO(Integer rideId, String status, String message, String startTime) {
        this.rideId = rideId;
        this.status = status;
        this.message = message;
        this.startTime = startTime;
    }

    public Integer getRideId() { return rideId; }
    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public String getStartTime() { return startTime; }
}
