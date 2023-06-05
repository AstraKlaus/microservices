package ru.microservices.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.microservices.config.TelegramBotConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

@Component
@Slf4j
public class TelegramBotService extends TelegramLongPollingBot {

    private final TelegramBotConfig config;

    private final RestTemplate restTemplate;

    private final Converter converter;

    public TelegramBotService(TelegramBotConfig config, RestTemplate restTemplate, Converter converter) {
        this.config = config;
        this.restTemplate = restTemplate;
        this.converter = converter;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasVoice()){
            long chatID = update.getMessage().getChatId();

            String fileId = update.getMessage().getVoice().getFileId();
            String filePath = getFilePath(fileId);

            try {
                File inputFile = File.createTempFile("input", ".ogg");

                try (FileOutputStream fos = new FileOutputStream(inputFile.getAbsolutePath())) {
                    fos.write(getTelegramAudio(filePath));
                }

                File wavFile = converter.convertOggToMp3(inputFile);
                byte[] fileContent = Files.readAllBytes(wavFile.toPath());
                System.out.println(fileContent.length + String.valueOf(wavFile.length()));
                String result = getRecognizedText(wavFile);


                SendMessage sendMessage = new SendMessage(String.valueOf(chatID) , result);
                execute(sendMessage);

                log.info("message is sent to chat {}", chatID);
            } catch (IOException | TelegramApiException e) {
                System.out.println(e.getMessage());
                log.error("error with send to chat {} {}",e.getMessage(),chatID);
            }

        }
    }

    private String getFilePath(String fileId) {
        GetFile getFile = new GetFile();
        getFile.setFileId(fileId);

        try {
            org.telegram.telegrambots.meta.api.objects.File file = execute(getFile);
            return file.getFileUrl(getBotToken());
        } catch (TelegramApiException e) {
            e.printStackTrace();
            log.error("error with getting file path {}",e.getMessage());
            return null;
        }
    }

    private byte[] getTelegramAudio(String path){
        return restTemplate.getForObject(path, byte[].class);
    }

    private String getRecognizedText(File audio){
        String url = "http://api-gateway:8080/api/recognition";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("audio", audio);

        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        System.out.println("Audio to send: "+audio.length());

        return restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class).getBody();
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

}
