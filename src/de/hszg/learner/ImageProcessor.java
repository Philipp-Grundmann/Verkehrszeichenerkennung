package de.hszg.learner;

import org.opencv.core.Mat;

/**
 * Klasse welche jegliche Nachbearbeitung von Bildern übernimmt, z.B. Erstellung von Feature Vektor
 * Jede Instanz der Klasse ist für die Verarbeitung von einem einzellen Bild verantwortlich.
 */
public class ImageProcessor {
    boolean save_crop=false;
    FeatureVector Output_Vektor;



    //Constuructor MAT als Input -> Ziel: Alle Feature Vektoren als Output
    ImageProcessor(Mat image){

       //TODO Vektor hinzufügen
    }

    FeatureVector getOutput_Vektor(){
        return Output_Vektor;
    }

}
