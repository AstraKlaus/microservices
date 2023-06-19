package ru.microservices.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
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
import java.text.MessageFormat;

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
                FileSystemResource audioResource = new FileSystemResource(wavFile);
                String result = getRecognizedText(audioResource);

                SendMessage sendMessage = new SendMessage(String.valueOf(chatID) , result);
                execute(sendMessage);

                log.info("message is sent to chat {}", chatID);
            } catch (IOException | TelegramApiException e) {
                System.out.println(e.getMessage());
                log.error("error with send to chat {} {}", e.getMessage(), chatID);
            }

        }
    }

    private String getFilePath(String fileId) {
        GetFile getFile = new GetFile();
        getFile.setFileId(fileId);
        try {
            org.telegram.telegrambots.meta.api.objects.File file = execute(getFile);
            log.info("Voice message {} is received", file.getFileId());
            return file.getFileUrl(getBotToken());
        } catch (TelegramApiException e) {
            log.error("error with getting file path {}", e.getMessage());
            return null;
        }
    }

    private byte[] getTelegramAudio(String path){
        return restTemplate.getForObject(path, byte[].class);
    }

    private String getRecognizedText(FileSystemResource audio){
        String url = "http://nginx:80/api/recognizeAudio";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", audio);

        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        return restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class).getBody();
    }

    public String sendFile(File audio){
        FileSystemResource audioResource = new FileSystemResource(audio);

        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("audio", audioResource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);

        try {
            restTemplate.exchange(
                    MessageFormat.format("{}bot{}/sendAudio?chat_id={}",
                            config.getUrl(), config.getToken(), config.getChatId()),
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );
            return "OK";
        } catch (Exception e) {
            log.error(e.getMessage());
            return "ERROR";
        }
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
