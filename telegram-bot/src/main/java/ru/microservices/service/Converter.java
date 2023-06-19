package ru.microservices.service;

import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;


@Component
@Slf4j
public class Converter {

    private final FFmpeg ffmpeg;

    public Converter(@Value("${ffmpeg.source}") String property) throws IOException {
        this.ffmpeg = new FFmpeg(new File(property).getPath());
    }

    public File convertOggToMp3(File inputFile) throws IOException {

        File outputFile = File.createTempFile("output", ".wav");
        log.info("{} is created", outputFile.getAbsolutePath());

        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(inputFile.getAbsolutePath())
                .overrideOutputFiles(true)
                .addOutput(outputFile.getAbsolutePath())
                .setAudioBitRate(32768)
                .setFormat("wav")
                .done();


        FFmpegExecutor executor = new FFmpegExecutor(this.ffmpeg);
        executor.createJob(builder).run();
        // Read the converted MP3 file as byte array
        //byte[] mp3Bytes = Files.readAllBytes(Path.of(outputFile.getAbsolutePath()));

        // Clean up temporary files
        //FileUtils.deleteQuietly(inputFile);
        //FileUtils.deleteQuietly(outputFile);

        try {
            executor.createTwoPassJob(builder).run();
            log.info("{} is converted, length is {}", outputFile.getName(), outputFile.length());
        } catch (IllegalArgumentException ignored) {
            //игнорируем ошибку, возникающую из-за отсутствия видеоряда
        }
        return outputFile;
    }

}
