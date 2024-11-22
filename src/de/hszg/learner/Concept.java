package de.hszg.learner;

public enum Concept {
    Unknown,
    Stoppschild,
    Vorfahrt_von_Rechts,
    Vorfahrt_gewähren,
    Fahrtrichtung_links,
    Fahrtrichtung_rechts,
    Vorfahrtsstraße,
    Unknown1;
}


/*
* Concept: Unknown Bitfolge: [false, false, false]
Concept: Vorfahrt_von_Rechts Bitfolge: [false, true, false]
Concept: Stoppschild Bitfolge: [false, false, true]
Concept: Fahrtrichtung_links Bitfolge: [true, false, false]
Concept: Fahrtrichtung_rechts Bitfolge: [true, false, true]
Concept: Vorfahrt_gewähren Bitfolge: [false, true, true]
Concept: Vorfahrtsstraße Bitfolge: [true, true, false]
* */