package com.example.fileupload.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/xml")
public class filclass {

    @GetMapping("/uploadPage")
    public String uploadPage() {
        return "upload"; // Thymeleaf template
    }

    @PostMapping("/upload")
    public ResponseEntity<byte[]> processXml(@RequestParam("file") MultipartFile file) throws Exception {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(file.getInputStream());

        NodeList footnotes = doc.getElementsByTagName("w:footnote");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8);

        // Add heading
        writer.write("Notes" + System.lineSeparator() + System.lineSeparator());

        // Footnote counter starting from 1
        int counter = 1;
        for (int i = 0; i < footnotes.getLength(); i++) {
            Node node = footnotes.item(i);

            // Only process element nodes
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element footnote = (Element) node;

                // Update footnote id attribute to start from 1
                if (footnote.hasAttribute("w:id")) {
                    footnote.setAttribute("w:id", String.valueOf(counter));
                }

                String content = footnote.getTextContent().trim();

                writer.write(counter + ". " + content + System.lineSeparator() + System.lineSeparator());
                counter++;
            }
        }

        writer.flush();
        writer.close();

        byte[] fileBytes = baos.toByteArray();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"footnotes.txt\"");

        return ResponseEntity.ok()
                .headers(headers)
                .body(fileBytes);
    }
}
