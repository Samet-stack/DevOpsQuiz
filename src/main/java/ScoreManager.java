import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Sauvegarde et classement des scores (ticket 7).
 *
 * Format du fichier (une ligne par partie, séparateur ';') :
 *   pseudo;score;total;date
 *   Alice;8;10;2026-11-19 10:30:00
 *
 * Par défaut le fichier est data/scores.csv. Dans Docker, on monte un volume
 * sur le dossier data/ pour que les scores survivent à la suppression du conteneur.
 */
public class ScoreManager {

    public static final Path FICHIER_PAR_DEFAUT = Path.of("data", "scores.csv");
    public static final String PSEUDO_PAR_DEFAUT = "Anonyme";

    private static final DateTimeFormatter FORMAT_DATE =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Une ligne du fichier de scores. */
    public record Entree(String pseudo, int score, int total, LocalDateTime date) { }

    private final Path fichier;

    public ScoreManager() {
        this(fichierParDefaut());
    }

    /**
     * Dossier des scores : "data" par défaut, ou la valeur de la propriété système
     * devopsquiz.data (utilisée par les tests pour ne pas toucher au vrai fichier).
     */
    static Path fichierParDefaut() {
        String dossier = System.getProperty("devopsquiz.data");
        return dossier == null ? FICHIER_PAR_DEFAUT : Path.of(dossier, "scores.csv");
    }

    public ScoreManager(Path fichier) {
        this.fichier = fichier;
    }

    /** Enregistre une partie avec la date du jour. Appelé par le quiz à la fin d'une partie. */
    public void sauvegarder(String pseudo, int score, int total) throws IOException {
        sauvegarder(pseudo, score, total, LocalDateTime.now());
    }

    /** Variante avec date explicite (utile pour les tests). */
    public void sauvegarder(String pseudo, int score, int total, LocalDateTime date) throws IOException {
        if (total <= 0 || score < 0 || score > total) {
            throw new IllegalArgumentException("Score invalide : " + score + "/" + total);
        }
        String ligne = nettoyerPseudo(pseudo) + ";" + score + ";" + total + ";" + date.format(FORMAT_DATE);
        Path dossier = fichier.toAbsolutePath().getParent();
        if (dossier != null) {
            Files.createDirectories(dossier);
        }
        Files.writeString(fichier, ligne + System.lineSeparator(), StandardCharsets.UTF_8,
            StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    /** Lit toutes les parties. Les lignes illisibles sont ignorées (et signalées dans les logs). */
    public List<Entree> lireTous() {
        List<Entree> resultat = new ArrayList<>();
        if (!Files.exists(fichier)) {
            return resultat;
        }
        List<String> lignes;
        try {
            lignes = Files.readAllLines(fichier, StandardCharsets.UTF_8);
        } catch (IOException e) {
            erreur("Lecture des scores impossible : " + e.getMessage());
            return resultat;
        }
        for (String ligne : lignes) {
            if (ligne.isBlank()) {
                continue;
            }
            Entree entree = lireLigne(ligne);
            if (entree == null) {
                erreur("Ligne de score ignorée : " + ligne);
            } else {
                resultat.add(entree);
            }
        }
        return resultat;
    }

    /** Classement : meilleur score en premier, à égalité la partie la plus ancienne. */
    public List<Entree> meilleurs(int nombre) {
        List<Entree> toutes = lireTous();
        toutes.sort(Comparator
            .comparingDouble((Entree e) -> (double) e.score() / e.total()).reversed()
            .thenComparing(Entree::date));
        return toutes.subList(0, Math.min(nombre, toutes.size()));
    }

    /** Affiche le classement (menu 3 du quiz). */
    public void afficherMeilleurs(PrintStream sortie, int nombre) {
        List<Entree> top = meilleurs(nombre);
        sortie.println();
        sortie.println("--- Meilleurs scores ---");
        if (top.isEmpty()) {
            sortie.println("Aucun score enregistré pour le moment.");
            return;
        }
        int rang = 1;
        for (Entree e : top) {
            sortie.printf("%2d. %-15s %d/%d  (%s)%n",
                rang++, e.pseudo(), e.score(), e.total(), e.date().format(FORMAT_DATE));
        }
    }

    // ---- utilitaires -----------------------------------------------------

    /** Log d'erreur sur la sortie d'erreur (visible avec docker logs). */
    private static void erreur(String message) {
        String date = LocalDateTime.now().format(FORMAT_DATE);
        System.err.println("[" + date + "] [ERROR] ScoreManager : " + message);
    }

    static String nettoyerPseudo(String pseudo) {
        if (pseudo == null) {
            return PSEUDO_PAR_DEFAUT;
        }
        String propre = pseudo.replace(';', ' ').replace('\n', ' ').replace('\r', ' ').trim();
        return propre.isEmpty() ? PSEUDO_PAR_DEFAUT : propre;
    }

    static Entree lireLigne(String ligne) {
        String[] morceaux = ligne.split(";", -1);
        if (morceaux.length != 4) {
            return null;
        }
        try {
            int score = Integer.parseInt(morceaux[1].trim());
            int total = Integer.parseInt(morceaux[2].trim());
            if (total <= 0 || score < 0 || score > total) {
                return null;
            }
            LocalDateTime date = LocalDateTime.parse(morceaux[3].trim(), FORMAT_DATE);
            return new Entree(morceaux[0].trim(), score, total, date);
        } catch (NumberFormatException | DateTimeParseException e) {
            return null;
        }
    }
}
