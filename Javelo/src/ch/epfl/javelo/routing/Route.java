package ch.epfl.javelo.routing;

import ch.epfl.javelo.projection.PointCh;

import java.util.List;

/**
 * Interface qui représente un itinéraire, qui peut être simplement entre deux points, ou comporter des points intermédiaires
 *
 * @author Albert Fares (341018)
 * @author Etienne Asselin (340201)
 */
public interface Route {

    /**
     *Retourne l'index de l'arête à la position donnée
     * @param position la position le long de l'arête en mètres
     * @return l'index de l'arête
     */
    int indexOfSegmentAt(double position);

    /**
     * Retourne la longueur de l'itinéraire
     * @return la longueur en mètres
     */
    double length();

    /**
     * Retourne la liste de la totalité des arêtes de l'itinéraire
     * @return la liste des arêtes
     */
    List<Edge> edges();

    /**
     * Retourne la liste de la totalité des coordonnées des noeuds de l'itinéraire
     * @return la liste des noeuds
     */
    List<PointCh> points();

    /**
     * Renvoie le point situé à la position donnée sur l'itinéraire
     * @param position la position le long de l'itinéraire
     * @return le point en question, sous forme PointCh
     */
    PointCh pointAt(double position);

    /**
     * Calcule l'altitude du point de l'itinéraire à la position donnée
     * @param position la position donnée en mètre le long de l'itinéraire
     * @return l'altitude au niveau de ce point
     */
    double elevationAt(double position);

    /**
     * Retourne l'identité du noeud de l'itinéraire le plus proche de la position donnée
     * @param position la position le long de l'itinéraire
     * @return l'identité du noeud le plus proche
     */
    int nodeClosestTo(double position);

    /**
     * Retourne le point de l'itinéraire se trouvant le plus proche du point de référence donnée
     * @param point point de référence sous forme PointCh
     * @return le point de l'itinéraire sous forme Routing Point
     */
    RoutePoint pointClosestTo(PointCh point);



}
