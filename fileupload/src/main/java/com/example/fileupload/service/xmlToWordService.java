package com.example.fileupload.service;

import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayOutputStream;
import org.springframework.web.multipart.MultipartFile;

@Service
public class xmlToWordService {

    public byte[] convertXmlToWord(MultipartFile file) throws Exception {

        // Parse XML
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(file.getInputStream());

        NodeList footnotes = doc.getElementsByTagNameNS("*", "footnote");

        // Create Word document
        XWPFDocument document = new XWPFDocument();

        // Heading "Notes"
        XWPFParagraph heading = document.createParagraph();
        XWPFRun runHeading = heading.createRun();
        runHeading.setText("Notes");
        runHeading.setFontFamily("Times New Roman");
        runHeading.setColor("4b6043");
        runHeading.setFontSize(18);

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
            runNum.setFontFamily("Times New Roman");

            // Process child nodes of footnote
            NodeList pNodes = fn.getChildNodes();
            for (int p = 0; p < pNodes.getLength(); p++) {
                Node pNode = pNodes.item(p);
                if (pNode.getNodeName().endsWith("p")) { 
                    NodeList rNodes = pNode.getChildNodes();
                    for (int r = 0; r < rNodes.getLength(); r++) {
                        Node rNode = rNodes.item(r);

                        // Normal text <w:r><w:t>
                        if (rNode.getNodeName().endsWith("r")) {
                            NodeList tNodes = rNode.getChildNodes();
                            for (int t = 0; t < tNodes.getLength(); t++) {
                                Node tNode = tNodes.item(t);
                                if (tNode.getNodeName().endsWith("t")) {
                                    String text = tNode.getTextContent();
                                    if (text != null && !text.isEmpty()) {
                                        XWPFRun run = paragraph.createRun();
                                        run.setText(text);
                                        run.setFontFamily("Times New Roman");
                                        run.setFontSize(12);
                                    }
                                }
                            }
                        }

                        // Hyperlink <w:hyperlink>
                        if (rNode.getNodeName().endsWith("hyperlink")) {
                            NodeList hrRuns = rNode.getChildNodes();
                            for (int hr = 0; hr < hrRuns.getLength(); hr++) {
                                Node hrRun = hrRuns.item(hr);
                                if (hrRun.getNodeName().endsWith("r")) {
                                    NodeList hrTexts = hrRun.getChildNodes();
                                    for (int ht = 0; ht < hrTexts.getLength(); ht++) {
                                        Node htNode = hrTexts.item(ht);
                                        if (htNode.getNodeName().endsWith("t")) {
                                            String url = htNode.getTextContent();
                                            if (url != null && !url.isEmpty()) {
                                                XWPFHyperlinkRun hyperlink = paragraph.createHyperlinkRun(url);
                                                hyperlink.setText(url);
                                                hyperlink.setColor("0000FF");
                                                hyperlink.setFontFamily("Times New Roman");
                                                hyperlink.setUnderline(UnderlinePatterns.SINGLE);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            counter++;
        }

        // Convert to byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        document.write(outputStream);
        document.close();
        return outputStream.toByteArray();
    }
}
