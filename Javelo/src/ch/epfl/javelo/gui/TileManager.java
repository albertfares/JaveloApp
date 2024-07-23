package ch.epfl.javelo.gui;
import ch.epfl.javelo.Preconditions;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Gestionnaire de tuiles OSM
 *
 * @author Albert Fares (341018)
 * @author Etienne Asselin (340201)
 */
public final class TileManager {

    /**
     * Constante qui représente la capacité maximale du cache mémoire
     */
    private final static int MEMORY_CACHE_CAPACITY = 100;
    /**
     * Load Factor par défaut du cache mémoire
     */
    private final static float MEMORY_CACHE_LOAD_FACTOR = 0.75f;
    /**
     * Chemin d'accès au cache disque
     */
    private final Path cacheDiskPath;

    /**
     * Cache mémoire pour un accès rapide aux tuiles
     */
    private final LinkedHashMap<TileId,Image> memoryCache;

    private ObjectProperty<URL> urlProperty;

    private ObjectProperty<URLConnection> urlConnectionProperty;

    private ObjectProperty<String> serverNameProperty;


    /**
     * Constructeur d'un gestionnaire de tuiles
     * @param cacheDiskPath chemin d'accès au cache disque
     * @param serverName nom du serveur
     */
    public TileManager(Path cacheDiskPath, ObjectProperty<String> serverName) {

        this.cacheDiskPath = cacheDiskPath;
        this.serverNameProperty = serverName;
        this.memoryCache = new LinkedHashMap<>(MEMORY_CACHE_CAPACITY
                ,MEMORY_CACHE_LOAD_FACTOR
                ,true);
        this.urlProperty = new SimpleObjectProperty<>();
        this.urlConnectionProperty = new SimpleObjectProperty<>();


    }

    /**
     * Méthode qui renvoie l'image correspondante à la tuile OSM de TileId donné
     * @param tileId TileId de la tuile
     * @return l'image de type Image correspondante à la tuile
     * @throws IOException en cas d'erreur entrée/sortie
     */
    public Image imageForTileAt(TileId tileId) throws IOException{
        if (memoryCache.containsKey(tileId)){
            return memoryCache.get(tileId);
        }

        Path filePath=cacheDiskPath.resolve("%d".formatted(tileId.zoomLevel))
                .resolve("%d".formatted(tileId.x))
                .resolve("%d.png".formatted(tileId.y));

        Image image = extractFromCacheDisk(tileId,filePath);

        if (image != null){
            return image;
        }

        urlProperty.set(new URL(
                "https://" + serverNameProperty.get()
                        + "/" + tileId.zoomLevel
                        + "/" + tileId.x
                        + "/" + tileId.y + ".png"));


        urlConnectionProperty.set(urlProperty.get().openConnection());


        urlConnectionProperty.get().setRequestProperty("User-Agent", "JaVelo");

        Files.createDirectories(filePath.getParent());

        try (InputStream i = urlConnectionProperty.get().getInputStream();
             OutputStream o = new FileOutputStream(filePath.toFile())){
            i.transferTo(o);

            Image imageFromServer = extractFromCacheDisk(tileId, filePath);
            addToCache(tileId, imageFromServer);

            return imageFromServer;
        }
    }

    /**
     * Methode outil qui ajoute l'élément passe en paramètre à la memory
     * cache si cette dernière n'a pas encore atteint sa capacité maximale
     * et efface l'élément qui a été acceder il y a le plus longtemps puis
     * lui ajoute l'element voulu sinon
     *
     * @param tileId Identité de la tuile
     * @param image Image associée a la tuile
     */
    private void addToCache(TileId tileId , Image image){
        Iterator<Map.Entry<TileId,Image>> iterator = memoryCache.entrySet().iterator();

        if (memoryCache.size() >= MEMORY_CACHE_CAPACITY) {

            memoryCache.remove(iterator.next().getKey());

        }
        memoryCache.put(tileId, image);
    }

    /**
     * Méthode outil qui retourne l'image de la tuile de tileId donnée en l'extrayant du cache disque
     *
     * @param tileId Identité de la tuile
     * @param filePath Chemin de l'image dans le cache disque
     * @return l'image de la tuille correspondante si elle est présente dans le cache disque, null sinon
     * @throws IOException en cas d'erreur entrée/sortie
     */
    private Image extractFromCacheDisk(TileId tileId,Path filePath) throws IOException{
        if (Files.exists(filePath)){

            try (InputStream stream = new FileInputStream(filePath.toFile())){
                Image image = new Image(stream);

                addToCache(tileId, image);

                return image;
            }
        }
        return null;
    }


    /**
     * Enregistrement qui permet une l'identification unique d'une tuile OSM
     */
    public static record TileId(int zoomLevel, int x, int y){

        /**
         * Constructeur compact qui vérifie la validité des arguments passés au constructeur
         * @param zoomLevel niveau de zoom
         * @param x index X de la tuile
         * @param y index Y de la tuile
         */
        public TileId{
            Preconditions.checkArgument(isValid(zoomLevel, x, y));
        }

        /**
         * Méthode statique qui permet de vérifier que les index passés en paramètre sont
         * valides pour une tuile OSM à un niveau de zoom donné
         * @param zoomLevel niveau de zoom
         * @param x index X de la tuile
         * @param y index Y de la tuile
         * @return vrai si les index sont valides, false sinon
         */
        public static boolean isValid(int zoomLevel, int x, int y){
            int maxTileIndex = (1 << zoomLevel) - 1;

            return (x >= 0 && y >= 0 && x <= maxTileIndex && y <= maxTileIndex);
        }
    }

    public void clearMemoryCache(){
        memoryCache.clear();
    }
}