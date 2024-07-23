package ch.epfl.javelo.routing;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;

import java.util.function.DoubleUnaryOperator;

/**
 * Enregistrement qui représente une arête d'un itinéraire
 *
 * @author Albert Fares (341018)
 * @author Etienne Asselin (340201)
 */
public record Edge(int fromNodeId, int toNodeId, PointCh fromPoint, PointCh toPoint, double length, DoubleUnaryOperator profile) {

    /**
     * Méthode statique qui facilite la création d'une instance de Edge
     * @param graph le graphe Javelo dans lequel se trouve l'arête
     * @param edgeId l'identité Javelo de l'arête
     * @param fromNodeId identité Javelo du noeud de départ de l'arête
     * @param toNodeId identité Javelo du noeud d'arrivée de l'arête
     * @return l'instance crée
     */
    public static Edge of(Graph graph, int edgeId, int fromNodeId, int toNodeId){
       return new Edge(fromNodeId, toNodeId,
               graph.nodePoint(fromNodeId),
               graph.nodePoint(toNodeId),
               graph.edgeLength(edgeId),
               graph.edgeProfile(edgeId));
    }

    /**
     * Détermine la position de la projection du point donné sur l'arête
     * @param point le point donné sous forme PointCh
     * @return la position en mètre de la projection le long de l'arête
     */
    public double positionClosestTo(PointCh point){
        return Math2.projectionLength(fromPoint.e(),
                fromPoint.n(),
                toPoint.e(),
                toPoint.n(),
                point.e(),
                point.n());
    }

    /**
     * Renvoie le point situé à la position donnée sur l'arête
     * @param position la position le long de l'arête
     * @return le point en question, sous forme PointCh
     */
    public PointCh pointAt(double position){
        if (length == 0){
            return fromPoint;
        }
        double positionFactor = position/length;

        double e = Math2.interpolate(fromPoint.e(),toPoint.e(),positionFactor);
        double n = Math2.interpolate(fromPoint.n(),toPoint.n(),positionFactor);

        return new PointCh(e, n);
    }

    /**
     * Calcule l'altitude du point de l'arête à la position donnée
     * @param position la position donnée en mètre le long de l'arête
     * @return l'altitude au niveau de ce point
     */
    public double elevationAt(double position){
        return profile.applyAsDouble(position);
    }


}
