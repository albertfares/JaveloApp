package ch.epfl.javelo;

/**
 * Préconditions pour vérifier la validité des arguments de méthodes
 *
 * @author Etienne Asselin (340201)
 *
 */
public final class Preconditions {

    /**
     * Constructeur privé car classe non instanciable
     */
    private Preconditions(){}

    /**
     * Lance une exception si le booléen passé en argument est faux
     * @param shouldBetrue le booléen
     * @throws IllegalArgumentException si le booléen est faux
     *
     */
    public static void checkArgument(boolean shouldBetrue){
        if (!shouldBetrue){
            throw new IllegalArgumentException();
        }
    }

}
