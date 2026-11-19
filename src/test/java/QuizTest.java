import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class QuizTest {

    @Test
    void bonneReponseAcceptee() {
        Question q = new Question("Linux", "Commande répertoire courant ?", new String[]{"ls", "pwd", "cd", "ps"}, 'B', "pwd affiche le chemin courant.");
        assertFalse(q.estCorrecte('B'));
        assertTrue(q.estCorrecte('b'));
    }

    @Test
    void mauvaiseReponseRefusee() {
        Question q = new Question("Docker", "Image de base ?", new String[]{"RUN", "COPY", "FROM", "CMD"}, 'C', "FROM définit l'image de base.");
        assertFalse(q.estCorrecte('A'));
        assertFalse(q.estCorrecte('D'));
    }

    @Test
    void banqueContientAuMoins20Questions() {
        assertTrue(BanqueQuestions.getToutesLesQuestions().size() >= 20);
    }

    @Test
    void categorieValideFiltreLesQuestions() {
        List<Question> docker = BanqueQuestions.getParCategorie("Docker");
        assertFalse(docker.isEmpty());
        assertTrue(docker.stream().allMatch(q -> "Docker".equalsIgnoreCase(q.getCategorie())));
    }

    @Test
    void quizGeneralRetourne10Questions() {
        List<Question> quiz = BanqueQuestions.getQuizGeneral(10);
        assertEquals(10, quiz.size());
    }
}
