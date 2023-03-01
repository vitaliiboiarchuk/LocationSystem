package com.example.locationsystem.user;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("/registration")
    public String registration(Model model) {
        model.addAttribute("user",new User());
        return "registration";
    }


    @PostMapping("/registration")
    public String registrationPost(@Valid User user, BindingResult bindingResult, Model model) {
        User userExists = userService.findByUserName(user.getUsername());
        if (userExists != null) {
            bindingResult.rejectValue("username", "error.user",
                    "Użytkownik z taką nazwą już istnieje");
        }

        if (user.getName().isEmpty()) {
            bindingResult.rejectValue("name","error.user","Pole nie może być puste!");
        }

        if (user.getUsername().isEmpty()) {
            bindingResult.rejectValue("email","error.user","Pole nie może być puste!");
        }
        if (user.getPassword().isEmpty()) {
            bindingResult.rejectValue("password","error.user","Pole nie może być puste!");
        }

        if (bindingResult.hasErrors()) {
            return "registration";
        } else {
            userService.saveUser(user);
            return "redirect:/login";
        }
    }

    @GetMapping("/login")
    public String login(@Valid User user, Model model) {
        model.addAttribute("user",user);
        return "login";
    }

}
