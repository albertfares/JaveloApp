package ch.epfl.javelo;

/**
 * Divers méthodes "outils" mathématiques
 *
 * @author Etienne Asselin(340201)
 * @author Albert Fares(341018)
 *
 */

public final class Math2 {

    /**
     * Constructeur privé car non instanciable
     */
    private Math2(){}

    /**
     * Calcule la partie entière par excès de la division de x par y
     * @param x-le numérateur
     * @param y-le dénominateur (doit être non nul)
     * @return la partie etntière par excès de la division de x par y
     */
    public static int ceilDiv(int x, int y){
        Preconditions.checkArgument(x >= 0 && y > 0);
        return (x + y - 1) / y;
    }

    /**
     * Détermine l'ordonnée y du point d'abcisse x sur la droite passant par les points (0,y0) et (1,y1)
     * @param y0 - l'ordonnée à l'origine de la droite
     * @param y1 - l'ordonnée au niveau de l'abscisse 1 de la droite
     * @param x - l'abcisse du point
     * @return l'ordonnée y
     */
    public static double interpolate(double y0, double y1, double x){
        return Math.fma(y1 - y0, x, y0);
    }

    /**
     *Limite la valeur de v à l'intervalle [min;max] avec v,min,max des entiers
     * @param min-Borne inférieure de l'intervalle
     * @param v-Valeur à limiter
     * @param max-Borne supérieure de l'intervalle
     * @return v si v est contenue dans l'intervalle sinon renvoie la borne la plus proche
     */
    public static int clamp(int min, int v, int max){
        return (int) clamp(min,(double)v, max);
    }

    /**
     * Méthode identique à précédente avec des doubles au lieu d'entiers
     * @param min-Borne inférieure de l'intervalle
     * @param v-Valeur à limiter
     * @param max-Borne supérieure de l'intervalle
     * @return v si v est contenue dans l'intervalle sinon renvoie la borne la plus proche
     */
    public static double clamp(double min, double v, double max){
        Preconditions.checkArgument(min <= max);

        return (v<=min) ? min : Math.min(v,max);
    }

    /**
     * Calcule l'arc sinus hyperbolique de x
     * @param x-L'argument de la fonction
     * @return l'arc sinus hyperbolique de x
     */
    public static double asinh(double x){
        return Math.log(x + Math.sqrt(1 + x*x));
    }


    /**
     * Cacule le produit scalaire entre deux vecteurs
     * @param uX coordonnee horizontale du premier point
     * @param uY coordonnee verticale du premier point
     * @param vX coordonnee horizontale du deuxieme point
     * @param vY coordonnee verticale du deuxieme point
     * @return le produit scalaire entre le vecteur u (de composantes uX et uY) et
     * le vecteur v (de composantes vX et vY)
     */
    public static double dotProduct (double uX, double uY, double vX, double vY){
        return Math.fma(uX,vX,uY*vY);
    }

    /**
     * Calcule la norme au carree d'un vecteur
     * @param uX composante horizontale du vecteur
     * @param uY composante verticale du vecteur
     * @return le carré de la norme du vecteur u de composantes uX et uY
     */
    public static double squaredNorm(double uX, double uY){
        return dotProduct(uX, uY, uX, uY);
    }

    /**
     * Calcule la norme d'un vecteur
     * @param uX composante horizontale du vecteur
     * @param uY composante verticale du vecteur
     * @return la norme du vecteur u de composantes uX et uY
     */
    public static double norm(double uX, double uY){
        return Math.sqrt(squaredNorm(uX, uY));
    }

    /**
     * Calcule la norme de la projection orthogonale d'un point sur un vecteur donne
     * @param aX coordonnee horizonale du premier point
     * @param aY coordonnee verticale du premier point
     * @param bX coordonnee horizonale du deuxieme point
     * @param bY coordonnee verticale du deuxieme point
     * @param pX coordonnee horizontale du troisieme point
     * @param pY coordonnee verticale du troisieme point
     * @return la longueur de la projection du vecteur allant du point A
     * (de coordonnées aX et aY) au point P (de coordonnées pX et pY) sur
     * le vecteur allant du point A au point B (de coordonnees bY et bY)
     */
    public static double projectionLength(double aX, double aY, double bX, double bY, double pX, double pY){
        return dotProduct(pX - aX,pY - aY,bX - aX,bY - aY)/norm(bX - aX,bY - aY);
    }

}
