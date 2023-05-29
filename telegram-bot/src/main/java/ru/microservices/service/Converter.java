package ru.microservices.service;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class Converter {

    private final FFmpeg ffmpeg;

    public Converter(@Value("${ffmpeg.source}") String ffmpegPath) throws IOException {
        this.ffmpeg = new FFmpeg(new File(ffmpegPath).getPath());
    }

    public File convertOggToMp3(String inputPath) throws IOException {

        File outputFile = File.createTempFile("output", ".wav");

        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(inputPath)
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
        } catch (IllegalArgumentException ignored) {
            //отлавливаем и игнорируем ошибку, возникающую из-за отсутствия видеоряда (конвертер предназначен для видео)
        }
        return outputFile;
    }

}
