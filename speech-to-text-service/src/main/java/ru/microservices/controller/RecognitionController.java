package ru.microservices.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.microservices.service.RecognitionService;

import java.io.IOException;

@RestController
@RequestMapping("/api/recognition")
public class RecognitionController {

    private final RecognitionService recognitionService;

    public RecognitionController(RecognitionService recognitionService) {
        this.recognitionService = recognitionService;
    }

    @PostMapping
    public ResponseEntity<String> speechRecognition(@RequestParam("audio") MultipartFile file){
        byte[] audio = new byte[0];
        try {
            audio = file.getBytes();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Cannot get bytes from file");
        }
        if (recognitionService.recognize(audio).equals("Cannot recognize")){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(recognitionService.recognize(audio));
        }else{
            return ResponseEntity.ok(recognitionService.recognize(audio));
        }
    }
}
