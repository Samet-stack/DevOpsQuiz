import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class DevOpsQuiz {

    public static void main(String[] args) {
        ecrireLog("INFO", "Lancement de DevOpsQuiz");

        ScoreManager scoreManager = new ScoreManager();
        Scanner sc = new Scanner(System.in);
        boolean quitter = false;

        while (!quitter) {
            afficherMenu();
            int choix = lireChoix(sc, 1, 4);

            if (choix == -1) {
                break;
            }

            switch (choix) {
                case 1:
                    jouerPartie(sc, scoreManager);
                    break;
                case 2:
                    afficherRegles();
                    break;
                case 3:
                    scoreManager.afficherMeilleurs(System.out, 10);
                    break;
                case 4:
                    System.out.println("À bientôt !");
                    quitter = true;
                    break;
            }
        }

        sc.close();
        ecrireLog("INFO", "Fermeture de DevOpsQuiz");
    }

    private static void afficherMenu() {
        System.out.println();
        System.out.println("DEVOPS QUIZ (v1.1.0)");
        System.out.println("1. Commencer un quiz");
        System.out.println("2. Consulter les règles");
        System.out.println("3. Afficher les meilleurs scores");
        System.out.println("4. Quitter");
        System.out.print("Votre choix : ");
    }

    private static void jouerPartie(Scanner sc, ScoreManager scoreManager) {
        System.out.print("\nEntrez votre pseudonyme : ");
        if (!sc.hasNextLine()) {
            return;
        }
        String pseudo = sc.nextLine().trim();
        if (pseudo.isEmpty()) {
            pseudo = ScoreManager.PSEUDO_PAR_DEFAUT;
        }

        System.out.println("\nChoisissez une catégorie :");
        System.out.println("1. Quiz général (10 questions)");
        System.out.println("2. Linux (" + BanqueQuestions.getParCategorie("Linux").size() + " questions)");
        System.out.println("3. Git (" + BanqueQuestions.getParCategorie("Git").size() + " questions)");
        System.out.println("4. Jenkins (" + BanqueQuestions.getParCategorie("Jenkins").size() + " questions)");
        System.out.println("5. Docker (" + BanqueQuestions.getParCategorie("Docker").size() + " questions)");
        System.out.println("6. DevOps (" + BanqueQuestions.getParCategorie("DevOps").size() + " questions)");
        System.out.print("Votre choix (1-6) : ");

        int cat = lireChoix(sc, 1, 6);
        if (cat == -1) {
            return;
        }

        List<Question> questions = selectionnerQuestions(cat);
        if (questions.isEmpty()) {
            ecrireLog("ERROR", "Aucune question disponible pour la catégorie choisie.");
            return;
        }

        long debutMs = System.currentTimeMillis();
        int score = 0;
        int total = questions.size();

        for (int i = 0; i < total; i++) {
            Question q = questions.get(i);
            q.afficher(i + 1, total);

            char rep = lireReponse(sc);
            if (rep == '\0') {
                return;
            }
            if (q.estCorrecte(rep)) {
                System.out.println("Bonne réponse !");
                score++;
            } else {
                System.out.println("Mauvaise réponse. La bonne réponse était : " + q.getBonneReponse());
            }
            System.out.println("Explication : " + q.getExplication());
        }

        long dureeSec = (System.currentTimeMillis() - debutMs) / 1000;
        afficherResultat(pseudo, score, total, dureeSec);
        try {
            scoreManager.sauvegarder(pseudo, score, total);
            ecrireLog("INFO", "Fin de partie : pseudo=" + pseudo + ", score=" + score + "/" + total);
        } catch (IOException e) {
            ecrireLog("ERROR", "Erreur sauvegarde score : " + e.getMessage());
            System.out.println("Impossible de sauvegarder le score.");
        }
    }

    private static List<Question> selectionnerQuestions(int choix) {
        switch (choix) {
            case 2: return BanqueQuestions.getParCategorie("Linux");
            case 3: return BanqueQuestions.getParCategorie("Git");
            case 4: return BanqueQuestions.getParCategorie("Jenkins");
            case 5: return BanqueQuestions.getParCategorie("Docker");
            case 6: return BanqueQuestions.getParCategorie("DevOps");
            default: return BanqueQuestions.getQuizGeneral(10);
        }
    }

    private static void afficherResultat(String pseudo, int score, int total, long dureeSec) {
        double pct = (total > 0) ? (score * 100.0) / total : 0.0;
        System.out.println();
        System.out.println("--- Résultat final ---");
        System.out.printf("Joueur : %s | Score : %d/%d (%.1f%%) | Temps : %ds%n", pseudo, score, total, pct, dureeSec);
        if (pct >= 80) {
            System.out.println("Excellent travail !");
        } else if (pct >= 50) {
            System.out.println("Pas mal, continue comme ça.");
        } else {
            System.out.println("À retravailler avec les supports de cours.");
        }
    }

    private static int lireChoix(Scanner sc, int min, int max) {
        while (true) {
            if (!sc.hasNextLine()) {
                return -1;
            }
            String saisie = sc.nextLine().trim();
            try {
                int valeur = Integer.parseInt(saisie);
                if (valeur >= min && valeur <= max) {
                    return valeur;
                }
                System.out.print("Choix invalide, entrez un nombre entre " + min + " et " + max + " : ");
                ecrireLog("ERROR", "Choix invalide : " + saisie);
            } catch (NumberFormatException e) {
                System.out.print("Entrez un chiffre entre " + min + " et " + max + " : ");
                ecrireLog("ERROR", "Saisie incorrecte : " + saisie);
            }
        }
    }

    private static char lireReponse(Scanner sc) {
        while (true) {
            System.out.print("Votre réponse (A, B, C ou D) : ");
            if (!sc.hasNextLine()) {
                return '\0';
            }
            String saisie = sc.nextLine().trim().toUpperCase();
            if (saisie.length() == 1 && "ABCD".indexOf(saisie.charAt(0)) != -1) {
                return saisie.charAt(0);
            }
            System.out.println("Saisie invalide, entrez A, B, C ou D.");
            ecrireLog("ERROR", "Réponse quiz invalide : " + saisie);
        }
    }

    private static void afficherRegles() {
        System.out.println();
        System.out.println("--- Règles du jeu ---");
        System.out.println("- Choisis une catégorie (Linux, Git, Jenkins, Docker, DevOps) ou le quiz général.");
        System.out.println("- Tu réponds à des questions avec les choix A, B, C ou D.");
        System.out.println("- 1 point par bonne réponse.");
        System.out.println("- Ton score est sauvegardé à la fin avec ton pseudo et la date.");
    }

    public static void ecrireLog(String niveau, String message) {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String ligne = "[" + date + "] [" + niveau + "] " + message;
        if ("ERROR".equals(niveau)) {
            System.err.println(ligne);
        } else {
            System.out.println(ligne);
        }
        try {
            Files.createDirectories(Path.of("logs"));
            Files.writeString(Path.of("logs/quiz.log"), ligne + System.lineSeparator(),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Erreur ecriture log");
        }
    }
}
