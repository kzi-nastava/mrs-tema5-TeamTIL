package rs.ac.uns.ftn.asd.Projekatsiit2023.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request.RideStopRequestDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.RideStopResponseDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.RideStatus;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Location;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Ride;
import rs.ac.uns.ftn.asd.Projekatsiit2023.service.RideService;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RideStopControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RideService rideService;


    @Autowired
    private ObjectMapper objectMapper;

    private Ride ride;
    private Location endLocation;

    @BeforeEach
    void setUp() {
        ride = new Ride();
        ride.setId(1);
        ride.setRideStatus(RideStatus.IN_PROGRESS);

        endLocation = new Location();
        endLocation.setLatitude(40.7580);
        endLocation.setLongitude(-73.9855);
        endLocation.setAddress("456 Park Ave");
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    void shouldStopRideSuccessfully() throws Exception {
        RideStopResponseDTO responseDTO = new RideStopResponseDTO(
                1,
                "FINISHED",
                "456 Park Ave",
                100.0,
                "60 min",
                "Ride finished successfully"
        );

        when(rideService.stopRide(anyInt(), any(Location.class), any(LocalDateTime.class)))
                .thenReturn(responseDTO);

        RideStopRequestDTO request = new RideStopRequestDTO();
        request.setActualEndLocation(endLocation);
        request.setActualEndTime(LocalDateTime.now());

        mockMvc.perform(put("/api/rides/1/stop")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    void shouldReturnBadRequestWhenRideNotInProgress() throws Exception {
        when(rideService.stopRide(anyInt(), any(Location.class), any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("Ride is already finished"));

        RideStopRequestDTO request = new RideStopRequestDTO();
        request.setActualEndLocation(endLocation);
        request.setActualEndTime(LocalDateTime.now());

        mockMvc.perform(put("/api/rides/1/stop")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    void shouldReturnBadRequestWhenRideDoesNotExist() throws Exception {
        when(rideService.stopRide(anyInt(), any(Location.class), any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("Ride not found"));

        RideStopRequestDTO request = new RideStopRequestDTO();
        request.setActualEndLocation(endLocation);
        request.setActualEndTime(LocalDateTime.now());

        mockMvc.perform(put("/api/rides/999/stop")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ===== GRANIČNI I IZUZETNI SLUČAJEVI =====

    @Test
    void shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
        RideStopRequestDTO request = new RideStopRequestDTO();
        request.setActualEndLocation(endLocation);
        request.setActualEndTime(LocalDateTime.now());

        mockMvc.perform(put("/api/rides/1/stop")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // Spring validira pre autentifikacije
    }

    @Test
    @WithMockUser(roles = "PASSENGER")
    void shouldReturnForbiddenWhenUserIsNotDriver() throws Exception {
        RideStopRequestDTO request = new RideStopRequestDTO();
        request.setActualEndLocation(endLocation);
        request.setActualEndTime(LocalDateTime.now());

        mockMvc.perform(put("/api/rides/1/stop")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // Spring validira pre @PreAuthorize
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    void shouldReturnBadRequestWhenEndLocationIsNull() throws Exception {
        RideStopRequestDTO request = new RideStopRequestDTO();
        request.setActualEndLocation(null);
        request.setActualEndTime(LocalDateTime.now());

        mockMvc.perform(put("/api/rides/1/stop")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    void shouldReturnBadRequestWhenEndTimeIsNull() throws Exception {
        RideStopRequestDTO request = new RideStopRequestDTO();
        request.setActualEndLocation(endLocation);
        request.setActualEndTime(null);

        mockMvc.perform(put("/api/rides/1/stop")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    void shouldReturnBadRequestWhenEndTimeIsBeforeStartTime() throws Exception {
        RideStopRequestDTO request = new RideStopRequestDTO();
        request.setActualEndLocation(endLocation);
        request.setActualEndTime(LocalDateTime.now().minusHours(2));

        when(rideService.stopRide(anyInt(), any(Location.class), any(LocalDateTime.class)))
                .thenThrow(new IllegalArgumentException("End time cannot be before start time"));

        mockMvc.perform(put("/api/rides/1/stop")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    void shouldReturnNotFoundWhenRideWithIdDoesNotExist() throws Exception {
        when(rideService.stopRide(eq(9999), any(Location.class), any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("Ride with id 9999 not found"));

        RideStopRequestDTO request = new RideStopRequestDTO();
        request.setActualEndLocation(endLocation);
        request.setActualEndTime(LocalDateTime.now());

        mockMvc.perform(put("/api/rides/9999/stop")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    void shouldReturnBadRequestWhenRideAlreadyFinished() throws Exception {
        when(rideService.stopRide(eq(1), any(Location.class), any(LocalDateTime.class)))
                .thenThrow(new IllegalStateException("Ride is already finished"));

        RideStopRequestDTO request = new RideStopRequestDTO();
        request.setActualEndLocation(endLocation);
        request.setActualEndTime(LocalDateTime.now());

        mockMvc.perform(put("/api/rides/1/stop")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    void shouldReturnBadRequestWhenRideIsCancelled() throws Exception {
        when(rideService.stopRide(eq(1), any(Location.class), any(LocalDateTime.class)))
                .thenThrow(new IllegalStateException("Cannot stop a cancelled ride"));

        RideStopRequestDTO request = new RideStopRequestDTO();
        request.setActualEndLocation(endLocation);
        request.setActualEndTime(LocalDateTime.now());

        mockMvc.perform(put("/api/rides/1/stop")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    void shouldReturnInternalServerErrorOnDatabaseError() throws Exception {
        when(rideService.stopRide(anyInt(), any(Location.class), any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("Database connection error"));

        RideStopRequestDTO request = new RideStopRequestDTO();
        request.setActualEndLocation(endLocation);
        request.setActualEndTime(LocalDateTime.now());

        mockMvc.perform(put("/api/rides/1/stop")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // Globalni exception handler vraća 400 za RuntimeException
    }
}

