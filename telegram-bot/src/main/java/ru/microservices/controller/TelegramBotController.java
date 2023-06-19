package ru.microservices.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.microservices.service.TelegramBotService;

import java.io.IOException;

@RestController
@RequestMapping("/api/telegram")
public class TelegramBotController {

    private final TelegramBotService telegramBotService;

    public TelegramBotController(TelegramBotService telegramBotService) {
        this.telegramBotService = telegramBotService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> sendLikeVoiceMessage(@RequestBody MultipartFile file){
        try {
            String response = telegramBotService.sendFile(file.getResource().getFile());
            if (response.equals("OK")) return ResponseEntity.ok(response);
            else return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error with sending file");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error with getting bytes from file");
        }
    }
}
