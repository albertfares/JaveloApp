package ch.epfl.javelo.routing;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.projection.PointCh;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe qui représente un itinéraire multiple
 *
 * @author Albert Fares (341018)
 * @author Etienne Asselin (340201)
 */
public final class MultiRoute implements Route{

    /**
     * Liste de route qui représente les itinéraires composants l'itinéraire
     */
    private final List<Route> segments;

    /**
     * Constructeur qui renvoie l'itinéraire multiple qui contient 
     * les itinéraires contenus dans segments
     * 
     * @param segments Ensemble d'itinéraire formant l'itinéraire multiple
     */
    public MultiRoute(List<Route> segments){

        Preconditions.checkArgument(segments != null && !segments.isEmpty());

        this.segments = List.copyOf(segments);
    }

    /**
     * Méthode qui détermine l'index de l'itinéraire simple 
     * (Single Route) à la position donnée
     * 
     * @param position la position le long de l'arête en mètres
     * @return l'index de l'itinéraire simple au niveau de cette position
     */
    @Override
    public int indexOfSegmentAt(double position) {

        int indexofSegment = indexOfRouteAtPosition(position);
        int numberofRoutes = 0;

        for (int i = 0; i < indexofSegment ; i++){
            numberofRoutes += countRoutes(i);
        }

        double positionOnSegment = position - lengthBeforeIndex(indexofSegment);

        return numberofRoutes + segments.get(indexofSegment).indexOfSegmentAt(positionOnSegment);
    }

    /**
     * Retourne la longueur de l'itinéraire
     * 
     * @return la longueur en mètres
     */
    @Override
    public double length() {
        double length = 0;

        for (Route route : segments){
            length += route.length();
        }

        return length;
    }

    /**
     * Retourne la liste de la totalité des arêtes de l'itinéraire
     * 
     * @return la liste des arêtes
     */
    @Override
    public List<Edge> edges() {

        List<Edge> edges = new ArrayList<>();

        for (Route route : segments){
            edges.addAll(route.edges());
        }

        return edges;
    }

    /**
     * Retourne la liste de la totalité des coordonnées des noeuds de l'itinéraire
     * 
     * @return la liste des noeuds
     */
    @Override
    public List<PointCh> points() {

        List<PointCh> pointChList = new ArrayList<>();

        Route firstRoute=segments.get(0);

        pointChList.add(firstRoute.pointAt(0));

        for (Route route : segments){
            List<PointCh> list = route.points();

            list.remove(0);

            pointChList.addAll(list);
        }

        return pointChList;
    }

    /**
     * Renvoie le point situé à la position donnée sur l'itinéraire
     * 
     * @param position la position le long de l'itinéraire
     * @return le point en question, sous forme PointCh
     */
    @Override
    public PointCh pointAt(double position) {

        int indexOfRouteAtPosition = indexOfRouteAtPosition(position);

        double positionOnSegment = position - lengthBeforeIndex(indexOfRouteAtPosition);

        return segments.get(indexOfRouteAtPosition).pointAt(positionOnSegment);
    }

    /**
     * Calcule l'altitude du point de l'itinéraire à la position donnée
     * 
     * @param position la position donnée en mètre le long de l'itinéraire
     * @return l'altitude au niveau de ce point
     */
    @Override
    public double elevationAt(double position) {

        int indexOfRouteAtPosition = indexOfRouteAtPosition(position);

        double positionOnSegment = position - lengthBeforeIndex(indexOfRouteAtPosition);

        return segments.get(indexOfRouteAtPosition).elevationAt(positionOnSegment);
    }

    /**
     * Retourne l'identité du noeud de l'itinéraire le plus proche de la position donnée
     * 
     * @param position la position le long de l'itinéraire
     * @return l'identité du noeud le plus proche
     */
    @Override
    public int nodeClosestTo(double position) {

        int indexOfRouteAtPosition=indexOfRouteAtPosition(position);

        double positionOnSegment = position - lengthBeforeIndex(indexOfRouteAtPosition);

        return segments.get(indexOfRouteAtPosition).nodeClosestTo(positionOnSegment);
    }

    /**
     *Retourne le point de l'itinéraire se trouvant le plus proche du point de référence donnée
     * 
     * @param point point de référence sous forme PointCh
     * @return le point de l'itinéraire sous forme Routing Point
     */
    @Override
    public RoutePoint pointClosestTo(PointCh point) {

        double distanceToPoint = Double.MAX_VALUE;
        RoutePoint closestRoutePoint = null;

        for (int i = 0; i < segments.size(); i++){
            RoutePoint routePointToCompare = segments.get(i)
                    .pointClosestTo(point)
                    .withPositionShiftedBy(lengthBeforeIndex(i));

            if (routePointToCompare.distanceToReference() <= distanceToPoint){
                closestRoutePoint = routePointToCompare;
                distanceToPoint = closestRoutePoint.distanceToReference();
            }
        }

        return closestRoutePoint;
    }

    /**
     * Determine l'index du segment à la position donnée
     * 
     * @param position position sur l'itineraire
     * @return l'index du segement
     */
    private int indexOfRouteAtPosition(double position){

        double clampedPosition = Math2.clamp(0, position, length());
        double length = 0;
        int indexOfSegment = 0;

        for (int i = 0; i < segments.size(); i++){
            length += segments.get(i).length();

            if (length >= clampedPosition){
                indexOfSegment = i;

                break;
            }
        }

        return indexOfSegment;
    }

    /**
     * Renvoie le nombre de routes qui composent le segment d'index donné
     * 
     * @param index index dans la liste des segments
     * @return le nombre de routes du segment à cet index
     */
    private int countRoutes(int index){
        Route routeAtIndex = segments.get(index);

        return routeAtIndex.indexOfSegmentAt(routeAtIndex.length()) + 1;
    }

    /**
     * Determine la longueur de la route constituée par les segments se trouvant
     * avant l'index du segment donné
     * 
     * @param index index du segment
     * @return la longueur en metres
     */
    private double lengthBeforeIndex(int index){

        double length = 0;

        for (int i = 0; i < index; i++){
            length += segments.get(i).length();
        }

        return length;
    }
}
