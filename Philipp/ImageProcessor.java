import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class ImageProcessor {

    static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    public static void main(String[] args) {
        // Pfad zum Ordner mit Bildern
        String folderPath = "C:\\Users\\Philipp\\Documents\\Master\\Maschinelles Lernen\\Verkehrszeichen\\Test";

        // Verarbeite alle Bilder im Ordner
        processFolder(folderPath);
    }

    // Funktion zum Verarbeiten aller Bilder in einem Ordner
    public static void processFolder(String folderPath) {
        File folder = new File(folderPath);
        File[] listOfFiles = folder.listFiles();

        if (listOfFiles == null) {
            System.out.println("Der Ordner ist leer oder existiert nicht.");
            return;
        }
        else{
            // Schleife durch alle Dateien im Ordner
            for (File file : listOfFiles) {
                if (file.isFile() && isImageFile(file)) {
                    System.out.println("Verarbeite Bild: " + file.getName());

                    // Bild laden
                    Mat image = Imgcodecs.imread(file.getAbsolutePath());

                    if (!image.empty()) {
                        //TODO:Funktion durch Classe ersetzen
                        processImage(image);
                    } else {
                        System.out.println("Fehler beim Laden des Bildes: " + file.getName());
                    }
                }
            }
        }
    }



    // Funktion zum Überprüfen, ob eine Datei ein Bild ist
    public static boolean isImageFile(File file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png") || fileName.endsWith(".bmp");
    }
    public static void processImage(Mat image){


    // Bild laden
    // Mat image = Imgcodecs.imread("C:\\Users\\Philipp\\Documents\\Master\\Maschinelles Lernen\\Verkehrszeichen\\X0Y0.jpg", Imgcodecs.IMREAD_UNCHANGED);

        Mat croppedImage = removeWhiteBorder(image);

        // Bild speichern
        saveImage("cropped_image.bmp", croppedImage);

        // Bild in Graustufen konvertieren
        Mat grayImage = convertToGrayscale(croppedImage);

        // Kanten mit Canny-Edge-Detection erkennen
        Mat edges = detectEdgesWithCanny(grayImage, 100, 200);

        // Eckenerkennung basierend auf den Kanten
        MatOfPoint corners = detectCorners(edges, 100, 0.3, 300);

        // Anzahl der erkannten Ecken ausgeben
        printCornerCount(corners,"feature_vectors.csv");

        // Ecken im Bild anzeigen
        drawCorners(image, corners);

        // Ausgabe des Bildes mit den gezeichneten Ecken
        saveImage("corners_with_canny.bmp", croppedImage);

        // Rasterparameter definieren
        int gridRows = 5; // Anzahl der Zeilen
        int gridCols = 5; // Anzahl der Spalten

        // Bildgröße und Raster analysieren (normalizedImage, gridRows, gridCols)
        analyzeImageInGrid(croppedImage, gridRows, gridCols);

        // Merkmalsvektor speichern
        CloseFeatureVector("feature_vectors.csv");

    }

    // Bild laden mit OpenCV
    private static Mat loadImage(String path, int imreadUnchanged) {
        return Imgcodecs.imread(path);
    }


    // Rasterbildanalyse mit OpenCV
    private static void analyzeImageInGrid(Mat image, int gridRows, int gridCols) {
        int width = image.width();
        int height = image.height();
        int cellWidth = width / gridCols;
        int cellHeight = height / gridRows;

        // Iteriere über das Raster
        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridCols; col++) {
                // Bereich des aktuellen Rasterfeldes
                int startX = col * cellWidth;
                int startY = row * cellHeight;
                int endX = Math.min(startX + cellWidth, width);
                int endY = Math.min(startY + cellHeight, height);

                // Farbanteile für das Raster berechnen
                double[] featureVector = analyzeColorInGrid(image.submat(startY, endY, startX, endX), row, col);

                // Ausgabe des Feature-Vektors für das Rasterfeld
                printFeatureVector(featureVector, row, col);

                // Merkmalsvektor speichern
                saveFeatureVector(featureVector, "feature_vectors.csv");
            }
        }
    }

    // Analyse der Farben im Rasterfeld und Erstellen des Feature-Vektors
    private static double[] analyzeColorInGrid(Mat grid, int row, int col) {
        BufferedImage bufferedImage = matToBufferedImage(grid); // Konvertierung zu BufferedImage
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

                classifyColor(red, green, blue,colorCounts);
            }
        }

        // Erstelle den Feature-Vektor basierend auf den Farbanteilen
        return createFeatureVector(colorCounts, totalPixels);
    }

    // Farbklassifizierung basierend auf den RGB-Werten
    private static void classifyColor(int red, int green, int blue,HashMap<String, Integer> colorCounts) {
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
    private static double[] createFeatureVector(HashMap<String, Integer> colorCounts, int totalPixels) {
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

    // Berechnung und Ausgabe des Feature-Vektors
    private static void printFeatureVector(double[] featureVector, int row, int col) {
        System.out.printf("Feature-Vector für Rasterfeld [%d, %d]:\n", row, col);
        System.out.printf("Rot: %.4f, Gelb: %.4f, Blau: %.4f, Weiß: %.4f, Schwarz: %.4f\n",
                featureVector[0], featureVector[1], featureVector[2], featureVector[3], featureVector[4]);
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


    // Methode zum Speichern der Merkmalsvektoren als CSV
    public static void saveFeatureVector(double[] featureVector, String fileName) {
        try (FileWriter writer = new FileWriter(fileName, true)) {
            for (int i = 0; i < featureVector.length; i++) {
                writer.append(Double.toString(featureVector[i]));
                if (i != featureVector.length - 1) {
                    writer.append(",");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Methode zum abschließen des VeatureVetores als CSV
    public static void CloseFeatureVector( String fileName) {
        try (FileWriter writer = new FileWriter(fileName, true)) {
            writer.append("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Funktion zum Entfernen der weißen Ränder
    public static Mat removeWhiteBorder(Mat image) {
        int top = 0, bottom = image.rows() - 1, left = 0, right = image.cols() - 1;
        int threshold = 240;  // Schwellwert für Weiß (näher an 255)

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


    // Bild in Graustufen umwandeln
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

    // Anzahl der Ecken ausgeben
    public static void printCornerCount(MatOfPoint corners,String fileName) {
        Point[] cornerPoints = corners.toArray();
        System.out.println("Anzahl der erkannten Ecken: " + cornerPoints.length);
        try (FileWriter writer = new FileWriter(fileName, true)) {
            writer.append(Integer.toString(cornerPoints.length));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Ecken im Bild zeichnen
    public static void drawCorners(Mat image, MatOfPoint corners) {
        Point[] cornerPoints = corners.toArray();
        for (Point corner : cornerPoints) {
            Imgproc.circle(image, corner, 5, new Scalar(0, 0, 255), 2);  // Ecken rot markieren
        }
    }

    // Bild speichern
    public static void saveImage(String outputPath, Mat image) {
        Imgcodecs.imwrite(outputPath, image);
        System.out.println("Bild mit erkannten Ecken wurde gespeichert: " + outputPath);
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
}

