package ch.epfl.javelo.data;

import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.Q28_4;
import java.nio.IntBuffer;

/**
 * Enregistrement qui représente le tableau de tous les noeuds Javelo
 *
 * @author Albert Fares (341018)
 * @author Etienne Asselin (340201)
 */
public record GraphNodes(IntBuffer buffer) {

    /**
     * Décalage d'index dans le tableau de la coordonnée est
     */
    private static final int OFFSET_E = 0;

    /**
     * Décalage d'index dans le tableau de la coordonnée nord
     */
    private static final int OFFSET_N = OFFSET_E + 1;

    /**
     * Décalage d'index dans le tableau des informations sur les arêtes
     */
    private static final int OFFSET_OUT_EDGES = OFFSET_N + 1;

    /**
     * Le nombre de int requis pour décrire un noeud
     */
    private static final int NODE_INTS = OFFSET_OUT_EDGES + 1;

    /**
     * Int constitué de 4 zeros a la position des bits de poids les plus forts et de uns pour le reste
     */
    private static final int TWENTY_EIGHT_ONES_INT = 0x0FFF_FFFF;

    /**
     * Int constitue de 4 uns a la position des bits de poids les plus forts et de zeros pour le reste
     */
    private static final int FOUR_ONES_INT = ~TWENTY_EIGHT_ONES_INT;

    /**
     * Nombre de positions de décalage logique à droite pour déterminer le nombre d'arêtes sortantes d'un noeud
     */
    private static final int SHIFT_DEGREE = Integer.SIZE - 4;


    /**
     * Compte le nombre de noeuds dans le tableau
     * @return le nombre de noeuds dans le tableau
     */
    public int count(){
        return buffer.capacity()/NODE_INTS;
    }

    /**
     * Vérifie la validité de nodeId, lance une IllegalArgumentException si le nodeId est invalide
     * @param nodeId Identité du noeud
     */
    private void checkNodeId(int nodeId){
        Preconditions.checkArgument(nodeId >= 0 && nodeId < count());
    }

    /**
     * Détermine la coordonnée est du noeud Javelo
     * @param nodeId Identité du noeud
     * @return la coordonnée est du noeud
     */
    public double nodeE(int nodeId){
        checkNodeId(nodeId);

        return Q28_4.asDouble(extractEastCoordinate(nodeId));
    }

    /**
     * Détermine la coordonnée nord du noeud Javelo
     * @param nodeId Identité du noeud
     * @return la coordonnée nord du noeud
     */
    public double nodeN(int nodeId){
        checkNodeId(nodeId);

        return Q28_4.asDouble(extractNorthCoordinate(nodeId));
    }

    /**
     * Détermine le nombre d'arêtes qui sortent du noeud Javelo
     * @param nodeId Identité du noeud
     * @return le nombre d'arêtes qui sortent du noeud Javelo
     */
    public int outDegree(int nodeId){
        checkNodeId(nodeId);

        return (extractEdgeInformation(nodeId) & FOUR_ONES_INT) >>> SHIFT_DEGREE;
    }

    /**
     * Détermine l'identité d'une arête sortante donnée du noeud Javelo
     * @param nodeId Identité du noeud
     * @param edgeIndex Index de l'arête parmi les arêtes sortant du noeud
     * @return l'identité de l'arête en question
     */
    public int edgeId(int nodeId,int edgeIndex){
        checkNodeId(nodeId);

        assert (edgeIndex >= 0 && edgeIndex<outDegree(nodeId));

        return (extractEdgeInformation(nodeId) & TWENTY_EIGHT_ONES_INT) + edgeIndex;
    }

    /**
     * Méthode qui extrait la coordonnée est du noeud d'identité donnée
     * @param nodeId Identité du noeud
     * @return la coordonnée est du noeud donné
     */
    private int extractEastCoordinate(int nodeId){
        return buffer.get(nodeId*NODE_INTS + OFFSET_E);
    }

    /**
     * Méthode qui extrait la coordonnée nord du noeud d'identité donné2
     * @param nodeId Identité du noeud
     * @return la coordonnée nord du noeud donnée
     */
    private int extractNorthCoordinate(int nodeId){
        return buffer.get(nodeId*NODE_INTS + OFFSET_N);
    }

    /**
     * Méthode qui extrait les informations sur les arêtes du noeud d'identité donné
     * @param nodeId Identité du noeud
     * @return les informations sur les arêtes du noeud donné
     */
    private int extractEdgeInformation(int nodeId){
        return buffer.get(nodeId*NODE_INTS + OFFSET_OUT_EDGES);
    }

}