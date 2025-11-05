package com.example.xmlfile.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

@Controller
public class xmlController {

    @GetMapping("/")
    public String index() {
        return "upload"; // Thymeleaf upload page
    }

    @PostMapping("/process")
    public ResponseEntity<byte[]> processXml(@RequestParam("file") MultipartFile file) throws Exception {
        //  Parse uploaded XML
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file.getInputStream());
        doc.getDocumentElement().normalize();

        // Get all footnotes
        NodeList footnotes = doc.getElementsByTagName("w:footnote");

        //  Create new XML document
        Document newDoc = dBuilder.newDocument();
        Element body = newDoc.createElementNS("http://schemas.microsoft.com/office/2006/xmlPackage", "w:body");
        body.setAttribute("xmlns:w", "http://schemas.microsoft.com/office/2006/xmlPackage");
        newDoc.appendChild(body);

        // Add heading
        Element heading = newDoc.createElementNS("http://schemas.microsoft.com/office/2006/xmlPackage", "w:p");
        Element runHeading = newDoc.createElementNS("http://schemas.microsoft.com/office/2006/xmlPackage", "w:r");
        Element textHeading = newDoc.createElementNS("http://schemas.microsoft.com/office/2006/xmlPackage", "w:t");
        textHeading.setTextContent("Notes");
        runHeading.appendChild(textHeading);
        heading.appendChild(runHeading);
        body.appendChild(heading);

        //  Process each footnote
        for (int i = 0; i < footnotes.getLength(); i++) {
            Element fn = (Element) footnotes.item(i);
            String id = fn.getAttribute("w:id");

            // Collect text inside this footnote
            StringBuilder sb = new StringBuilder();
            NodeList paragraphs = fn.getElementsByTagName("w:p");
            for (int j = 0; j < paragraphs.getLength(); j++) {
                Element pElem = (Element) paragraphs.item(j);
                NodeList runs = pElem.getElementsByTagName("w:r");
                for (int k = 0; k < runs.getLength(); k++) {
                    Element rElem = (Element) runs.item(k);
                    NodeList texts = rElem.getElementsByTagName("w:t");
                    for (int t = 0; t < texts.getLength(); t++) {
                        String text = texts.item(t).getTextContent()
                                .replaceAll("\\s+", " ") // clean spaces/newlines
                                .trim();
                        if (!text.isEmpty()) {
                            if (sb.length() > 0) sb.append(" "); // separate runs
                            sb.append(text);
                        }
                    }
                }
            }

            // Skip empty footnotes
            if (sb.length() == 0) continue;

            // Create <w:p> for this footnote
            Element p = newDoc.createElementNS("http://schemas.microsoft.com/office/2006/xmlPackage", "w:p");

            // Add footnote number
            Element rNum = newDoc.createElementNS("http://schemas.microsoft.com/office/2006/xmlPackage", "w:r");
            Element tNum = newDoc.createElementNS("http://schemas.microsoft.com/office/2006/xmlPackage", "w:t");
            tNum.setTextContent(id + ". ");
            rNum.appendChild(tNum);
            p.appendChild(rNum);

            // Add footnote text
            Element rText = newDoc.createElementNS("http://schemas.microsoft.com/office/2006/xmlPackage", "w:r");
            Element tText = newDoc.createElementNS("http://schemas.microsoft.com/office/2006/xmlPackage", "w:t");
            tText.setTextContent(sb.toString().trim());
            rText.appendChild(tText);
            p.appendChild(rText);

            body.appendChild(p);
        }

        //  Convert new XML to bytes
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes"); // pretty print
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(newDoc), new StreamResult(baos));

        byte[] outputBytes = baos.toByteArray();

        //  Return XML as downloadable file
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=footnotes.xml")
                .contentType(MediaType.APPLICATION_XML)
                .body(outputBytes);
    }
}
