package de.hszg.learner;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class Evaluator {
	/** the percentage (between 0 und 100) of vectors from the data to be used for the test, all others are training
	*/
	private static int testRate = 40;
	/*
	//Philips Pfade
	//Datei welche Featurevektoren enthält und eingelesen wird
	static String filename_feature_vektor ="C:\\Users\\Philipp\\Documents\\Master\\Maschinelles Lernen\\VektorData_20241030_105858.dat";

	//Datei in welche die Ergebnisse einer Laufzeit eingelesen werden
	//String filename_results_statistics = "C:\\Users\\Philipp\\Documents\\Master\\Maschinelles Lernen\\ergebnisse_"+MyDataCreator.generateDateTimeforFilename()+".csv";
	String filename_results_statistics = "C:\\Users\\Philipp\\Documents\\Master\\Maschinelles Lernen\\ergebniss.csv";
*/

	//Markus Pfade
	//Datei welche Featurevektoren enthält und eingelesen wird
	static String filename_feature_vektor ="C:\\Code\\Daten\\VektorData_20241030_011840.dat";

	//Datei in welche die Ergebnisse einer Laufzeit eingelesen werden
	//String filename_results_statistics = "C:\\Users\\Philipp\\Documents\\Master\\Maschinelles Lernen\\ergebnisse_"+MyDataCreator.generateDateTimeforFilename()+".csv";
	String filename_results_statistics = "C:\\Code\\Daten\\ergebniss.csv";




	public Evaluator(String filename) {
		List<FeatureVector> vectors = readData(filename);
		Learner learner = new LazyLerningKNN_Lerner(3);

		// TODO: folgendes muss zur Evaluierung mehrfach ausgef�hrt werden
		// Verschiedene Teilmengen finden und Verschiedene Reihenfolgen festlegen,
		// wie oft, das h�ngt vom gew�nschten Vertrauensintervall ab
		int i=0;

		do{
			vectors = mixData(vectors, i);					//shuffle mit i als Seed
			List<List<FeatureVector>> sets = extractTrainingData(vectors); //Aufteilung der Featurevektordaten in Test und Trainingsdaten

			Vector<Integer> result_lern=new Vector<>();		//Vektor mit Werten bezüglich des Lernprozesses
			result_lern.add(0, vectors.size());		//Vektor 0 = Gesamtanzahl aller Datensätze
			result_lern.add(1, sets.get(0).size());	//Vektor 1 = Anzahl der Trainingsdaten
			result_lern.add(2, sets.get(1).size());	//Vektor 2 = Anzahl der Testdaten
			result_lern.add(3,i);						//Vektor 3 = N eines Durchganges


			learner.learn(sets.get(0));
			Vector<Integer> result_classify = evaluate(sets.get(1),learner);
			evalResult(result_lern, result_classify);

			i++;
		}while(i<3); //TODO: eine andere Abbruchbedingung verwenden



	}
	/**
	 * Evaluate the result from the test for output or furthjer considerations
	 * @param result_lern Ergebnissvektor der Lernfunktion mit Einstellungsparametern
	 * @param result_classify Ergebnissvektor des Klassifizierungsprozesses, welche die Grundlage für eine statistische Auswertung bilden
	 */
	private void evalResult(Vector<Integer>result_lern, Vector<Integer> result_classify) {
		// TODO hier muss mehr Auswertung passieren, insbes: Vertrauensintervalle etc
		System.out.println("Learning result: \n correct: "+result_classify.get(0)+"\n unknown: "+result_classify.get(1)+"\n wrong: "+result_classify.get(2));

		// Angenommene Werte für richtig, falsch und unbekannt klassifizierte Bilder
		int richtig = result_classify.get(0);  // Beispielwert für richtig klassifizierte Bilder
		int falsch = result_classify.get(1);    // Beispielwert für falsch klassifizierte Bilder
		int unbekannt = result_classify.get(2); // Beispielwert für unbekannt klassifizierte Bilder

		// Gesamtanzahl der Bilder berechnen
		int gesamt = richtig + falsch + unbekannt;
		if (gesamt == 0) {
			System.out.println("Die Gesamtanzahl der Bilder darf nicht 0 sein.");
			return;
		}

		// Konstante für 95%-Konfidenzintervall (z-Wert = 1.96)
		double zWert = 1.96;

		// Berechnungen
		double erfolgsrate = berechneErfolgsrate(richtig, gesamt);
		double durchschnittlicherFehler = berechneDurchschnittlichenFehler(falsch, gesamt);
		double standardabweichung = berechneStandardabweichung(erfolgsrate, gesamt);
		double[] konfidenzintervall = berechneKonfidenzintervall(erfolgsrate, zWert, standardabweichung);

		// Ausgabe der Ergebnisse
		System.out.printf("Erfolgsrate (richtig klassifizierte Bilder): %.4f%n", erfolgsrate);
		System.out.printf("Durchschnittlicher Fehler (falsch klassifizierte Bilder): %.4f%n", durchschnittlicherFehler);
		System.out.printf("Standardabweichung: %.4f%n", standardabweichung);
		System.out.printf("Konfidenzintervall: [%.4f, %.4f]%n", konfidenzintervall[0], konfidenzintervall[1]);

		// Pfad zur CSV-Datei ist in Klasse globale definiert
		schreibeStatistikInCsv(filename_results_statistics,result_lern,gesamt, erfolgsrate, durchschnittlicherFehler, standardabweichung, konfidenzintervall);

	}
	/** evaluate the learner with a given test set. 
	 * 
	 * @param list: The set of test examples containing the correct concept
	 * @param learner: The learner to be tests
	 * 
	 * @return a vector containing the test results: success, unknown, false
	 */
	 private Vector<Integer> evaluate(List<FeatureVector> list, Learner learner) {
		int success=0;int unknown=0;int fault =0;
		for(FeatureVector fv : list){
			Concept c = learner.classify(fv);
			if(c.equals(Concept.Unknown)) unknown++;
			else if(c.equals(fv.getConcept())) success++;
			else fault++;
				
		}
		Vector<Integer> res = new Vector<>();
		res.add(0,success);
		res.add(1,unknown);
		res.add(2,fault);
		return res;
	}
/**
 * 
 * @param vectors a list of vectors
 * @return list containing the same vectors as parameter but 
 * (usually) in different order
 */
	private List<FeatureVector> mixData(List<FeatureVector> vectors, int seedValue) {
		Collections.shuffle(vectors, new Random(seedValue));
		return vectors;
	}

	/**
	 * Split the set of festure vectors in a set of traing data and a set of test data.
	 * For representative results it is essential to mix the order of vectors 
	 * before splitting the set
	 * 
	 * @param vectors :a List fo Feature Vectors we can use for the test
	 * @return a List containt two Lists: first the training data, second the test data they are disjunct subsets of vector 
	 *
	 */
	private List<List<FeatureVector>> extractTrainingData(
			List<FeatureVector> vectors) {
		List<List<FeatureVector>> result = new LinkedList<>();
		List<FeatureVector> trainingData = new LinkedList<>();
		List<FeatureVector> testData = new LinkedList<>();
		
		int cut = (int) (testRate/100.0 * vectors.size());
		trainingData.addAll(vectors.subList(0,cut));
		testData.addAll(vectors.subList(cut+1, vectors.size()));
		
		result.add(trainingData);
		result.add(testData);
		return result;
	}

	/** read data from file
	 * 
	 * @param filename the file with this name should contain a serialized List<FeatureVector> containt all the data
	 * @return all the data
	 */
	private List<FeatureVector> readData(String filename) {
		List<FeatureVector> vectors = null;
		try{
			ObjectInputStream in = new ObjectInputStream(new BufferedInputStream( new FileInputStream(filename)));
			vectors = (List<FeatureVector>) in.readObject();
			in.close();
		}catch(Throwable t){
			System.out.println("Could not read Data from file: "+filename);
			System.exit(1);
		}
		return vectors;
	}

	/** startet das Programm mit den hinterlegten Trainingsdaten
	 */
	public static void main(String[] args){
		//filename_feature_vektor ist als static String innerhalb der Klasse definiert
		new Evaluator(filename_feature_vektor);
	}

	// Methode zur Berechnung der Erfolgsrate (richtig klassifizierte Bilder / alle Bilder)
	public static double berechneErfolgsrate(int richtig, int gesamt) {
		return (double) richtig / gesamt;
	}

	// Methode zur Berechnung des durchschnittlichen Fehlers (falsch klassifizierte Bilder / alle Bilder)
	public static double berechneDurchschnittlichenFehler(int falsch, int gesamt) {
		return (double) falsch / gesamt;
	}

	// Methode zur Berechnung der Standardabweichung für eine Binomialverteilung
	public static double berechneStandardabweichung(double erfolgsrate, int gesamt) {
		return Math.sqrt(erfolgsrate * (1 - erfolgsrate) / gesamt);
	}

	// Methode zur Berechnung des Konfidenzintervalls
	public static double[] berechneKonfidenzintervall(double erfolgsrate, double zWert, double standardabweichung) {
		double untereGrenze = erfolgsrate - zWert * standardabweichung;
		double obereGrenze = erfolgsrate + zWert * standardabweichung;

		// Sicherheitskontrolle, damit das Konfidenzintervall zwischen 0 und 1 bleibt
		untereGrenze = Math.max(0, untereGrenze);
		obereGrenze = Math.min(1, obereGrenze);

		return new double[]{untereGrenze, obereGrenze};
	}

	public static void schreibeStatistikInCsv(
			String dateiPfad,
			Vector<Integer> result_lern,
			int gesamtanzahl, //Addition aller Auswertedaten
			double erfolgsrate,
			double durchschnittlicherFehler,
			double standardabweichung,
			double[] konfidenzintervall) {

		// Format für Datum und Uhrzeit
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd; HH:mm:ss");
		String zeitstempel = LocalDateTime.now().format(formatter);

		// CSV-Zeile im gewünschten Format erstellen
		String zeile = String.format("%s; %d; %d; %d; %d; %d; %.4f; %.4f; %.4f; [%.4f; %.4f]%n",
				zeitstempel,
				result_lern.get(0),gesamtanzahl,result_lern.get(1), result_lern.get(2), result_lern.get(3),
				erfolgsrate, durchschnittlicherFehler, standardabweichung,
				konfidenzintervall[0], konfidenzintervall[1]);

		try {
			// Überprüfen, ob die Datei existiert
			File datei = new File(dateiPfad);
			boolean istDateiNeu = !datei.exists();

			// Datei im Append-Modus öffnen
			try (FileWriter writer = new FileWriter(dateiPfad, true)) {
				// Falls die Datei neu ist, schreibe die Kopfzeile
				if (istDateiNeu) {
					//writer.write("Datum; Uhrzeit; Erfolgsrate; Durchschnittlicher Fehler; Standardabweichung; Konfidenzintervall\n");
					writer.write("Datum; Uhrzeit;Gesamtanzahl; Gesamtanzahl_Add; Anz_Trainingsdaten; Anz_Testdaten; n-Runde; Erfolgsrate; Durchschnittlicher Fehler; Standardabweichung; Konfidenzintervall\n");
				}
				// Schreibe die Datenzeile
				writer.write(zeile);

				System.out.println("Ergebnisse wurden erfolgreich in die CSV-Datei geschrieben.");
			}
		} catch (IOException e) {
			System.out.println("Fehler beim Schreiben in die CSV-Datei: " + e.getMessage());
		}
	}
}
