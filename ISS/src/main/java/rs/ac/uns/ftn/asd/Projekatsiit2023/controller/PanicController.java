package rs.ac.uns.ftn.asd.Projekatsiit2023.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request.PanicRequestDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.PanicResponseDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.service.PanicService;

import java.util.List;

@RestController
@RequestMapping("/api/panic")
@Validated
public class PanicController {

    private final PanicService panicService;

    @Autowired
    public PanicController(PanicService panicService) {
        this.panicService = panicService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('REGISTERED_USER', 'DRIVER', 'ADMINISTRATOR')")
    public PanicResponseDTO createPanic(@Valid @RequestBody PanicRequestDTO request) {
        return panicService.createPanic(request);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public List<PanicResponseDTO> getAllPanics() {
        System.out.println("DEBUG: getAllPanics called");
        List<PanicResponseDTO> result = panicService.getAllPanics();
        System.out.println("DEBUG: Returning " + result.size() + " panic notifications");
        return result;
    }

    @GetMapping("/unhandled")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public List<PanicResponseDTO> getUnhandledPanics() {
        return panicService.getUnhandledPanics();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public PanicResponseDTO getPanicById(@PathVariable Integer id) {
        return panicService.getPanicById(id);
    }

    @PutMapping("/{id}/handle")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public PanicResponseDTO markPanicAsHandled(@PathVariable Integer id) {
        return panicService.markPanicAsHandled(id);
    }
}
