package ch.epfl.javelo.gui;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.PointWebMercator;
import ch.epfl.javelo.routing.Route;
import ch.epfl.javelo.routing.RoutePoint;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import java.util.function.Consumer;

/**
 * Gestionnaire de l'affichage de la carte annotée
 *
 * @author Albert Fares (341018)
 * @author Etienne Asselin (340201)
 */
public final class AnnotatedMapManager {

    /**
     * Constante qui represente la distance maximale a laquelle la souris est detectee par l'itineraire
     */
    private static final double MAX_VISIBLE_DISTANCE = 15;

    /**
     * Paramètres de fond de carte initiaux
     */
    private final static MapViewParameters INITIAL_MVP = new MapViewParameters(12, 543200, 370650);

    /**
     * Bean javaFX de l'itineraire
     */
    private final RouteBean routeBean;

    /**
     * Paneau empilateur
     */
    private final StackPane pane;

    /**
     * Propriete qui stocke la position de la souris sur l'itineraire
     */
    private final DoubleProperty mousePositionOnRoute;

    /**
     * Propriete qui stocke les paramètres du fond de carte
     */
    private final ObjectProperty<MapViewParameters> mapViewParameters;

    /**
     * Propriete qui stocke la position de la souris
     */
    private final ObjectProperty<Point2D> mouseProperty;

    private BaseMapManager baseMapManager;


    /**
     * Constructeur de la carte «annotée»
     * @param graph le graphe du réseau routier
     * @param tileManager gestionnaire des tuiles OSM
     * @param routeBean bean javaFX de l'itineraire
     * @param error erreur qui pourrait être soulevée
     */
    public AnnotatedMapManager(Graph graph, TileManager tileManager, RouteBean routeBean, Consumer<String> error) {
        this.routeBean = routeBean;
        this.mousePositionOnRoute = new SimpleDoubleProperty();
        this.mouseProperty = new SimpleObjectProperty<>();
        this.mapViewParameters = new SimpleObjectProperty<>(INITIAL_MVP);

        WaypointsManager waypointsManager=new WaypointsManager(graph, this.mapViewParameters,routeBean.waypointsProperty(),error);

        this.baseMapManager = new BaseMapManager(tileManager,waypointsManager, this.mapViewParameters);

        RouteManager routeManager = new RouteManager(routeBean, this.mapViewParameters);

        this.pane=new StackPane(baseMapManager.pane(),routeManager.pane(),waypointsManager.pane());
        pane.getStylesheets().add("map.css");

        eventsHandler();
    }

    /**
     * Accesseur du panneau javaFX principal
     * @return le panneau
     */
    public Pane pane(){
        return pane;
    }

    /**
     * Acesseur de la propriété de la position de la souris sur l'itineraire
     * @return la propriété
     */
    public ReadOnlyDoubleProperty mousePositionOnRouteProperty(){
        return mousePositionOnRoute;
    }

    /**
     * Gestionnaire des evenements de la carte annotée
     */
    private void eventsHandler(){

        pane.setOnMouseMoved(e -> mouseProperty.set(new Point2D(e.getX(),e.getY())));
        pane.setOnMouseDragged(e -> mouseProperty.set(null));
        pane.setOnMouseExited(e -> mouseProperty.set(null));

        mousePositionOnRoute.bind(Bindings.createDoubleBinding(()-> {
            Point2D mousePosition=mouseProperty.get();
            Route route = routeBean.routeProperty().get();
            MapViewParameters mapViewParameters = this.mapViewParameters.get();

            if (route != null) {
                if (mousePosition == null) return Double.NaN;

                PointWebMercator point = mapViewParameters.pointAt(mousePosition.getX(), -mousePosition.getY());
                PointCh pointCh = point.toPointCh();

                if (pointCh == null){
                    return Double.NaN;
                }

                RoutePoint routePoint = route.pointClosestTo(pointCh);
                PointWebMercator routePointWebMercator = PointWebMercator.ofPointCh(routePoint.point());

                double XPixelDiff = mapViewParameters.viewX(point) - mapViewParameters.viewX(routePointWebMercator);
                double YPixelDiff = mapViewParameters.viewY(point) - mapViewParameters.viewY(routePointWebMercator);
                double pixelDiff = Math2.norm(XPixelDiff, YPixelDiff);

                if (pixelDiff <= MAX_VISIBLE_DISTANCE) {
                    return routePoint.position();
                }
            }

            return Double.NaN;

        },mouseProperty,routeBean.routeProperty(), mapViewParameters));

    }

    public void forceDrawMap(){
        baseMapManager.forceDrawMap();
    }

}
