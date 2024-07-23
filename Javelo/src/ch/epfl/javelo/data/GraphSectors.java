package ch.epfl.javelo.data;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.SwissBounds;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Enregistrement qui représente le tableau qui contient la totalité des secteurs Javelo
 *
 * @author Albert Fares (341018)
 * @author Etienne Asselin (340201)
 */
public record GraphSectors(ByteBuffer buffer) {

    /**
     *Racine carré du nombre de secteurs qui représente donc la longueur du côté du carré de secteurs en secteurs
     */
    private final static int SQRT_SECTORS = 128;

    /**
     * Décalage d'index dans le tableau de l'identité du premier noeud du secteur
     */
    private final static int OFFSET_STARTNODEID = 0;

    /**
     * Décalage d'index dans le tableau du nombre de noeuds dans le secteur
     */
    private final static int OFFSET_NUMBEROFNODES=OFFSET_STARTNODEID + Integer.BYTES;

    /**
     * Nombre de Bytes total qu'occupe un secteur dans le tableau
     */
    private final static int SECTOR_BYTES = OFFSET_NUMBEROFNODES + Short.BYTES;


    /**
     * A partir d'un point en Suisse et d'une distance, détermine quels secteurs le carré,
     * centré en ce point et dont les côtés font le double de la distance donnée, chevauche
     * @param center PointCh qui représente le centre du carré
     * @param distance Double qui représente la moitié du côté du carré
     * @return la liste des secteurs que le carré chevauche
     */
    public List<Sector> sectorsInArea(PointCh center, double distance){
        double eCoordinateMax = center.e() + distance;
        double eCoordinateMin = center.e() - distance;
        double nCoordinateMax = center.n() + distance;
        double nCoordinateMin = center.n() - distance;

        int xMin = xCoordinatesFromEastCoordinate(eCoordinateMin);
        int xMax = xCoordinatesFromEastCoordinate(eCoordinateMax);
        int yMin = yCoordinatesFromNorthCoordinate(nCoordinateMin);
        int yMax = yCoordinatesFromNorthCoordinate(nCoordinateMax);

        int downLeftCornerSectorId = yMin*SQRT_SECTORS + xMin;

        int width = xMax-xMin + 1;
        int height = yMax-yMin + 1;

        int arrayLength = height*width;

        List<Sector> sectors = new ArrayList<>();

        for (int i = 0; i < arrayLength; i++){
            int sectorId = extractSectorId(downLeftCornerSectorId, width, i);
            sectors.add(extractSector(sectorId));
        }

        return sectors;
    }

    /**
     * Methode outil qui permet de calculer les coordonnnés horizontales
     * dans le tableau de secteurs à partir d'une coordonnée est CH1903+
     * @param eastCoordinate Coordonnée est CH1903+
     * @return l'abcisse dans le tableau de secteurs correspondante
     */
    private int xCoordinatesFromEastCoordinate(double eastCoordinate){
        return (int) Math2.clamp(0, (eastCoordinate - SwissBounds.MIN_E) /SwissBounds.WIDTH*SQRT_SECTORS, (SQRT_SECTORS - 1));
    }

    /**
     * Methode outil qui permet de calculer les coordonnés verticales
     * dans le tableau de secteurs à partir d'une coordonnée nord CH1903+
     * @param northCoordinate Coordonnée nord CH1903+
     * @return l'ordonnée dans le tableau de secteurs correspondante
     */
    private int yCoordinatesFromNorthCoordinate(double northCoordinate){
        return (int) Math2.clamp(0, (northCoordinate - SwissBounds.MIN_N)/SwissBounds.HEIGHT*SQRT_SECTORS, (SQRT_SECTORS - 1));
    }

    /**
     * Methode outil qui extrait l'index du secteur a partir de l'index du
     * secteur situé en bas a gauche, la largeur en secteurs de l'aire et
     * l'index du secteur parmis les secteurs dans l'aire
     * @param downLeftCornerSectorId index du secteur situé en bas a gauche
     * @param width largeur en secteurs de l'aire
     * @param index index du secteur parmis les secteurs dans l'aire
     * @return l'index du secteur
     */
    private int extractSectorId(int downLeftCornerSectorId, int width, int index){
        return downLeftCornerSectorId + index%width + (index/width)*SQRT_SECTORS;
    }

    /**
     * Extrait un secteur du tableau à partir de son identité en prélevant
     * l'identité du premier et du dernier noeud contenu dans le secteur
     * @param sectorId Identité du secteur
     * @return le Secteur
     */
    private Sector extractSector(int sectorId){
        int startNodeId = buffer.getInt(sectorId*SECTOR_BYTES + OFFSET_STARTNODEID);

        return new Sector(startNodeId, startNodeId+Short.toUnsignedInt(buffer
                .getShort(sectorId*SECTOR_BYTES + OFFSET_NUMBEROFNODES)));
    }

    /**
     * Enregistrement Sector qui représente les secteurs, caractérisés
     * par l'identité de leur premier et de leur dernier noeud
     */
    public record Sector(int startNodeId, int endNodeId){

        /**
         * Constructeur compact qui vérifie que les identités des premier
         * et dernier noeud ne sont pas négatives
         * @param startNodeId identité du premier noeud
         * @param endNodeId identité du dernier noeud
         */
        public Sector{
            Preconditions.checkArgument(startNodeId >= 0 && endNodeId >= 0);
        }
    }

}