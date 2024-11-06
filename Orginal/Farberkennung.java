
package de.hszg.learner;

class Farberkennung{

/*
Dies ist die Implementierung von Dana Müller zur Farberkennung 
es war gewünscht, diese für Aufgabe 1 zur Verfügung zu stellen, damit der Arbeitsaufwand dort nicht zu groß ist
das ist ok, weil Farberkennung kein KI Thema ist 

TODO: das als statische Methode oder so in das Framework für die Verkehrszeichenerkennung einbauen

*/
// argb ist Wert der Methode getRGB(x,y) des BufferedImage

private String getColorOfPixel(int argb) {
		int alpha = (argb >> 24) & 0xff;
		int red = (argb >> 16) & 0xff;
		int green = (argb >> 8) & 0xff;
		int blue = (argb) & 0xff;
		Farbe f = new Farbe(red, green, blue);
		float luminance = (red * 0.2126f + green * 0.7152f + blue * 0.0722f) / 255;
		if (luminance >= 0.5f) {
			// bright color
			return f.getFarbeHell();
		} else {
			// dark color
			return f.getFarbeDunkel();
		}

	}




public class Farbe {

	public int r;
	public int g;
	public int b;

	public Farbe(int r, int g, int b) {
		this.r = r;
		this.g = g;
		this.b = b;
	}


	public String getFarbeHell() {
		if(Math.abs(r - g) < 10 && Math.abs(g - b) < 10 && Math.abs(r - b) < 10 && r>245){
			return "unbekannt";
		}
		if ((r > g + 20 && r > b + 20) && (Math.abs(g - b) < 40)) {
			return "Rot";
		} else if ((r >= g && r > b) && (g - b > 40)) {
			return "Gelb";
		}
		else if (Math.abs(r - g) < 30 && Math.abs(g - b) < 30 && Math.abs(r - b) < 30) {
			//System.out.println(r+" "+g+" "+b);
			return "Schwarz";
		} else if (r < g && g < b && r>170) {
			//System.out.println(r+" "+g+" "+b);
			return "Blau";
		}

		return "unbekannt";
	}

	public String getFarbeDunkel() {
		if ((r > g + 20 && r > b + 20) && (Math.abs(g - b) < 40)) {
			return "Rot";
		} else if ((r >= g && r > b) && (g - b > 40)) {
			return "Gelb";
		}
		else if (Math.abs(r - g) < 15 && Math.abs(g - b) < 15 && Math.abs(r - b) < 15 && r < 30) {
			return "Schwarz";
		} else if (r < g && g < b && r < 75) {
			return "Blau";
		}

		return "unbekannt";
	}

}

}