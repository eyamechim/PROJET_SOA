package org.example;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;

public class Main {

    private static final String XML_PATH = "src/main/resources/cabinet.xml";
    private static final String XSD_PATH = "src/main/resources/cabinet.xsd";

    public static void main(String[] args) {
        try {

            if (validerXmlAvecXsd()) {
                System.out.println("\n=== Requetes XPath du cabinet medical ===");
                interrogerAvecXPath();
            } else {

                System.out.println("XPath non execute : corriger d'abord le fichier XML.");
            }
        } catch (Exception e) {

            System.err.println("Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static boolean validerXmlAvecXsd() {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new File(XSD_PATH));
            javax.xml.validation.Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new File(XML_PATH)));
            System.out.println("Validation reussie : cabinet.xml respecte cabinet.xsd.");
            return true;
        } catch (Exception e) {

            System.out.println("Validation echouee : " + e.getMessage());
            return false;
        }
    }

    private static void interrogerAvecXPath() throws Exception {

        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(new File(XML_PATH));
        document.getDocumentElement().normalize();


        XPath xpath = XPathFactory.newInstance().newXPath();

        afficherResultats(xpath, document, "//patient/nom", "Tous les patients");
        afficherResultats(xpath, document, "//patient[@actif='true']/nom", "Patients actifs");
        afficherResultats(xpath, document,
                "//rendezVousItem[@statut='confirme']/motif", "Rendez-vous confirmes");


        afficherResultats(xpath, document,
                "//rendezVousItem[@statut='confirme' and @medecinId='M001']/motif",
                "Rendez-vous confirmes chez le medecin M001");


        afficherResultats(xpath, document,
                "//rendezVousItem[date='2026-11-02']/motif", "Rendez-vous du 2 novembre 2026");


        afficherResultats(xpath, document,
                "//rendezVousItem[@statut='en_attente']/motif", "Rendez-vous en attente");
    }


    private static void afficherResultats(XPath xpath, Document document,
                                          String expression, String titre) throws Exception {
        NodeList resultats = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);
        System.out.println("\n" + titre + " :");


        if (resultats.getLength() == 0) {
            System.out.println("  (aucun resultat)");
            return;
        }


        for (int i = 0; i < resultats.getLength(); i++) {
            Node noeud = resultats.item(i);
            System.out.println("  - " + noeud.getTextContent());
        }
    }
}
