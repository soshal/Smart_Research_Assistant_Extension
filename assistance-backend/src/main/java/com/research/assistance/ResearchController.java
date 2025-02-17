package com.research.assistance;

import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Data
@CrossOrigin(origins = "*")
@RequestMapping("/api/research")
public class ResearchController {

    private final ResearchService service;

    @PostMapping("/process")
    public ResponseEntity<String> processContent(@RequestBody Research research){

        String result = service.processContent(research);
        return ResponseEntity.ok(result);
    }
}
