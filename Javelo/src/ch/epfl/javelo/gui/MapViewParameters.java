package ch.epfl.javelo.gui;

import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.projection.PointWebMercator;
import javafx.geometry.Point2D;

/**
 * Enregistrement qui représente les paramètres du fond de carte présenté dans l'interface graphique.
 *
 * @author Albert Fares (341018)
 * @author Etienne Asselin (340201)
 */
public record MapViewParameters(int zoomLevel, double xCoordinate, double yCoordinate) {

    /**
     * Niveau de zoom maximal
     */
    public static final int MAX_ZOOM = 19;

    /**
     * Niveau de zoom minimal
     */
    public static final int MIN_ZOOM = 8;

    /**
     * Constructeur compact qui permet de vérifier que les arguments passés
     * en paramètres au constructeur sont valides
     * @param zoomLevel niveau de zoom
     * @param xCoordinate coordonnée x en pixel
     * @param yCoordinate coordonnée y du pixel
     */
    public MapViewParameters {
        Preconditions.checkArgument(0<=zoomLevel && zoomLevel<=MAX_ZOOM
                && 0 <= xCoordinate && xCoordinate < 1<<(zoomLevel+PointWebMercator.ZOOM_0)
                && 0 <= yCoordinate && yCoordinate < 1<<(zoomLevel+PointWebMercator.ZOOM_0));
    }

    /**
     * Retourne les coordonnées du coin haut-gauche sous la forme d'un objet de type Point2D
     *
     * @return un Point2D avec les coordonnées respectives
     */
    public Point2D topLeft(){
        return new Point2D(xCoordinate, yCoordinate);
    }

    /**
     * Retourne une instance de MapViewParameters identique au récepteur,
     * mais avec les coordonnees du coin haut-gauche passées en argument.
     *
     * @param xCoordinate coordonnées X du coin haut-gauche
     * @param yCoordinate coordonnées Y du coin haut-gauche
     * @return l'instance de MapViewParameters voulue
     */
    public MapViewParameters withMinXY (double xCoordinate, double yCoordinate){
        return new MapViewParameters(this.zoomLevel, xCoordinate, yCoordinate);
    }

    /**
     * Retourne un PointWebMercator a partir du point de coordonnees x et y relative au coin haut-gauche
     *
     * @param pointXCoordinate coordonnée x exprimeé par rapport au coin haut-gauche
     * @param pointYCoordinate coordonnée y exprimeé par rapport au coin haut-gauche
     * @return le point sous la forme d'une instance de PointWebMercator
     */
    public PointWebMercator pointAt(double pointXCoordinate, double pointYCoordinate){

        return PointWebMercator.of(this.zoomLevel,
                this.xCoordinate + pointXCoordinate,
                this.yCoordinate - pointYCoordinate);
    }

    /**
     * Retourne la position X du PointWebMercator passé en argument exprimée
     * par rapport au coin haut-gauche de la portion de carte affichée à l'écran.
     *
     * @param point PointWebMercator pour lequel on voudrait savoir la position
     * @return la position X du point
     */
    public double viewX(PointWebMercator point){
        return point.xAtZoomLevel(zoomLevel) - this.xCoordinate;
    }

    /**
     * Retourne la position Y du PointWebMercator passé en argument exprimée
     * par rapport au coin haut-gauche de la portion de carte affichée à l'écran.
     *
     * @param point PointWebMercator pour lequel on voudrait savoir la position
     * @return la position Y du point
     */
    public double viewY(PointWebMercator point){
        return this.yCoordinate - point.yAtZoomLevel(zoomLevel);
    }

}
