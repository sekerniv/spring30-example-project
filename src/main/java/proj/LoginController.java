package proj;

import jakarta.servlet.http.HttpSession;
import java.sql.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
public class LoginController {

    private static final String DB_URL = "jdbc:sqlite:seinfeld.db";

    @GetMapping("/login")
    public String showLogin(Model model,
                            @RequestParam(value = "message", required = false) String message,
                            @RequestParam(value = "email", required = false) String email) {
        if (message != null) model.addAttribute("message", message);
        if (email != null) model.addAttribute("email", email);
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String email,
                          @RequestParam String password,
                          HttpSession session,
                          Model model) throws Exception {

        String sql = "SELECT email, password, role, full_name FROM users WHERE email = ?";
        String dbEmail = null, dbPassword = null, role = "USER", fullName = null;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dbEmail = rs.getString("email");
                    dbPassword = rs.getString("password");
                    role = rs.getString("role") != null ? rs.getString("role") : "USER";
                    fullName = rs.getString("full_name");
                }
            }
        }

        if (dbEmail == null || !password.equals(dbPassword)) {
            model.addAttribute("error", "Invalid email or password.");
            model.addAttribute("email", email);
            return "login";
        }

        session.setAttribute("username", fullName != null ? fullName : dbEmail);
        session.setAttribute("email", dbEmail);
        session.setAttribute("role", role);

        return "redirect:/";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?message=You%20have%20logged%20out";
    }
}
