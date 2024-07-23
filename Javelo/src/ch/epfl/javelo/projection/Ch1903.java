package ch.epfl.javelo.projection;

/**
 * Convertisseur entre les coordonnees suisses (CH1903+) et les coordonnees WGS84
 *
 * @author Albert Fares (341918)
 */

public class Ch1903 {
    /**
     * Constructeur privé car non instanciable
     */
    private Ch1903() {}

    /**
     * Convertie les coordonnees WGS84 en coordonnees suisse (EST)
     * @param lon longitude fournie en radians
     * @param lat latitude fournie en radians
     * @return la coordonnée E (est) du point de longitude lon et latitude lat dans le système WGS8.
     */
    public static double e(double lon, double lat) {

        double lon1 = WGS84Lon1(lon);
        double lat1 = WGS84Lat1(lat);

        return 2600072.37 + 211455.93 * lon1 - 10938.51 * lon1
                * lat1 - 0.36 * lon1 * lat1 * lat1 - 44.54 * Math.pow(lon1, 3);
    }

    /**
     * Convertie les coordonnees WGS84 en coordonnees suisses (NORD)
     * @param lon longitude fournie en radians
     * @param lat latitude fournie en radians
     * @return la coordonnée N (nord) du point de longitude lon et latitude lat dans le système WGS84.
     */
    public static double n(double lon, double lat) {

        double lon1 = WGS84Lon1(lon);
        double lat1 = WGS84Lat1(lat);

        return 1200147.07 + 308807.95 * lat1 + 3745.25 * lon1 *
                lon1 + 76.63 * lat1 * lat1 - 194.56 * lat1 * lon1 * lon1 + 119.79 * Math.pow(lat1, 3);
    }

    /**
     * Convertie les coordonnees suisses en coordonnees WGS84 (longitude)
     * @param e coordonnee EST fournie en metres
     * @param n coordonnee NORD fournie en metres
     * @return la longitude dans le système WGS84 du point dont les coordonnées sont e et n dans le système suisse.
     */
    public static double lon(double e, double n) {

        double x = 1e-6 * (e - 2600000);
        double y = 1e-6 * (n - 1200000);

        double lon1 = 2.6779094 + 4.728982 * x +0.791484 * x * y + 0.1306
                * x * y * y - 0.0436* x * x * x;

        return Math.toRadians(lon1 * (100.0 / 36));
    }


    /**
     * Convertie les coordonnees suisses en coordonnees WGS84 (latitude)
     * @param e coordonnee EST fournie en metres
     * @param n coordonnee NORD fournie en metres
     * @return la latitude dans le système WGS84 du point dont les coordonnées sont e et n dans le système suisse.
     */
    public static double lat(double e, double n){

        double x = 1e-6 * (e - 2600000);
        double y = 1e-6 * (n - 1200000);

        double lat1 = 16.9023892 + 3.238272 * y - 0.270978* x * x - 0.002528
                * y * y - 0.0447* x * x * y - 0.0140 * y * y * y;

        return Math.toRadians(lat1*(100.0/36));
    }


    /**
     * Calcule la longitude intermediaire utilisee dans les conversions en coordonnees suisses.
     * @param lon la longitude fournie en radians.
     * @return la longitude intermediaire en degrees.
     */
    private static double WGS84Lon1(double lon) {

        return 1e-4 * (3600 * Math.toDegrees(lon) - 26782.5);
    }

    /**
     * Calcule la latitude intermediaire utilisee dans les conversions en coordonnees suisses.
     * @param lat la longitude fournie en radians.
     * @return la latitude intermediaire en degrees.
     */
    private static double WGS84Lat1(double lat) {

        return 1e-4 * (3600 * Math.toDegrees(lat) - 169028.66);
    }

}
