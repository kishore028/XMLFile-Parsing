package com.example.fileupload.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/xml")
public class filclass {

    @Autowired
    com.example.fileupload.service.xmlToWordService xmlToWordService;

    @GetMapping("/uploadPage")
    public String uploadPage() {
        return "upload"; // Thymeleaf template
    }

    @PostMapping("/upload")
    public ResponseEntity<byte[]> processXml(@RequestParam("file") MultipartFile file) throws Exception {

        byte[] wordBytes = xmlToWordService.convertXmlToWord(file);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"footnotes.docx\"");

        return ResponseEntity.ok()
                .headers(headers)
                .body(wordBytes);
    }
}
