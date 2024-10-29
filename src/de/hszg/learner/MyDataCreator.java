package de.hszg.learner;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;


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
		saveManyFeatureVektores(f, "C:\\3500");

	}

	MyDataCreator(String FolderPathnameImages, String FolderPathnameData, LearnerType LernerTypeInput ){
		//Bilder aus Ordner einlesen und FeatureVektor erstellen und in eine Datei in FolderPathnameData abspeichern 

		// Verarbeite alle Bilder in einem Ordner
		File folder = new File(FolderPathnameImages);
		File[] listOfFiles = folder.listFiles();

		List<FeatureVector> AllFeatureVektores=new LinkedList<>();

		if (listOfFiles == null) {
			System.out.println("Der Ordner der Bilddaten ist leer oder existiert nicht.");
		}
		else{
			// Schleife durch alle Dateien im Ordner
			for (File file : listOfFiles) {
				if (file.isFile() && isImageFile(file)) {
					System.out.println("Verarbeite Bild: " + file.getName());
					System.out.println(file.getAbsolutePath());

					// Bild laden
					Mat image = Imgcodecs.imread(file.getAbsolutePath());

					if (!image.empty()) {
						//Bild wird verarbeitet und Featurevektor wird der Liste hinzugefügt
						ImageProcessor MyImageProcessor=new ImageProcessor(image);
						FeatureVector MyFeatureVektor=MyImageProcessor.getOutput_Vektor();
						AllFeatureVektores.add(MyFeatureVektor);
					} else {
						System.out.println("Fehler beim Laden des Bildes: " + file.getName());
					}
				}
			}
		}

		//Schreibt alle Featurevektoren in eine .dat-Datei
		saveManyFeatureVektores(AllFeatureVektores.toArray(new FeatureVector[0]), "C:\\3500");
		
		
		//FolderPathenameData in ausgewählen Lerner laden/lernen
		
		//Lerner mit Testdaten Evaluieren 
		
		//Ausgabe der Testergebnisse

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

	public void saveManyFeatureVektores(FeatureVector[] ArrayOfAllFeaturevektores,String FolderPathnameData) {
		String filename = FolderPathnameData+"\\VektorData"+generateDateTimeforFilename()+".dat";

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


	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		new MyDataCreator("C:\\3500","C:\\3500",LearnerType.EagerLerning);  //Testdaten erstellen
		//new MyDataCreator(); //DummyData Creater
	}
}
