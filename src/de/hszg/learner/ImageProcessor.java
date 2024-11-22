package de.hszg.learner;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Klasse welche jegliche Nachbearbeitung von Bildern übernimmt, z.B. Erstellung von Feature Vektor
 * Jede Instanz der Klasse ist für die Verarbeitung von einem einzellen Bild verantwortlich.
 */
public class ImageProcessor {
    boolean save_crop_Image=false;
    boolean save_corner_Marker=false;

    // Rasterparameter definieren
    int gridRows; // Anzahl der Zeilen
    int gridCols; // Anzahl der Spalten
    int CountFields;

    int corner=0;
    double[] AlleFarbvektoren;
    Concept c; //Concept des Bildes

    FeatureVector Output_Vektor;

    //Constuructor MAT als Input -> Ziel: Alle Feature Vektoren als Output
    ImageProcessor(Mat image, Concept concept,int gridCols, int gridRows){

        this.gridRows=gridRows;
        this.gridCols=gridCols;
        CountFields=gridCols*gridRows;
        AlleFarbvektoren=new double[CountFields];

        c=concept;
        if (image == null) {
            return; //nur zur sicherheit
        }

        Mat croppedImage = removeWhiteBorder(image);

        if (croppedImage == null) {
            return;//nur zur sicherheit
        }

        if (save_crop_Image) {
            saveImage("C:\\temp\\cropped_image"+MyDataCreator.generateDateTimeforFilename()+".bmp", croppedImage);
        }

        //
        //Eckenerkennung, mit Mitteln von OpenCV auf Graustufenbild
        //
        /*
        Mat grayImage = convertToGrayscale(croppedImage); // Bild in Graustufen konvertieren
        Mat edges = detectEdgesWithCanny(grayImage, 100, 200); // Kanten mit Canny-Edge-Detection erkennen
        MatOfPoint cornersMatOfPoint = detectCorners(edges, 100, 0.3, 300); // Eckenerkennung basierend auf den Kanten
        Point[] cornerPoints = cornersMatOfPoint.toArray();
        corner=cornerPoints.length; //Erkannt
         */
        //TODO Erkennung überarbeiten
        corner=0;

        //System.out.println("Anzahl erkannter ecken: "+corner);

        // Anzahl der erkannten Ecken ausgeben
        //printCornerCount(cornersMatOfPoint,"feature_vectors.csv");


        if (save_corner_Marker) {
            //Ecken in Bild markieren
           // for (Point corner : cornerPoints) {
            //    Imgproc.circle(croppedImage, corner, 5, new Scalar(0, 0, 255), 2);  // Ecken rot markieren
           // }

            // Ausgabe des Bildes mit eingezeichneten Ecken
            saveImage("C:\\temp\\corner_image"+MyDataCreator.generateDateTimeforFilename()+".bmp", croppedImage);
        }

        // Bildgröße und Raster analysieren (normalizedImage, gridRows, gridCols)
        AlleFarbvektoren= analyzeImageInGrid(croppedImage, gridRows, gridCols);
        //ToDo Analyze Image Grid

        //Output_Vektor=new MyFeatureVector(corner,AlleFarbvektoren,c);

        Output_Vektor=new MyFeatureVector(AlleFarbvektoren, c,gridRows,gridCols );

        // Merkmalsvektor speichern
        // CloseFeatureVector("feature_vectors.csv");
    }

    // Funktion zum Entfernen der weißen Ränder
    public static Mat removeWhiteBorder(Mat image) {
        int top = 0, bottom = image.rows() - 1, left = 0, right = image.cols() - 1;
        int threshold = 240;  // Schwellwert für Weiß (näher an 255)

        double[] pixel = image.get(0, 0);
        threshold=(int) ((pixel[0]+pixel[1]+pixel[2])/3)-20;
        //System.out.println("Schwellwert für Croop:" + threshold);


        // Weiß von oben entfernen
        for (int row = 0; row < image.rows(); row++) {
            if (!isWhiteRow(image, row, threshold)) {
                top = row;
                break;
            }
        }

        // Weiß von unten entfernen
        for (int row = image.rows() - 1; row >= 0; row--) {
            if (!isWhiteRow(image, row, threshold)) {
                bottom = row;
                break;
            }
        }

        // Weiß von links entfernen
        for (int col = 0; col < image.cols(); col++) {
            if (!isWhiteColumn(image, col, threshold)) {
                left = col;
                break;
            }
        }

        // Weiß von rechts entfernen
        for (int col = image.cols() - 1; col >= 0; col--) {
            if (!isWhiteColumn(image, col, threshold)) {
                right = col;
                break;
            }
        }

        // Bild zuschneiden
        Rect roi = new Rect(left, top, right - left + 1, bottom - top + 1);

        return new Mat(image, roi); // Rückgabe des zugeschnittenen Bildes
    }

