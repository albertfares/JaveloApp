package ch.epfl.javelo.routing;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.projection.PointCh;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Classe qui représente un itinéraire simple.
 *
 * @author Albert Fares (341018)
 * @author Etienne Asselin (340201)
 */

public final class SingleRoute implements Route {

    /**
     * Liste d'arêtes qui constituent l'itineraire.
     */
    private final List<Edge> edges;

    /**
     * Tableau servant à la recherche dichotomique sur les arêtes de l'itinéraire
     */
    private final double[] filledArray;

    /**
     * Longeur de la route
     */
    private final double length;

    /**
     * Liste des coordonnées des noeuds de l'itinéraire
     */
    private final List<PointCh> points;

    /**
     * Constructeur qui retourne l'itinéraire simple composé des arêtes données et leve une
     * IllegalArgumentException si la liste d'arêtes est vide
     * @param edges liste des aretes qui constituent la route
     */
    public SingleRoute(List<Edge> edges) {
        Preconditions.checkArgument(edges != null && edges.size() > 0);

        this.edges = List.copyOf(edges);
        filledArray = new double[edges.size()+1];
        fillBinarySearchArray(filledArray);

        length = lengthCalculator();
        this.points = pointsCalculator();
    }

    /**
     * Retourne l'index de la route à la position donnée
     * @param position la position le long de la route en mètres
     * @return 0 pour une singleRoute
     */
    @Override
    public int indexOfSegmentAt(double position) {
        return 0;
    }

    /**
     * Retourne la longueur de l'itinéraire
     * @return la longueur en mètres
     */
    @Override
    public double length() {
        return length;
    }

    /**
     * Retourne la liste de la totalité des arêtes de l'itinéraire
     * @return la liste des arêtes
     */
    @Override
    public List<Edge> edges() {
        return edges;
    }

    /**
     * Retourne la liste de la totalité des coordonnées des noeuds de l'itinéraire
     * @return la liste des noeuds
     */
    @Override
    public List<PointCh> points() {
        return new ArrayList<>(points);
    }

    /**
     * Renvoie le point situé à la position donnée sur l'itinéraire
     * @param position la position le long de l'itinéraire
     * @return le point en question, sous forme PointCh
     */
    @Override
    public PointCh pointAt(double position) {

        int index = Arrays.binarySearch(filledArray, position);

        int complementaryIndex = ~index;

        if (index == -1) {
            return edges.get(0).pointAt(0);
        }

        if (complementaryIndex > edges.size() || index == edges.size()) {
            Edge lastEdge=edges.get(edges.size() - 1);

            return lastEdge.pointAt(lastEdge.length());

        }
        if (index < 0) {
            double positionOnEdge = position - filledArray[complementaryIndex - 1];

            return edges.get(complementaryIndex - 1).pointAt(positionOnEdge);

        }
        return edges.get(index).pointAt(0);

    }

    /**
     * Calcule l'altitude du point de l'itinéraire à la position donnée
     * @param position la position donnée en mètre le long de l'itinéraire
     * @return l'altitude au niveau de ce point
     */
    @Override
    public double elevationAt(double position) {

        int index = Arrays.binarySearch(filledArray, position);

        int complementaryIndex = ~index;


        if (index == -1) {
            return edges.get(0).elevationAt(0);
        }

        if (complementaryIndex > edges.size() || index == edges.size()) {
            Edge lastEdge = edges.get(edges.size() - 1);

            return lastEdge.elevationAt(lastEdge.length());

        }
        if (index < 0) {
            double positionOnEdge = position - filledArray[complementaryIndex - 1];

            return edges.get(complementaryIndex - 1).elevationAt(positionOnEdge);

        } else {

            return edges.get(index).elevationAt(0);
        }
    }

