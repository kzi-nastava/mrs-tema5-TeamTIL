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
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request.RideRequestDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.RideCreatedResponseDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.service.RideService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * INTEGRACIONI TESTOVI za RideController — POST /api/rides (2.4.1 Porucivanje voznje)
 * Testiramo:
 *   - Da li kontroler ispravno prima HTTP zahtev i vraca odgovor
 *   - Da li Spring Security blokira neovlascene korisnike
 *   - Da li kontroler ispravno prenosi greske iz servisa kao HTTP odgovore
*/

@SpringBootTest
@AutoConfigureMockMvc
class RideControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * RideService je mokovan, ne pozivamo pravu logiku ni bazu
     * Testiramo samo da li kontroler sloj ispravno radi
     */
    @MockBean
    private RideService rideService;

    @Autowired
    private ObjectMapper objectMapper;

    private RideRequestDTO validRequest;
    private RideCreatedResponseDTO successResponse;

    private String futureTime() {
        return LocalDateTime.now().plusHours(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }

    @BeforeEach
    void setUp() {
        RideRequestDTO.LocationDTO start = new RideRequestDTO.LocationDTO(
                "Bulevar Oslobodjenja 1, Novi Sad", 45.2671, 19.8335);
        RideRequestDTO.LocationDTO end = new RideRequestDTO.LocationDTO(
                "Trg slobode 1, Novi Sad", 45.2550, 19.8449);

        validRequest = new RideRequestDTO(
                List.of(start, end),
                Collections.emptyList(),
                "STANDARD",
                false,
                false,
                futureTime()
        );

        successResponse = new RideCreatedResponseDTO(
                1, "REQUESTED", 1500.0, "Petar Petrovic",
                "petar@example.com", "Volkswagen Golf (STANDARD)",
                "Ride successfully created and assigned to driver",
                "17 Feb 2026, 13:00", "17 Feb 2026, 13:30", 10.0, 30.0
        );
    }

    // ================================================================
    // POZITIVNI TESTOVI
    // ================================================================

    //osnovni slucaj: validan zahtev, ulogovan korisnik 200 OK sa svim podacima
    @Test
    @WithMockUser(username = "korisnik@example.com", roles = "REGISTERED_USER")
    void shouldCreateRideSuccessfully() throws Exception {
        when(rideService.createNewRide(any(RideRequestDTO.class), anyString()))
                .thenReturn(successResponse);

        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REQUESTED"))
                .andExpect(jsonPath("$.driverName").value("Petar Petrovic"))
                .andExpect(jsonPath("$.estimatedPrice").value(1500.0));
    }

    //voznja sa ulinkovanim putnicima: lista mejlova se ispravno salje
    @Test
    @WithMockUser(username = "korisnik@example.com", roles = "REGISTERED_USER")
    void shouldCreateRideWithPassengers() throws Exception {
        validRequest.setPassengerEmails(List.of("putnik1@example.com", "putnik2@example.com"));
        when(rideService.createNewRide(any(RideRequestDTO.class), anyString()))
                .thenReturn(successResponse);

        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());
    }

   //voznja sa stanicama, bitan redosled
    @Test
    @WithMockUser(username = "korisnik@example.com", roles = "REGISTERED_USER")
    void shouldCreateRideWithIntermediateStops() throws Exception {
        RideRequestDTO.LocationDTO stop = new RideRequestDTO.LocationDTO(
                "Futoska 1, Novi Sad", 45.2600, 19.8400);
        validRequest.setLocations(List.of(
                validRequest.getLocations().get(0), stop, validRequest.getLocations().get(1)));
        when(rideService.createNewRide(any(RideRequestDTO.class), anyString()))
                .thenReturn(successResponse);

        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());
    }

    //LUXURY vozilo: odgovor treba da sadrzi ispravne info o vozilu
    @Test
    @WithMockUser(username = "korisnik@example.com", roles = "REGISTERED_USER")
    void shouldCreateRideWithLuxuryVehicle() throws Exception {
        validRequest.setVehicleType("LUXURY");
        RideCreatedResponseDTO luxuryResponse = new RideCreatedResponseDTO(
                2, "REQUESTED", 2250.0, "Marko Markovic", "marko@example.com",
                "Mercedes S-Class (LUXURY)", "Ride successfully created and assigned to driver",
                "17 Feb 2026, 13:00", "17 Feb 2026, 13:30", 10.0, 30.0);
        when(rideService.createNewRide(any(RideRequestDTO.class), anyString()))
                .thenReturn(luxuryResponse);

        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicleInfo").value("Mercedes S-Class (LUXURY)"));
    }

    //baby friendly opcija se ispravno prenosi kroz kontroler
    @Test
    @WithMockUser(username = "korisnik@example.com", roles = "REGISTERED_USER")
    void shouldCreateRideWithBabyFriendly() throws Exception {
        validRequest.setBabyFriendly(true);
        when(rideService.createNewRide(any(RideRequestDTO.class), anyString()))
                .thenReturn(successResponse);

        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());
    }

    //pet friendly opcija se ispravno prenosi kroz kontroler
    @Test
    @WithMockUser(username = "korisnik@example.com", roles = "REGISTERED_USER")
    void shouldCreateRideWithPetFriendly() throws Exception {
        validRequest.setPetFriendly(true);
        when(rideService.createNewRide(any(RideRequestDTO.class), anyString()))
                .thenReturn(successResponse);

        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());
    }

    //VAN tip vozila
    @Test
    @WithMockUser(username = "korisnik@example.com", roles = "REGISTERED_USER")
    void shouldCreateRideWithVanVehicle() throws Exception {
        validRequest.setVehicleType("VAN");
        when(rideService.createNewRide(any(RideRequestDTO.class), anyString()))
                .thenReturn(successResponse);

        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());
    }

    // ================================================================
    // NEGATIVNI TESTOVI — autentifikacija i autorizacija
    // ================================================================

    //neautentifikovani korisnik
    @Test
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    //vozac pokusava da poruci voznju
    @Test
    @WithMockUser(roles = "DRIVER")
    void shouldReturn403WhenDriverTriesToCreateRide() throws Exception {
        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    //admin pokusava da poruci voznju
    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void shouldReturn403WhenAdminTriesToCreateRide() throws Exception {
        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    // ================================================================
    // NEGATIVNI TESTOVI — biznis validacija
    // ================================================================

    //nema dostupnih vozaca, servis baca gresku, kontroler vraca 400
    @Test
    @WithMockUser(username = "korisnik@example.com", roles = "REGISTERED_USER")
    void shouldReturn400WhenNoAvailableDrivers() throws Exception {
        when(rideService.createNewRide(any(RideRequestDTO.class), anyString()))
                .thenThrow(new RuntimeException("No available drivers at the moment"));

        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No available drivers at the moment"));
    }

    //putnik ne postoji u bazi
    @Test
    @WithMockUser(username = "korisnik@example.com", roles = "REGISTERED_USER")
    void shouldReturn400WhenPassengerNotFound() throws Exception {
        when(rideService.createNewRide(any(RideRequestDTO.class), anyString()))
                .thenThrow(new RuntimeException("Passenger not found: korisnik@example.com"));

        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Passenger not found: korisnik@example.com"));
    }

    //blokiran korisnik ne sme porucivati voznje
    @Test
    @WithMockUser(username = "blokiran@example.com", roles = "REGISTERED_USER")
    void shouldReturn400WhenUserIsBlocked() throws Exception {
        when(rideService.createNewRide(any(RideRequestDTO.class), anyString()))
                .thenThrow(new RuntimeException("Your account has been blocked. Reason: Krsenje pravila"));

        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Your account has been blocked. Reason: Krsenje pravila"));
    }

    //neregistrovani orisnik ne moze da bude ulinkovan
    @Test
    @WithMockUser(username = "korisnik@example.com", roles = "REGISTERED_USER")
    void shouldReturn400WhenCoPassengerNotRegistered() throws Exception {
        validRequest.setPassengerEmails(List.of("nepostojeci@example.com"));
        when(rideService.createNewRide(any(RideRequestDTO.class), anyString()))
                .thenThrow(new RuntimeException(
                        "The following passengers are not registered users: nepostojeci@example.com. " +
                                "Only registered users can be added as co-passengers."));

        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    //nepostojeci tip vozila
    @Test
    @WithMockUser(username = "korisnik@example.com", roles = "REGISTERED_USER")
    void shouldReturn400WhenInvalidVehicleType() throws Exception {
        validRequest.setVehicleType("NEPOSTOJECI_TIP");
        when(rideService.createNewRide(any(RideRequestDTO.class), anyString()))
                .thenThrow(new RuntimeException("Invalid vehicle type: NEPOSTOJECI_TIP"));

        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    //api nije uspeo proceniti rutu
    @Test
    @WithMockUser(username = "korisnik@example.com", roles = "REGISTERED_USER")
    void shouldReturn400WhenRouteEstimationFails() throws Exception {
        when(rideService.createNewRide(any(RideRequestDTO.class), anyString()))
                .thenThrow(new RuntimeException("Could not estimate route"));

        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Could not estimate route"));
    }

    // ================================================================
    // GRANICNI SLUCAJEVI
    // ================================================================

    //samo jedna lokacija a minimun je start i end
    @Test
    @WithMockUser(username = "korisnik@example.com", roles = "REGISTERED_USER")
    void shouldReturn400WhenOnlyOneLocation() throws Exception {
        validRequest.setLocations(List.of(
                new RideRequestDTO.LocationDTO("Bulevar Oslobodjenja 1", 45.2671, 19.8335)));
        when(rideService.createNewRide(any(RideRequestDTO.class), anyString()))
                .thenThrow(new RuntimeException("At least start and end location are required"));

        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    //prazna lista lokacija
    @Test
    @WithMockUser(username = "korisnik@example.com", roles = "REGISTERED_USER")
    void shouldReturn400WhenLocationsListIsEmpty() throws Exception {
        validRequest.setLocations(Collections.emptyList());
        when(rideService.createNewRide(any(RideRequestDTO.class), anyString()))
                .thenThrow(new RuntimeException("At least start and end location are required"));

        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    //null lista lokacija
    @Test
    @WithMockUser(username = "korisnik@example.com", roles = "REGISTERED_USER")
    void shouldReturn400WhenLocationsListIsNull() throws Exception {
        validRequest.setLocations(null);
        when(rideService.createNewRide(any(RideRequestDTO.class), anyString()))
                .thenThrow(new RuntimeException("At least start and end location are required"));

        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    //zakazano u proslosti
    @Test
    @WithMockUser(username = "korisnik@example.com", roles = "REGISTERED_USER")
    void shouldReturn400WhenScheduledTimeIsInPast() throws Exception {
        validRequest.setScheduledTime(LocalDateTime.now().minusHours(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        when(rideService.createNewRide(any(RideRequestDTO.class), anyString()))
                .thenThrow(new RuntimeException("Scheduled time must be in the future!"));

        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    //svi vozaci su zauzeti
    @Test
    @WithMockUser(username = "korisnik@example.com", roles = "REGISTERED_USER")
    void shouldReturn400WhenAllDriversBusy() throws Exception {
        when(rideService.createNewRide(any(RideRequestDTO.class), anyString()))
                .thenThrow(new RuntimeException("No available drivers at the moment"));

        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No available drivers at the moment"));
    }
}