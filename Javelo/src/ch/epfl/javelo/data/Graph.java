package ch.epfl.javelo.data;

import ch.epfl.javelo.Functions;
import ch.epfl.javelo.projection.PointCh;
import java.io.IOException;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;


/**
 *Classe qui représente le graphe JaVelo.
 *
 * @author Albert Fares (341018)
 * @author Etienne Asselin (340201)
 */

public final class Graph {

    /**
     * Noeud du graph JaVelo
     */
    private final GraphNodes nodes;
    /**
     * Secteurs du graph JaVelo
     */
    private final GraphSectors sectors;
    /**
     * Aretes du graph JaVelo
     */
    private final GraphEdges edges;
    /**
     * Attributs du graph JaVelo
     */
    private final List<AttributeSet> attributeSets;

    /**
     * Constructeur du graph JaVelo
     * @param nodes Noeud du graph JaVelo
     * @param sectors Secteurs du graph JaVelo
     * @param edges Aretes du graph JaVelo
     * @param attributeSets Attributs du graph JaVelo
     */
    public Graph(GraphNodes nodes, GraphSectors sectors, GraphEdges edges, List<AttributeSet> attributeSets){
        this.nodes =nodes;
        this.sectors = sectors;
        this.edges = edges;
        this.attributeSets = List.copyOf(attributeSets);
    }

    /**
     * Méthode permettant de charger le graphe depuis un répertoire
     * @param basePath chemin d'accès du repertoire
     * @return le graphe JaVelo obtenu à partir des fichiers se trouvant
     * dans le répertoire dont le chemin d'accès est basePath
     * @throws IOException en cas d'erreur d'entrée/sortie.
     */
    public static Graph loadFrom(Path basePath) throws IOException{
        IntBuffer nodesBuffer=extractBufferFromPath("nodes.bin",basePath).asIntBuffer();

        ByteBuffer sectorsBuffer=extractBufferFromPath("sectors.bin",basePath);

        ByteBuffer edgesBuffer=extractBufferFromPath("edges.bin",basePath);

        IntBuffer profileIdsBuffer=extractBufferFromPath("profile_ids.bin",basePath).asIntBuffer();

        ShortBuffer elevationsBuffer=extractBufferFromPath("elevations.bin",basePath).asShortBuffer();

        LongBuffer attributesBuffer=extractBufferFromPath("attributes.bin",basePath).asLongBuffer();


        List<AttributeSet> attributeSets = new ArrayList<>(attributesBuffer.capacity());

        for (int i = 0 ; i<attributesBuffer.capacity() ; i++){
            long attributeLong = attributesBuffer.get(i);

            AttributeSet attribute = new AttributeSet(attributeLong);

            attributeSets.add(attribute);
        }

        return new Graph(new GraphNodes(nodesBuffer),
                new GraphSectors(sectorsBuffer),
                new GraphEdges(edgesBuffer,profileIdsBuffer,elevationsBuffer),
                attributeSets);
    }

    /**
     * Méthode outil qui permet d'extraire le ByteBuffer du Path de base donné en paramètre en fonction
     * de la chaîne de caractères décrivant le Path du fichier contenant le buffer
     * @param pathString la chaîne de caractères du Path contenant le buffer
     * @param basePath le Path de base
     * @return le ByteBuffer Mémoire tampon à extraire
     * @throws IOException en cas d'erreurd'entrée/sortie
     */
    private static ByteBuffer extractBufferFromPath(String pathString,Path basePath) throws IOException{
        Path path = basePath.resolve(pathString);
        ByteBuffer buffer;
        try (FileChannel channel = FileChannel.open(path)) {
            buffer = extractBuffer(channel);
        }
        return buffer;
    }

    /**
     * Retourne le nombre total de nœuds dans le graphe
     * @return le nombre total de nœuds
     */
    public int nodeCount(){
        return nodes.count();
    }

    /**
     * Retourne la position du nœud d'identité donnée
     * @param nodeId identite du noeud
     * @return un PointCh dont les coordonnees sont celles du noeud
     */
    public PointCh nodePoint (int nodeId){
        return new PointCh(nodes.nodeE(nodeId), nodes.nodeN(nodeId));
    }

