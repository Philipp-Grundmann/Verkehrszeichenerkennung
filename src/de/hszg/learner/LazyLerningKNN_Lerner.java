package de.hszg.learner;

import java.util.ArrayList;
import java.util.List;

public class LazyLerningKNN_Lerner implements Learner {
    private List<FeatureVector> trainingSet=new ArrayList<>();
    private int k; // Anzahl der Nachbarn

    public LazyLerningKNN_Lerner(int k) {
        this.k = k;
    }

    @Override
    public void learn(List<FeatureVector> trainingSet) {
        this.trainingSet = trainingSet;
    }

    @Override
    public Concept classify(FeatureVector example) {
        // Hier wird die Klassifizierung basierend auf den k nächsten Nachbarn durchgeführt

        List<Distance> distances = new ArrayList<>();
        for (FeatureVector fv : trainingSet) {
            double distance = calculateDistance(fv, example);
            distances.add(new Distance(fv, distance));
        }

        distances.sort((d1, d2) -> Double.compare(d1.distance, d2.distance));

        // Zähle die häufigste Klasse unter den k nächsten Nachbarn
        int[] classVotes = new int[Concept.values().length];

        for (int i = 0; i < k && i < distances.size(); i++) {
            FeatureVector neighbor = distances.get(i).featureVector;
            classVotes[neighbor.getConcept().ordinal()]++;
        }

        // Finde die Klasse mit den meisten Stimmen
        int maxVotes = -1;
        int bestClassIndex = -1;
        for (int i = 0; i < classVotes.length; i++) {
            if (classVotes[i] > maxVotes) {
                maxVotes = classVotes[i];
                bestClassIndex = i;
            }
        }

        return Concept.values()[bestClassIndex];
    }

    private double calculateDistance(FeatureVector fv1, FeatureVector fv2) {
        // Berechne die euklidische Distanz zwischen zwei Feature-Vektoren
        double sum = 0.0;
        //sum += Math.pow(fv1.getCornerCount() - fv2.getCornerCount(), 2);
        for (int i = 0; i < fv1.getNumFeatures(); i++) {
            sum += Math.pow(fv1.getFeatureValue(i) - fv2.getFeatureValue(i), 2);
        }
        return Math.sqrt(sum);
    }

    // Hilfsklasse für die Distanzberechnung
    private static class Distance {
        FeatureVector featureVector;
        double distance;

        Distance(FeatureVector featureVector, double distance) {
            this.featureVector = featureVector;
            this.distance = distance;
        }
    }
}

