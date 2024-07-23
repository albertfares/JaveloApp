package ch.epfl.javelo.gui;

import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.routing.CityBikeCF;
import ch.epfl.javelo.routing.CostFunction;
import ch.epfl.javelo.routing.GpxGenerator;
import ch.epfl.javelo.routing.RouteComputer;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.function.Consumer;

/**
 * Classe principale qui représente l'application Javelo
 *
 * @author Albert Fares (341018)
 * @author Etienne Asselin (340201)
 */
public final class JaVelo extends Application {

    public static final Background LIGHT_MODE = new Background(new BackgroundFill(Paint.valueOf("ghostwhite"),null,null));
    public static final Background DARK_MODE = new Background(new BackgroundFill(Paint.valueOf("black"),null,null));

    /**
     * Méthode main de l'application
     * @param args Arguments de la méthode
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Méthode qui représente le point d'entrée de l'application et initialise tout les objets
     * @param primaryStage scene principale de l'application
     * @throws Exception en cas d'éventuels erreurs
     */
    @Override
    public void start(Stage primaryStage) throws Exception {

        Path cacheBasePath = Path.of("Javelo/osm-cache");
        try {
            deleteDirectoryStream(cacheBasePath);
        } catch (IOException e) {
            e.printStackTrace();
        }


        String defaultTileServerHost = "tile.openstreetmap.org";
        String cyclismTileServerHost = "a.tile-cyclosm.openstreetmap.fr/cyclosm";
        String BNWTileServerHost = "stamen-tiles.a.ssl.fastly.net/toner";
        String pastelTileServerHost = "a.tile.openstreetmap.fr/hot";

        Graph graph = Graph.loadFrom(Path.of("Javelo/javelo-data"));
        String tileServerHost = "tile.openstreetmap.org";

        ObjectProperty<String> server = new SimpleObjectProperty<>();
        server.set(tileServerHost);
        ObjectProperty<Background> mode = new SimpleObjectProperty<>();
        mode.set(LIGHT_MODE);




        CostFunction costFunction=new CityBikeCF(graph);


        TileManager tileManager=new TileManager(cacheBasePath,server);
        ErrorManager errorManager=new ErrorManager();

        Consumer<String> errorConsumer = errorManager::displayError;

        RouteComputer routeComputer = new RouteComputer(graph,costFunction);
        RouteBean routeBean = new RouteBean(routeComputer);
        AnnotatedMapManager annotatedMapManager=new AnnotatedMapManager(graph,tileManager,routeBean,errorConsumer);

        ElevationProfileManager elevationProfileManager=new ElevationProfileManager(routeBean.profileProperty(),routeBean.highlightedPositionProperty(),mode,routeBean);


        SplitPane splitPane = new SplitPane(annotatedMapManager.pane());
        StackPane stackPane=new StackPane(splitPane, errorManager.pane());
        splitPane.orientationProperty().set(Orientation.VERTICAL);

        Menu menu = new Menu("Fichier");
        Menu menu1 = new Menu("Outils");
        Menu menu2 = new Menu("Carte");
        Menu menu3 = new Menu("Affichage");


        MenuItem menuItem = new MenuItem("Exporter GPX");
        MenuItem menuItem1 = new MenuItem("Effacer les waypoints");
        MenuItem menuItem2 = new MenuItem("Inverser l'itineraire");
        MenuItem defaut = new MenuItem("Défaut");
        MenuItem cyclisme = new MenuItem("Cyclisme");
        MenuItem noirEtBlanc = new MenuItem("Noir et blanc");
        MenuItem pastel = new MenuItem("Pastel");
        MenuItem darkMode = new MenuItem("Dark mode");
        MenuItem lightMode = new MenuItem("Light mode");

        menu.getItems().add(menuItem);
        menu1.getItems().addAll(menuItem1,menuItem2);
        menu2.getItems().addAll(defaut,cyclisme,noirEtBlanc,pastel);
        menu3.getItems().addAll(darkMode,lightMode);


        MenuBar menuBar = new MenuBar(menu, menu1, menu2, menu3);

        menuBar.getStylesheets().add("bonus.css");


        menuBar.backgroundProperty().bind(Bindings.createObjectBinding(()->{
            if (mode.get().equals(DARK_MODE)) {
                return new Background(new BackgroundFill(Paint.valueOf("black"),null,null));

            }
            else return new Background(new BackgroundFill(Paint.valueOf("ghostwhite"),null,null));
        },mode));



        routeBean.highlightedPositionProperty().bind(Bindings.createDoubleBinding(() -> {

            if (annotatedMapManager.mousePositionOnRouteProperty().get() >= 0) {
                return annotatedMapManager.mousePositionOnRouteProperty().get();

            }
            else return elevationProfileManager.mousePositionOnProfileProperty().get();
        },elevationProfileManager.mousePositionOnProfileProperty(),annotatedMapManager.mousePositionOnRouteProperty()));


        routeBean.profileProperty().addListener((e,oldS,newS)-> {
            if (newS == null) {
                splitPane.getItems().remove(elevationProfileManager.pane());
            }
            else if(oldS == null){
                splitPane.getItems().add(elevationProfileManager.pane());
                splitPane.getStylesheets().add("bonus.css");
                SplitPane.setResizableWithParent(splitPane.getItems().get(splitPane.getItems().size()-1),false);
            }
        });




        BorderPane pane = new BorderPane();

        pane.centerProperty().set(stackPane);
        pane.topProperty().set(menuBar);



        menuItem.setOnAction(event-> {
            try {
                GpxGenerator.writeGpx("javelo.gpx",routeBean.routeProperty().get(),routeBean.profileProperty().get());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });

        menuItem.disableProperty().bind(Bindings.createBooleanBinding(()->routeBean.routeProperty().get() == null,routeBean.routeProperty()));

        menuItem1.setOnAction(event -> routeBean.waypointsProperty().clear());

        menuItem1.disableProperty().bind(Bindings.createBooleanBinding(()->routeBean.routeProperty().get() == null,routeBean.routeProperty()));


        menuItem2.setOnAction(event -> Collections.reverse(routeBean.waypointsProperty()));

        menuItem2.disableProperty().bind(Bindings.createBooleanBinding(()->routeBean.routeProperty().get() == null,routeBean.routeProperty()));



        pastel.setOnAction(event ->{
            try {
                deleteDirectoryStream(cacheBasePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            tileManager.clearMemoryCache();
            server.set(pastelTileServerHost);
            annotatedMapManager.forceDrawMap();
        } );

        noirEtBlanc.setOnAction(event ->{
            try {
                deleteDirectoryStream(cacheBasePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            tileManager.clearMemoryCache();
            server.set(BNWTileServerHost);
            annotatedMapManager.forceDrawMap();
        } );

        cyclisme.setOnAction(event ->{
            try {
                deleteDirectoryStream(cacheBasePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            tileManager.clearMemoryCache();
            server.set(cyclismTileServerHost);
            annotatedMapManager.forceDrawMap();
        } );

        defaut.setOnAction(event ->{
            try {
                deleteDirectoryStream(cacheBasePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            tileManager.clearMemoryCache();
            server.set(defaultTileServerHost);
            annotatedMapManager.forceDrawMap();
        } );

        lightMode.setOnAction(event -> mode.set(LIGHT_MODE));

        darkMode.setOnAction(event -> mode.set(DARK_MODE));


        Scene scene = new Scene(pane);

        splitPane.backgroundProperty().bind(Bindings.createObjectBinding(()->{
            if (mode.get().equals(DARK_MODE)) {
                return new Background(new BackgroundFill(Paint.valueOf("black"),null,null));

            }
            else return new Background(new BackgroundFill(Paint.valueOf("ghostwhite"),null,null));
        },mode));


        primaryStage.setScene(scene);
        primaryStage.setTitle("JaVelo");
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);



        primaryStage.show();
    }


    private void deleteDirectoryStream(Path path) throws IOException {
        Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }
}
