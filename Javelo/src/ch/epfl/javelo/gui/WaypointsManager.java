package ch.epfl.javelo.gui;

import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.PointWebMercator;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Gestionnaire de l'affichage et de l'interaction avec les points de passage
 *
 * @author Albert Fares (341018)
 * @author Etienne Asselin (340201)
 */
public final class WaypointsManager {

    /**
     * Constante qui represente la distance de recherche
     */
    public static final int SEARCH_DISTANCE = 500;

    /**
     * Path SVG qui decrit le dehors du pin
     */
    public static final String PIN_OUTSIDE = "M-8-20C-5-14-2-7 0 0 2-7 5-14 8-20 20-40-20-40-8-20";

    /**
     * Path SVG qui decrit l'interieur du pin
     */
    public static final String PIN_INSIDE = "M0-23A1 1 0 000-29 1 1 0 000-23";

    /**
     * Graphe du réseau routier
     */
    private final Graph graph;

    /**
     * Propriété stockant les paramètres du fond de carte
     */
    private final ObjectProperty<MapViewParameters> mapViewParameters;

    /**
     * Liste observable des points de passages
     */
    private final ObservableList<Waypoint> wayPoints;

    /**
     * Panneau javaFX
     */
    private final Pane pane;

    /**
     * booleen qui indique si les pins doivent etre effacés de la carte
     */
    private boolean deletePins;

    /**
     * Liste de groupes JavaFX contenants les pins propres a un itineraire
     */
    private final List<Group> groups;

    /**
     * Booleen qui indique si la carte est en mouvement
     */
    private boolean isMoving;

    /**
     * Consomateur d'erreurs
     */
    private final Consumer<String> error;

    /**
     * Gestionnaire d'evenements des points de passages
     */
    private final WaypointsEventsHandler waypointsEventsHandler;

    /**
     * Constructeur du gestionnaire de l'affichage et de l'interaction avec les points de passage
     * @param graph Graphe du réseau routier
     * @param mapViewParameters Paramètres du fond de carte
     * @param wayPoints Liste observable des points de passages
     * @param error Consomateur d'erreurs
     */
    public WaypointsManager(Graph graph, ObjectProperty<MapViewParameters> mapViewParameters, ObservableList<Waypoint> wayPoints, Consumer<String> error) {
        this.graph = graph;
        this.error = error;
        this.mapViewParameters = mapViewParameters;
        this.wayPoints = wayPoints;
        this.groups = new ArrayList<>();
        this.pane = new Pane();
        this.waypointsEventsHandler = new WaypointsEventsHandler();

        startManager();
    }

    /**
     * Accesseur du panneau javaFX principal
     * @return le panneau javaFX
     */
    public Pane pane() {
        return pane;
    }

    /**
     * Methode outil qui dessiner les pins
     */
    private void drawPins(){

        if (deletePins){
            pane.getChildren().clear();
            groups.clear();
        }

        for (int i = 0 ; i < wayPoints.size(); i++) {

            Group group = drawPin(i);

            pane.getChildren().add(group);
            groups.add(group);


            group.setOnMouseClicked(event -> {
                if ( !isMoving){
                    wayPoints.remove(groups.indexOf(group));
                    groups.remove(group);
                    drawAndReposition();}
            });
        }
        deletePins = false;

    }

    /**
     * Methode outil qui permet de positionner les pins sur le carte
     */
    private void positionPins(){

        int waypointIndex = 0;

        for (Group g : groups) {

            PointWebMercator pointWebMercator = PointWebMercator.ofPointCh(wayPoints.get(waypointIndex).point());

            double xPosition = (mapViewParameters.get().viewX(pointWebMercator));
            double yPosition = (mapViewParameters.get().viewY(pointWebMercator));

            g.setLayoutX(xPosition);
            g.setLayoutY(-yPosition);

            waypointsEventsHandler.manageShift(g, xPosition, yPosition);

            waypointIndex++;
        }
    }

