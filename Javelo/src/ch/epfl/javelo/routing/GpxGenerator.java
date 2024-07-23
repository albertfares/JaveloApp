package ch.epfl.javelo.routing;
import ch.epfl.javelo.projection.PointCh;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

/**
 * Classe qui représente un générateur d'itinéraire au format GPX
 *
 * @author Albert Fares (341018)
 * @author Etienne Asselin (340201)
 */
public class GpxGenerator {

    /**
     * Constructeur priveé car la classe n'est pas instanciable
     */
    private GpxGenerator() {}

    /**
     * Methode qui crée le document GPX pour la route et de profil tout deux passés en argument
     * @param route Route pour laquelle on voudrait generer le GPX
     * @param profile Profil en long de la route
     * @return le document GPX de la route
     */
    public static Document createGpx(Route route, ElevationProfile profile) {
        Document doc = newDocument();

        Element root = doc
                .createElementNS("http://www.topografix.com/GPX/1/1",
                        "gpx");
        doc.appendChild(root);

        root.setAttributeNS(
                "http://www.w3.org/2001/XMLSchema-instance",
                "xsi:schemaLocation",
                "http://www.topografix.com/GPX/1/1 "
                        + "http://www.topografix.com/GPX/1/1/gpx.xsd");
        root.setAttribute("version", "1.1");
        root.setAttribute("creator", "JaVelo");

        Element metadata = doc.createElement("metadata");
        root.appendChild(metadata);

        Element name = doc.createElement("name");
        metadata.appendChild(name);
        name.setTextContent("Route JaVelo");

        Element routeElement = doc.createElement("rte");
        root.appendChild(routeElement);

        Iterator<Edge> iterator = route.edges().iterator();
        double currentLength = 0;

        for (PointCh pointCh : route.points()) {

            Element point = doc.createElement("rtept");
            routeElement.appendChild(point);

            point.setAttribute("lat", String.valueOf(Math.toDegrees(pointCh.lat())));
            point.setAttribute("lon", String.valueOf(Math.toDegrees(pointCh.lon())));

            Element elevation = doc.createElement("ele");
            point.appendChild(elevation);

            String textContent;

            if (iterator.hasNext()){
                Edge edge = iterator.next();
                textContent = String.valueOf(profile.elevationAt(currentLength));
                currentLength += edge.length();
            }
            else {
                textContent = String.valueOf(profile.elevationAt(route.length()));
            }
            elevation.setTextContent(textContent);
        }

        return doc;
    }

    /**
     * Methode qui ecrit le GPX correspondant dans le fichier de nom passé en argument
     * @param fileName nom du fichier GPX crée
     * @param route route pour laquelle on voudrait ecrire le fichier GPX
     * @param profile le profile en long de la route
     * @throws IOException en cas d'erreur d'entrée/sortie
     */
    public static void writeGpx(String fileName, Route route, ElevationProfile profile) throws IOException {
        Document doc = createGpx(route, profile);
        Writer w = new FileWriter(fileName);

        try {
            Transformer transformer = TransformerFactory
                    .newDefaultInstance()
                    .newTransformer();

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(doc), new StreamResult(w));

        } catch (TransformerException e) {
            throw new Error(e);

        }
    }

    /**
     * Methode outil qui permet de crée un Document
     * @return le document
     */
    private static Document newDocument() {
        try {
            return DocumentBuilderFactory
                    .newDefaultInstance()
                    .newDocumentBuilder()
                    .newDocument();

        } catch (ParserConfigurationException e) {
            throw new Error(e);
        }
    }

}
