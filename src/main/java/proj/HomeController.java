package proj;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class HomeController {

    @GetMapping("/")
    public String helloWorld(Model model, HttpSession session) {
        return "home";
    }

    @GetMapping("/characters")
    public String characters(Model model) {
        model.addAttribute("pageTitle", "Characters");
        return "characters";
    }

    @GetMapping("/favorites")
    public String favorites(Model model) {
        model.addAttribute("loggedIn", false); // TODO: set from session/auth
        return "favorites";
    }

    @GetMapping("/about")
    public String about(Model model, HttpSession session) {
        return "about";
    }
    
}
