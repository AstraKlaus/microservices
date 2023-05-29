package ru.microservices.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.microservices.config.TelegramBotConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Component
@Slf4j
public class TelegramBotService extends TelegramLongPollingBot {

    private final TelegramBotConfig config;

    private final RecognitionService recognitionService;

    private final Converter converter;

    public TelegramBotService(TelegramBotConfig config, RecognitionService recognitionService, Converter converter) {
        this.config = config;
        this.recognitionService = recognitionService;
        this.converter = converter;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasVoice()){
            long chatID = update.getMessage().getChatId();

            String fileId = update.getMessage().getVoice().getFileId();
            String filePath = getFilePath(fileId);

            try {
                File wavFile = converter.convertOggToMp3(filePath);
                byte[] wavAudio = Files.readAllBytes(wavFile.toPath());
                String result = recognitionService.recognize(wavAudio);
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

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

}
