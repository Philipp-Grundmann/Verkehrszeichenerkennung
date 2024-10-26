15.10.2024
        - Bild geladen --> in 5x5 Raster aufgeteilt --> Dominate Farbe jedes feldes bestimmt --> feature Vector geschrieben und in csv gespeichert. Dabei ist aufgefallen, dass wir nur weiße Felder erhalten haben.
        - Cropp funktion eingefügt, dass zuerst von außen her die Ränder weggeschnitten werden und dann erst das Raster aufgelegt wird.
    ToDo:
        - Ermittlung Dominanter Farbe überdenken
        - Farbbreichse für R G B festlegen, da durch minimale abweichungen eine schlechte Kategorisierung besteht
        - Cany Edge detection

16.10.2024
1.	Farberkennung angepasst auf Farbanteile Rot gelb blau schwarz weiß in einem Feld
2.	FeatureVector in der excel auf eine zeile geschrieben pro bild eine Zeile
3.	Corner detection eingebaut, die das Bild abspeichert und Ecken zählt.
4.	Anzahl schreiben in vector


19.10.2024
    - Farberkennung vermeintlich verbessert und auf RGBA angepasst
    - blau wird nach wie Vor nicht erkannt.



26.10.2024
    - Farberkennung verbessert
    - Cropping der bilder wieder eingebunden
    - Farberkennung von OpenCV auf Framework umgestellt