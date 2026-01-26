package rs.ac.uns.ftn.asd.Projekatsiit2023.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request.PanicRequestDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.PanicResponseDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.service.PanicService;

@RestController
@RequestMapping("/api/panic")
public class PanicController {

    private final PanicService panicService;

    @Autowired
    public PanicController(PanicService panicService) {
        this.panicService = panicService;
    }

    @PostMapping
    public PanicResponseDTO createPanic(@RequestBody PanicRequestDTO request) {
        return panicService.createPanic(request);
    }
}
