package ch.epfl.javelo.projection;
import ch.epfl.javelo.Preconditions;

/**
 * Représente un point dans le système Web Mercator
 * Le contructeur prend en argument les coordonnes x et y exprimees dans le systeme web mercator
 *
 * @author Albert Fares (341918)
 * @author Etienne Asselin (340201)
 */

public record PointWebMercator(double x, double y) {

    /**
     * Logarithme en base 2 de la longueur du coté de la carte de la terre (256) au niveau de zoom 0
     */
    public final static int ZOOM_0 = 8;

    /**
     *  Constructeur qui verfie les coordonnées qu'il reçoit et lève une IllegalArgumentException
     *  sinon.
     * @param x coordonnee horizontale dans le systeme web mercator
     * @param y coordonnee verticale dans le systeme we mercator
     */
    public PointWebMercator{
        Preconditions.checkArgument(isInInterval(x, y));
        }

    /**
     * Retourne le point dont les coordonnées sont x et y au niveau de zoom zoomLevel
     * @param zoomLevel niveau du zoom auquel se trouve le point
     * @param x coordonnee horizontale du point dans le systeme web mercator
     * @param y coordonnee verticale du point dans le systeme web mercator
     * @return le point recherché
     */
    public static PointWebMercator of(int zoomLevel, double x, double y){
        return new PointWebMercator(
                Math.scalb(x, -(ZOOM_0 + zoomLevel)),
                Math.scalb(y, -(ZOOM_0 + zoomLevel)));
    }

    /**
     * Retourne le point Web Mercator correspondant au point du système de coordonnées suisse donné.
     * @param pointCh point dans le systeme suisse
     * @return le point correspondant dans le systeme web mercator
     */
    public static PointWebMercator ofPointCh(PointCh pointCh){
        return new PointWebMercator(
                WebMercator.x(pointCh.lon()),
                WebMercator.y(pointCh.lat()));
    }

    /**
     * Retourne la coordonnée x au niveau de zoom donné
     * @param zoomLevel niveau du zoom auquel se trouve le point
     * @return la coordonnnee horizonale x du point correspondant
     */
    public double xAtZoomLevel(int zoomLevel){
        return Math.scalb(this.x,ZOOM_0 + zoomLevel);
    }

    /**
     * Retourne la coordonnée y au niveau de zoom donné
     * @param zoomLevel niveau du zoom auquel se trouve le point
     * @return la coordonnnee verticale y du point correspondant
     */
    public double yAtZoomLevel(int zoomLevel){
        return Math.scalb(this.y, ZOOM_0 + zoomLevel);
    }

    /**
     * retourne la longitude du point, en radians
     * @return la longitude
     */
    public double lon(){
        return WebMercator.lon(x);
    }

    /**
     * retourne la latitude du point, en radians
     * @return la latitude
     */
    public double lat(){
        return WebMercator.lat(y);
    }

    /**
     * retourne le point de coordonnées suisses se trouvant à la même
     * position que le récepteur ou null si ce point n'est pas
     * dans les limites de la Suisse définies par la classe SwissBounds.
     * @return le point de coordonnees suisses correspondant
     */
    public PointCh toPointCh(){
        double e = Ch1903.e(lon(), lat());
        double n = Ch1903.n(lon(), lat());

        return (SwissBounds.containsEN(e,n)) ? new PointCh(e,n) : null;
    }

    /**
     * Verifie si les coordonnes passees en arguments sont bien comprises dans l'intervalle [0;1]
     * (methode utilisee dans lee constructeur compact)
     * @param x coordonnee horizonale dans le systeme web mercator
     * @param y coordonnee verticale dans le systeme web mercator
     * @return false si les coordonnes ne sont pas dans l'intervalle et true sinon.
     */
    private boolean isInInterval(double x, double y){
        return !( x < 0 || x > 1 || y < 0 || y > 1);
    }


}
