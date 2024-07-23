package ch.epfl.javelo.gui;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.projection.PointWebMercator;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe qui gère l'affichage et l'interaction avec le fond de carte
 *
 * @author Albert Fares (341018)
 * @author Etienne Asselin (340201)
 */
public final class BaseMapManager {

    /**
     * Constante representant la longueur en pixel d'une tuile OSM
     */
    public static final int TILE_SIDE = 256;

    /**
     * Gestionnaire de tuiles OSM
     */
    private final TileManager tiles;

    /**
     * Propriété stockant les paramètres du fond de carte
     */
    private final ObjectProperty<MapViewParameters> mapViewParameters;

    /**
     * Panneau javaFX
     */
    private final Pane pane;

    /**
     * Canvas javaFX
     */
    private final Canvas canvas;

    /**
     * Gestionnaire des points de passages
     */
    private final WaypointsManager waypointsManager;

    /**
     * Gestionnaire d'événements
     */
    private final MapEventsHandler eventsHandler;

    /**
     * booléen qui indique si un redessin de la carte est necessaire
     */
    private boolean redrawNeeded;


    /**
     * Constructeur de la carte
     * @param tiles tuiles qui constituent la carte
     * @param waypointsManager gestionnaire des points de passages
     * @param mapViewParameters paramètres du fond de carte
     */
    public BaseMapManager(TileManager tiles, WaypointsManager waypointsManager,
                          ObjectProperty<MapViewParameters> mapViewParameters) {

        this.waypointsManager = waypointsManager;
        this.tiles = tiles;
        this.mapViewParameters = mapViewParameters;
        this.canvas = new Canvas();
        this.pane = new Pane(canvas);
        this.eventsHandler = new MapEventsHandler();

        startManager();
    }

    /**
     * Accesseur du panneau javaFx
     * @return le panneau javaFX contenant la carte
     */
    public Pane pane(){
        return pane;
    }

