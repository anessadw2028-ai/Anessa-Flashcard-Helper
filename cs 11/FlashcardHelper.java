import java.sql.*;
import java.util.Scanner;

public class FlashcardHelper {

    static final String DB_URL = "jdbc:sqlite:flashcard_helper.db";

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        try {
            Connection conn = DriverManager.getConnection(DB_URL);

            while (true) {

                System.out.println("\nFLASHCARD STUDY HELPER");
                System.out.println("1. Add Flashcard");
                System.out.println("2. View Flashcards");
                System.out.println("3. Study Flashcards");
                System.out.println("4. View Statistics");
                System.out.println("5. Edit Flashcard");
                System.out.println("6. Reset Statistics");
                System.out.println("7. Rename Category");
                System.out.println("8. Exit");
                System.out.print("Choose an option: ");

                int choice = Integer.parseInt(scanner.nextLine());

                switch (choice) {

                    case 1:
                        addFlashcard(conn, scanner);
                        break;

                    case 2:
                        viewFlashcards(conn);
                        break;

                    case 3:
                        studyFlashcards(conn, scanner);
                        break;
                        
                    case 4:
                        viewStatistics(conn);
                        break;
                        
                    case 5:
                        editFlashcard(conn, scanner);
                        break;
                        
                    case 6:
                        resetStatistics(conn, scanner);
                        break;
                        
                    case 7:
                        renameCategory(conn, scanner);
                        break;
                        
                    case 8:
                        conn.close();
                        scanner.close();
                        System.out.println("Goodbye!");
                        return;

                    default:
                        System.out.println("Invalid option.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addFlashcard(Connection conn, Scanner scanner) {

        try {

            System.out.print("Question: ");
            String question = scanner.nextLine();

            System.out.print("Answer: ");
            String answer = scanner.nextLine();

            System.out.print("Category: ");
            String category = scanner.nextLine();

            String sql =
                    "INSERT INTO flashcard(question, answer, category) VALUES (?, ?, ?)";

            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, question);
            pstmt.setString(2, answer);
            pstmt.setString(3, category);

            pstmt.executeUpdate();

            System.out.println("Flashcard added successfully!");

        } catch (SQLException e) {

            System.out.println("This question already exists.");
        }
    }

    public static void viewFlashcards(Connection conn) {

        try {

            String sql = "SELECT * FROM flashcard";

            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery(sql);

            System.out.println("\nALL FLASHCARDS");

            while (rs.next()) {

                System.out.println("------------------------");
                System.out.println("ID: " + rs.getInt("card_id"));
                System.out.println("Question: " + rs.getString("question"));
                System.out.println("Answer: " + rs.getString("answer"));
                System.out.println("Category: " + rs.getString("category"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
public static void studyFlashcards(Connection conn, Scanner scanner) {

    try {

        String sql = "SELECT * FROM flashcard ORDER BY card_id";

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        while (rs.next()) {

            int cardId = rs.getInt("card_id");
            String question = rs.getString("question");
            String answer = rs.getString("answer");

            System.out.println("\n------------------------");
            System.out.println("Question:");
            System.out.println(question);

            System.out.println("\nPress Enter to reveal answer...");
            scanner.nextLine();

            System.out.println("Answer:");
            System.out.println(answer);

            System.out.println("\nRate yourself:");
            System.out.println("1. Wrong");
            System.out.println("2. Half Right");
            System.out.println("3. Right");

            int rating = Integer.parseInt(scanner.nextLine());

            double score = 0.0;

            if (rating == 1) {
                score = 0.0;
            } else if (rating == 2) {
                score = 0.5;
            } else if (rating == 3) {
                score = 1.0;
            }

            String insertLog =
                    "INSERT INTO attempt_log(card_id, score) VALUES (?, ?)";

            PreparedStatement pstmt =
                    conn.prepareStatement(insertLog);

            pstmt.setInt(1, cardId);
            pstmt.setDouble(2, score);

            pstmt.executeUpdate();
            String updateAppearances =
        "UPDATE flashcard SET appearance_count = appearance_count + 1 WHERE card_id=?";

PreparedStatement updateStmt =
        conn.prepareStatement(updateAppearances);

updateStmt.setInt(1, cardId);

updateStmt.executeUpdate();
        }

        System.out.println("\nStudy session complete!");

    } catch (Exception e) {
        e.printStackTrace();
    }
}
public static void viewStatistics(Connection conn) {

    try {

       String sql =
        """
        SELECT
            f.question,
            f.appearance_count,
            COUNT(a.log_id) AS attempts,
            ROUND(COALESCE(AVG(a.score), 0) * 100, 1) AS percent_correct
        FROM flashcard f
        LEFT JOIN attempt_log a
        ON f.card_id = a.card_id
        GROUP BY f.card_id
        ORDER BY f.card_id
        """;

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        System.out.println("\nFLASHCARD STATISTICS");

        while (rs.next()) {
           System.out.println("------------------------");
System.out.println("Question: " + rs.getString("question"));
System.out.println("Attempts: " + rs.getInt("attempts"));
System.out.println("Appearances: " + rs.getInt("appearance_count"));
System.out.println("Percent Correct: "
        + rs.getDouble("percent_correct") + "%");
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }
}
public static void editFlashcard(Connection conn, Scanner scanner) {

    try {

        System.out.print("Enter Flashcard ID: ");
        int id = Integer.parseInt(scanner.nextLine());

        System.out.print("New Question: ");
        String question = scanner.nextLine();

        System.out.print("New Answer: ");
        String answer = scanner.nextLine();

        String sql =
                "UPDATE flashcard SET question=?, answer=? WHERE card_id=?";

        PreparedStatement pstmt =
                conn.prepareStatement(sql);

        pstmt.setString(1, question);
        pstmt.setString(2, answer);
        pstmt.setInt(3, id);

        int rows = pstmt.executeUpdate();

        if (rows > 0) {
            System.out.println("Flashcard updated.");
        } else {
            System.out.println("Flashcard not found.");
        }

    } catch (SQLException e) {
        System.out.println("Question already exists.");
    }
}
public static void resetStatistics(Connection conn, Scanner scanner) {

    try {

        System.out.println("\nReset Statistics");
        System.out.println("1. Reset one flashcard");
        System.out.println("2. Reset all flashcards");
        System.out.print("Choose an option: ");

        int choice = Integer.parseInt(scanner.nextLine());

        if (choice == 1) {

            System.out.print("Enter Flashcard ID: ");
            int id = Integer.parseInt(scanner.nextLine());

            String sql =
                    "DELETE FROM attempt_log WHERE card_id=?";

            PreparedStatement pstmt =
                    conn.prepareStatement(sql);

            pstmt.setInt(1, id);

            int rows = pstmt.executeUpdate();

            System.out.println(rows + " attempt records deleted.");

        } else if (choice == 2) {

            System.out.print("Are you sure? (Y/N): ");
            String confirm = scanner.nextLine();

            if (confirm.equalsIgnoreCase("Y")) {

                String sql = "DELETE FROM attempt_log";

                PreparedStatement pstmt =
                        conn.prepareStatement(sql);

                int rows = pstmt.executeUpdate();

                System.out.println(rows + " attempt records deleted.");
            }

        } else {

            System.out.println("Invalid option.");
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}
public static void renameCategory(Connection conn, Scanner scanner) {

    try {

        System.out.print("Current Category: ");
        String oldCategory = scanner.nextLine();

        System.out.print("New Category: ");
        String newCategory = scanner.nextLine();

        String sql =
                "UPDATE flashcard SET category=? WHERE category=?";

        PreparedStatement pstmt =
                conn.prepareStatement(sql);

        pstmt.setString(1, newCategory);
        pstmt.setString(2, oldCategory);

        pstmt.executeUpdate();

        System.out.println("Category renamed.");

    } catch (Exception e) {
        e.printStackTrace();
    }
}
}
