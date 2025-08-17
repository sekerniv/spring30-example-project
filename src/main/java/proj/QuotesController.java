package proj;

import jakarta.servlet.http.HttpSession;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class QuotesController {

    @GetMapping("/quotes")
    public String randomQuote(Model model, HttpSession session) throws Exception {
        String url = "jdbc:sqlite:seinfeld.db"; // <-- change path/name if needed
        String sql = "SELECT quote, speaker, episode " +
                     "FROM seinfeld_quotes " +
                     "ORDER BY RANDOM() LIMIT 1";

        String quote = "Add quotes to the database to get started!";
        String speaker = "System";
        String episode = "Setup";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                quote = rs.getString("quote");
                speaker = rs.getString("speaker");
                episode = rs.getString("episode");
            }
        }

        model.addAttribute("quote", quote);
        model.addAttribute("speaker", speaker);
        model.addAttribute("episode", episode);
        model.addAttribute("username", session.getAttribute("username")); // optional header user

        return "quotes"; // renders templates/quotes.html
    }
}
