package ch.epfl.javelo.projection;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;


/**
 * Coordonnées de points en Suisse en CH1903+
 *
 * @author Albert Fares (341018)
 * @author Etienne Asselin (340201)
 */

public record PointCh(double e,double n){

    /**
     * Constructeur compact qui lève une IllegalArgumentException si les coordonnées fournies
     * ne sont pas dans les limites de la Suisse définies par SwissBounds.
     */
    public PointCh {
        Preconditions.checkArgument(SwissBounds.containsEN(e, n));
    }

    /**
     * Calcule la distance au carré entre deux point dont on connait les coordonnees suisses
     * @param that point pour lequel on voudrait connaitre la distance
     * @return le carré de la distance en mètres séparant le récepteur (this) de l'argument (that)
     */
    public double squaredDistanceTo(PointCh that){
        return Math2.squaredNorm(this.e - that.e,this.n - that.n);
    }

    /**
     * Calcule la distance entre deux point dont on connait les coordonnees suisses
     * @param that point pour lequel on voudrait connaitre la distance
     * @return la distance en mètres séparant le récepteur (this) de l'argument (that)
     */
    public double distanceTo(PointCh that){
        return Math.sqrt(squaredDistanceTo(that));
    }

    /**
     * Calcule la longitude d'un point donne en coordonnees suisses
     * @return la longitude du point, dans le système WGS84, en radians
     */
    public double lon(){
        return Ch1903.lon(this.e, this.n);
    }

    /**
     * Calcule la latitude d'un point donne en coordonnees suisses
     * @return la latitude du point, dans le système WGS84, en radians
     */
    public double lat(){
        return Ch1903.lat(this.e, this.n);
    }



}



