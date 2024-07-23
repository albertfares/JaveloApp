package ch.epfl.javelo.projection;

/**
 * Description des limites de la suisse pour les coordonnées de type CH1903+
 *
 * @author Etienne Asselin(340201)
 */

public final class SwissBounds {
    /**
     * Limite minimal des coordonnées est du code CH1903+ pour la suisse
     */
    public static final double MIN_E = 2485000;
    /**
     * Limite maximal des coordonnées est du code CH1903+ pour la suisse
     */
    public static final double MAX_E = 2834000;
    /**
     * Limite minimal des coordonnées nord du code CH1903+ pour la suisse
     */
    public static final double MIN_N = 1075000;
    /**
     * Limite maximal des coordonnées nord du code CH1903+ pour la suisse
     */
    public static final double MAX_N = 1296000;
    /**
     * Largeur de la suisse en mètre
     */
    public static final double WIDTH = MAX_E - MIN_E;
    /**
     * Hauteur de la suisse en mètre
     */
    public static final double HEIGHT = MAX_N - MIN_N;

    /**
     * Constructeur privé car non instanciable
     */
    private SwissBounds(){}

    /**
     * Vérifie si les coordonnées sont bien contenues en Suisse
     * @param e - coordonnée est
     * @param n - coordonnée nord
     * @return true si les coordonnées sont contenues dans les limites
     */
    public static boolean containsEN(double e,double n) {
        return e <= MAX_E && e >= MIN_E && n >= MIN_N && n <= MAX_N;
    }

}
