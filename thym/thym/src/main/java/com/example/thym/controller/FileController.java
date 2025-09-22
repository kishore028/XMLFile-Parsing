package com.example.thym.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;

@Controller
public class FileController {

    @GetMapping("/")
    public String index() {
        return "upload"; // Thymeleaf page
    }

    @PostMapping("/process")
    public ResponseEntity<byte[]> processXml(@RequestParam("file") MultipartFile file) throws Exception {
        // 1️⃣ Parse the uploaded XML
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document inputDoc = dBuilder.parse(file.getInputStream());
        inputDoc.getDocumentElement().normalize();

        // 2️⃣ Create new output document
        Document outputDoc = dBuilder.newDocument();

        // 3️⃣ Create root <pkg:package>
        Element pkgPackage = outputDoc.createElementNS("http://schemas.microsoft.com/office/2006/xmlPackage", "pkg:package");
        outputDoc.appendChild(pkgPackage);

        // 4️⃣ Copy the first <pkg:part> (Relationships) as-is
        NodeList parts = inputDoc.getElementsByTagName("pkg:part");
        if (parts.getLength() > 0) {
            Node relPart = parts.item(0);
            Node importedRelPart = outputDoc.importNode(relPart, true);
            pkgPackage.appendChild(importedRelPart);
        }

        // 5️⃣ Create new <pkg:part> for footnotes
        Element footnotePart = outputDoc.createElement("pkg:part");
        footnotePart.setAttribute("pkg:name", "/word/document.xml");
        footnotePart.setAttribute("pkg:contentType", "application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml");
        pkgPackage.appendChild(footnotePart);

        // 6️⃣ Add <pkg:xmlData>
        Element xmlData = outputDoc.createElement("pkg:xmlData");
        footnotePart.appendChild(xmlData);

        // 7️⃣ Add <w:document>
        Element wDocument = outputDoc.createElementNS("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "w:document");
        xmlData.appendChild(wDocument);

        // 8️⃣ Add <w:body>
        Element wBody = outputDoc.createElementNS("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "w:body");
        wDocument.appendChild(wBody);

        // 9️⃣ Extract all <w:footnote> from input and add to w:body
        NodeList footnotes = inputDoc.getElementsByTagNameNS("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "footnote");
        for (int i = 0; i < footnotes.getLength(); i++) {
            Node footnote = footnotes.item(i);
            Node importedFootnote = outputDoc.importNode(footnote, true);
            wBody.appendChild(importedFootnote);
        }

        // 10️⃣ Transform to byte[]
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(outputDoc), new StreamResult(baos));
        byte[] outputBytes = baos.toByteArray();

        // 11️⃣ Return downloadable file
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=footnotes.xml")
                .contentType(MediaType.APPLICATION_XML)
                .body(outputBytes);
    }
}
