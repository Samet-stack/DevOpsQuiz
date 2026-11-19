import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests de bout en bout : on joue de vraies parties en remplaçant le clavier (System.in)
 * par un texte, et on lit ce que l'application affiche.
 * Couvre « calcul du score » et « saisie invalide » du sujet.
 * Les scores sont écrits dans un dossier temporaire (propriété devopsquiz.data).
 */
class QuizPartieTest {

    @TempDir
    Path dossier;

    /** Lance DevOpsQuiz.main avec les saisies données et renvoie tout ce qui est affiché. */
    private String jouer(String saisies) {
        InputStream ancienneEntree = System.in;
        PrintStream ancienneSortie = System.out;
        PrintStream ancienneErreur = System.err;
        String ancienDossier = System.getProperty("devopsquiz.data");
        ByteArrayOutputStream capture = new ByteArrayOutputStream();
        PrintStream flux = new PrintStream(capture, true, StandardCharsets.UTF_8);
        try {
            System.setProperty("devopsquiz.data", dossier.toString());
            System.setIn(new ByteArrayInputStream(saisies.getBytes(StandardCharsets.UTF_8)));
            System.setOut(flux);
            System.setErr(flux);
            DevOpsQuiz.main(new String[0]);
        } finally {
            System.setIn(ancienneEntree);
            System.setOut(ancienneSortie);
            System.setErr(ancienneErreur);
            if (ancienDossier == null) {
                System.clearProperty("devopsquiz.data");
            } else {
                System.setProperty("devopsquiz.data", ancienDossier);
            }
        }
        return capture.toString(StandardCharsets.UTF_8);
    }

    private static final int TOTAL = BanqueQuestions.getParCategorie("Docker").size();

    /** Les questions sont mélangées à chaque partie : on répond donc toujours la même lettre. */
    private String memeReponse(char lettre) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < TOTAL; i++) {
            sb.append(lettre).append('\n');
        }
        return sb.toString();
    }

    /** Nombre de questions Docker dont la bonne réponse est cette lettre (calcul indépendant du jeu). */
    private int nombreDeBonnes(char lettre) {
        int n = 0;
        for (Question q : BanqueQuestions.getParCategorie("Docker")) {
            if (q.getBonneReponse() == lettre) {
                n++;
            }
        }
        return n;
    }

    @Test
    void leScoreAfficheEstLeNombreDeBonnesReponses() {
        for (char lettre : new char[] {'A', 'B', 'C', 'D'}) {
            String sortie = jouer("1\nAlice\n5\n" + memeReponse(lettre) + "4\n");

            assertTrue(sortie.contains("Score : " + nombreDeBonnes(lettre) + "/" + TOTAL),
                "lettre " + lettre);
        }
    }

    @Test
    void lesQuatreLettresReunisRepartissentExactementToutesLesQuestions() {
        int somme = 0;
        for (char lettre : new char[] {'A', 'B', 'C', 'D'}) {
            somme += nombreDeBonnes(lettre);
        }
        assertEquals(TOTAL, somme);
    }

    @Test
    void lesScoresDeLaPartieSontSauvegardesAvecLePseudo() throws IOException {
        jouer("1\nAlice\n5\n" + memeReponse('A') + "4\n");

        String contenu = Files.readString(dossier.resolve("scores.csv"), StandardCharsets.UTF_8);
        assertTrue(contenu.startsWith("Alice;" + nombreDeBonnes('A') + ";" + TOTAL + ";"));
    }

    @Test
    void lesScoresSauvegardesApparaissentDansLeMenuDesMeilleursScores() {
        String sortie = jouer("1\nAlice\n5\n" + memeReponse('A') + "3\n4\n");

        assertTrue(sortie.contains("Meilleurs scores"));
        assertTrue(sortie.contains("Alice"));
    }

    @Test
    void saisieInvalideDansLeMenuNePlantePasEtEstTracee() {
        String sortie = jouer("abc\n9\n4\n");

        assertTrue(sortie.contains("[ERROR] Saisie incorrecte : abc"));
        assertTrue(sortie.contains("[ERROR] Choix invalide : 9"));
        assertTrue(sortie.contains("bientôt"));
    }

    @Test
    void reponseInvalidePendantLeQuizEstRedemandeeSansPerdreDeQuestion() {
        String sortie = jouer("1\nEve\n5\nZ\n" + memeReponse('A') + "4\n");

        assertTrue(sortie.contains("[ERROR] Réponse quiz invalide : Z"));
        assertTrue(sortie.contains("Score : " + nombreDeBonnes('A') + "/" + TOTAL));
    }

    @Test
    void pseudoVideDevientAnonyme() {
        String sortie = jouer("1\n\n5\n" + memeReponse('A') + "4\n");

        assertTrue(sortie.contains("Joueur : " + ScoreManager.PSEUDO_PAR_DEFAUT));
    }
}
