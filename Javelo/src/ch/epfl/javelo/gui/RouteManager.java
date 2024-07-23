package ch.epfl.javelo.gui;

import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.PointWebMercator;
import ch.epfl.javelo.routing.Route;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polyline;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestionnaire de l'affichage graphique de l'itinéraire
 *
 * @author Albert Fares (341018)
 * @author Etienne Asselin (340201)
 */
public final class RouteManager {

    /**
     * Bean JavaFx de l'itinéraire
     */
    private final RouteBean routeBean;

    /**
     * Propriété contenant les paramètres de fond de carte
     */
    private final ReadOnlyObjectProperty<MapViewParameters> mapViewParameters;

    /**
     * Panneau JavaFx du gestionnaire
     */
    private final Pane pane;

    /**
     * Polyligne qui affiche l'itinéraire courant
     */
    private final Polyline polyline;

    /**
     * Cercle qui affiche la position à mettre en évidence
     */
    private final Circle circle;

    /**
     * Constante qui définit le rayon du cercle
     */
    private static final int RADIUS = 5;

    /**
     * Constructeur du gestionnaire de l'affichage de l'itinéraire
     * @param routeBean Bean JavaFx de l'itinéraire
     * @param mapViewParameters Propriété contenant les paramètres de fond de carte
     */
    public RouteManager(RouteBean routeBean, ReadOnlyObjectProperty<MapViewParameters> mapViewParameters) {

        this.routeBean = routeBean;
        this.mapViewParameters = mapViewParameters;

        RouteManagerHandler handler = new RouteManagerHandler();

        this.circle = new Circle(RADIUS);
        this.polyline=new Polyline();

        circle.visibleProperty().bind(Bindings.createBooleanBinding(()->!Double.isNaN(routeBean.highlightedPositionProperty().get())
                ,routeBean.highlightedPositionProperty()));
        polyline.visibleProperty().bind(Bindings.createBooleanBinding(()->routeBean.routeProperty().get()!=null
                ,routeBean.routeProperty()));

        styleNodes();

        this.pane = new Pane(polyline, circle);

        pane.setPickOnBounds(false);

        routeBean.routeProperty().addListener( observable ->
            draw());

        mapViewParameters.addListener((p,oldS,newS) -> {
            if (newS.zoomLevel() != oldS.zoomLevel()){
                draw();

            }else if (!oldS.topLeft().equals(newS.topLeft())){
                reposition(oldS,newS);
            }
        });

        handler.setOnClick();

        routeBean.highlightedPositionProperty().addListener((e,oldS,newS)-> drawCircle());

    }

    /**
     * Getter du panneau de gestionnaire de l'affichage de l'itinéraire
     * @return le panneau
     */
    public Pane pane() {
        return pane;
    }

    /**
     * Méthode outil qui permet de définir le style d'affichage du cercle et de la polyligne
     */
    private void styleNodes(){

        circle.setId("highlight");
        circle.getStyleClass().add("highlight");

        polyline.setId("route");
        polyline.getStyleClass().add("route");
    }

    /**
     * Méthode outil qui permet de dessiner le cercle et la polyligne
     */
    private void draw(){
        drawPolyline();
        drawCircle();
    }

    /**
     * Méthode outil qui dessine la polyligne en fonction de l'itinéraire courant
     */
    private void drawPolyline() {

        if (routeBean.routeProperty().get() != null) {


            List<Double> list = new ArrayList<>();

            for (PointCh p : routeBean.routeProperty().get().points()) {
                PointWebMercator pointWebMercator = PointWebMercator.ofPointCh(p);

                list.add(pointWebMercator.xAtZoomLevel(mapViewParameters.get().zoomLevel()));
                list.add(pointWebMercator.yAtZoomLevel(mapViewParameters.get().zoomLevel()));
            }

            polyline.getPoints().setAll(list);

            polyline.setLayoutX(-mapViewParameters.get().xCoordinate());
            polyline.setLayoutY(-mapViewParameters.get().yCoordinate());

        }

    }

    /**
     * Méthode outil qui permet de dessiner le cercle
     */
    private void drawCircle (){

        if (routeBean.routeProperty().get() != null) {
            PointCh routePoint = routeBean.routeProperty().get().pointAt(routeBean.highlightedPosition());
            PointWebMercator point = PointWebMercator.ofPointCh(routePoint);

            circle.setLayoutX(mapViewParameters.get().viewX(point));
            circle.setLayoutY(-mapViewParameters.get().viewY(point));
        }
    }

    /**
     * Méthode outil qui permet de repositionner le cercle et la polyligne
     * @param oldMVP les anciens paramètres de fond de carte
     * @param newMVP les nouveaux paramètres de fond de carte
     */
    private void reposition(MapViewParameters oldMVP, MapViewParameters newMVP){

        double deltaX = oldMVP.xCoordinate() - newMVP.xCoordinate();
        double deltaY = oldMVP.yCoordinate() - newMVP.yCoordinate();

        circle.setLayoutX(circle.getLayoutX() + deltaX);
        circle.setLayoutY(circle.getLayoutY() + deltaY);

        polyline.setLayoutX(polyline.getLayoutX() + deltaX);
        polyline.setLayoutY(polyline.getLayoutY() + deltaY);
    }

    /**
     * Gestionnaire d'évènements du gestionnaire d'affichage de l'itinéraire
     */
    private class RouteManagerHandler{

        /**
         * Méthode qui gère la situation ou l'utilisateur clique sur le panneau
         */
        private void setOnClick(){

            circle.setOnMouseClicked(e->{
                Route route = routeBean.routeProperty().get();

                PointCh pointCh = route.pointAt(routeBean.highlightedPosition());
                Waypoint waypoint = new Waypoint(pointCh,route.nodeClosestTo(routeBean.highlightedPosition()));

                int index = routeBean.indexOfNonEmptySegmentAt(routeBean.highlightedPosition())+1;
                routeBean.waypointsProperty().add(index, waypoint);
            });
        }

    }
}
