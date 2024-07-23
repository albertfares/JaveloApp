package ch.epfl.javelo;

import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;

/**
 * Contient des méthodes permettant de créer des objets représentant
 * des fonctions mathématiques des réels vers les réels
 *
 * @author Etienne Asselin(340201)
 */
public final class Functions{

    /**
     * Constructeur prive car la classe est non instantiable
     */
    private Functions(){}

    /**
     * Crée une fonction constante dont la valeur est toujours y
     * @param y constante
     * @return la fonction constante
     */
    public static DoubleUnaryOperator constant(double y){
        return new Constant(y);
    }

    /**
     * Cree une fonction obtenue par interpolation linéaire entre les échantillons
     * samples, espacés régulièrement et couvrant la plage allant de 0 à xMax ; lève
     * IllegalArgumentException si le tableau samples contient moins de deux éléments,
     * ou si xMax est inférieur ou égal à 0.
     * @param samples tableau de float contenant les echantillons
     * @param xMax coordonnee maximale
     * @return la fonction
     */
    public static DoubleUnaryOperator sampled(float[] samples, double xMax){
        return new Sampled(samples, xMax);
    }

    /**
     * Classe imbriquee utilisee par la methode constant de la classe mere
     */
    private static final class Constant implements DoubleUnaryOperator{

        /**
         * Constante retournée par la fonction
         */
        private final double constant;

        /**
         * Constructeur privé qui affecte la constante
         * @param constant-La constante
         */
        private Constant(double constant){
            this.constant = constant;
        }

        /**
         * Méthode qui calcule la valeur de la fonction en operand, ici la constante
         * @param operand-argument passé à la fonction
         * @return la constante
         */
        @Override
        public double applyAsDouble(double operand) {
            return constant;
        }
    }

    /**
     * Classe imbriquee utilisee dans la methode sampled de la classe mere
     */

    private static final class Sampled implements DoubleUnaryOperator{

        private static final int MINIMUM_NUMBER_OF_SAMPLES=2;

        /**
         * Tableau d'échantillons
         */
        private final float[] samples;

        /**
         * Valeur maximale de la plage de x
         */
        private final double xMax;

        /**
         * Constructeur privé qui affecte samples et xMax aux attributs du même nom
         * @param samples-Tableau d'échantillons
         * @param xMax-Valeur maximale de la plage de x
         */
        private Sampled(float[] samples, double xMax){
            Preconditions.checkArgument(samples.length >= MINIMUM_NUMBER_OF_SAMPLES && xMax > 0);

            this.xMax = xMax;
            this.samples= Arrays.copyOf(samples, samples.length);
        }

        /**
         * Méthode qui calcule l'interpolation linéaire de operand par rapport aux valeurs de tous les échantillons de samples
         * @param operand-Valeur passée en paramètre à la fonction
         * @return l'interpolation linéaire
         */
        @Override
        public double applyAsDouble(double operand) {
            double placeInSpan = (Math2.clamp(0, operand, xMax) * (samples.length - 1)) / xMax;

            int placeInSpanFloor = (int) placeInSpan;
            int placeInSpanCeil = (int) Math.ceil(placeInSpan);

            if (placeInSpanFloor == placeInSpanCeil) {
                return samples[placeInSpanFloor];

            } else {
                return Math2.interpolate(samples[placeInSpanFloor],
                        samples[placeInSpanCeil],
                        placeInSpan - placeInSpanFloor);
            }
        }
    }
}