    // Prüfen, ob eine gesamte Zeile weiß ist
    public static boolean isWhiteRow(Mat image, int row, int threshold) {
        for (int col = 0; col < image.cols(); col++) {
            double[] pixel = image.get(row, col);  // RGB-Werte des Pixels
            if (!(pixel[0] >= threshold && pixel[1] >= threshold && pixel[2] >= threshold)) {
                return false;  // Nicht weiß
            }
        }
        return true;  // Ganze Zeile ist weiß
    }

    // Prüfen, ob eine gesamte Spalte weiß ist
    public static boolean isWhiteColumn(Mat image, int col, int threshold) {
        for (int row = 0; row < image.rows(); row++) {
            double[] pixel = image.get(row, col);  // RGB-Werte des Pixels
            if (!(pixel[0] >= threshold && pixel[1] >= threshold && pixel[2] >= threshold)) {
                return false;  // Nicht weiß
            }
        }
        return true;  // Ganze Spalte ist weiß
    }

    // Bild speichern
    public static void saveImage(String outputPath, Mat image) {
        Imgcodecs.imwrite(outputPath, image);
        System.out.println("Bild wurde gespeichert unter: " + outputPath);
    }

    public static Mat convertToGrayscale(Mat image) {
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
        return grayImage;
    }

    // Canny-Kantenerkennung
    public static Mat detectEdgesWithCanny(Mat grayImage, int lowThreshold, int highThreshold) {
        Mat edges = new Mat();
        Imgproc.Canny(grayImage, edges, lowThreshold, highThreshold);
        return edges;
    }

    // Ecken mit Shi-Tomasi Corner Detection erkennen
    public static MatOfPoint detectCorners(Mat edges, int maxCorners, double qualityLevel, double minDistance) {
        MatOfPoint corners = new MatOfPoint();
        Imgproc.goodFeaturesToTrack(edges, corners, maxCorners, qualityLevel, minDistance);
        return corners;
    }

