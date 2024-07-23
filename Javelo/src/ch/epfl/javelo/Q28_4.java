package ch.epfl.javelo;

/**
 * Permet de convertir des nombres entre la représentation Q28.4 et d'autres représentations
 *
 * @author Etienne Asselin (340201)
 */

public final class Q28_4 {

    /**
     * Nombre de Bits après le 'fixed point' dans la représentation Q28.4
     */
    private static final int BITS_AFTER_POINT = 4;

    /**
     * Constructeur prive car la classe est non instiantiable
     */
    private Q28_4(){}

    /**
     * Calcule la valeur Q28.4 correspondant à l'entier passe en parametre
     * @param i entier
     * @return la valeur Q28.4 correspondante
     */
    public static int ofInt(int i){
        return i << BITS_AFTER_POINT;
    }

    /**
     * Calcule la valeur de type double égale à la valeur Q28.4 donnée
     * @param q28_4 valeur Q28.4
     * @return la valeur du type double correspondante
     */
    public static double asDouble(int q28_4){
        return Math.scalb(q28_4, -BITS_AFTER_POINT);
    }

    /**
     * Calcule la valeur de type float correspondant à la valeur Q28.4 donnée
     * @param q28_4 valeur Q28.4
     * @return le float correpondant
     */
    public static float asFloat(int q28_4){
        return Math.scalb(q28_4, -BITS_AFTER_POINT);
    }

}
