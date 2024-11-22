package de.hszg.learner;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;


public class MyDataCreator {

	private static final String filename = "DummyData"+generateDateTimeforFilename()+".dat";

	/**
	 * Constuctor für DummyDaten
	 */
	MyDataCreator(){
		double[] ColorArray = new double[125];
		int DummyCorner = 1;

		FeatureVector[] f = new FeatureVector[6];
		f[0] = new MyFeatureVector(DummyCorner,ColorArray,Concept.Stoppschild);

		/*
		List<FeatureVector> res = new LinkedList<>();
		for(FeatureVector fv : f) res.add(fv);
		try{
			ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(filename)));
			out.writeObject(res);
			out.close();
		}catch(Throwable t){
			System.out.println("DummyDataCreator: Could not create DummyData.dat");
			t.printStackTrace();
		}*/
		saveManyFeatureVektores(f, "C:\\3500","DEMO");

	}


	//Idee für Eigenen Data Creater, erstellt Daten basierend auf einem Ordner
	MyDataCreator(String FolderPathnameImages, String FolderPathnameData, int gridCols, int gridRows){
		//Bilder aus Ordner einlesen und FeatureVektor erstellen und in eine Datei in FolderPathnameData abspeichern 

		// Verarbeite alle Bilder in einem Ordner
		//File folder = new File(FolderPathnameImages);
		//File[] listOfFiles = folder.listFiles();

		List<FeatureVector> AllFeatureVektores=new LinkedList<>();
		//Map<File, Concept> fileToConceptMap = loadFilesByFolder(FolderPathnameImages, 50);
		Map<File, Concept> fileToConceptMap = loadFilesByFolder(FolderPathnameImages, 50);
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
			case "209 - Fahrtrichtung links": 	return Concept.Fahrtrichtung_links;
			case "306 - Vorfahrtsstrasse": 		return Concept.Vorfahrtsstraße;
			case "206 - Stop": 					return Concept.Stoppschild;
			case "205 - Vorfahrt gewaehren": 	return  Concept.Vorfahrt_gewähren;
			case "209 - Fahrtrichtung rechts":	return Concept.Fahrtrichtung_rechts;
			case "102 - Vorfahrt von rechts":	return Concept.Vorfahrt_von_Rechts;
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

	public void saveManyFeatureVektores(FeatureVector[] ArrayOfAllFeaturevektores,String FolderPathnameData, String nametag) {
		String filename = FolderPathnameData+"\\VektorData"+generateDateTimeforFilename()+nametag+".dat";

		List<FeatureVector> res = new LinkedList<>();
		for(FeatureVector fv : ArrayOfAllFeaturevektores) res.add(fv);
		try{
			ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(filename)));
			out.writeObject(res);
			out.close();
		}catch(Throwable t){
			System.out.println("DummyDataCreator: Could not create DummyData.dat");
			t.printStackTrace();
		}
	}

	/*
	public static File[] shuffleArrayWithSeed(File[] files, int RandomSeed) {
		List<File> files_list= new ArrayList<>(Arrays.stream(files).toList());
		Collections.shuffle(files_list, new Random(RandomSeed));
		return (File[]) files_list.toArray();
	}*/


	public static File[] shuffleArrayWithSeed(File[] files, int randomSeed) {
		List<File> filesList = new ArrayList<>(Arrays.asList(files));
		Collections.shuffle(filesList, new Random(randomSeed));
		return filesList.toArray(new File[0]);
	}


	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		//loadFilesByFolder("C:\\Verkehrszeichen",3);

		new MyDataCreator("C:\\Verkehrszeichen","C:\\3500", 2,2);  //Testdaten erstellen - hat funktioniert
		new MyDataCreator("C:\\Verkehrszeichen","C:\\3500", 3,3);  //Testdaten erstellen - hat funktioniert
		new MyDataCreator("C:\\Verkehrszeichen","C:\\3500", 4,4);  //Testdaten erstellen - hat funktioniert
		new MyDataCreator("C:\\Verkehrszeichen","C:\\3500", 5,5);  //Testdaten erstellen - hat funktioniert
		new MyDataCreator("C:\\Verkehrszeichen","C:\\3500", 7,7);  //Testdaten erstellen - hat funktioniert
		new MyDataCreator("C:\\Verkehrszeichen","C:\\3500", 10,10);  //Testdaten erstellen - hat funktioniert
		new MyDataCreator("C:\\Verkehrszeichen","C:\\3500", 4,3);  //Testdaten erstellen - hat funktioniert
		new MyDataCreator("C:\\Verkehrszeichen","C:\\3500", 3,4);  //Testdaten erstellen - hat funktioniert
		new MyDataCreator("C:\\Verkehrszeichen","C:\\3500", 3,5);  //Testdaten erstellen - hat funktioniert
		new MyDataCreator("C:\\Verkehrszeichen","C:\\3500", 5,3);  //Testdaten erstellen - hat funktioniert
		//new MyDataCreator(); //DummyData Creater
	}
}
