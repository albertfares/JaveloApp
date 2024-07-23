package ch.epfl.javelo.gui;

import ch.epfl.javelo.routing.ElevationProfile;
import ch.epfl.javelo.routing.Route;
import ch.epfl.javelo.routing.RoutePoint;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Affine;
import java.util.ArrayList;
import java.util.List;

import static ch.epfl.javelo.gui.JaVelo.DARK_MODE;

/**
 * Gestionaire de l'affichage et l'interaction avec le profil en long d'un itinéraire
 *
 * @author Albert Fares (341018)
 * @author Etienne Asselin (340201)
 */
public final class ElevationProfileManager {

    /**
     * Propriété en lecture seule qui stocke le profil de l'itineraire
     */
    private final ReadOnlyObjectProperty<ElevationProfile> elevationProfile;

    /**
     * Propriété en lecture seule qui stocke la position mise en evidence
     */
    private final ReadOnlyDoubleProperty highlightedProperty;

    /**
     * Propriété qui stocke la position de la souris sur le profil courant
     */
    private final ReadOnlyDoubleWrapper mousePositionOnProfile;

    /**
     * Panneau à frontieres javaFX
     */
    private final BorderPane pane;

    /**
     * Panneau javaFX
     */
    private final Pane centerPane;

    /**
     * Propriété JavaFx du Rectangle2D contenant le dessin du profil
     */
    private final ObjectProperty<Rectangle2D> rectangleProperty;

    /**
     * L'ensemble des décalages par rapport au panneau du rectangle
     */
    private final Insets insets;

    /**
     * Polygone JavaFx qui permet de dessiner le profil
     */
    private final Polygon polygon;

    /**
     * Line JavaFx qui permet d'afficher la position à mettre en évidence sur le profil
     */
    private final Line line;

    /**
     *
     * Path javaFx qui représente la grille
     */
    private final Path path;

    /**
     * Tableau contenant les différentes échelles horizontales possibles
     */
    private final static int[] POS_STEPS = { 1000, 2000, 5000, 10_000, 25_000, 50_000, 100_000 };

    /**
     * Constante qui represente la distance minimale entre les lignes verticales de la grille
     */
    private static final int MIN_HORIZONTAL_SPACING = 50;

    /**
     * Tableau contenant les différentes échelles verticales possibles
     */
    private final static int[] ELE_STEPS = { 5, 10, 20, 25, 50, 100, 200, 250, 500, 1_000 };

    /**
     * Constante qui represente la distance minimale entre les lignes horizontales de la grille
     */
    private static final int MIN_VERTICAL_SPACING = 25;

    /**
     * Première gradation vertical dans la vraie vie, qui a comme valeur la première valeur supérieur à l'altitude minimale
     * et divisible par l'échelle verticale courante
     */
    private int initialVerticalPosition;

    /**
     * Group javaFX qui contient les étiquettes des gradations
     */
    private final Group group;

    /**
     * Propriété qui stocke la fonction affine qui permet de passer de l'écran au monde réel
     */
    private final ObjectProperty<Affine> screenToWorldProperty;

    /**
     * Propriété qui stocke la fonction affine qui permet de passer du monde réel à l'écran
     */
    private final ObjectProperty<Affine> worldToScreenProperty;

    /**
     * Vbox JavaFx contenant les informations sur le profil courant
     */

    private final VBox vBox;
    /**
     * Text javaFX contenant les informations sur le profil courant
     */
    private final Text stats;

    /**
     * Constante qui represente le facteur multiplicatif entre les kilometres et les metres
     */
    private final static int KM_FACTOR=1000;

    private final VBox informationBox;

    private final ObjectProperty<Background> mode;


    private final RouteBean routeBean;

    private final Path waypointsPath;





