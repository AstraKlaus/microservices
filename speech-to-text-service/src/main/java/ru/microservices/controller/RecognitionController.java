package ru.microservices.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.microservices.exception.RecognizeException;
import ru.microservices.service.RecognitionService;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/recognizeAudio")
public class RecognitionController {

    private final RecognitionService recognitionService;

    public RecognitionController(RecognitionService recognitionService) {
        this.recognitionService = recognitionService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> speechRecognition(@RequestBody MultipartFile file){
        byte[] fileData = recognitionService.multiPartFileToByte(file);
        String recognizedText = recognitionService.recognize(fileData);
        return ResponseEntity.ok(recognizedText);
    }

    @ExceptionHandler
    private ResponseEntity<String> handleException(RecognizeException e){
        return new ResponseEntity<>("Error in file recognition", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    private ResponseEntity<String> handleException(RuntimeException e){
        return new ResponseEntity<>("File is corrupted or empty", HttpStatus.BAD_REQUEST);
    }
}
