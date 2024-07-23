package ch.epfl.javelo.routing;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;
import java.util.Arrays;

/**
 * Classe qui représente un calculateur de profil en long.
 *
 * @author Etienne Asselin (340201)
 * @author Albert Fares (341018)
 *
 */
public final class ElevationProfileComputer {

    /**
     * Constructeur privé car la classe est non instanciable
     */
    private ElevationProfileComputer(){}

    /**
     * Methode qui retourne le profil en long de l'itinéraire route, en garantissant que l'espacement
     * entre les échantillons du profil est d'au maximum maxStepLength mètres
     * lève IllegalArgumentException si cet espacement n'est pas strictement positif.
     * @param route itineraire
     * @param maxStepLength espacement des echantillons
     * @return le profile en long de l'itineraire
     */
    public static ElevationProfile elevationProfile(Route route,double maxStepLength){
        Preconditions.checkArgument(maxStepLength > 0);

        double routeLength = route.length();
        int numberOfSamples = numberOfSamples(routeLength,maxStepLength);
        double stepLength = routeLength/(numberOfSamples - 1);
        float[] samples = new float[numberOfSamples];

        for (int i = 0; i<numberOfSamples; i++){
            double positionForIndex = i*stepLength;
            float elevationAtIndex = (float) route.elevationAt(positionForIndex);

            samples[i] = elevationAtIndex;
        }

        return new ElevationProfile(routeLength, fillHolesInSamples(samples));
    }

    /**
     * Methode outil qui calcule le nombre d'echantillons du profile
     * @param routeLength longueur de la route
     * @param maxStepLength espacement des echantillons
     * @return le nombre d'echantillons
     */
    private static int numberOfSamples(double routeLength, double maxStepLength){
        return  1 + (int) Math.ceil(routeLength/maxStepLength);
    }

    /**
     * Méthode servant à combler tous les "trous" du profil de notre route dû aux arêtes sans profil.
     * @param samples Tableau des altitudes du profil contenant les "trous", des Double.NaN
     * @return le tableau comblé
     */
    private static float[] fillHolesInSamples(float[] samples){
        int indexOfFirstNonNan = -1;

        for (int i = 0; i < samples.length; i++){
            if (!Float.isNaN(samples[i])){
                indexOfFirstNonNan = i;

                break;
            }
        }
        if (indexOfFirstNonNan != -1) {
            Arrays.fill(samples, 0, indexOfFirstNonNan + 1, samples[indexOfFirstNonNan]);

        } else {
            Arrays.fill(samples, 0);

            return samples;
        }

        int indexOfLastNonNan = -1;
        for (int j = samples.length-1; j > -1; j--){
            if (!Float.isNaN(samples[j])){
                indexOfLastNonNan = j;

                break;
            }
        }
        Arrays.fill(samples, indexOfLastNonNan, samples.length, samples[indexOfLastNonNan]);

        for (int j = 0; j < samples.length; j++){
            if(Float.isNaN(samples[j])){
                int inferiorIndex = firstInferiorIndexValidValue(samples, j);
                int superiorIndex = firstSuperiorIndexValidValue(samples, j);

                interpolateMiddleHole(inferiorIndex,superiorIndex,samples,j);
            }
        }

        return samples;
    }

    /**
     * Méthode outil qui renvoie l'index de la première valeur valide (différente de NaN)
     * du tableau d'index supérieur à l'index donné
     * @param samples tableau contenant les valeurs d'altitude
     * @param index index de la valeur donné
     * @return l'index de la première valeur valide suivant la valeur d'index donné
     */
    private static int firstSuperiorIndexValidValue(float[] samples,int index){
        int validindex = -1;

        for (int i = index + 1; i < samples.length; i++){
            if (!Float.isNaN(samples[i])){
                validindex = i;

                break;
            }
        }

        return validindex;
    }

    /**
     * Méthode outil qui renvoie l'index de la dernière valeur valide (différente de NaN)
     * du tableau d'index inférieur à l'index donné
     * @param samples tableau contenant les valeurs d'altitude
     * @param index index de la valeur donné
     * @return l'index de la dernière valeur valide précédent la valeur d'index donné
     */
    private static int firstInferiorIndexValidValue(float[] samples, int index){
        int validIndex = -1;

        for (int i = index - 1; i > -1; i--){
            if (!Float.isNaN(samples[i])){
                validIndex = i;

                break;
            }
        }

        return validIndex;
    }

    /**
     * Methode outil qui permet d'interpoler les valeurs invalides dues aux aretes
     * aux milieu de la route qui n'ont pas de profils
     * @param inferiorIndex Dernier index valide inferieur à index
     * @param superiorIndex Premier index valide supérieur à index
     * @param samples Tableau d'échantillons
     * @param index index de l'échantillon invalide dans le tableau
     */
    private static void interpolateMiddleHole(int inferiorIndex, int superiorIndex, float[] samples, int index){
        samples[index] = (float) Math2.interpolate(samples[inferiorIndex], samples[superiorIndex],
                ((double) (index - inferiorIndex))/(superiorIndex - inferiorIndex));
    }

}
