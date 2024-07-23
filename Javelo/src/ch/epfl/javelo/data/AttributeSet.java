package ch.epfl.javelo.data;

import ch.epfl.javelo.Preconditions;

import java.util.*;

/**
 * Enregistrment qui représente un ensemble d'attributs OpenStreetMap
 * possède un unique attribut "bits" de type long qui représente le contenu
 * de l'ensemble au moyen d'un bit par valeur possible; c'est-à-dire que le bit
 * d'index b de cette valeur vaut 1 si et seulement si l'attribut b est contenu dans
 * l'enum Attribute
 *
 * @author Albert Fares (341018)
 * @author Etienne Asselin (340201)
 */


public record AttributeSet(long bits) {

    /**
     * L'enum Attribute contient 62 attributs, donc la valeur maximale de l'attribut bits est
     * de 62 1 aux positions de poids le plus faible
     */
    private static final long MAXIMUM_BITS_VALUE=(1L<<Attribute.COUNT)-1;

    /**
     * Constructeur compact qui lève une IllegalArgumentException si la valeur passée en arguement
     * contient un bit à 1 qui ne correspond à aucun attribut valide.
     * @param bits vecteur de bits qui represente le contenu de l'ensemble d'attributs OpenStreetMap voulu
     */
    public AttributeSet {
        Preconditions.checkArgument(bits >= 0 && bits <= MAXIMUM_BITS_VALUE);
    }

    /**
     * Construit un ensemble contenant un certain nombre d'attributs
      * @param attributes attributs de types Attribute (enum)
     * @return un ensemble contenant les attributs passes en parametres
     */
    public static AttributeSet of(Attribute ... attributes){
        long bits = 0;


        for (Attribute attribute : attributes){
            bits |= 1L << attribute.ordinal();
        }

        return new AttributeSet(bits);
    }

    /**
     * Verifie si l'ensemble content un certain attribut
     * @param attribute attribut pout lequel on veux verifier la condition
     * @return true si ensemble récepteur contient l'attribut donné et false sinon
     */
    public boolean contains(Attribute attribute){
        long mask = 1L << attribute.ordinal();

        return ((bits & mask) != 0);
    }

    /**
     * Verifie si l'ensemble récepteur intersect celui passe en argument
     * @param that ensemble pour lequel on veut verifier la condition
     * @return true si l'intersection de l'ensemble récepteur avec celui passé en argument n'est pas vide.
     */
    public boolean intersects(AttributeSet that){
        return (this.bits & that.bits) != 0;
    }

    /**
     * Redefinition de la methode toString qui permet d'afficher un ensemble
     * @return un String qui represente l'ensemble
     */
    @Override
    public String toString() {
        StringJoiner s = new StringJoiner(",","{","}");

        for (Attribute attribute : Attribute.values()){
            if (this.contains(attribute)){
                s.add(attribute.toString());
            }
        }

        return s.toString();
    }

}