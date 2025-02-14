import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import net.sourceforge.tess4j.*;
import java.sql.*;
import java.util.*;
import

public class DuolingoAI {
    private static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:duolingo_ai.db");
            Statement stmt = conn.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS progress (id INTEGER PRIMARY KEY, question TEXT, answer TEXT)");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    private static void saveProgress(String question, String answer) {
        String sql = "INSERT INTO progress(question, answer) VALUES(?, ?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, question);
            pstmt.setString(2, answer);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static String predictAnswer(String question) {
        Map<String, String> knowledgeBase = new HashMap<>();
        try (Connection conn = connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT question, answer FROM progress")) {
            while (rs.next()) {
                knowledgeBase.put(rs.getString("question"), rs.getString("answer"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return knowledgeBase.getOrDefault(question, "Réponse inconnue, apprentissage en cours");
    }

    private static void handleSpecialCases(Robot robot, String text) {
        if (text.contains("Je ne peux pas parler") || text.contains("Je ne peux pas écouter")) {
            System.out.println("Détection d'une question avec audio. Sélection de l'option correspondante.");
            robot.mouseMove(600, 400); // Ajuster la position du clic
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        }
    }

    public static void main(String[] args) {
        try {
            // Capture d'écran
            Robot robot = new Robot();
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage screenShot = robot.createScreenCapture(screenRect);
            ImageIO.write(screenShot, "png", new File("screenshot.png"));

            // OCR avec Tesseract
            File imageFile = new File("screenshot.png");
            ITesseract tesseract = new Tesseract();
            tesseract.setDatapath("tessdata"); // Dossier contenant les fichiers de langue
            String extractedText = tesseract.doOCR(imageFile);
            System.out.println("Texte détecté : " + extractedText);
            
            // Vérification des cas spéciaux
            handleSpecialCases(robot, extractedText);
            
            // Prédiction de la réponse basée sur l'apprentissage
            String predictedAnswer = predictAnswer(extractedText);
            System.out.println("Réponse prédite : " + predictedAnswer);
            
            // Sauvegarde de la progression
            saveProgress(extractedText, predictedAnswer);
            
            // Interaction basique (exemple : clic sur la réponse prévue)
            robot.mouseMove(500, 500);
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
