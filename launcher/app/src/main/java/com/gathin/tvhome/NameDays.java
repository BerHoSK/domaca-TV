package com.gathin.tvhome;

import java.util.Calendar;

/**
 * Domáca TV: slovenské meniny na dnešný deň, rovnaký kalendár ako v Rodinári.
 * Bez siete, len tabuľka. Prázdny reťazec = v ten deň nemá meniny nikto (napr. 1. január).
 */
final class NameDays {

    /** [mesiac 0-11][deň 0-30]. */
    private static final String[][] SK = {
            {"", "Alexandra, Karina", "Daniela", "Drahoslav", "Andrea", "Antónia", "Bohuslava", "Severín", "Alexej", "Dáša", "Malvína", "Ernest", "Rastislav", "Radovan", "Dobroslav", "Kristína", "Nataša", "Bohdana", "Drahomíra, Mário", "Dalibor", "Vincent", "Zora", "Miloš", "Timotej", "Gejza", "Tamara", "Bohuš", "Alfonz", "Gašpar", "Ema", "Emil"},
            {"Tatiana", "Erika, Erik", "Blažej", "Veronika", "Agáta", "Dorota", "Vanda", "Zoja", "Zdenko", "Gabriela", "Dezider", "Perla", "Arpád", "Valentín", "Pravoslav", "Ida, Liana", "Miloslava", "Jaromír", "Vlasta", "Lívia", "Eleonóra", "Etela", "Roman, Romana", "Matej", "Frederik, Frederika", "Viktor", "Alexander", "Zlatica", "Radomír"},
            {"Albín", "Anežka", "Bohumil, Bohumila", "Kazimír", "Fridrich", "Radoslav, Radoslava", "Tomáš", "Alan, Alana", "Františka", "Branislav, Bruno", "Angela, Angelika", "Gregor", "Vlastimil", "Matilda", "Svetlana", "Boleslav", "Ľubica", "Eduard", "Jozef", "Víťazoslav", "Blahoslav", "Beňadik", "Adrián", "Gabriel", "Marián", "Emanuel", "Alena", "Soňa", "Miroslav", "Vieroslava", "Benjamín"},
            {"Hugo", "Zita", "Richard", "Izidor", "Miroslava", "Irena", "Zoltán", "Albert", "Milena", "Igor", "Július", "Estera", "Aleš", "Justína", "Fedor", "Dana, Danica", "Rudolf", "Valér", "Jela", "Marcel", "Ervín", "Slavomír", "Vojtech", "Juraj", "Marek", "Jaroslava", "Jaroslav", "Jarmila", "Lea", "Anastázia"},
            {"", "Žigmund", "Galina", "Florián", "Lesana, Lesia", "Hermína", "Monika", "Ingrida", "Roland", "Viktória", "Blažena", "Pankrác", "Servác", "Bonifác", "Žofia", "Svetozár", "Gizela", "Viola", "Gertrúda", "Bernard", "Zina", "Júlia, Juliana", "Želmíra", "Ela", "Urban", "Dušan", "Iveta", "Viliam", "Vilma", "Ferdinand", "Petronela, Petrana"},
            {"Žaneta", "Xénia", "Karolína", "Lenka", "Laura", "Norbert", "Róbert", "Medard", "Stanislava", "Margaréta", "Dobroslava", "Zlatko", "Anton", "Vasil", "Vít", "Blanka, Bianka", "Adolf", "Vratislav", "Alfréd", "Valéria", "Alojz", "Paulína", "Sidónia", "Ján", "Tadeáš, Olívia", "Adriána", "Ladislav, Ladislava", "Beáta", "Peter, Pavol, Petra", "Melánia"},
            {"Diana", "Berta", "Miloslav", "Prokop", "Cyril, Metod", "Patrik, Patrícia", "Oliver", "Ivan", "Lujza", "Amália", "Milota", "Nina", "Margita", "Kamil", "Henrich", "Drahomír", "Bohuslav", "Kamila", "Dušana", "Iľja, Eliáš", "Daniel", "Magdaléna", "Oľga", "Vladimír", "Jakub", "Anna, Hana", "Božena", "Krištof", "Marta", "Libuša", "Ignác"},
            {"Božidara", "Gustáv", "Jerguš", "Dominik, Dominika", "Hortenzia", "Jozefína", "Štefánia", "Oskar", "Ľubomíra", "Vavrinec", "Zuzana", "Darina", "Ľubomír", "Mojmír", "Marcela", "Leonard", "Milica", "Elena, Helena", "Lýdia", "Anabela", "Jana", "Tichomír", "Filip", "Bartolomej", "Ľudovít", "Samuel", "Silvia", "Augustín", "Nikola, Nikolaj", "Ružena", "Nora"},
            {"Drahoslava", "Linda, Rebeka", "Belo", "Rozália", "Regína", "Alica", "Marianna", "Miriama", "Martina", "Oleg", "Bystrík", "Mária", "Ctibor", "Ľudomil", "Jolana", "Ľudmila", "Olympia", "Eugénia", "Konštantín", "Ľuboslav, Ľuboslava", "Matúš", "Móric", "Zdenka", "Ľuboš, Ľubor", "Vladislav", "Edita", "Cyprián", "Václav", "Michal, Michaela", "Jarolím"},
            {"Arnold", "Levoslav", "Stela", "František", "Viera", "Natália", "Eliška", "Brigita", "Dionýz", "Slavomíra", "Valentína", "Maximilián", "Koloman", "Boris", "Terézia", "Vladimíra", "Hedviga", "Lukáš", "Kristián", "Vendelín", "Uršuľa", "Sergej", "Alojzia", "Kvetoslava", "Aurel", "Demeter", "Sabína", "Dobromila", "Klára", "Šimon, Simona", "Aurélia"},
            {"Denis, Denisa", "", "Hubert", "Karol", "Imrich", "Renáta", "René", "Bohumír", "Teodor", "Tibor", "Martin, Maroš", "Svätopluk", "Stanislav", "Irma", "Leopold", "Agnesa", "Klaudia", "Eugen", "Alžbeta", "Félix", "Elvíra", "Cecília", "Klement", "Emília", "Katarína", "Kornel", "Milan", "Henrieta", "Vratko", "Ondrej, Andrej"},
            {"Edmund", "Bibiána", "Oldrich", "Barbora, Barbara", "Oto", "Mikuláš", "Ambróz", "Marína", "Izabela", "Radúz", "Hilda", "Otília", "Lucia", "Branislava, Bronislava", "Ivica", "Albína", "Kornélia", "Sláva", "Judita", "Dagmara", "Bohdan", "Adela", "Nadežda", "Adam, Eva", "", "Štefan", "Filoména", "Ivana, Ivona", "Milada", "Dávid", "Silvester"},
    };

    static String today() {
        return on(Calendar.getInstance());
    }

    static String on(Calendar c) {
        int m = c.get(Calendar.MONTH);
        int d = c.get(Calendar.DAY_OF_MONTH) - 1;
        if (m < 0 || m >= SK.length || d < 0 || d >= SK[m].length) return "";
        return SK[m][d];
    }

    private NameDays() {
    }
}
