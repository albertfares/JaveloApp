package ch.epfl.javelo.projection;
import ch.epfl.javelo.Math2;

/**
 * Contient des methodes permettent de convertir entre les coordonnées WGS 84 et les coordonnées Web Mercator
 *
 * @author Etienne Asselin (340201)
 * @author Allbert Fares (341918)
 */


public final class WebMercator {

    /**
     * Constructeur privé car la classe est non instantiable
     */
    private WebMercator(){}

    /**
     * Calcule la coordonnée x de la projection d'un point se trouvant à la longitude lon, donnée en radians
     * @param lon longitude du point
     * @return la coordonnee x en radians dans le systeme web mercator
     */
    public static double x(double lon){
        return (lon+Math.PI)/(2*Math.PI);
    }

    /**
     * Calcule la coordonnée y de la projection d'un point se trouvant à la longitude lon, donnée en radians
     * @param lat longitude du point
     * @return la coordonnee y en radians dans le systeme web mercator
     */
    public static double y(double lat){
        return (Math.PI - Math2.asinh(Math.tan(lat)))/(2*Math.PI);
    }

    /**
     * Calcule la longitude, en radians, d'un point dont la projection se trouve à la coordonnée x donnée
     * @param x coordonnee horizontale du point dans le systeme web mercator
     * @return la longitude du point
     */
    public static double lon(double x){
        return 2*Math.PI*x - Math.PI;
    }

    /**
     * Calcule la latitude, en radians, d'un point dont la projection se trouve à la coordonnée x donnée
     * @param y coordonnee verticale du point dans le systeme web mercator
     * @return la latitude du point
     */
    public static double lat(double y){
        return Math.atan(Math.sinh(Math.PI - 2*Math.PI*y));
    }

}