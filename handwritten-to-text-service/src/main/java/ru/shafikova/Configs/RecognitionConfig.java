package ru.shafikova.Configs;

import lombok.Data;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class RecognitionConfig {
    @Value("${leadtools.libPath}")
    protected String libPath;

    @Value("${leadtools.license}")
    protected String LICENSE_PATH;

    @Value("${leadtools.filePath}")
    protected String FILE_PATH;

    @Value("${leadtools.ICRPath}")
    protected String ICR_PATH;

    @Value("${leadtools.savePath}")
    protected String SAVE_PATH;
}
