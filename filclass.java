package com.example.fileupload.controller;

import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
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

        NodeList footnotes = doc.getElementsByTagNameNS("*", "footnote");

        // 2️⃣ Create Word document
        XWPFDocument document = new XWPFDocument();

        // 3️⃣ Heading "Notes"
        XWPFParagraph heading = document.createParagraph();
        XWPFRun runHeading = heading.createRun();
        runHeading.setText("Notes");
        runHeading.setBold(true);
        runHeading.setFontSize(14);

        int counter = 1;

        for (int i = 0; i < footnotes.getLength(); i++) {
            Node footnote = footnotes.item(i);
            if (footnote.getNodeType() != Node.ELEMENT_NODE) continue;
            Element fn = (Element) footnote;
            String type = fn.getAttribute("w:type");
            if ("separator".equals(type) || "continuationSeparator".equals(type)) continue;

            XWPFParagraph paragraph = document.createParagraph();

            // Footnote number
            XWPFRun runNum = paragraph.createRun();
            runNum.setText(counter + ". ");
            runNum.setBold(true);

            // Process footnote children
            appendFootnoteContent(paragraph, fn);

            counter++;
        }

        // Write Word document to bytes
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        document.write(outputStream);
        document.close();
        byte[] wordBytes = outputStream.toByteArray();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"footnotes.docx\"");

        return ResponseEntity.ok()
                .headers(headers)
                .body(wordBytes);
    }

    // Recursive method to process footnote content
    private void appendFootnoteContent(XWPFParagraph paragraph, Node node) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if (child.getNodeName().endsWith("r")) {
                String text = child.getTextContent();
                if (text != null && !text.isEmpty()) {
                    XWPFRun run = paragraph.createRun();
                    run.setText(text);
                    run.setFontSize(12);
                }
            } else if (child.getNodeName().endsWith("hyperlink")) {
                NodeList hrChildren = child.getChildNodes();
                for (int j = 0; j < hrChildren.getLength(); j++) {
                    Node hrChild = hrChildren.item(j);
                    if (hrChild.getNodeName().endsWith("r")) {
                        NodeList rChildren = hrChild.getChildNodes();
                        for (int k = 0; k < rChildren.getLength(); k++) {
                            Node rChild = rChildren.item(k);
                            if (rChild.getNodeName().endsWith("t")) {
                                String url = rChild.getTextContent();
                                XWPFHyperlinkRun hyperlink = paragraph.createHyperlinkRun(url);
                                hyperlink.setText(url);
                                hyperlink.setColor("0000FF");
                                hyperlink.setUnderline(UnderlinePatterns.SINGLE);
                            }
                        }
                    }
                }
            } else {
                // Recursively process other nodes
                appendFootnoteContent(paragraph, child);
            }
        }
    }
}
