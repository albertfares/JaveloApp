package ch.epfl.javelo.routing;

import ch.epfl.javelo.Functions;
import ch.epfl.javelo.gui.RouteBean;

import java.util.Arrays;
import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.function.DoubleUnaryOperator;


/**
 * Classe qui représente le profil en long d'un itinéraire simple ou multiple.
 *
 * @author Albert Fares (341018)
 * @author Etienne Asselin (340201)
 */
public final class ElevationProfile {


    /**
     * Longueur de l'itineraire
     */
    private final double length;

    /**
     * Echantillons d'altitude de l'itineraire
     */
    private final float[] elevationSamples;

    /**
     * Fonction qui associe une altitude a une position sur le profile
     */
    private final DoubleUnaryOperator sampled;

    /**
     * Dénivlé positif total en mètres
     */
    private final double totalAscent;

    /**
     * Dénivlé negatif total en mètres
     */
    private final double totalDescent;

    /**
     * Elevation minimale du profil
     */
    private final double minElevation;

    /**
     * Elevation maximale du profil
     */
    private final double maxElevation;

    /**
     * Constructeur d'un ElevationProfile
     * @param length longueur de l'itineraire
     * @param elevationSamples échantillons d'altitude de l'itineraire
     */
    public ElevationProfile(double length, float[] elevationSamples) {
        this.length = length;
        this.elevationSamples = Arrays.copyOf(elevationSamples,elevationSamples.length);
        this.sampled = Functions.sampled(this.elevationSamples, this.length);
        this.totalDescent=totalDescentCalculator();
        this.totalAscent=totalAscentCalculator();

        DoubleSummaryStatistics stats = new DoubleSummaryStatistics();

        for (float elevationSample : elevationSamples) {
            stats.accept(elevationSample);
        }

        this.minElevation =  stats.getMin();
        this.maxElevation = stats.getMax();

    }

    /**
     * Retourne la longueur du profil, en mètres
     * @return la longueur en mètres
     */
    public double length() {
        return length;
    }

    /**
     * Retourne l'altitude minimale du profil, en mètres
     * @return l'altitude minimale
     */
    public double minElevation() {
        return minElevation;
    }

    /**
     * Retourne l'altitude maximale du profil, en mètres
     * @return l'altitude maximale
     */
    public double maxElevation() {
        return maxElevation;
    }

    /**
     * Retourne le dénivelé positif total du profil, en mètres
     * @return le dénivelé positif total
     */
    public double totalAscent(){
        return totalAscent;
    }

    /**
     * Retorune le dénivelé négatif total du profil, en mètres
     * @return le denivelé negatif total
     */
    public double totalDescent(){
        return totalDescent;
    }

    /**
     * Retourne l'altitude du profil à la position donnée
     * @param position position le long du profile
     * @return l'altitude du profil à la position donnée ; le premier échantillon est retourné lorsque la
     * position est négative, le dernier lorsqu'elle est supérieure à la longueur.
     */
    public double elevationAt(double position) {
        return sampled.applyAsDouble(position);
    }

    /**
     * Méthode outil pour calculer le dénivelé positif total du profil, en mètres
     * @return le dénivelé positif total
     */
    private double totalAscentCalculator() {
        double total = 0;

        for (int i = 1; i < elevationSamples.length; i++){
            if (elevationSamples[i]>elevationSamples[i - 1]){
                total += elevationSamples[i] - elevationSamples[i - 1];
            }
        }

        return total;
    }

    /**
     * Methode outil pour calculer le dénivelé négatif total du profil, en mètres
     * @return le dénivelé négatif total
     */
    private double totalDescentCalculator() {
        double total = 0;

        for (int i = 1; i < elevationSamples.length; i++){
            if (elevationSamples[i]<elevationSamples[i - 1]){
                total += elevationSamples[i - 1]-elevationSamples[i];
            }
        }

        return total;
    }

    public double gradientAt(double position){
        double position1=position+ RouteBean.DEFAULT_MAX_STEP_LENGTH;
        return (elevationAt(position1)-elevationAt(position))/(position1-position) * 100;
    }

}
