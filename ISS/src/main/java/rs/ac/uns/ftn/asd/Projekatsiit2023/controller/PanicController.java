package rs.ac.uns.ftn.asd.Projekatsiit2023.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request.PanicRequestDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.PanicResponseDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.service.PanicService;

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
    @PreAuthorize("hasAnyRole('REGISTERED_USER', 'DRIVER')")
    public PanicResponseDTO createPanic(@Valid @RequestBody PanicRequestDTO request) {
        return panicService.createPanic(request);
    }
}
