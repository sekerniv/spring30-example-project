package proj;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ExampleController {

    @GetMapping("/example_bootstrap_grid")
    public String bootstrapGridExample(Model model, HttpSession session) {
        return "/examples/example_bootstrap_grid";
    }

    @GetMapping("/example_bootstrap_responsive_grid")
    public String bootstrapResponsiveGridExample(Model model, HttpSession session) {
        return "/examples/example_bootstrap_responsive_grid";
    }
}
