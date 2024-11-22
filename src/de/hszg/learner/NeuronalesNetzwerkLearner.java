package de.hszg.learner;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class NeuronalesNetzwerkLearner implements Learner{
    private Perceptron[] perceptrons;
    private int anz_perceptron;

    public NeuronalesNetzwerkLearner(int inputSize) {
        anz_perceptron=3;
        perceptrons = new Perceptron[anz_perceptron];
        double learningRate = 0.01;

        for (int i = 0; i < anz_perceptron; i++) {
            perceptrons[i] = new Perceptron(inputSize, learningRate);
        }
    }

    @Override
    public Vector<Integer> learn(List<FeatureVector> trainingSet) {
        int epochs= 400;
        for ( int i = 0; i < epochs; i++) {  //Anzahl der Durchgänge
            for (int j = 0; j < trainingSet.size(); j++) { //Für jedes Bild
                FeatureVector input = trainingSet.get(j);
                boolean[] targetOutputArray= input.getArrayOfConcept();

                for (int k = 0; k < perceptrons.length; k++) {
                    //Wenn Ziel und ergebnisvektor unterschiedlich sind: Lernen
                    if ( perceptrons[k].output(input)!=targetOutputArray[k]) {
                        perceptrons[k].train(input,targetOutputArray[k]);
                    }
                }
            }
        }

        Vector<Integer> res = new Vector<>();
        res.add(0, trainingSet.size()); //Anzahl aller gelernten Feature-Vektoren
        res.add(1, epochs); //Anzahl Epochen
        res.add(2, anz_perceptron); //Anzahl Percepton
        res.add(3, trainingSet.getFirst().getNumFeatures()); //Anzahl Features je Bild
        res.add(4, trainingSet.getFirst().getSizePerImage()); //Sollte das Gleiche sein
        res.add(5, trainingSet.getFirst().getGridCols());
        res.add(6, trainingSet.getFirst().getGridRows());
        return res;
    }

    @Override
    public Vector<Concept> classify(FeatureVector f) {
        boolean[] outputs = new boolean[perceptrons.length];

        for (int i = 0; i < perceptrons.length; i++) {
            outputs[i]=perceptrons[i].output(f);
        }


        //Rückgabevektor
        Vector<Concept> res = new Vector<>();
        res.addFirst(BitArrayToConcept(outputs)); //Ermittelter Vektor ,
        res.add(1, f.getConcept()); //Hinterlegter richtiges Verkehrszeichen
        return res;
    }

    public static boolean[] ConceptToOutputArray(Concept concept){
        int counter= concept.ordinal();     //Enum in Zahl
        boolean[] bitArray = new boolean[3]; //Ausgabe Array
        for (int i = 0; i < 3; i++) {
            bitArray[2 - i] = (counter & (1 << i)) != 0; // Prüft, ob das i-te Bit gesetzt ist
        }
        return bitArray;

        /*
        *   Concept: Unknown                Bitfolge: [false, false, false]
            Concept: Vorfahrt_von_Rechts    Bitfolge: [false, true, false]
            Concept: Stoppschild            Bitfolge: [false, false, true]
            Concept: Fahrtrichtung_links    Bitfolge: [true, false, false]
            Concept: Fahrtrichtung_rechts   Bitfolge: [true, false, true]
            Concept: Vorfahrt_gewähren      Bitfolge: [false, true, true]
            Concept: Vorfahrtsstraße        Bitfolge: [true, true, false]
        * */
    }

    //Umkehrfunktion zu ConceptToOutputArray
    public static Concept BitArrayToConcept(boolean[] bitArray) {
        // Umwandlung des Bit-Arrays in eine Ganzzahl,
        // Ganzzahl kann in Concept transformiert werden

        int count = 0;
        for (int i = 0; i < 3; i++) {
            if (bitArray[i]) {
                count |= (1 << (2 - i)); // Setzt das entsprechende Bit
            }
        }

        // Rückgabe des entsprechenden Enum-Werts
        return Concept.values()[count];
    }
}