    /**
     * Methode outil qui permet de lier les differents attributs de classe
     */
    private void startManager(){

        canvas.sceneProperty().addListener((p, oldS, newS) -> {
            assert oldS == null;
            newS.addPreLayoutPulseListener(this::redrawIfNeeded);
        });

        eventsHandler.manageZoom();
        eventsHandler.managePanning();
        eventsHandler.manageWaypoints();


        pane.widthProperty().addListener(e -> redrawOnNextPulse());
        pane.heightProperty().addListener(e -> redrawOnNextPulse());
        mapViewParameters.addListener(e -> redrawOnNextPulse());

        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());

    }

    /**
     * Methode outil qui redessine la carte si ceci est jugé necessaire
     */
    private void redrawIfNeeded() {
        if (!redrawNeeded) return;
        redrawNeeded = false;
        drawMap();
    }

    /**
     * Methode qui demande le redessin de la carte a la prochaine impulsion
     */
    private void redrawOnNextPulse() {
        redrawNeeded = true;
        Platform.requestNextPulse();
    }

    /**
     * Methode outil qui permet de dessiner la carte
     */
    private void drawMap(){

        MapViewParameters view = mapViewParameters.get();

        double topLeftX = view.xCoordinate();
        double topLeftY = view.yCoordinate();

        int zoomLevel = view.zoomLevel();

        int tilesInWidth = Math2.ceilDiv((int) canvas.getWidth(), TILE_SIDE) + 1;
        int tilesInHeight = Math2.ceilDiv((int) canvas.getHeight(), TILE_SIDE) + 1;

        int clampedTilesInWidth = Math2.clamp(0, tilesInWidth, (1 << zoomLevel));
        int clampedTilesInHeight = Math2.clamp(0, tilesInHeight, (1 << zoomLevel));

        List<Image> images = new ArrayList<>();

        int xIndexOfTopLeftTile = (int) (topLeftX/TILE_SIDE);
        int yIndexOfTopLeftTile = (int) (topLeftY/TILE_SIDE);

        for (int y = 0; y < clampedTilesInHeight ; y++) {
            for (int x = 0; x < clampedTilesInWidth; x++) {

                try {
                    images.add(tiles.imageForTileAt(new TileManager.TileId(zoomLevel,
                            Math2.clamp(0,xIndexOfTopLeftTile+ x, ((1 << zoomLevel) - 1)),
                            Math2.clamp(0, yIndexOfTopLeftTile + y, ((1 << zoomLevel) - 1)))));

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        int imageIndex = 0;

        double xDifference = topLeftX % TILE_SIDE;
        double yDifference = topLeftY % TILE_SIDE;

        for (int y = 0; y < clampedTilesInHeight ; y++) {
            for (int x = 0; x < clampedTilesInWidth; x++) {

                Image imageToDraw = images.get(imageIndex);

                canvas.getGraphicsContext2D().drawImage(imageToDraw,
                        (x * TILE_SIDE) - xDifference,
                        (y * TILE_SIDE) - yDifference);

                imageIndex++;
            }
        }
    }


    /**
     * Classe impriquee qui permet de gerer les evenements lies au zoom et au deplacement de la carte
     */
    private final class MapEventsHandler {

        /**
         * booleen qui indique si la carte est en cour de deplacement
         */
        private boolean isMoving;

        /**
         * Methode outil qui permet l'ajout de points de passages sur la carte
         */
        private void manageWaypoints(){

            pane.setOnMouseClicked(event ->{

                if (!isMoving){
                    PointWebMercator mouse = mapViewParameters.get().pointAt(event.getX(), -event.getY());

                    waypointsManager.addWaypoint(mouse.toPointCh());
                }
            });
        }

        /**
         * Methode qui permet de gérer le zoom sur la carte
         */
        private void manageZoom(){

            SimpleLongProperty minScrollTime = new SimpleLongProperty();
            pane.setOnScroll(e -> {
                if (e.getDeltaY() == 0d) return;
                long currentTime = System.currentTimeMillis();
                if (currentTime < minScrollTime.get()) return;
                minScrollTime.set(currentTime + 200);
                int zoomDelta = (int) Math.signum(e.getDeltaY());

                int oldZoom = mapViewParameters.get().zoomLevel();

                double mouseX = e.getX();
                double mouseY = e.getY();

                int newZoom = Math2.clamp(MapViewParameters.MIN_ZOOM,
                        (zoomDelta + oldZoom),
                        MapViewParameters.MAX_ZOOM);

                PointWebMercator mousePoint = mapViewParameters.get().pointAt(mouseX, -mouseY);

                double newX = mousePoint.xAtZoomLevel(newZoom) - mouseX;
                double newY = mousePoint.yAtZoomLevel(newZoom) - mouseY;

                if (newX < 0){
                    newX = 0;
                }
                if (newY < 0){
                    newY = 0;
                }

                mapViewParameters.set(new MapViewParameters(newZoom, newX, newY));

            });

        }

        /**
         * Methode qui permet de gerer le deplacement de la carte
         */
        private void managePanning(){

            pane.setOnMousePressed(event -> {
                isMoving = false;

                ObjectProperty<Point2D> positionOnClick =
                        new SimpleObjectProperty<>(new Point2D(event.getX(), event.getY()));

                pane.setOnMouseDragged(event1 -> {
                    isMoving = true;

                    Point2D currentPosition = new Point2D(event1.getX(), event1.getY());

                    double deltaX = positionOnClick.get().getX() - currentPosition.getX();
                    double deltaY = positionOnClick.get().getY() - currentPosition.getY();

                    double maxTopLeftX = Math.scalb(TILE_SIDE, mapViewParameters.get().zoomLevel()) - pane.getWidth();
                    double maxTopLeftY = Math.scalb(TILE_SIDE, mapViewParameters.get().zoomLevel()) - pane.getHeight();

                    mapViewParameters.set(mapViewParameters.get().withMinXY(
                            Math2.clamp(0, mapViewParameters.get().xCoordinate() + deltaX, maxTopLeftX),
                            Math2.clamp(0, mapViewParameters.get().yCoordinate() + deltaY, maxTopLeftY)));

                    positionOnClick.set(new Point2D(event1.getX(), event1.getY()));

                });
            });
        }
    }

    public void forceDrawMap(){
        drawMap();
    }

}







