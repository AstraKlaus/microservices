package ru.microservices.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.microservices.service.TelegramBotService;

@Component
@Slf4j
public class TelegramBotInitializer {

    @Autowired
    TelegramBotService telegramBot;

    @EventListener({ContextRefreshedEvent.class})
    public void init() throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            telegramBotsApi.registerBot(telegramBot);
            log.info("Bot {} is registered", telegramBot.getBotUsername());
        } catch (TelegramApiException e) {
            log.error("Error with registering bot {}", telegramBot.getBotUsername());
        }
    }
}
