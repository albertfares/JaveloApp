package ch.epfl.javelo.data;

import ch.epfl.javelo.Bits;
import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Q28_4;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;


/**
 * Enregistrement qui représente le tableau de toutes les arêtes du graphe JaVelo.
 *
 * @author Albert Fares (341018)
 * @author Etienne Asselin (340201)
 */
public record GraphEdges(ByteBuffer edgesBuffer, IntBuffer profileIds, ShortBuffer elevations) {

    /**
     * Décalage d'index du debut du noeud de destinaton d'une arete
     */
    private static final int OFFSET_DESTINATIONNODEID = 0;

    /**
     * Décalage d'index du debut de la longeur d'une arête
     */
    private static final int OFFSET_EDGELENGTH = OFFSET_DESTINATIONNODEID + Integer.BYTES;

    /**
     * Décalage d'index du debut du gain de l'élévation d'une arête
     */
    private static final int OFFSET_ELEVATIONGAIN = OFFSET_EDGELENGTH + Short.BYTES;

    /**
     * Décalage d'index du debut des identités de l'ensemble d'attributs
     */
    private static final int OFFSET_IDATTRIBUTES = OFFSET_ELEVATIONGAIN + Short.BYTES;

    /**
     * Le nombre de bytes total qu'occupe une arête dans le tableau
     */
    private static final int EDGES_BYTES=OFFSET_IDATTRIBUTES + Short.BYTES;

    /**
     * Type de profile inexistant
     */
    private static final int NO_PROFILE = 0;

    /**
     * Type de profil contenant des échantillons décompressés
     */
    private static final int DECOMPRESSED_PROFILE = 1;

    /**
     * Type de profil contenant des échantillons compressés au format Q4.4
     */
    private static final int COMPRESSED_Q4_4_PROFILE = 2;

    /**
     * Type de profile contenant des échantillons compressés au format Q0.4
     */
    private static final int COMPRESSED_Q0_4_PROFILE = 3;


    /**
     * Verifie si l'arête d'identité donnée va dans le sens inverse
     * de la voie OSM dont elle provient
     * @param edgeId identite de l'arête
     * @return true si l'arête est inversée et false sinon
     */
    public boolean isInverted(int edgeId) {
        return (edgesBuffer.getInt(edgeId * EDGES_BYTES + OFFSET_DESTINATIONNODEID) < 0);

    }

    /**
     * Retourne l'identité du nœud destination de l'arête d'identité donnée
     * @param edgeId identite de l'arête
     * @return l'identité du nœud destination
     */
    public int targetNodeId(int edgeId) {
        int destinationNodeId = edgesBuffer.getInt(edgeId * EDGES_BYTES + OFFSET_DESTINATIONNODEID);

        return (destinationNodeId < 0) ? ~destinationNodeId : destinationNodeId;
    }

    /**
     * Retourne la longueur, en mètres, de l'arête d'identité donnée
     * @param edgeId identité de l'arête
     * @return la longueur de l'arête
     */
    public double length(int edgeId) {
        double length = Q28_4.asDouble(Short.toUnsignedInt(edgesBuffer.
                getShort(EDGES_BYTES * edgeId + OFFSET_EDGELENGTH)));

        assert length >= 0;

        return length;
    }

    /**
     * Retourne le dénivelé positif, en mètres, de l'arête d'identité donnée
     * @param edgeId identité de l'arete
     * @return le dénivelé positif de l'arête
     */
    public double elevationGain(int edgeId) {
        return Q28_4.asDouble(Short.toUnsignedInt(edgesBuffer.
                getShort(EDGES_BYTES * edgeId + OFFSET_ELEVATIONGAIN)));
    }

    /**
     * Verifie si l'arête d'identité donnée possède un profil.
     * @param edgeId identité de l'arete
     * @return true si l'arête possède un profile et false sinon
     */
    public boolean hasProfile(int edgeId) {
        return getType(edgeId)!=NO_PROFILE;
    }

