package rs.ac.uns.ftn.asd.Projekatsiit2023.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request.RideEndRequestDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.RideEndResponseDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Location;
import rs.ac.uns.ftn.asd.Projekatsiit2023.service.RideService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RideEndControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RideService rideService;

    @Autowired
    private ObjectMapper objectMapper;

    private RideEndRequestDTO validRequest;
    private RideEndResponseDTO responseNoNextRide;
    private RideEndResponseDTO responseWithNextRide;

    @BeforeEach
    void setUp() {
        Location endLocation = new Location();
        endLocation.setAddress("Trg slobode 1, Novi Sad");
        endLocation.setLatitude(45.2550);
        endLocation.setLongitude(19.8449);

        validRequest = new RideEndRequestDTO(endLocation);

        responseNoNextRide = new RideEndResponseDTO(
                1,
                "Trg slobode 1, Novi Sad",
                1480.0,
                "35 min",
                null, null, null, null,
                false
        );

        responseWithNextRide = new RideEndResponseDTO(
                1,
                "Trg slobode 1, Novi Sad",
                1480.0,
                "35 min",
                2,
                "Bulevar Oslobodjenja 5, Novi Sad",
                "Futoska 1, Novi Sad",
                "15:00",
                true
        );
    }

    // ================================================================
    // POZITIVNI TESTOVI
    // ================================================================

    // Osnovan slučaj: vozač završava vožnju, nema sledeće vožnje, 200 OK
    @Test
    @WithMockUser(roles = "DRIVER")
    void shouldEndRideSuccessfullyWithoutNextRide() throws Exception {
        when(rideService.endRideAndNotify(eq(1), any(RideEndRequestDTO.class)))
                .thenReturn(responseNoNextRide);

        mockMvc.perform(put("/api/rides/1/end")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rideId").value(1))
                .andExpect(jsonPath("$.hasNextRide").value(false))
                .andExpect(jsonPath("$.finalPrice").value(1480.0))
                .andExpect(jsonPath("$.duration").value("35 min"));
    }

    // Vozač završava vožnju i ima sledeću zakazanu vožnju — dobija podatke o njoj
    @Test
    @WithMockUser(roles = "DRIVER")
    void shouldEndRideSuccessfullyWithNextRide() throws Exception {
        when(rideService.endRideAndNotify(eq(1), any(RideEndRequestDTO.class)))
                .thenReturn(responseWithNextRide);

        mockMvc.perform(put("/api/rides/1/end")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasNextRide").value(true))
                .andExpect(jsonPath("$.nextRideId").value(2))
                .andExpect(jsonPath("$.nextRideFrom").value("Bulevar Oslobodjenja 5, Novi Sad"))
                .andExpect(jsonPath("$.nextRideTo").value("Futoska 1, Novi Sad"))
                .andExpect(jsonPath("$.nextRideScheduledTime").value("15:00"));
    }

    // ================================================================
    // NEGATIVNI TESTOVI — autentifikacija i autorizacija
    // ================================================================

    // Neautentifikovani korisnik dobija grešku
    @Test
    void shouldReturnErrorWhenNotAuthenticated() throws Exception {
        mockMvc.perform(put("/api/rides/1/end")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().is4xxClientError());
    }

    // Putnik (REGISTERED_USER) ne sme završiti vožnju
    @Test
    @WithMockUser(roles = "REGISTERED_USER")
    void shouldReturnErrorWhenPassengerTriesToEndRide() throws Exception {
        mockMvc.perform(put("/api/rides/1/end")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().is4xxClientError());
    }

    // Administrator ne sme završiti vožnju
    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void shouldReturnErrorWhenAdminTriesToEndRide() throws Exception {
        mockMvc.perform(put("/api/rides/1/end")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().is4xxClientError());
    }

    // Negativan ID vožnje — kontroler vraća 400 pre poziva servisa
    @Test
    @WithMockUser(roles = "DRIVER")
    void shouldReturn400WhenRideIdIsNegative() throws Exception {
        mockMvc.perform(put("/api/rides/-1/end")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    // ID vožnje je nula — kontroler vraća 400 pre poziva servisa
    @Test
    @WithMockUser(roles = "DRIVER")
    void shouldReturn400WhenRideIdIsZero() throws Exception {
        mockMvc.perform(put("/api/rides/0/end")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    // Vožnja nije pronađena u bazi
    @Test
    @WithMockUser(roles = "DRIVER")
    void shouldReturn400WhenRideNotFound() throws Exception {
        when(rideService.endRideAndNotify(eq(999), any(RideEndRequestDTO.class)))
                .thenThrow(new RuntimeException("Ride not found"));

        mockMvc.perform(put("/api/rides/999/end")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    // Pokušaj završetka vožnje koja je već završena
    @Test
    @WithMockUser(roles = "DRIVER")
    void shouldReturn400WhenRideAlreadyFinished() throws Exception {
        when(rideService.endRideAndNotify(eq(1), any(RideEndRequestDTO.class)))
                .thenThrow(new RuntimeException("Ride is already finished"));

        mockMvc.perform(put("/api/rides/1/end")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    // Pokušaj završetka vožnje koja je otkazana
    @Test
    @WithMockUser(roles = "DRIVER")
    void shouldReturn400WhenRideIsCancelled() throws Exception {
        when(rideService.endRideAndNotify(eq(1), any(RideEndRequestDTO.class)))
                .thenThrow(new RuntimeException("Cannot end a cancelled ride"));

        mockMvc.perform(put("/api/rides/1/end")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    // Cenovnik za tip vozila nije konfigurisan
    @Test
    @WithMockUser(roles = "DRIVER")
    void shouldReturn400WhenPriceConfigMissing() throws Exception {
        when(rideService.endRideAndNotify(eq(1), any(RideEndRequestDTO.class)))
                .thenThrow(new RuntimeException("Price config not found"));

        mockMvc.perform(put("/api/rides/1/end")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    // Greška baze podataka tokom završetka
    @Test
    @WithMockUser(roles = "DRIVER")
    void shouldReturn500WhenDatabaseErrorOccurs() throws Exception {
        when(rideService.endRideAndNotify(eq(1), any(RideEndRequestDTO.class)))
                .thenThrow(new RuntimeException("Database connection error"));

        mockMvc.perform(put("/api/rides/1/end")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isInternalServerError());
    }

    // Vožnja koja traje 0 minuta — isti početak i kraj
    @Test
    @WithMockUser(roles = "DRIVER")
    void shouldEndRideWithZeroDuration() throws Exception {
        RideEndResponseDTO zeroResponse = new RideEndResponseDTO(
                1, "Trg slobode 1", 200.0, "0 min",
                null, null, null, null, false
        );
        when(rideService.endRideAndNotify(eq(1), any(RideEndRequestDTO.class)))
                .thenReturn(zeroResponse);

        mockMvc.perform(put("/api/rides/1/end")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duration").value("0 min"));
    }
}

