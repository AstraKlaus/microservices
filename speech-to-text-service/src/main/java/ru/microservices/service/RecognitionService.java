package ru.microservices.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;

@Service
@Slf4j
public class RecognitionService {

    public String recognize(byte[] audioData) {
        try {
            Configuration configuration = new Configuration();

            configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
            configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
            configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");

            StreamSpeechRecognizer recognizer = new StreamSpeechRecognizer(configuration);
            InputStream stream = new ByteArrayInputStream(audioData);

            recognizer.startRecognition(stream);
            SpeechResult result;

            StringBuilder answer = new StringBuilder();
            while ((result = recognizer.getResult()) != null) {
                answer.append(result.getHypothesis());
            }
            log.info("message [{}] is recognized", answer);
            recognizer.stopRecognition();
        return answer.toString();
        } catch (IOException e) {
            e.printStackTrace();
            log.error("error with recognizing {}", e.getMessage());
            return "Cannot recognize";
        }
    }
}