    /**
     * Retourne l'identité du noeud de l'itinéraire le plus proche de la position donnée
     * @param position la position le long de l'itinéraire
     * @return l'identité du noeud le plus proche
     */
    @Override
    public int nodeClosestTo(double position) {

        int index = Arrays.binarySearch(filledArray, position);

        int complementaryIndex = ~index;

        if (index == -1) {
            return edges.get(0).fromNodeId();
        }

        if (complementaryIndex > edges.size() || index == edges.size()) {
            return edges.get(edges.size() - 1).toNodeId();

        }

        if (index < 0) {
            double halfwayBetweenSurroundingNodes = (filledArray[(complementaryIndex)] - filledArray[complementaryIndex - 1])/2;
            double positionInArray = position - filledArray[complementaryIndex - 1];

            if (positionInArray > halfwayBetweenSurroundingNodes) {
                return edges.get((complementaryIndex) - 1).toNodeId();
            }

            if (positionInArray <= halfwayBetweenSurroundingNodes) {
                return edges.get(complementaryIndex - 1).fromNodeId();
            }
        }
        return edges.get(index).fromNodeId();
    }

    /**
     * Retourne le point de l'itinéraire se trouvant le plus proche du point de référence donnée
     * @param point point de référence sous forme PointCh
     * @return le point de l'itinéraire sous forme Routing Point
     */
    @Override
    public RoutePoint pointClosestTo(PointCh point) {
        double smallestLength = Double.MAX_VALUE;
        int index = 0;
        double positionOnEdge = 0;

        PointCh closestPoint = null;

        for (int i = 0; i < edges.size(); i++) {
            double projectionPosition = Math2.clamp(0, edges.get(i).positionClosestTo(point), edges.get(i).length());

            PointCh tempClosePoint = this.pointAt(positionOfPointOnRoute(projectionPosition, i));

            if (smallestLength >= tempClosePoint.distanceTo(point)) {
                closestPoint = tempClosePoint;
                index = i;
                smallestLength = tempClosePoint.distanceTo(point);
                positionOnEdge = projectionPosition;
            }
        }



        return new RoutePoint(closestPoint, positionOfPointOnRoute(positionOnEdge, index), smallestLength);
    }

    /**
     * Methode outil qui permet de retrouver la position d'un point sur l'itineraire sachant sa
     * position sur l'arete d'identite donnee
     * @param positionOnEdge position du point sur l'arete d'identite donnee
     * @param edgeIndex index de l'arete sur laquelle se trouve le point
     * @return la position du point sur l'itineraire en metres.
     */
    private double positionOfPointOnRoute(double positionOnEdge, int edgeIndex) {
        double totalPosition = 0;

        for (int i = 0; i < edgeIndex; i++) {
            totalPosition += edges.get(i).length();
        }
        totalPosition += positionOnEdge;

        return totalPosition;
    }

    /**
     * Méthode outil qui remplit le tableau array des longueurs successives de la route
     * à chaque edge dans l'optique d'effectuer un binarySearch sur ce tableau
     * @param array Tableau des longueurs
     */
    private void fillBinarySearchArray(double[] array) {

        for (int i = 1; i < edges.size() + 1; i++) {
            double lengthNew = array[i - 1] + edges.get(i - 1).length();

            array[i] = lengthNew;
        }
    }

    /**
     * Méthode outil qui calcule la longueur de l'itinéraire
     * @return la longueur en mètres
     */
    private double lengthCalculator(){
        double totalLength = 0;

        for (Edge edge : edges) {
            totalLength += edge.length();
        }

        return totalLength;
    }

    /**
     * Méthode outil qui détermine la liste des coordonnées des noeuds de l'itinéraire
     * @return la liste des coordonnées
     */
    private List<PointCh> pointsCalculator(){
        List<PointCh> pointChList = new ArrayList<>();

        for (Edge edge : edges) {
            pointChList.add(edge.pointAt(0));
        }

        Edge lastEdge = edges.get(edges.size() - 1);
        pointChList.add(lastEdge.pointAt(lastEdge.length()));

        return pointChList;
    }
}
