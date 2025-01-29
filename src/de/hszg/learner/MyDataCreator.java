package de.hszg.learner;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;

import javax.swing.*;


public class MyDataCreator {

	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		//loadFilesByFolder("C:\\Verkehrszeichen",3);

		new MyDataCreator("D:\\1.2_Master\\IdeaProjects\\Verkehrszeichen","src/de/hszg/learner/ergebnisse", 4,4);  //Testdaten erstellen - hat funktioniert
	}
		//Idee für Eigenen Data Creater, erstellt Daten basierend auf einem Ordner
	MyDataCreator(String FolderPathnameImages, String FolderPathnameData, int gridCols, int gridRows){
		//Bilder aus Ordner einlesen und FeatureVektor erstellen und in eine Datei in FolderPathnameData abspeichern 

		// Verarbeite alle Bilder in einem Ordner
		//File folder = new File(FolderPathnameImages);
		//File[] listOfFiles = folder.listFiles();

		List<FeatureVector> AllFeatureVektores=new LinkedList<>();
		//Map<File, Concept> fileToConceptMap = loadFilesByFolder(FolderPathnameImages, 50);
		Map<File, Concept> fileToConceptMap = loadFilesByFolder(FolderPathnameImages, 500);
		int sum=fileToConceptMap.size();
		int progress=0;

		if (fileToConceptMap.isEmpty()) {
			System.out.println("Der Ordner der Bilddaten ist leer oder existiert nicht.");
		} else {
			for (Map.Entry<File, Concept> entry : fileToConceptMap.entrySet()) {
				File file = entry.getKey();
				Concept concept = entry.getValue();

				progress++;
				float progress_percent=(float) progress/sum*100;

				if (file.isFile() && isImageFile(file)) {
					System.out.printf("Fortschritt: %.2f%% (%d/%d) | Verarbeite Bild: %s | Konzept: %s%n", progress_percent, progress, sum, file.getName(), concept);

					//System.out.println(file.getAbsolutePath());

					Mat image = Imgcodecs.imread(file.getAbsolutePath());

					if (!image.empty()) {
						// Bild wird verarbeitet und Featurevektor wird der Liste hinzugefügt
						ImageProcessor myImageProcessor = new ImageProcessor(image,concept,gridCols,gridRows);
						FeatureVector myFeatureVector = myImageProcessor.getOutput_Vektor();
						System.out.println("Ausgabe Vektor: ");
						//System.out.print(myFeatureVector.getPrintVektorValue());
						AllFeatureVektores.add(myFeatureVector);
					} else {
						System.out.println("Fehler beim Laden des Bildes: " + file.getName());
							}
						}
					}
		}

		String nametag="_S"+gridCols+"xZ"+gridRows;

		//Schreibt alle Featurevektoren in eine .dat-Datei
		saveManyFeatureVektores(AllFeatureVektores.toArray(new FeatureVector[0]), FolderPathnameData, nametag);
		
		
		//FolderPathenameData in ausgewählen Lerner laden/lernen
		
		//Lerner mit Testdaten Evaluieren 
		
		//Ausgabe der Testergebnisse

	}


	//Rückgabestrukur {Concept1=[Filename1, Filename2, Filename3,...],Concept2=[Filename1, Filename2, Filename3,...],.. }
	public static Map<File, Concept> loadFilesByFolder(String rootFolderPath, int maxFiles) {
		Map<Concept, List<File>> filesByFolder = new HashMap<>();
		Map<File, Concept> fileToConceptMap = new HashMap<>(); //Wird benötigt für angepasste Ausgabe

		File rootFolder = new File(rootFolderPath);

		if (!rootFolder.isDirectory()) {
			System.err.println("Pfad ist kein Verzeichnis: " + rootFolderPath);
			return fileToConceptMap;
		}

		File[] mainFolders = rootFolder.listFiles(File::isDirectory);
		if (mainFolders != null) {
			for (File mainFolder : mainFolders) {
				List<File> filesList = new ArrayList<>();
				loadFilesRecursiv(mainFolder, filesList, maxFiles);
				filesByFolder.put(mainFolderName_StrToConcept(mainFolder.getName()), filesList); //MainFolder.GetName() -> Verarbeiten in
			}
		}
		System.out.println("Verarbeitete Dateien: "); //Systemausgabe über die verarbeiteten Dateien
		//System.out.println(filesByFolder);
		//Rückgabestrukur {Concept1=[Filename1, Filename2, Filename3,...],Concept2=[Filename1, Filename2, Filename3,...],.. }

		//Anpassen der Datenstruktur in [[Concept1, Filename1], [Concept1, Filename2], [Concept1, Filename3],...]
		for (Map.Entry<Concept, List<File>> entry : filesByFolder.entrySet()) {
			Concept concept = entry.getKey();        // Das Concept
			List<File> files = entry.getValue();     // Die Liste der Files

			for (File file : files) {
				fileToConceptMap.put(file, concept); // Datei als Schlüssel, Concept als Wert
			}
		}
		System.out.println(fileToConceptMap);
		System.out.println("Anzahl der Verarbeiten Dateien: "+fileToConceptMap.size());
		return fileToConceptMap;
	}


	public static Concept mainFolderName_StrToConcept(String MainFolderName){
		switch (MainFolderName){
			case "FAHRTRICHTUNG_LINKS": 	return Concept.FAHRTRICHTUNG_LINKS;
			case "VORFAHRTSSTRASSE": 		return Concept.VORFAHRTSSTRASSE;
			case "STOP": 					return Concept.STOP;
			case "VORFAHRT_GEWAEHREN": 	return  Concept.VORFAHRT_GEWAEHREN;
			case "FAHRTRICHTUNG_RECHTS":	return Concept.FAHRTRICHTUNG_RECHTS;
			case "VORFAHRT_VON_RECHTS":	return Concept.VORFAHRT_VON_RECHTS;
			default:
				System.out.println("Das Verkehrszeichen konnte nicht Identifiziert werden!");
				return Concept.Unknown;
		}
	}

	/*
	//Läd rekursiv die zu verarbeitenden Dateien aus einem Verzeichnis
	private static void loadFilesRecursively(File folder, List<File> filesList, int maxFiles) {
		if (maxFiles != -1 && filesList.size() >= maxFiles) return;



		File[] files = folder.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					loadFilesRecursively(file, filesList, maxFiles);
				} else {
					filesList.add(file);
					if (maxFiles != -1 && filesList.size() >= maxFiles) break;
				}
			}
		}
	} */



	private static void loadFilesRecursiv(File folder, List<File> filesList, int maxFiles) {
		File[] subFiles = folder.listFiles();

		subFiles=shuffleArrayWithSeed(subFiles, 6438);


		// Prüfen, ob der aktuelle Ordner ein Blatt-Ordner ist (keine Unterordner)
		if (subFiles != null && containsOnlyFiles(subFiles)) {
			int fileCount = 0;
			for (File file : subFiles) {
				if (file.isFile()) {
					filesList.add(file);
					fileCount++;
					if (maxFiles != -1 && fileCount >= maxFiles) {
						break;
					}
				}
			}
		} else if (subFiles != null) {
			// Ordner ist kein Blatt-Ordner, rekursiv fortfahren
			for (File subFile : subFiles) {
				if (subFile.isDirectory()) {
					loadFilesRecursiv(subFile, filesList, maxFiles);
				}
			}
		}
	}


	private static boolean containsOnlyFiles(File[] files) {
		for (File file : files) {
			if (file.isDirectory()) {
				return false;
			}
		}
		return true;
	}


	/**
	 * Zeit+Datumsformatierung für Dateinamen
	 * Funktion wurde mit ChatGPT generiert und übernommen
	 * @return
	 */
	public static String generateDateTimeforFilename() {
		// Aktuelles Datum und Uhrzeit abrufen
		LocalDateTime now = LocalDateTime.now();

		// Datumsformat definieren
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

		// Datum und Uhrzeit formatieren
		String dateTimeString = now.format(formatter);

		return "_" + dateTimeString;
	}

	/** Funktion zum Überprüfen, ob eine Datei ein Bild ist.
	 * Überprüfung erfolgt durch Abgleich der Dateiendung.
	 */
	public static boolean isImageFile(File file) {
		String fileName = file.getName().toLowerCase();
		return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png") || fileName.endsWith(".bmp");
	}

	public void saveManyFeatureVektores(FeatureVector[] arrayOfAllFeatureVectors, String folderPathnameData, String nametag) {
		// Pfad für die .dat-Datei
		String filename = folderPathnameData + "\\VektorData" + generateDateTimeforFilename() + nametag + ".dat";

		// Feature-Vektoren in .dat speichern
		List<FeatureVector> res = new LinkedList<>();
		Collections.addAll(res, arrayOfAllFeatureVectors);
		try (ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(filename)))) {
			out.writeObject(res);
			System.out.println("Feature-Vektoren erfolgreich in die Datei geschrieben: " + filename);
		} catch (Throwable t) {
			System.err.println("Fehler beim Speichern der Feature-Vektoren in der .dat-Datei");
			t.printStackTrace();
		}

		// Pfad für die CSV-Datei
		String csvFilePath = folderPathnameData + "\\VektorData" + generateDateTimeforFilename() + nametag + ".csv";

		// Feature-Vektoren in CSV speichern
		saveFeatureVectorToCSV(csvFilePath, arrayOfAllFeatureVectors);
	}


	public static File[] shuffleArrayWithSeed(File[] files, int randomSeed) {
		List<File> filesList = new ArrayList<>(Arrays.asList(files));
		Collections.shuffle(filesList, new Random(randomSeed));
		return filesList.toArray(new File[0]);
	}

	public static void saveFeatureVectorToCSV(String outputPath, FeatureVector[] featureVectors) {
		try (FileWriter writer = new FileWriter(outputPath, true)) {
			for (FeatureVector featureVector : featureVectors) {
				StringBuilder line = new StringBuilder();

				// Konzept (Label) hinzufügen
				line.append(featureVector.getConcept().toString());

				// Alle Feature-Werte hinzufügen
				for (int i = 0; i < featureVector.getNumFeatures(); i++) {
					line.append(",");
					line.append(featureVector.getFeatureValue(i));
				}

				// Neue Zeile hinzufügen
				line.append("\n");

				// In die Datei schreiben
				writer.write(line.toString());
			}
			System.out.println("Alle Feature-Vektoren erfolgreich in die CSV-Datei geschrieben: " + outputPath);
		} catch (IOException e) {
			System.err.println("Fehler beim Schreiben der Feature-Vektoren in die CSV-Datei: " + e.getMessage());
		}
	}
}
