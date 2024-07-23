package ch.epfl.javelo.routing;

import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;

import java.util.*;

/**
 * Classe qui détermine l'itinéraire optimal entre deux noeuds Javelo
 *
 * @author Albert Fares (341018)
 * @author Etienne Asselin (340201)
 */
public final class RouteComputer {

    /**
     * Graphe Javelo
     */
    private final Graph graph;

    /**
     * Fonction qui permet de calculer le facteur multiplicateur relatif à la praticabilité à vélo d'une arête
     */
    private final CostFunction costFunction;

    /**
     * Constructeur qui affecte le graphe et la fonction decoût aux attributs
     *
     * @param graph Graphe Javelo
     * @param costFunction Fonction de Cout
     */
    public RouteComputer(Graph graph, CostFunction costFunction) {
        this.graph = graph;
        this.costFunction = costFunction;
    }

    /**
     * Détermine le meilleur itinéraire simple entre les deux noeuds Javelo donnés
     *
     * @param startNodeId Identité du noeud de départ
     * @param endNodeId   Identité du noeud d'arrivée
     * @return l'itinéraire simple idéale entre les deux noeuds, null s'il n'existe pas
     */
    public Route bestRouteBetween(int startNodeId, int endNodeId) {

        Preconditions.checkArgument(startNodeId != endNodeId);

        double[] distances = new double[graph.nodeCount()];
        int[] predecesseur = new int[graph.nodeCount()];
        Map<Integer, Integer> map = new HashMap<>();

        Arrays.fill(distances, Double.POSITIVE_INFINITY);

        distances[startNodeId] = 0;

        PriorityQueue<WeightedNode> enExploration = new PriorityQueue<>();

        enExploration.add(new WeightedNode(startNodeId, 0));

        PointCh endPoint = graph.nodePoint(endNodeId);

        while (!enExploration.isEmpty()) {
            WeightedNode node = enExploration.remove();

            if (node.nodeId == endNodeId) {
                List<Edge> edgeList = new ArrayList<>();
                int currentNodeId = endNodeId;

                while (currentNodeId != startNodeId) {
                    int previousNodeId = predecesseur[currentNodeId];

                    edgeList.add(Edge.of(graph, map.get(currentNodeId), previousNodeId, currentNodeId));

                    currentNodeId = previousNodeId;
                }

                Collections.reverse(edgeList);
                return new SingleRoute(edgeList);
            }

            if (distances[node.nodeId] != Float.NEGATIVE_INFINITY) {

                for (int i = 0; i < graph.nodeOutDegree(node.nodeId); i++) {

                    int edgeId = graph.nodeOutEdgeId(node.nodeId, i);
                    int nodeOutId = graph.edgeTargetNodeId(edgeId);

                    float distance = (float) (distances[node.nodeId] + costFunction
                            .costFactor(node.nodeId, edgeId) * graph.edgeLength(edgeId));

                    if (distance < distances[nodeOutId]) {
                        distances[nodeOutId] = distance;
                        WeightedNode targetNode = new WeightedNode(nodeOutId, (float) (distance + graph
                                .nodePoint(nodeOutId).distanceTo(endPoint)));

                        enExploration.add(targetNode);

                        predecesseur[nodeOutId] = node.nodeId;

                        map.put(nodeOutId, edgeId);
                    }
                }
                distances[node.nodeId] = Float.NEGATIVE_INFINITY;
            }
        }

        return null;
    }

    /**
     * Enregistrement qui représente un noeud carctérisé par son identité et sa distance à des noeuds précis
     *
     */
    private record WeightedNode(int nodeId, float distance) implements Comparable<WeightedNode> {

        /**
         * {@inheritDoc}
         */
        @Override
        public int compareTo(WeightedNode that) {
            return Float.compare(this.distance, that.distance);
        }

    }

}



