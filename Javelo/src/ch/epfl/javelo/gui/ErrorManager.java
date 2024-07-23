package ch.epfl.javelo.gui;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Gestionnsaire de l'affichage des messages d'erreur.
 *
 * @author Albert Fares (341018)
 * @author Etienne Asselin (340201)
 */
public final class ErrorManager {

    /**
     * vBox javaFX
     */
    private final VBox vBox;

    /**
     * Animation javaFX
     */
    private final Animation animation;

    /**
     * Constante qui represente la durée que prend le message d'erreur pour apparaitre
     */
    private final static Duration APPEARANCE_DURATION = new Duration(200);

    /**
     * Constante qui represente la durée que prend le message d'erreur pour disparaitre
     */
    private final static Duration FADE_DURATION = new Duration(500);

    /**
     * Constante qui represente l'opacite initiale du message d'erreur
     */
    private final static double INITIAL_OPACITY_FACTOR = 0;

    /**
     * Constante qui represente l'opacite finale du message d'erreur
     */
    private final static double OPACITY_FACTOR = 0.8;

    /**
     * Constante qyu represente la durée pour laquelle le message d'erreur s'affiche a l'ecran
     */
    private final static Duration PAUSE_DURATION = new Duration(2000);


    /**
     * Constructeur du message d'erreur
     */
    public ErrorManager(){
        vBox = new VBox();
        vBox.getStylesheets().add("error.css");

        vBox.setMouseTransparent(true);

        FadeTransition firstFadeTransition = new FadeTransition();

        firstFadeTransition.setFromValue(INITIAL_OPACITY_FACTOR);
        firstFadeTransition.setToValue(OPACITY_FACTOR);
        firstFadeTransition.setDuration(APPEARANCE_DURATION);

        FadeTransition secondFadeTransition = new FadeTransition();

        secondFadeTransition.setFromValue(OPACITY_FACTOR);
        secondFadeTransition.setToValue(INITIAL_OPACITY_FACTOR);
        secondFadeTransition.setDuration(FADE_DURATION);

        PauseTransition pauseTransition=new PauseTransition(PAUSE_DURATION);

        animation=new SequentialTransition(vBox,firstFadeTransition,pauseTransition,secondFadeTransition);

    }

    /**
     * Accesseur du panneau javaFX principal
     * @return le panneau JavaFx
     */
    public Pane pane() {
        return vBox;
    }

    /**
     * Methode outil qui permet d'afficher les messages d'erreur a l'ecran
     * @param string le message d'erreur
     */
    public void displayError(String string){

        vBox.getChildren().clear();
        animation.stop();
        Text text=new Text(string);
        vBox.getChildren().add(text);

        java.awt.Toolkit.getDefaultToolkit().beep();

        animation.play();
    }



}
