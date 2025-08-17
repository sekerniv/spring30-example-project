package proj;

import jakarta.servlet.http.HttpServletRequest;
import java.sql.*;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
public class RegisterController {

    private static final String DB_URL = "jdbc:sqlite:seinfeld.db";

    @GetMapping("/register")
    public String showRegister() {
        return "register";
    }

    @PostMapping("/register")
    public String doRegister(HttpServletRequest req, Model model) throws Exception {
        String fullName = val(req.getParameter("fullName"));
        String email    = val(req.getParameter("email"));
        String dob      = req.getParameter("dob");
        String[] hobbiesArr = req.getParameterValues("hobbies");
        String hobbies = (hobbiesArr == null ? "" :
                Arrays.stream(hobbiesArr).map(String::trim).collect(Collectors.joining(",")));
        String password = req.getParameter("password");
        String confirm  = req.getParameter("confirmPassword");
        boolean terms   = "on".equals(req.getParameter("terms"));

        // ---- Server-side validation (matches client JS) ----
        if (fullName.length() < 2 || !fullName.matches("^[A-Za-zא-ת ]+$"))
            return error(model, "Full name must be at least 2 characters and contain only letters and spaces.", fullName, email, dob);

        if (!email.matches("^[A-Za-z][A-Za-z0-9_-]*@[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]{2,3}$"))
            return error(model, "Invalid email format.", fullName, email, dob);

        if (dob == null || dob.isEmpty())
            return error(model, "Date of birth is required.", fullName, email, dob);

        // NEW: 6–16 chars, at least one letter and one number
        boolean hasLetter = password != null && password.matches(".*[A-Za-z].*");
        boolean hasDigit  = password != null && password.matches(".*\\d.*");
        if (password == null || password.length() < 6 || password.length() > 16 || !hasLetter || !hasDigit)
            return error(model, "Password must be 6-16 characters and include at least one letter and one number.", fullName, email, dob);

        if (!password.equals(confirm))
            return error(model, "Passwords do not match.", fullName, email, dob);

        if (!terms)
            return error(model, "You must accept the terms to continue.", fullName, email, dob);

        if (hobbies.isEmpty())
            return error(model, "Choose at least one hobby.", fullName, email, dob);
        // ---- End validation ----

        // Check uniqueness of email
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM users WHERE email = ?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return error(model, "Email already registered.", fullName, email, dob);
                }
            }
        }

        // Insert (plaintext for class demo)
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO users (email, password, full_name, date_of_birth, hobbies, role) VALUES (?,?,?,?,?,'USER')")) {
            ps.setString(1, email);
            ps.setString(2, password);
            ps.setString(3, fullName);
            ps.setString(4, dob);
            ps.setString(5, hobbies);
            ps.executeUpdate();
        }

        return "redirect:/login?message=Account%20created%20successfully!%20Please%20sign%20in.&email=" + email;
    }

    private static String val(String s) { return s == null ? "" : s.trim(); }

    private String error(Model model, String msg, String fullName, String email, String dob) {
        model.addAttribute("error", msg);
        model.addAttribute("fullName", fullName);
        model.addAttribute("email", email);
        model.addAttribute("dob", dob);
        return "register";
    }
}