    /**
     * retourne le tableau des échantillons du profil de l'arête d'identité
     * donnée, qui est vide si l'arête ne possède pas de profil
     * @param edgeId identité de l'arete
     * @return le tableau des échantillons
     */
    public float[] profileSamples(int edgeId) {


        float[] samples = new float[numberOfSamples(edgeId)];
        float firstSample =  Q28_4.asFloat(Short.toUnsignedInt(elevations.get(getFirstSampleId(edgeId))));

        switch (getType(edgeId)){

            case NO_PROFILE:
                return new float[0];

            case DECOMPRESSED_PROFILE:
                for (int i = 0; i < numberOfSamples(edgeId); i++) {
                    samples[i] = Q28_4.asFloat(Short.toUnsignedInt(elevations.get(getFirstSampleId(edgeId) + i)));
                }

                break;

            case COMPRESSED_Q4_4_PROFILE:
                fillSamples(COMPRESSED_Q4_4_PROFILE,samples,edgeId,firstSample);

                break;

            case COMPRESSED_Q0_4_PROFILE:
              fillSamples(COMPRESSED_Q0_4_PROFILE,samples,edgeId,firstSample);
        }

        return (isInverted(edgeId))? reverse(samples) : samples;
    }

    /**
     * Retourne l'identité de l'ensemble d'attributs attaché à l'arête d'identité donnée.
     * @param edgeId identite de l'arete
     * @return l'identité de l'ensemble d'attributs
     */
    public int attributesIndex(int edgeId){
        return Short.toUnsignedInt(edgesBuffer.getShort(EDGES_BYTES*edgeId + OFFSET_IDATTRIBUTES));
    }

    /**
     * Methode outil qui permet de remplir le tableau d'echantillions pour ceux du type 2 et 3
     * @param type type de l'echantillion
     * @param samples tableau a remplir
     * @param edgeId identite de l'arete
     * @param firstSample premier echantillion
     */
    private void fillSamples(int type, float[] samples, int edgeId, float firstSample){

            samples[0] = firstSample;
            for (int i = 1; i < numberOfSamples(edgeId); i ++) {

                samples[i] = samples[i - 1] + Q28_4.asFloat(Bits.extractSigned(elevations.
                        get(Math2.ceilDiv(i, (type==COMPRESSED_Q4_4_PROFILE)? 2 : 4)+getFirstSampleId(edgeId)), (type==COMPRESSED_Q4_4_PROFILE)?startIndexType2(i) : startIndexType3(i),(type==COMPRESSED_Q4_4_PROFILE)? Byte.SIZE : Byte.SIZE/2));
            }
    }

    /**
     * Retourne le type de l'arête d'identité donnée
     * @param edgeId identite de l'arete
     * @return le type qui depend des echantillons que l'arete possede
     */
    private int getType(int edgeId) {
        return (Bits.extractUnsigned(profileIds.get(edgeId),Integer.SIZE - 2, 2));
    }

    /**
     * Retourne le nombre d'echantillons que possede une arete d'identite donnée
     * @param edgeId identité de l'arête
     * @return le nombre d'echantillons
     */
    int numberOfSamples(int edgeId) {
        return 1 + Math2.ceilDiv(Short.toUnsignedInt(edgesBuffer.
                getShort(EDGES_BYTES * edgeId + OFFSET_EDGELENGTH)), Q28_4.ofInt(2));
    }

    /**
     * Retourne l'index du premier échantillon que possede une arête d'identité donnée
     * @param edgeId identité de l'arête
     * @return l'index du premier échantillon
     */
    private int getFirstSampleId(int edgeId) {
        return Bits.extractUnsigned(profileIds.get(edgeId),0, Integer.SIZE - 2);
    }

    /**
     * Methode outil specifique aux aretes de types 2 qui permet le choix de
     * l'index dans le short du debut du vecteur de 8 bits extrait
     * @param i l'index de l'echantillon
     * @return l'index du debut de l'extraction
     */
    private int startIndexType2(int i){
        return (i%2 == 0)? 0 : 8;
    }

    /**
     * Methode outil specifique aux aretes de types 3 qui permet le choix de
     * l'index dans le short du debut du vecteur de 4 bits extrait
     * @param i l'index de l'echantillon
     * @return l'index du debut de l'extraction
     */
    private int startIndexType3(int i){
        return 12 - 4*((i - 1)%4);
    }

    /**
     * Methode permettant d'inverser un tableau
     * @param samples tableau d'echantillons qu'on voudrait inverser
     * @return le tableau inversé
     */
    private float[] reverse(float[] samples) {

        float[] reversed = new float[samples.length];

        for (int j = 0; j < samples.length; j++) {
            reversed[j] = samples[samples.length - 1 - j];
        }

        return reversed;
    }

}
