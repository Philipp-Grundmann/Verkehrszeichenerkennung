package de.hszg.learner;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

public class Perceptron {
    private int randomseed= 3484;
    private double[] weights; //Gewichte welche zur Berechnung genutzt werden
    private double bias;
    private double lerningRate;

    public Perceptron(int inputs, double lerningRate) {
        weights=new double[inputs+1]; //Erstellt das Array für die Inputweights
        this.lerningRate=lerningRate;

        //füllt Weightsarray zufällig
        Random rand = new Random(randomseed);
        for (int i = 0; i < weights.length; i++) {
            weights[i] = rand.nextDouble(); // Gibt einen Wert zwischen 0.0 und 1.0 zurück
        }
        bias=rand.nextDouble(); //Setzt Bias

        System.out.println("Perzeptrn erezugt - Weigths vom Perzeptrn: "+weights+" Bias: "+bias);
    }

    public boolean output(FeatureVector f) {
        if (berechneSumme(f.getFeatureVektor())>0) {
            return true;
        }else return false;
    }

    // Berechnet die gewichtete Summe der Eingaben inkl. Bias
    private double berechneSumme(double[] VektorValues) {
        double sum = bias*weights[weights.length-1];
        for (int i = 0; i < VektorValues.length; i++) {
            sum += VektorValues[i] * weights[i];
        }
        return sum;
    }

    public void train(FeatureVector f, boolean target) {
        int output = booleanToInt(output(f));   //Berechnung des Perzeptrons-Outputs
        int error = booleanToInt(target) - output;            //Angenommener Ziel - Berechneter output

        // Gewichte und Bias aktualisieren (Minsky-Papert-Regel)
        for (int i = 0; i < weights.length-1; i++) {
            weights[i] += lerningRate * error * f.getFeatureValue(i);
        }
        bias += lerningRate * error;
    }

    public int booleanToInt(boolean input) {
        int res = 0;
        if (input) {
            res = 1;
        }
        return res;
    }





}
