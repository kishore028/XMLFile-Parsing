package com.example.fileupload.controller;

import org.apache.poi.xwpf.usermodel.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.*;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayOutputStream;

@Controller
@RequestMapping("/xml")
public class filclass {

    @GetMapping("/uploadPage")
    public String uploadPage() {
        return "upload"; // Thymeleaf template
    }

    @PostMapping("/upload")
    public ResponseEntity<byte[]> processXml(@RequestParam("file") MultipartFile file) throws Exception {

        // 1️⃣ Parse XML
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(file.getInputStream());

        NodeList footnotes = doc.getElementsByTagName("w:footnote");

        // 2️⃣ Create Word document
        XWPFDocument document = new XWPFDocument();

        // Heading "Notes"
        XWPFParagraph heading = document.createParagraph();
        heading.setStyle("Heading1");
        XWPFRun runHeading = heading.createRun();
        runHeading.setText("Notes");
        runHeading.setBold(true);
        runHeading.setFontSize(14);

        // 3️⃣ Add footnotes as paragraphs
        int counter = 1;
        for (int i = 0; i < footnotes.getLength(); i++) {
            Node node = footnotes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element footnote = (Element) node;

                // Update w:id starting from 1
                if (footnote.hasAttribute("w:id")) {
                    footnote.setAttribute("w:id", String.valueOf(counter));
                }

                String content = footnote.getTextContent().trim();

                XWPFParagraph p = document.createParagraph();
                XWPFRun run = p.createRun();
                run.setText(counter + ". " + content);
                run.setFontSize(12);

                counter++;
            }
        }

        // 4️⃣ Write Word file to memory
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document.write(baos);
        document.close();

        byte[] docBytes = baos.toByteArray();

        // 5️⃣ Prepare HTTP headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"footnotes.docx\"");

        return ResponseEntity.ok()
                .headers(headers)
                .body(docBytes);
    }
}
