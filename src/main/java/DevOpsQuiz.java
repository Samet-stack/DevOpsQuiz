import java.util.Scanner;

public class DevOpsQuiz {

    public static void main(String[] args) {
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
                    System.out.println("Le quiz arrive bientôt...");
                    break;
                case 2:
                    afficherRegles();
                    break;
                case 3:
                    System.out.println("Aucun score enregistré pour le moment.");
                    break;
                case 4:
                    System.out.println("À bientôt !");
                    quitter = true;
                    break;
            }
        }

        sc.close();
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
            } catch (NumberFormatException e) {
                System.out.print("Entrez un chiffre entre " + min + " et " + max + " : ");
            }
        }
    }

    private static void afficherMenu() {
        System.out.println();
        System.out.println("DEVOPS QUIZ");
        System.out.println("1. Commencer un quiz");
        System.out.println("2. Consulter les règles");
        System.out.println("3. Afficher les meilleurs scores");
        System.out.println("4. Quitter");
        System.out.print("Votre choix : ");
    }

    private static void afficherRegles() {
        System.out.println();
        System.out.println("--- Règles du jeu ---");
        System.out.println("- Choisis une catégorie (Linux, Git/GitHub, Jenkins, Docker, DevOps) ou le quiz général.");
        System.out.println("- Tu réponds à 10 questions avec les choix A, B, C ou D.");
        System.out.println("- 1 point par bonne réponse.");
        System.out.println("- Ton score est sauvegardé à la fin avec ton pseudo et la date.");
    }
}