    /**
     * Constructeur du gestionaire de l'affichage et de l'interaction
     * @param elevationProfile profil en long de l'itineraire
     * @param highlightedProperty position mise en evidence
     */
    public ElevationProfileManager(ReadOnlyObjectProperty<ElevationProfile> elevationProfile,
                                   ReadOnlyDoubleProperty highlightedProperty, ObjectProperty<Background> mode, RouteBean routeBean ) {
        this.elevationProfile = elevationProfile;
        this.highlightedProperty = highlightedProperty;
        this.vBox = new VBox();
        this.group = new Group();
        this.stats = new Text();
        this.mode = mode;
        this.routeBean=routeBean;

        stats.fillProperty().bind(Bindings.createObjectBinding(()->{
            if (mode.get().equals(DARK_MODE)) {
                return Paint.valueOf("white");
            }
            else return Paint.valueOf("black");
        },mode));


        informationBox = new VBox();


        informationBox.backgroundProperty().bind(Bindings.createObjectBinding(()-> {
            if (mode.get().equals(DARK_MODE)) {
                return new Background(new BackgroundFill(Color.rgb(41,41,41,1),null,null));

            }
            else return new Background(new BackgroundFill(Color.rgb(241, 241, 241,1),null,null));
        },mode));






        line = new Line(0,0,0,0);

        line.strokeProperty().bind(Bindings.createObjectBinding(()-> {
            if (mode.get().equals(DARK_MODE)) {
                return Paint.valueOf("white");
            }
            else return Paint.valueOf("black");

        },mode));



        path = new Path();
        polygon = new Polygon();
        waypointsPath = new Path();



        centerPane = new Pane(group, path,polygon, waypointsPath,line, informationBox);
        pane = new BorderPane();
        pane.backgroundProperty().bind(mode);
        insets = new Insets(10,10,20,40);

        screenToWorldProperty = new SimpleObjectProperty<>();
        worldToScreenProperty = new SimpleObjectProperty<>();

        rectangleProperty = new SimpleObjectProperty<>();
        mousePositionOnProfile = new ReadOnlyDoubleWrapper();

        mousePositionOnProfile.set(Double.NaN);

        styleNodes();

        startManager();


    }

    /**
     * Accesseur du panneau javaFX principal
     * @return le panneau
     */
    public Pane pane(){
        return pane;
    }

    /**
     * Methode outil qui permet de styliser les differents noeuds javaFX
     */
    private void styleNodes(){
        vBox.setId("profile_data");
        vBox.getStyleClass().add("profile_data");

        polygon.setId("profile");
        polygon.getStyleClass().add("profile");

        path.setId("grid");
        path.getStyleClass().add("grid");

        pane.centerProperty().set(centerPane);
        pane.bottomProperty().set(vBox);
        pane.getStylesheets().add("bonus.css");

        waypointsPath.setId("waypoints_path");
        waypointsPath.getStyleClass().add("waypoints_path");
    }