    /**
     * Methode qui permet d'ajouter des points de passages a l'itineraire courant
     * @param pointCh le point en coordonnées CH1903+ du point de passage à rajouter
     */
    public void addWaypoint(PointCh pointCh){

        if (pointCh == null) error.accept("Le point placé n'est pas en Suisse !");
        else {

            int nodeId = graph.nodeClosestTo(pointCh, SEARCH_DISTANCE);

            if (nodeId == -1) {
                error.accept("Aucune route à proximité !");
            } else {
                wayPoints.add(new Waypoint(pointCh, nodeId));
            }

            drawAndReposition();
        }

    }


    /**
     * Methode outils qui redessiner et repositionne les pins
     */
    private void drawAndReposition (){
        deletePins = true;
        drawPins();
        positionPins();
        deletePins = false;
    }

    /**
     * Methode outil qui se charge de gerer les modifications en cas de changements dans les proprietés de la carte
     * et de l'itineraire courant
     */
    private void startManager(){

        pane.setPickOnBounds(false);
        drawPins();
        positionPins();

        mapViewParameters.addListener(observable -> {
            deletePins = true;
            positionPins();
        });

        wayPoints.addListener((ListChangeListener<Waypoint>) c -> drawAndReposition());

    }

    /**
     * Methode outil qui permet de créer le groupe javaFX representant un pin
     * @param waypointIndex l'index du point de passage associé au pin
     * @return le gourpe javaFX representant un pin
     */
    private Group drawPin(int waypointIndex){

        SVGPath pinOutside = new SVGPath();
        SVGPath pinInside = new SVGPath();

        pinOutside.getStyleClass().add("pin_outside");
        pinOutside.setContent(PIN_OUTSIDE);

        pinInside.getStyleClass().add("pin_inside");
        pinInside.setContent(PIN_INSIDE);

        Group group = new Group(pinOutside, pinInside);

        group.getStyleClass().add("pin");

        if (waypointIndex == 0) {
            group.getStyleClass().add("first");
        } else if (waypointIndex == wayPoints.size() - 1) {
            group.getStyleClass().add("last");
        } else {
            group.getStyleClass().add("middle");
        }

        return group;
    }

    /**
     * Classe imbriquée qui represent le gestionnaire d'evenements des points de passages
     */
    private class WaypointsEventsHandler {

        /**
         * Methode qui permet de bouger les pins sur la carte
         * @param g groupe javaFX qui represente le pin a bouger
         * @param xPosition position horizontale initiale du point de passage
         * @param yPosition position verticale initiale du point de passage
         */
        private void manageShift(Group g, double xPosition, double yPosition) {

            g.setOnMousePressed(event -> {
                isMoving = false;
                ObjectProperty<Point2D> positionOnClick =
                        new SimpleObjectProperty<>(new Point2D(event.getX(), event.getY()));

                g.setOnMouseDragged(event1 -> {
                    isMoving = true;

                    Point2D currentPosition = new Point2D(event1.getX(), event1.getY());

                    double deltaX = positionOnClick.get().getX() - currentPosition.getX();
                    double deltaY = positionOnClick.get().getY() - currentPosition.getY();

                    g.setTranslateX(-deltaX);
                    g.setTranslateY(-deltaY);

                    positionOnClick.set(new Point2D(event.getX() + deltaX, event.getY() + deltaY));

                    pane.setOnMouseReleased(event2 -> {
                        if (isMoving) {

                            int index = groups.indexOf(g);

                            PointWebMercator newPin = mapViewParameters.get()
                                    .pointAt(xPosition - deltaX, yPosition + deltaY);
                            PointCh pointCh = newPin.toPointCh();

                            if (pointCh != null) {
                                int nodeId = graph.nodeClosestTo(pointCh, SEARCH_DISTANCE);

                                if (nodeId == -1) {
                                    error.accept("Aucune route à proximité !");

                                } else {
                                    wayPoints.remove(index);
                                    groups.remove(g);
                                    wayPoints.add(index, new Waypoint(newPin.toPointCh(), nodeId));
                                }

                            } else {
                                error.accept("Le point n'est pas en Suisse !");
                            }
                            drawAndReposition();
                        }
                    });
                });
            });
        }

    }

}







