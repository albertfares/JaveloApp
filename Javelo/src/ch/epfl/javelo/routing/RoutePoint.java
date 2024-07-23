package ch.epfl.javelo.routing;

import ch.epfl.javelo.projection.PointCh;

/**
 * Enregistrement qui représente le point d'un itinéraire le plus proche d'un point de
 * référence donné, qui se trouve dans le voisinage de l'itinéraire
 *
 * @author Albert Fares (341018)
 * @author Etienne Asselin (340201)
 */

public record RoutePoint(PointCh point, double position, double distanceToReference) {

    /**
     * Constante qui represente un point inexistant
     */
    public static final RoutePoint NONE = new RoutePoint(null, Double.NaN, Double.POSITIVE_INFINITY);

    /**
     * Retourne un point identique au récepteur (this) mais dont la position est
     * décalée de la différence donnée, qui peut être positive ou négative
     * @param positionDifference difference de position
     * @return le point dont la position est modifiee
     */
    public RoutePoint withPositionShiftedBy(double positionDifference) {
        return new RoutePoint(point, position + positionDifference, distanceToReference);
    }

    /**
     * Retourne this si sa distance à la référence est inférieure ou égale à celle de that, et that sinon
     * @param that le point auquel on voudrais comparer this
     * @return this si sa distance à la référence est inférieure ou égale à celle de that, et that sinon
     */
    public RoutePoint min(RoutePoint that) {
        return (this.distanceToReference <= that.distanceToReference) ? this : that;
    }

    /**
     * Retourne this si sa distance à la référence est inférieure ou égale à thatDistanceToReference,
     * et une nouvelle instance de RoutePoint dont les attributs sont les arguments passés à min sinon.
     * @param thatPoint pointCh pour lequel on voudrais comparer this
     * @param thatPosition position du point le long de l'itinéraire
     * @param thatDistanceToReference la distance, en mètres, entre le point et la référence
     * @return retourne this si sa distance à la référence est inférieure ou égale à thatDistanceToReference,
     * et une nouvelle instance de RoutePoint dont les attributs sont les arguments passés à min sinon.
     */
    public RoutePoint min(PointCh thatPoint, double thatPosition, double thatDistanceToReference) {
        return (this.distanceToReference<=thatDistanceToReference) ?
                this : new RoutePoint(thatPoint, thatPosition, thatDistanceToReference);
    }

}