    /**
     * Methode outil qui gere les liaisons entre les propriétés de classe
     */
    private void startManager(){

        elevationProfile.addListener( (p,oldS,newS)->{if (newS != null){
            draw();
            stats();
            disposeBothGrids();
        }});

        rectangleProperty.bind(Bindings.createObjectBinding(()-> {

            double rectangleWidth = centerPane.widthProperty().get() - (insets.getLeft() + insets.getRight());
            double rectangleHeight = centerPane.heightProperty().get() - (insets.getTop() + insets.getBottom());

            if (rectangleWidth < 0) rectangleWidth = 0;
            if (rectangleHeight < 0) rectangleHeight = 0;

            return new Rectangle2D(insets.getLeft(), insets.getTop(), rectangleWidth, rectangleHeight);

        },centerPane.widthProperty(),centerPane.heightProperty()));



        screenToWorldProperty.bind(Bindings.createObjectBinding(()->{
            Rectangle2D rectangle = rectangleProperty.get();
            ElevationProfile profile = elevationProfile.get();

//            System.out.println("vnjnzv");

            if (profile == null){
                return null;
            }

            Affine affine = new Affine();

            double firstXTranslation = -insets.getLeft();
            double firstYTranslation = -(rectangle.getHeight() + insets.getTop());

            affine.prependTranslation(firstXTranslation, firstYTranslation);

            double XScale = profile.length() / rectangle.getWidth();
            double YScale = -(profile.maxElevation() - profile.minElevation()) / rectangle.getHeight();

            affine.prependScale(XScale,YScale);

            double secondXTranslation = 0;
            double secondYTranslation = profile.minElevation();

            affine.prependTranslation(secondXTranslation,secondYTranslation);

            return affine;

        },rectangleProperty, elevationProfile));

        worldToScreenProperty.bind(Bindings.createObjectBinding(()-> {
            Affine affine = screenToWorldProperty.get();



            if (affine == null){
                return null;
            }

//            System.out.println("Inverse:"+screenToWorldProperty.get().createInverse());
            return screenToWorldProperty.get().createInverse();

        },screenToWorldProperty));

        line.layoutXProperty().bind(Bindings.createDoubleBinding(()-> {
                    if (worldToScreenProperty.get() == null) return Double.NaN;

                    else {double cursor=highlightedProperty.get();
                        return worldToScreenProperty.get().transform(cursor,0).getX();}
                }
                ,highlightedProperty,worldToScreenProperty));

        line.startYProperty().bind(Bindings.select(rectangleProperty,"minY"));
        line.endYProperty().bind(Bindings.select(rectangleProperty,"maxY"));
        line.visibleProperty().bind(highlightedProperty.greaterThanOrEqualTo(0));

        informationBox.layoutXProperty().bind(Bindings.createDoubleBinding(()-> {
            if (line.layoutXProperty().get() < (rectangleProperty.get().getMaxX()-informationBox.getWidth())){
                return line.layoutXProperty().get();
            }
            else
                return line.getLayoutX() - informationBox.getWidth();
        },line.layoutXProperty()));


        informationBox.visibleProperty().bind(mousePositionOnProfile.greaterThan(0));
        informationBox.layoutYProperty().bind(line.layoutYProperty());


        Text statsAtPosition=new Text();
        statsAtPosition.fillProperty().bind(Bindings.createObjectBinding(()->{
            if (mode.get().equals(DARK_MODE)) {
                return Paint.valueOf("white");
            }
            else return Paint.valueOf("black");
        },mode));
        statsAtPosition.textProperty().bind(Bindings.createStringBinding(()->{
            double position=highlightedProperty.get();
            if (elevationProfile.get()!=null && !Double.isNaN(position)){
                return String.format("""
        Pente: %.1f %%\s
        Distance: %.0f m\s
        Altitude: %.0f m""",elevationProfile.get().gradientAt(position),position,elevationProfile.get().elevationAt(position));
            }
            return "";
        },elevationProfile,highlightedProperty));
        informationBox.getChildren().add(statsAtPosition);

        rectangleProperty.addListener(observable ->{draw();disposeBothGrids();});




        worldToScreenProperty.addListener((p,oldS,newS)->{
            waypointsPath.getElements().clear();
            Route route=routeBean.routeProperty().get();
            if (route!=null){
                for (int i=1;i<routeBean.waypointsProperty().size()-1;i++){
                    Waypoint waypoint=routeBean.waypointsProperty().get(i);
                    RoutePoint routePoint=route.pointClosestTo(waypoint.point());
                    double x=worldToScreenProperty.get().transform(routePoint.position(),0).getX();
                    PathElement start = new MoveTo(x, rectangleProperty.get().getMinY());
                    PathElement end = new LineTo(x, rectangleProperty.get().getMaxY());
                    waypointsPath.getElements().add(start);
                    waypointsPath.getElements().add(end);
                }

            }
        });

        eventsHandler();
    }

