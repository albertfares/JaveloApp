package ch.epfl.javelo.gui;

import ch.epfl.javelo.routing.*;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.util.Pair;
import java.util.*;

/**
 * Bean JavaFx de l'itinéraire JaVelo
 *
 * @author Albert Fares (341018)
 * @author Etienne Asselin (340201)
 */
public final class RouteBean{

    /**
     * Espacement par défaut des échantillons d'altitude du profil
     */
    public static final double DEFAULT_MAX_STEP_LENGTH = 5.0;

    /**
     * Capacité par défaut du cache mémoire
     */
    private static final int CACHE_CAPACITY = 30;

    /**
     * Load Factor par défaut du cache mémoire
     */
    private static final float CACHE_LOAD_FACTOR = 0.75f;

    /**
     * Liste observable JavaFx des points de passage de l'itinéraire courant
     */
    private final ObservableList<Waypoint> waypoints;

    /**
     * Propriété JavaFx contenant l'itinéraire courant
     */
    private final ReadOnlyObjectWrapper<Route> route;

    /**
     * Propriété JavaFx contenant la position à mettre en évidence sur l'itinéraire courant
     */
    private final DoubleProperty highlightedPosition;

    /**
     * Propriété JavaFx contenant le profil de l'itinéraire courant
     */
    private final ReadOnlyObjectWrapper<ElevationProfile> elevationProfile;

    /**
     * Caclulateur d'itinéraires
     */
    private final RouteComputer computer;

    /**
     * Cache mémoire pour un accès rapide aux itinéraires
     */
    private final Map<Pair<Waypoint,Waypoint>,Route> cache;


    /**
     * Constructeur du bean de l'itinéraire
     * @param computer Caclulateur d'itinéraires
     */
    public RouteBean(RouteComputer computer) {
        this.computer=computer;
        this.cache = new LinkedHashMap<>(CACHE_CAPACITY, CACHE_LOAD_FACTOR, true);
        this.route = new ReadOnlyObjectWrapper<>();
        this.elevationProfile = new ReadOnlyObjectWrapper<>();
        this.waypoints = FXCollections.observableArrayList();
        this.waypoints.addListener((ListChangeListener<Waypoint>) c-> determineRoute());
        this.highlightedPosition=new SimpleDoubleProperty(Double.NaN);
    }

    /**
     * Getter de la propriété en lecture seule de l'itinéraire courant
     * @return la propriété
     */
    public ReadOnlyObjectProperty<Route> routeProperty() {
        return route.getReadOnlyProperty();
    }

    /**
     * Getter de la liste observable des points de passage
     * @return la liste
     */
    public ObservableList<Waypoint> waypointsProperty() { return waypoints;}

    /**
     * Getter de la propriété en lecture seule du profil de l'itinéraire
     * @return la prpriété du profil
     */
    public ReadOnlyObjectProperty<ElevationProfile> profileProperty(){
        return elevationProfile.getReadOnlyProperty();
    }

    /**
     * Méthode qui permet de déterminer l'index dans la liste des points de passage
     * correspondant à la position donné sur l'itinéraire
     * @param position position le long de l'itinéraire
     * @return l'index
     */
    public int indexOfNonEmptySegmentAt(double position) {
        int index = route.get().indexOfSegmentAt(position);
        for (int i = 0; i <= index; i += 1) {
            int n1 = waypoints.get(i).closestNodeId();
            int n2 = waypoints.get(i + 1).closestNodeId();
            if (n1 == n2) index += 1;
        }
        return index;
    }

    /**
     * Getter de la propriété de la position à mettre en évidence
     * @return la propriété
     */
    public DoubleProperty highlightedPositionProperty(){
        return highlightedPosition;
    }

    /**
     * Getter de la position à mettre en évidence
     * @return la position
     */
    public double highlightedPosition(){
        return highlightedPosition.doubleValue();
    }

    /**
     * Méthode outil qui permet de modifier l'itinéraire et le profil courants
     * en fonction de la liste des points de passage
     */
    private void determineRoute(){

        switch (waypoints.size()) {

            case 0, 1 ->  noRoute();

            case 2 -> {
                Pair<Waypoint,Waypoint> pair=extractPair(0);
                if (pair.getKey().closestNodeId()!=pair.getValue().closestNodeId()){

                    Route bestRoute = bestRouteBetweenPair(pair);
                    addToCache(pair,bestRoute);
                    route.set(bestRoute);

                    if (bestRoute != null) {
                        elevationProfile.set(ElevationProfileComputer
                                .elevationProfile(bestRoute, DEFAULT_MAX_STEP_LENGTH));
                    } else {
                        noRoute();}
                }
            }

            default -> {
                List<Route> routes = new ArrayList<>();
                boolean failed = false;

                for (int i=0; i< waypoints.size()-1;i++){
                    Pair<Waypoint,Waypoint> pair = extractPair(i);

                    if (pair.getKey().closestNodeId() != pair.getValue().closestNodeId()){
                        Route bestRoute = bestRouteBetweenPair(pair);
                        addToCache(pair,bestRoute);

                        if (bestRoute == null){
                            noRoute();
                            failed = true;
                            break;
                        }
                        routes.add(bestRoute);
                    }
                }
                if (!failed){
                    Route bestRoute = new MultiRoute(routes);
                    route.set(bestRoute);
                    elevationProfile.set(ElevationProfileComputer
                            .elevationProfile(bestRoute, DEFAULT_MAX_STEP_LENGTH));
                }
            }
        }
    }

    /**
     * Méthode outil qui gère le cas où il n'y a aucun itinéraire courant
     */
    private void noRoute() {
        route.set(null);
        elevationProfile.set(null);
    }

    /**
     * Méthode outil qui permet d'extraire de la liste des points de passage une
     * paire de points de passage à l'index donné
     * @param index L'index de la paire dans la liste
     * @return la paire des points de passage
     */
    private Pair<Waypoint,Waypoint> extractPair(int index){
        return new Pair<>(waypoints.get(index),waypoints.get(index + 1));
    }

    /**
     * Méthode outil qui renvoie le meilleur itinéraire entre la paire de points de passage donnée,
     * soit en l'extrayant du cache ou en le calculant.
     * @param pair la paire des points de passage
     * @return l'itinéraire optimal
     */
    private Route bestRouteBetweenPair(Pair<Waypoint,Waypoint> pair){
        return cache.getOrDefault(pair,
                computer.bestRouteBetween(pair.getKey().closestNodeId(),
                        pair.getValue().closestNodeId()));
    }

    /**
     * Méthode outil qui sert à rajouter la route calculée au cache mémoire, et supprime
     * la route accédée il y a le plus longtemps si il n'y a pas la place
     * @param pair pair de points de passages
     * @param route meilleure route calculee entre la pair des points de passages
     */
    private void addToCache(Pair<Waypoint,Waypoint> pair, Route route){
        Iterator<Map.Entry<Pair<Waypoint,Waypoint>,Route>> iterator = cache.entrySet().iterator();

        if (cache.size() >= CACHE_CAPACITY) {
            cache.remove(iterator.next().getKey());

        }
        cache.put(pair, route);
    }
}