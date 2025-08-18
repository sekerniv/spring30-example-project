package proj;

import jakarta.servlet.http.HttpSession;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
public class EpisodesController {

    
    private static final String DB_URL = "jdbc:sqlite:seinfeld.db";

    
    public static class EpisodeRow {
        public String title;
        public String plot;
        public int year;
        public int season;
        public int episode;
        public String originalAirDate;   // keep as text from DB
        public String imageFileName;
        public int votes;
    }

    @GetMapping("/episodes")
    public String listEpisodes(@RequestParam(value = "season", required = false) Integer seasonParam,
                               Model model,
                               HttpSession session) throws Exception {

        // Ensure the votes table exists (first-use convenience)
        ensureVotesTable();

        // 1) Distinct seasons for the selector
        List<Integer> seasons = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT DISTINCT Season FROM episodes ORDER BY Season")) {
            while (rs.next()) seasons.add(rs.getInt(1));
        }

        // 2) Load episodes (all or by season) + votes via LEFT JOIN
        String baseSql =
            "SELECT e.Title, e.Plot, e.Year, e.Season, e.Episode, e.Original_Air_Date, e.Image_File_Name, " +
            "       COALESCE(v.votes, 0) AS votes " +
            "FROM episodes e " +
            "LEFT JOIN episode_votes v ON v.Title = e.Title ";
        String order = "ORDER BY e.Season, e.Episode";

        List<EpisodeRow> rows = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            PreparedStatement ps;
            if (seasonParam != null) {
                ps = conn.prepareStatement(baseSql + "WHERE e.Season = ? " + order);
                ps.setInt(1, seasonParam);
            } else {
                ps = conn.prepareStatement(baseSql + order);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    EpisodeRow r = new EpisodeRow();
                    r.title = rs.getString("Title");
                    r.plot = rs.getString("Plot");
                    r.year = rs.getInt("Year");
                    r.season = rs.getInt("Season");
                    r.episode = rs.getInt("Episode");
                    r.originalAirDate = rs.getString("Original_Air_Date");
                    r.imageFileName = rs.getString("Image_File_Name");
                    r.votes = rs.getInt("votes");
                    rows.add(r);
                }
            }
        }

        // 3) Model
        model.addAttribute("seasons", seasons);
        model.addAttribute("selectedSeason", seasonParam); // can be null for "All"
        model.addAttribute("episodes", rows);
        // optional convenience for template
        model.addAttribute("loggedIn", session.getAttribute("username") != null);

        return "episodes"; // templates/episodes.html
    }

    @PostMapping("/episodes/vote")
    public String vote(@RequestParam("title") String title,
                       @RequestParam(value = "season", required = false) Integer seasonParam,
                       HttpSession session) throws Exception {
        // Require login
        if (session.getAttribute("username") == null) {
            return "redirect:/login?message=Please%20login%20to%20vote";
        }

        ensureVotesTable();

        // Upsert vote count by Title
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(
                 "INSERT INTO episode_votes (Title, votes) VALUES (?, 1) " +
                 "ON CONFLICT(Title) DO UPDATE SET votes = votes + 1"
             )) {
            ps.setString(1, title);
            ps.executeUpdate();
        }

        // Redirect back to the list keeping the current season filter
        return (seasonParam == null) ? "redirect:/episodes"
                                     : "redirect:/episodes?season=" + seasonParam;
    }

    private void ensureVotesTable() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement st = conn.createStatement()) {
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS episode_votes (" +
                "  Title TEXT PRIMARY KEY," +
                "  votes INTEGER NOT NULL DEFAULT 0" +
                ")"
            );
        }
    }
}