    /**
     * Methode outil qui gere les evenements
     */
    private void eventsHandler(){

        centerPane.setOnMouseMoved(event -> {

            if (screenToWorldProperty.get() != null) {

                Point2D mouse = new Point2D(event.getX(), event.getY());

                Point2D point2D = screenToWorldProperty.get().transform(mouse);

                int mousePosition = (int) point2D.getX();

                if (rectangleProperty.get().contains(mouse)) {
                    mousePositionOnProfile.set(mousePosition);
                } else {
                    mousePositionOnProfile.set(Double.NaN);
                }

            }
        });
        centerPane.setOnMouseExited(event -> mousePositionOnProfile.set(Double.NaN));
    }

    /**
     * Méthode outil qui permet de disposer les gradations verticales et horizontales de la grille
     */
    private void disposeBothGrids(){
        path.getElements().clear();
        group.getChildren().clear();

        if (worldToScreenProperty.get()!=null){
            disposeGrid(POS_STEPS, MIN_HORIZONTAL_SPACING);
            disposeGrid(ELE_STEPS,MIN_VERTICAL_SPACING);
        }

    }

    /**
     * Méthode outil qui permet de disposer la grille sur le panneau central
     * @param steps tableau contenant les différentes échelles possibles
     * @param minSpacing distance minimale entre les lignes a respecter
     */
    private void disposeGrid(int[] steps, int minSpacing){

        if (worldToScreenProperty.get()!= null) {

            boolean isVertical=(steps==POS_STEPS);
            boolean haveToUseMaxSpacing=true;

            double smallestValidGapOnScreen = Double.MAX_VALUE;

            int worldDistanceBetweenLines = 0;
            int maxWorldDistance = 0;
            double maxScreenDistance = 0;
            int verticalspacing = 0;

            double initialScreenVertical = 0;

            for (int i : steps) {
                double screenDistance;
                screenDistance=(isVertical)?worldToScreenProperty.get().deltaTransform(i,0).getX():
                        worldToScreenProperty.get().deltaTransform(0,-i).getY();
                if (minSpacing <= screenDistance && screenDistance <= smallestValidGapOnScreen) {
                    smallestValidGapOnScreen = screenDistance;
                    worldDistanceBetweenLines = i;
                    if (!isVertical) verticalspacing = i;
                    haveToUseMaxSpacing=false;
                }
                if (i > maxWorldDistance) {
                    maxWorldDistance = i;
                    maxScreenDistance = screenDistance;
                }
            }
            if (haveToUseMaxSpacing) {
                smallestValidGapOnScreen = maxScreenDistance;
                worldDistanceBetweenLines = maxWorldDistance;
                if (!isVertical) verticalspacing = maxWorldDistance;
            }

            int numberOfLines;
            double rectangleDim=(isVertical)?rectangleProperty.get().getWidth():
                    rectangleProperty.get().getHeight();

            numberOfLines = (int) (rectangleDim / smallestValidGapOnScreen) + 1;
            if (!isVertical) {
                initialVerticalPosition=findInitialVerticalLinePosition(verticalspacing);
                initialScreenVertical = worldToScreenProperty.get().transform(0, initialVerticalPosition).getY();
            }

            for (int i = 0; i < numberOfLines; i++) {
                PathElement start;
                PathElement end;

                boolean tooHigh = false;
                if (isVertical) {
                    double x = rectangleProperty.get().getMinX() + (i * smallestValidGapOnScreen);

                    start = new MoveTo(x, rectangleProperty.get().getMinY());
                    end = new LineTo(x, rectangleProperty.get().getMaxY());

                    labels(x, rectangleProperty.get().getMaxY(), i, worldDistanceBetweenLines, true);
                } else {
                    double y = initialScreenVertical - (i * smallestValidGapOnScreen);

                    start = new MoveTo(rectangleProperty.get().getMinX(), y);
                    end = new LineTo(rectangleProperty.get().getMaxX(), y);

                    labels(rectangleProperty.get().getMinX(), y, i, worldDistanceBetweenLines, false);

                    if (y<rectangleProperty.get().getMinY()){
                        tooHigh=true;
                    }
                }

                if (!tooHigh){
                    path.getElements().add(start);
                    path.getElements().add(end);
                }
            }
        }
    }