    /**
     * Retourne le nombre d'arêtes sortant du nœud d'identité donnée
     * @param nodeId identite du noeud
     * @return le nombre d'arêtes sortantes
     */
    public int nodeOutDegree(int nodeId){
        return nodes.outDegree(nodeId);
    }

    /**
     * Retourne l'identité de la edgeIndex-ième arête sortant du nœud d'identité nodeId
     * @param nodeId identite du noeud
     * @param edgeIndex identite de l'arête
     * @return l'identite de l'arête
     */
    public int nodeOutEdgeId (int nodeId, int edgeIndex){
        return nodes.edgeId(nodeId, edgeIndex);
    }

    /**
     * Retourne l'identité du nœud se trouvant le plus proche du point donné, à la
     *distance maximale donnée en metres
     * @param point pointCh pour lequel on voudrais retourner le noeud le plus proche
     * @param searchDistance la distance de recherche
     * @return l'identite du noeud le plus proche et -1 si aucun nœud ne correspond aux criteres de recherche
     */
    public int nodeClosestTo(PointCh point, double searchDistance){
        List<GraphSectors.Sector> sectorsInArea = this.sectors.sectorsInArea(point, searchDistance);

        double smallestDistance=searchDistance*searchDistance;

        int indexClosestTo= -1;

        for (GraphSectors.Sector sector : sectorsInArea){
            for (int i = sector.startNodeId(); i < sector.endNodeId(); i++){
                PointCh nodePoint=nodePoint(i);


                    double distance = point.squaredDistanceTo(nodePoint);

                    if (distance <= smallestDistance){
                        smallestDistance = distance;
                        indexClosestTo = i;
                    }

            }
        }
        return indexClosestTo;
    }

    /**
     * Retourne l'identité du nœud destination de l'arête d'identité donnée
     * @param edgeId identite de l'arete
     * @return l'identite du noeud
     */
    public int edgeTargetNodeId(int edgeId){
        return edges.targetNodeId(edgeId);
    }

    /**
     * Verifie si l'arete d'identite donnee est inversee
     * @param edgeId identite de l'arete
     * @return true si elle est inversee et false sinon
     */
    public boolean edgeIsInverted(int edgeId){
        return edges.isInverted(edgeId);
    }

    /**
     * Retourne l'ensemble des attributs OSM attachés à l'arête d'identité donnée
     * @param edgeId identite de l'arete
     * @return l'ensemble des attributs OSM
     */
    public AttributeSet edgeAttributes(int edgeId){
        return attributeSets.get(edges.attributesIndex(edgeId));
    }

    /**
     * Retourne la longueur, en mètres, de l'arête d'identité donnée
     * @param edgeId identite de l'arete
     * @return la longueur en metres
     */
    public double edgeLength(int edgeId){
        return edges.length(edgeId);
    }

    /**
     * Retourne le dénivelé positif total de l'arête d'identité donnée
     * @param edgeId identite de l'arete
     * @return le dénivelé positif total
     */
    public double edgeElevationGain(int edgeId){
        return edges.elevationGain(edgeId);
    }

    /**
     * Retourne le profil en long de l'arête d'identité donnée sous la forme d'une fonction
     * @param edgeId identite de l'arete
     * @return le profil en long de l'arête et Double.NaN si l'arete ne possede pas de profile
     */
    public DoubleUnaryOperator edgeProfile(int edgeId){
        return (edges.hasProfile(edgeId)) ?
                Functions.sampled(edges.profileSamples(edgeId), edges.length(edgeId)) :
                Functions.constant(Double.NaN);
    }

    /**
     * Methode outil qui permet d'extraire un buffer a partir d'un FileChannel passé en argument
     * @param channel FileChannel pour lequel on voudrais extraire le buffer
     * @return le MappedByteBuffer
     * @throws IOException en cas d'erreur d'entrée/sortie
     */
    private static MappedByteBuffer extractBuffer(FileChannel channel) throws IOException{
        return channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
    }

}
