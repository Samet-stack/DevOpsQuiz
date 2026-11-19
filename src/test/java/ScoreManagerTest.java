import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ScoreManagerTest {

    @TempDir
    Path dossier;

    private ScoreManager manager() {
        return new ScoreManager(dossier.resolve("data").resolve("scores.csv"));
    }

    @Test
    void sauvegarderEcritPseudoScoreEtDate() throws IOException {
        ScoreManager m = manager();
        m.sauvegarder("Alice", 8, 10, LocalDateTime.of(2026, 11, 19, 10, 30, 0));

        List<ScoreManager.Entree> tous = m.lireTous();
        assertEquals(1, tous.size());
        assertEquals("Alice", tous.get(0).pseudo());
        assertEquals(8, tous.get(0).score());
        assertEquals(10, tous.get(0).total());
        assertEquals(LocalDateTime.of(2026, 11, 19, 10, 30, 0), tous.get(0).date());
    }

    @Test
    void classementTrieLesMeilleursScoresEnPremier() throws IOException {
        ScoreManager m = manager();
        m.sauvegarder("Bob", 5, 10, LocalDateTime.of(2026, 11, 19, 9, 0, 0));
        m.sauvegarder("Alice", 9, 10, LocalDateTime.of(2026, 11, 19, 10, 0, 0));
        m.sauvegarder("Chloe", 7, 10, LocalDateTime.of(2026, 11, 19, 11, 0, 0));

        List<ScoreManager.Entree> top = m.meilleurs(10);
        assertEquals("Alice", top.get(0).pseudo());
        assertEquals("Chloe", top.get(1).pseudo());
        assertEquals("Bob", top.get(2).pseudo());
    }

    @Test
    void classementLimiteAuNombreDemande() throws IOException {
        ScoreManager m = manager();
        for (int i = 0; i < 5; i++) {
            m.sauvegarder("J" + i, i, 10, LocalDateTime.of(2026, 11, 19, 10, i, 0));
        }
        assertEquals(3, m.meilleurs(3).size());
    }

    @Test
    void egaliteDepartageeParLaPartieLaPlusAncienne() throws IOException {
        ScoreManager m = manager();
        m.sauvegarder("Recent", 8, 10, LocalDateTime.of(2026, 11, 20, 8, 0, 0));
        m.sauvegarder("Ancien", 8, 10, LocalDateTime.of(2026, 11, 19, 8, 0, 0));

        assertEquals("Ancien", m.meilleurs(2).get(0).pseudo());
    }

    @Test
    void pseudoVideDevientAnonyme() throws IOException {
        ScoreManager m = manager();
        m.sauvegarder("   ", 4, 10);
        m.sauvegarder(null, 6, 10);

        for (ScoreManager.Entree e : m.lireTous()) {
            assertEquals(ScoreManager.PSEUDO_PAR_DEFAUT, e.pseudo());
        }
    }

    @Test
    void pseudoAvecSeparateurNeCasseJamaisLeFichier() throws IOException {
        ScoreManager m = manager();
        m.sauvegarder("Eve;99;10;hack", 3, 10);

        List<ScoreManager.Entree> tous = m.lireTous();
        assertEquals(1, tous.size());
        assertEquals(3, tous.get(0).score());
        assertEquals("Eve 99 10 hack", tous.get(0).pseudo());
    }

    @Test
    void scoreHorsLimitesEstRefuse() {
        ScoreManager m = manager();
        assertThrows(IllegalArgumentException.class, () -> m.sauvegarder("Zoe", 11, 10));
        assertThrows(IllegalArgumentException.class, () -> m.sauvegarder("Zoe", -1, 10));
        assertThrows(IllegalArgumentException.class, () -> m.sauvegarder("Zoe", 5, 0));
    }

    @Test
    void ligneCorrompueEstIgnoreeSansPlanterLApplication() throws IOException {
        Path fichier = dossier.resolve("scores.csv");
        Files.writeString(fichier,
            "Alice;8;10;2026-11-19 10:30:00\n"
            + "ligne n'importe quoi\n"
            + "Bob;abc;10;2026-11-19 10:31:00\n"
            + "\n"
            + "Chloe;6;10;2026-11-19 10:32:00\n",
            StandardCharsets.UTF_8);

        List<ScoreManager.Entree> tous = new ScoreManager(fichier).lireTous();
        assertEquals(2, tous.size());
        assertEquals("Alice", tous.get(0).pseudo());
        assertEquals("Chloe", tous.get(1).pseudo());
    }

    @Test
    void fichierAbsentDonneUneListeVide() {
        assertTrue(manager().lireTous().isEmpty());
        assertTrue(manager().meilleurs(10).isEmpty());
    }

    @Test
    void lireLigneRenvoieNullPourUneLigneInvalide() {
        assertNull(ScoreManager.lireLigne("pas;assez"));
        assertNull(ScoreManager.lireLigne("Alice;12;10;2026-11-19 10:30:00"));
    }

    @Test
    void affichageDuClassementListeLesJoueurs() throws IOException {
        ScoreManager m = manager();
        m.sauvegarder("Alice", 9, 10, LocalDateTime.of(2026, 11, 19, 10, 0, 0));

        ByteArrayOutputStream sortie = new ByteArrayOutputStream();
        m.afficherMeilleurs(new PrintStream(sortie, true, StandardCharsets.UTF_8), 10);
        String texte = sortie.toString(StandardCharsets.UTF_8);

        assertTrue(texte.contains("Alice"));
        assertTrue(texte.contains("9/10"));
    }

    @Test
    void affichageSansScoreAfficheUnMessage() {
        ByteArrayOutputStream sortie = new ByteArrayOutputStream();
        manager().afficherMeilleurs(new PrintStream(sortie, true, StandardCharsets.UTF_8), 10);

        assertTrue(sortie.toString(StandardCharsets.UTF_8).contains("Aucun score"));
    }
}
