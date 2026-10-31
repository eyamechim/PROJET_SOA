package TP1;

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

/** Validation XML/XSD et consultation XPath pour un cabinet medical. */
public class Main {
    // Fichiers du projet, situes dans src/main/resources.
    private static final String XML_PATH = "src/main/resources/cabinet.xml";
    private static final String XSD_PATH = "src/main/resources/cabinet.xsd";

    public static void main(String[] args) {
        try {
            // La condition garantit qu'on interroge seulement un XML valide.
            if (validerXmlAvecXsd()) {
                System.out.println("\n=== Requetes XPath du cabinet medical ===");
                interrogerAvecXPath();
            } else {
                // Branche executee si le XML ne respecte pas le XSD.
                System.out.println("XPath non execute : corriger d'abord le fichier XML.");
            }
        } catch (Exception e) {
            // Gestion des erreurs de fichier, de parsing ou des API XML.
            System.err.println("Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Verifie que les donnees XML respectent le schema XSD. */
    private static boolean validerXmlAvecXsd() {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new File(XSD_PATH));
            javax.xml.validation.Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new File(XML_PATH)));
            System.out.println("Validation reussie : cabinet.xml respecte cabinet.xsd.");
            return true;
        } catch (Exception e) {
            // Toute erreur de structure ou de type rend la validation fausse.
            System.out.println("Validation echouee : " + e.getMessage());
            return false;
        }
    }

    /** Charge le document et lance les differentes recherches XPath. */
    private static void interrogerAvecXPath() throws Exception {
        // DOM charge le XML en arbre afin de pouvoir le parcourir.
        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(new File(XML_PATH));
        document.getDocumentElement().normalize();

        // XPathFactory cree le moteur d'evaluation des expressions XPath.
        XPath xpath = XPathFactory.newInstance().newXPath();

        afficherResultats(xpath, document, "//patient/nom", "Tous les patients");
        afficherResultats(xpath, document, "//patient[@actif='true']/nom", "Patients actifs");
        afficherResultats(xpath, document,
                "//rendezVousItem[@statut='confirme']/motif", "Rendez-vous confirmes");

        // Deux conditions : rendez-vous confirme ET associe au medecin M001.
        afficherResultats(xpath, document,
                "//rendezVousItem[@statut='confirme' and @medecinId='M001']/motif",
                "Rendez-vous confirmes chez le medecin M001");

        // Le filtre sur la date permet de retrouver les rendez-vous du 2 novembre.
        afficherResultats(xpath, document,
                "//rendezVousItem[date='2026-11-02']/motif", "Rendez-vous du 2 novembre 2026");

        // On selectionne les rendez-vous dont le statut est en attente.
        afficherResultats(xpath, document,
                "//rendezVousItem[@statut='en_attente']/motif", "Rendez-vous en attente");
    }

    /** Evalue l'expression puis affiche les valeurs retournees. */
    private static void afficherResultats(XPath xpath, Document document,
                                          String expression, String titre) throws Exception {
        NodeList resultats = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);
        System.out.println("\n" + titre + " :");

        // Si la selection est vide, on affiche un message et on quitte la methode.
        if (resultats.getLength() == 0) {
            System.out.println("  (aucun resultat)");
            return;
        }

        // Boucle sur les noeuds choisis par XPath et affiche leur texte.
        for (int i = 0; i < resultats.getLength(); i++) {
            Node noeud = resultats.item(i);
            System.out.println("  - " + noeud.getTextContent());
        }
    }
}