    /**
     * Méthode outil qui permet de déterminer la première ligne verticale sur le profil
     * @param verticalWorldSpacing échelle verticale dans le
     */
    private int findInitialVerticalLinePosition(int verticalWorldSpacing){
        int verticalPosition=0;

        while (verticalPosition<elevationProfile.get().minElevation()){
            verticalPosition+=verticalWorldSpacing;
        }

        return verticalPosition;
    }




    /**
     * Methode outil qui dessine le profil en long de l'itineraire
     */
    private void draw(){

        if (screenToWorldProperty.get() != null) {
            List<Double> list = new ArrayList<>();

            list.add(rectangleProperty.get().getMinX());
            list.add(rectangleProperty.get().getHeight()+insets.getTop());

            for (double i = rectangleProperty.get().getMinX(); i <= rectangleProperty.get().getMaxX(); i++) {

                Point2D position = screenToWorldProperty.get().transform(i, 0);
                double elevationAt = elevationProfile.get().elevationAt(position.getX());
                Point2D elevation = worldToScreenProperty.get().transform(0, elevationAt);

                list.add(i);
                list.add(elevation.getY());

            }
            list.add(rectangleProperty.get().getMaxX());
            list.add(rectangleProperty.get().getHeight() + insets.getTop());

            polygon.getPoints().setAll(list);
        }

    }

    /**
     * Accesseur en lecture seule de la propriété de la position de la souris sur le profil
     * @return la propriété
     */
    public ReadOnlyDoubleProperty mousePositionOnProfileProperty(){
        return mousePositionOnProfile.getReadOnlyProperty();
    }

    /**
     * Methode outil qui gere les informations du profil a afficher
     */
    private void stats(){

        double longueur = elevationProfile.get().length()/KM_FACTOR;
        double montee = elevationProfile.get().totalAscent();
        double descente = elevationProfile.get().totalDescent();
        double altitudeMin = elevationProfile.get().minElevation();
        double altitudeMax = elevationProfile.get().maxElevation();

        stats.setText(String.format("Longueur : %.1f km" +
                "     Montée : %.0f m" +
                "     Descente : %.0f m" +
                "     Altitude : de %.0f m à %.0f m", longueur ,montee ,descente ,altitudeMin ,altitudeMax));

        vBox.getChildren().clear();
        vBox.getChildren().add(stats);
    }

    /**
     * Methode outil qui permet de creer les etiquettes du profil et de les placer convenablement
     * @param x position horizontale de l'etiquette
     * @param y position verticale de l'etiquette
     * @param i numero de la ligne pour laquelle l'etiquette est associée
     * @param worldDistance distance correspondante a l'etiquette dans le monde reel
     * @param isVertical booleen qui indique si l'etiquette est verticale ou pas
     */
    private void labels(double x, double y, int i, int worldDistance, boolean isVertical){

        Text text = new Text();
        String style = !isVertical? "vertical" : "horizontal";

        text.getStyleClass().add("grid_label");
        text.getStyleClass().add(style);
        text.setFont(Font.font("Avenir", 10));

        VPos pos;
        pos = isVertical? VPos.TOP : VPos.CENTER;
        text.setTextOrigin(pos);

        double factor;
        factor = (isVertical)? KM_FACTOR : 1;

        int position = (isVertical)? i * worldDistance : initialVerticalPosition + i * worldDistance;

        if (position / factor <= elevationProfile.get().maxElevation() || isVertical)
            text.setText(String.valueOf((int)(position/factor)));

        double Xshift = isVertical? text.prefWidth(0) / 2 : text.getLayoutBounds().getWidth() + 2 ;
        double shiftedX = x - Xshift;

        text.setLayoutX(shiftedX);
        text.setLayoutY(y);

        text.fillProperty().bind(Bindings.createObjectBinding(()->{
            if (mode.get().equals(DARK_MODE)) {
                return Paint.valueOf("white");
            }
            else return Paint.valueOf("black");
        },mode));

        group.getChildren().add(text);
    }

}
