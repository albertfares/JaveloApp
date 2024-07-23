package ch.epfl.javelo;

/**
 * Classe outil qui permet d'extraire une séquence de bits d'un vecteur de 32 bits
 *
 * @author Etienne Asselin (340201)
 */
public final class Bits {

    /**
     * Constructeur privé car la classe est non instantiable
     */
    private Bits(){}

    /**
     * Extrait du vecteur de 32 bits 'value' la plage de 'length' bits
     * commençant au bit d'index 'start', qu'elle interprète comme une
     * valeur signée en complément à deux, ou lève IllegalArgumentException
     * si la plage est invalide
     * @param value vecteur de bits
     * @param start index auquel le bit extrait commence
     * @param length longueur du bits qu'on veut extraire
     * @return valeur extraite interpretee comme une valeur signée en complément à deux
     */
    public static int extractSigned(int value, int start, int length){
        Preconditions.checkArgument(start >= 0
                && length >= 0
                && start < Integer.SIZE
                && length <= Integer.SIZE);

        int valueAfterShiftToTheLeft = value << Integer.SIZE - (start + length);

        return valueAfterShiftToTheLeft >> Integer.SIZE - length;
    }

    /**
     * Extrait du vecteur de 32 bits 'value' la plage de 'length' bits
     * commençant au bit d'index 'start', qu'elle interprète comme une
     * valeur non signée en complément à deux, ou lève IllegalArgumentException
     * si la plage est invalide est également si length vaut 32.
     * @param value vecteur de bits
     * @param start index auquel le bit extrait commence
     * @param length longueur du bits qu'on veut extraire
     * @return valeur extraite interpretee comme une valeur non signée en complément à deux
     */
    public static int extractUnsigned(int value, int start, int length){
        Preconditions.checkArgument(start >= 0
                && length >= 0
                && start < Integer.SIZE
                && length < Integer.SIZE);

        int valueAfterShiftToTheLeft = value << Integer.SIZE - (start + length);

        return valueAfterShiftToTheLeft >>> Integer.SIZE - length;
    }

}