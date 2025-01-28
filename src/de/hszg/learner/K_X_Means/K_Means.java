package de.hszg.learner.K_X_Means;


import java.awt.*;
import java.io.IOException;
import java.util.List;

public class K_Means {

    public static void main(String[] args) {
        try {
            //String csvFilePath = "D:\\1.2_Master\\IdeaProjects\\ML-3\\src\\de\\hszg\\learner\\ergebnisse\\VektorData_20250121_193812_S7xZ7.csv";// Pfad zu den FV
            //String outputPath = "D:\\1.2_Master\\IdeaProjects\\ML-3\\src\\de\\hszg\\learner\\ergebnisse\\kmeans_results.csv";// Pfad zur Ausgabe-CSV-Datei der Cluster
            int randomSeed = 42; //Seed
            double trainSplitRatio = 0.8;   // einstellung wie viele der gesamtdaten zu trainingsdaten werden

            String outputPath = "src/de/hszg/learner/ergebnisse/kmeans_results.csv";
            String csvFilePath = "src/de/hszg/learner/ergebnisse/VektorData_20250125_154632_S4xZ4.csv";
            String resultPath = "src\\de\\hszg\\learner\\ergebnisse/K_Means_Result.csv";


            //String csvData = new String(Files.readAllBytes(Paths.get(outputPath)));
            // Ausgabe des Inhalts
            //System.out.println(csvData);

            DataLoader dataLoader = new DataLoader(csvFilePath, randomSeed);
            DataLoader.DataSet dataSet = dataLoader.loadData(trainSplitRatio);

            /**
            // Zugriff auf Trainings- und Testdaten
            System.out.println("Trainingsdaten:");
            System.out.println(dataSet.trainData);
            System.out.println("Trainingslabels:");
            System.out.println(dataSet.trainLabels);

            System.out.println("Testdaten:");
            System.out.println(dataSet.testData);
            System.out.println("Testlabels:");
            System.out.println(dataSet.testLabels);
             **/


            // K-Means konfigurieren und ausführen
            K_Means_Modell K_Means_Modell = new K_Means_Modell(12,   150, 42);
            K_Means_Modell.run(dataSet.trainData,dataSet.trainLabels, outputPath,dataSet.testLabels,dataSet.testData);

            // Cluster laden
            ClusterCSVLoader clusterLoader = new ClusterCSVLoader(outputPath);
            List<ClusterCSVLoader.Cluster> clusters = clusterLoader.loadClusters();

            // Evaluator initialisieren
            Evaluator evaluator = new Evaluator(dataSet.testData, dataSet.testLabels, clusters,resultPath);
            // Evaluation durchführen
            evaluator.evaluate();
            } catch (IOException e) {
                System.err.println("Fehler beim Laden der Dateien: " + e.getMessage());
            }


    }
}


