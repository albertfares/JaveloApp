package ch.epfl.javelo.routing;

/**
 * Interface qui représente une fonction de coût
 *
 * @author Albert Fares (341018)
 * @author Etienne Asselin (340201)
 */
public interface CostFunction {

    /**
     * Retourne le facteur par lequel la longueur de l'arête d'identité edgeId,
     * partant du nœud d'identité nodeId, doit être multipliée
     *
     * @param nodeId identité du noeud
     * @param edgeId identité de l'arête
     * @return le facteur
     */
    double costFactor(int nodeId,int edgeId);
}
