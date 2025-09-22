package com.example.thym.controller;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import java.io.File;

public class FootnoteTransformer {

    public static void main(String[] args) throws Exception {
    	 DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    	 DocumentBuilder builder = factory.newDocumentBuilder();
    	 Document doc = builder.parse("input.xml");
    	 
    	 NodeList file = doc.getElementsByTagName("w:footnote");
    	 for(int i =0;i<file.getLength();i++) {
    		 Node node = file.item(i);
    		 System.out.println("content : " + node.getTextContent());
    	 }
    	 
       
    }
}