    // Rasterbildanalyse mit OpenCV
    private static double[] analyzeImageInGrid(Mat image, int gridRows, int gridCols) {
        int width = image.width();
        int height = image.height();
        int cellWidth = width / gridCols;
        int cellHeight = height / gridRows;
        double[] AlleFarbVektoren= new double[gridCols*gridRows*5];
        int i = 0;

        // Iteriere über das Raster
        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridCols; col++) {
                // Bereich des aktuellen Rasterfeldes
                int startX = col * cellWidth;
                int startY = row * cellHeight;
                int endX = Math.min(startX + cellWidth, width);
                int endY = Math.min(startY + cellHeight, height);

                // Farbanteile für einen Rasterteil berechnen
                // ist der Farbvektor für einen Bildausschnitt mit den fünf Werten: Rot, Gelb, Blau, Weiß, Schwarz
                double[] FarbVektor = analyzeColorInGrid(image.submat(startY, endY, startX, endX));
                System.arraycopy(FarbVektor,0,AlleFarbVektoren,i*5,5);
                //System.out.println("Ausgabe aller Farbvektoren eines Bildes"+ Arrays.toString(AlleFarbVektoren));

                // Ausgabe des Feature-Vektors für das Rasterfeld
                // Funktion aktuelle nicht implimentier
                //printFeatureVector(featureVector, row, col);
                i++;

            }
        }
        return AlleFarbVektoren;
        //return null;
    }

    // Analyse der Farben im Rasterfeld und Erstellen des Feature-Vektors für ein Rasterfeld
    //
    private static double[] analyzeColorInGrid(Mat grid) {
        BufferedImage bufferedImage = matToBufferedImage(grid); // Konvertierung zu BufferedImage

        if (bufferedImage != null) {
            int totalPixels = bufferedImage.getWidth() * bufferedImage.getHeight();
            HashMap<String, Integer> colorCounts = new HashMap<>();

            colorCounts.put("Rot", 0);
            colorCounts.put("Gelb", 0);
            colorCounts.put("Blau", 0);
            colorCounts.put("Weiß", 0);
            colorCounts.put("Schwarz", 0);

            // Über alle Pixel des Rasters iterieren
            for (int y = 0; y < bufferedImage.getHeight(); y++) {
                for (int x = 0; x < bufferedImage.getWidth(); x++) {
                    int rgb = bufferedImage.getRGB(x, y); // Beispiel: ARGB-Wert des Pixels (0,0)
                    int alpha = (rgb >> 24) & 0xff;
                    int red = (rgb >> 16) & 0xff;
                    int green = (rgb >> 8) & 0xff;
                    int blue = (rgb) & 0xff;

                    classifyColor(red, green, blue, colorCounts);
                }
            }

            // Erstelle den Feature-Vektor basierend auf den Farbanteilen
            return normalizeColorVector(colorCounts, totalPixels);
        }
        return new double[5];
    }


    // Farbklassifizierung basierend auf den RGB-Werten
    private static void classifyColor(int red, int green, int blue, HashMap<String, Integer> colorCounts) {
        float luminance = (red * 0.2126f + green * 0.7152f + blue * 0.0722f) / 255;

        if (luminance >= 0.5f) {
            // bright color
            if (isRed(red, green, blue)) {
                colorCounts.put("Rot", colorCounts.get("Rot") + 1);
            } else if (isYellow(red, green, blue)) {
                colorCounts.put("Gelb", colorCounts.get("Gelb") + 1);
            } else if (isbrightBlue(red, green, blue)) {
                colorCounts.put("Blau", colorCounts.get("Blau") + 1);
            } else if (isbrightBlack(red, green, blue)) {
                colorCounts.put("Schwarz", colorCounts.get("Schwarz") + 1);
            } else if (isWhite(red, green, blue)) {
                colorCounts.put("Weiß", colorCounts.get("Weiß") + 1);
            };
        } else {
            // dark color
            if (isRed(red, green, blue)) {
                colorCounts.put("Rot", colorCounts.get("Rot") + 1);
            } else if (isYellow(red, green, blue)) {
                colorCounts.put("Gelb", colorCounts.get("Gelb") + 1);
            } else if (isdarkBlue(red, green, blue)) {
                colorCounts.put("Blau", colorCounts.get("Blau") + 1);
            } else if (isdarkBlack(red, green, blue)) {
                colorCounts.put("Schwarz", colorCounts.get("Schwarz") + 1);
            } else if (isWhite(red, green, blue)) {
                colorCounts.put("Weiß", colorCounts.get("Weiß") + 1);
            };
        }
    }


    // Erstellung des Feature-Vektors aus den Farbanteilen
    private static double[] normalizeColorVector(HashMap<String, Integer> colorCounts, int totalPixels) {
        double rotProportion = (double) colorCounts.get("Rot") / totalPixels *10;
        double gelbProportion = (double) colorCounts.get("Gelb") / totalPixels *10;
        double blauProportion = (double) colorCounts.get("Blau") / totalPixels*10;
        double weißProportion = (double) colorCounts.get("Weiß") / totalPixels*10;
        double schwarzProportion = (double) colorCounts.get("Schwarz") / totalPixels*10;

        // Feature-Vektor
        return new double[]{
                rotProportion,
                gelbProportion,
                blauProportion,
                weißProportion,
                schwarzProportion
        };
    }

        public static BufferedImage matToBufferedImage(Mat mat) {
            if (mat.empty()) {
                return null; // Rückgabe null, falls das Bild leer ist
            }

            // Standardmäßige RGB-Konvertierung für farbige Bilder
            if (mat.channels() == 3) {
                // Von BGR zu RGB konvertieren
                Mat matRGB = new Mat();
                Imgproc.cvtColor(mat, matRGB, Imgproc.COLOR_BGR2RGB);
                byte[] data = new byte[matRGB.channels() * matRGB.cols() * matRGB.rows()];
                matRGB.get(0, 0, data);
                BufferedImage image = new BufferedImage(matRGB.cols(), matRGB.rows(), BufferedImage.TYPE_3BYTE_BGR);
                image.getRaster().setDataElements(0, 0, matRGB.cols(), matRGB.rows(), data);
                return image;
            }
            // Für Graustufenbilder
            else if (mat.channels() == 1) {
                byte[] data = new byte[mat.cols() * mat.rows()];
                mat.get(0, 0, data);
                BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), BufferedImage.TYPE_BYTE_GRAY);
                image.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), data);
                return image;
            }
            return null; // Rückgabe null, falls ein anderes Format nicht unterstützt wird
        }

    // Dynamische Farbschwellenwerte basierend auf Helligkeit
    private static boolean isRed(int red, int green, int blue) {
        return (red > green + 20 && red > blue + 20) && (Math.abs(green - blue) < 30);
    }

    private static boolean isYellow(int red, int green, int blue) {
        return ((red >= green && red > blue) && (green - blue > 40));
    }

    private static boolean isbrightBlue(int red, int green, int blue) {
        return red < 120 && green < blue && blue > 180;
    }

    private static boolean isbrightBlack(int red, int green, int blue) {
        return Math.abs(red - green) < 30 && Math.abs(green - blue) < 30 && Math.abs(red - blue) < 30
                && red < 100 && green < 100 && blue < 100;
    }

    private static boolean isdarkBlue(int red, int green, int blue) {
        return red < green && green < blue && red < 75;
    }

    private static boolean isdarkBlack(int red, int green, int blue) {
        return Math.abs(red - green) < 15 && Math.abs(green - blue) < 15 && Math.abs(red - blue) < 30
                && red < 60 && green < 60 && blue < 60;
    }

    private static boolean isWhite(int red, int green, int blue) {
        return red > 200 && green > 200 && blue > 200;
    }







    FeatureVector getOutput_Vektor(){
        //System.out.println("Erzeugter Vektor - Concept: "+ c +" | Output-Vektor: "+Output_Vektor.toString());
        return Output_Vektor;
    }

}
