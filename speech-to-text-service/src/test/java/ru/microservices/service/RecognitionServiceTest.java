package ru.microservices.service;


import org.assertj.core.api.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.microservices.exception.RecognizeException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class RecognitionServiceTest {


    public RecognitionService recognitionService = new RecognitionService();

    @Test
    void shouldRecognizeILoveYou() {
        File audio = new File("src\\test\\resources\\Iloveyou.wav");
        byte[] fileContent = new byte[0];
        try {
            fileContent = Files.readAllBytes(audio.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        String expected = recognitionService.recognize(fileContent);
        assertThat(expected).isEqualTo("i love you");
    }

}