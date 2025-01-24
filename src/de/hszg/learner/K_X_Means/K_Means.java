package de.hszg.learner.K_X_Means;

import java.io.IOException;

public class K_Means {

    public static void main(String[] args) {
        try {
            String csvFilePath = "C:\\temp\\feature_vectors.csv";
            int randomSeed = 42; //Seed
            double trainSplitRatio = 0.8;   // einstellung wie viele der gesamtdaten zu trainingsdaten werden

            DataLoader dataLoader = new DataLoader(csvFilePath, randomSeed);
            DataLoader.DataSet dataSet = dataLoader.loadData(trainSplitRatio);

            // Zugriff auf Trainings- und Testdaten
            System.out.println("Trainingsdaten:");
            System.out.println(dataSet.trainData);
            System.out.println("Trainingslabels:");
            System.out.println(dataSet.trainLabels);

            System.out.println("Testdaten:");
            System.out.println(dataSet.testData);
            System.out.println("Testlabels:");
            System.out.println(dataSet.testLabels);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


