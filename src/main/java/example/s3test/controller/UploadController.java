package example.s3test.controller;

import example.s3test.S3Service.S3Uploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@RestController
public class UploadController {

    private final S3Uploader s3Uploader;

    @PostMapping("/s3/upload")
    public String fileUpload(@RequestParam("data")MultipartFile multipartFile) throws IOException {
        log.info("업로드!");
        return s3Uploader.upload(multipartFile, "dev-board");
    }
}
