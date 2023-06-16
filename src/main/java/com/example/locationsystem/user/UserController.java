package com.example.locationsystem.user;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping("/")
    public String home(HttpServletRequest request, Model model) {
        Cookie cookie = WebUtils.getCookie(request, "user");
        if (cookie != null) {
            model.addAttribute("myProfile",true);
        } else {
            model.addAttribute("welcomePage",true);
        }
        return "homePage";
    }

    @GetMapping("/registration")
    public String registerGet(Model model) {
        model.addAttribute("user", new User());
        return "entry/registration";
    }

    @PostMapping("/registration")
    public String registerPost(@Valid User user) {
        User findUser = userService.findByUsername(user.getUsername());
        if (findUser != null || user.getUsername().isEmpty() || user.getPassword().isEmpty()) {
            return "redirect:/registration?error=true";
        } else {
            userService.saveUser(user);
            return "redirect:/login";
        }
    }

    @GetMapping("/login")
    public String loginGet(@Valid User user, Model model) {
        model.addAttribute("user", user);
        return "entry/login";
    }

    @PostMapping("/login")
    public String loginPost(@RequestParam("username") String username, @RequestParam("password") String
            password, HttpServletResponse response) {
        User user = userService.findUserByUsernameAndPassword(username, password);
        if (user != null) {
            Cookie cookie = new Cookie("user", user.getId().toString());
            cookie.setPath("/");
            response.addCookie(cookie);
            return "redirect:/";
        } else return "redirect:/login?error=true";
    }

    @RequestMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = WebUtils.getCookie(request, "user");
        if (cookie != null) {
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        }
        return "redirect:/";
    }
}
