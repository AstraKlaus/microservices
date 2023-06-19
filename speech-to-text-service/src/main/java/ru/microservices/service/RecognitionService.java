package ru.microservices.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import org.springframework.web.multipart.MultipartFile;
import ru.microservices.exception.RecognizeException;


@Slf4j
@Service
public class RecognitionService {

    Configuration configuration = new Configuration();

    public RecognitionService(){
        configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
        configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
        configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");
    }

    public String recognize(byte[] audio) {
        try {
            StreamSpeechRecognizer recognizer = new StreamSpeechRecognizer(configuration);
            InputStream stream = new ByteArrayInputStream(audio);

            recognizer.startRecognition(stream);
            SpeechResult result;

            StringBuilder answer = new StringBuilder();
            while ((result = recognizer.getResult()) != null) {
                answer.append(result.getHypothesis());
            }
            log.info("Message [{}] is recognized", answer);
            recognizer.stopRecognition();
            return answer.toString();
        } catch (IOException e) {
            log.error("Error with recognition {}", e.getMessage());
            throw new RecognizeException();
        }
    }

    public byte[] multiPartFileToByte(MultipartFile file){
        byte[] audio;
        try {
            audio = file.getBytes();
            log.info("File {} is received", file.getName());
        } catch (IOException e) {
            log.error("Error with file {}", file.getName());
            throw new RuntimeException(e);
        }
        return audio;
    }
}
