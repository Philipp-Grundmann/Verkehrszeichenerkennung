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
		int[] ColorArray = new int[125];
		int DummyCorner = 1;

		FeatureVector[] f = new FeatureVector[6];
		f[0] = new MyFeatureVector(DummyCorner,ColorArray,Concept.Stoppschild);
		f[1] = new MyFeatureVector(DummyCorner,ColorArray,Concept.Stoppschild);
		f[2] = new MyFeatureVector(DummyCorner,ColorArray,Concept.Stoppschild);
		f[3] = new MyFeatureVector(DummyCorner,ColorArray,Concept.Stoppschild);
		f[4] = new MyFeatureVector(DummyCorner,ColorArray,Concept.Stoppschild);
		f[5] = new MyFeatureVector(DummyCorner,ColorArray,Concept.Stoppschild);
		
		List<FeatureVector> res = new LinkedList<>();
		for(FeatureVector fv : f) res.add(fv);
		try{
			ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(filename)));
			out.writeObject(res);
			out.close();
		}catch(Throwable t){
			System.out.println("DummyDataCreator: Could not create DummyData.dat");
			t.printStackTrace();
		}
	}

	MyDataCreator(String FolderPathnameImages, String FolderPathnameData, LearnerType LernerTypeInput ){
		//Bilder aus Ordner einlesen und FeatureVektor erstellen und in eine Datei in FolderPathnameData abspeichern 
		
		// Beispielpfad für Struktur: 
		//String folderPath = "C:\\Users\\Philipp\\Documents\\Master\\Maschinelles Lernen\\Verkehrszeichen\\Test";

		// Verarbeite alle Bilder im Ordner
		File folder = new File(FolderPathnameData);
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

	public static void main(String[] args) {
		new MyDataCreator();
	}
}
