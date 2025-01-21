package de.hszg.learner;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;


public class MyDataCreator {

	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		//loadFilesByFolder("C:\\Verkehrszeichen",3);

		/*//Pfad Philipp Laptop
		new MyDataCreator(
				"C:\\Users\\Philipp\\Documents\\Master\\Maschinelles Lernen\\Verkehrszeichen",
				"C:\\Users\\Philipp\\Documents\\Master\\Maschinelles Lernen\\Verkehrszeichenerkennung\\ergebnisse 14.01.2025",
				7,7);*/
		//Pfad Philipp Festrechner
		new MyDataCreator(
				"D:\\1.2_Master\\IdeaProjects\\Verkehrszeichen",
				".\\ergebnisse",
				7,7);
		//Pfad Vincent
		/*new MyDataCreator(
				"C:\\Users\\Philipp\\Documents\\Master\\Maschinelles Lernen\\Verkehrszeichen",
				"C:\\Users\\Philipp\\Documents\\Master\\Maschinelles Lernen\\Verkehrszeichenerkennung\\ergebnisse 14.01.2025",
				7,7);*/
	}


	//Idee für Eigenen Data Creater, erstellt Daten basierend auf einem Ordner
	MyDataCreator(String FolderPathnameImages, String FolderPathnameData, int gridCols, int gridRows){
		//Bilder aus Ordner einlesen und FeatureVektor erstellen und in eine Datei in FolderPathnameData abspeichern 

		// Verarbeite alle Bilder in einem Ordner
		//File folder = new File(FolderPathnameImages);
		//File[] listOfFiles = folder.listFiles();

		List<FeatureVector> AllFeatureVektores=new LinkedList<>();
		//Map<File, Concept> fileToConceptMap = loadFilesByFolder(FolderPathnameImages, 10);
		Map<File, Concept> fileToConceptMap = loadFilesByFolder(FolderPathnameImages, 600);
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
		// Beispiel für die Verwendung der Methode

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
			case "FAHRTRICHTUNG_LINKS": 	return Concept.Fahrtrichtung_links;
			case "VORFAHRTSSTRASSE": 		return Concept.Vorfahrtsstraße;
			case "STOP": 					return Concept.Stoppschild;
			case "VORFAHRT_GEWAEHREN": 		return  Concept.Vorfahrt_gewähren;
			case "FAHRTRICHTUNG_RECHTS":	return Concept.Fahrtrichtung_rechts;
			case "VORFAHRT_VON_RECHTS":		return Concept.Vorfahrt_von_Rechts;
			default:
				System.out.println("Das Verkehrszeichen konnte nicht Identifiziert werden!");
				return Concept.Unknown;
		}
	}



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

	public void saveManyFeatureVektores(FeatureVector[] ArrayOfAllFeaturevektores,String FolderPathnameData, String nametag) {
		String filename = FolderPathnameData+"\\VektorData"+generateDateTimeforFilename()+nametag+".dat";

		List<FeatureVector> res = new LinkedList<>();
		for(FeatureVector fv : ArrayOfAllFeaturevektores) res.add(fv);
		try{
			ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(filename)));
			out.writeObject(res);
			out.close();
			// Beispiel für die Verwendung der Methode

		}catch(Throwable t){
			System.out.println("DummyDataCreator: Could not create DummyData.dat");
			t.printStackTrace();
		}

		String outputPath = "D:\\1.2_Master\\IdeaProjects\\ML-3\\ergebnisse\\features_without_concept.csv";
		writeToCSV(outputPath, ArrayOfAllFeaturevektores);

	}


	public static File[] shuffleArrayWithSeed(File[] files, int randomSeed) {
		List<File> filesList = new ArrayList<>(Arrays.asList(files));
		Collections.shuffle(filesList, new Random(randomSeed));
		return filesList.toArray(new File[0]);
	}



	public static void writeToCSV(String outputPath, FeatureVector[] arrayOfAllFeatureVectors) {
		try (PrintWriter writer = new PrintWriter(new FileWriter(outputPath))) {
			// Schreibe die Header-Zeile (kannst du je nach den Feature-Vektoren anpassen)
			writer.println("Feature1, Feature2, Feature3, ..., FeatureN"); // Beispiel-Header

			// Iteriere durch alle Feature-Vektoren im Array
			for (FeatureVector vector : arrayOfAllFeatureVectors) {
				double[] features = vector.getFeatureVektor(); // Alle Features des Vektors
				StringBuilder sb = new StringBuilder();

				// Füge die Features in die Zeile ein, getrennt durch Kommas
				for (double feature : features) {
					sb.append(feature).append(",");
				}

				// Entferne das letzte Komma und schreibe die Zeile in die CSV-Datei
				sb.setLength(sb.length() - 1); // Entferne das letzte Komma
				writer.println(sb.toString());
			}
		} catch (IOException e) {
			System.out.println("Fehler beim Schreiben der CSV-Datei: " + e.getMessage());
		}
	}
}
