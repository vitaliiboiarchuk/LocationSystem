package com.example.locationsystem.user;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/login")
    public String login(@Valid User user, Model model) {
        model.addAttribute("user",user);
        return "entry/login";
    }

    @GetMapping("/registration")
    public String registration(Model model) {
        model.addAttribute("user",new User());
        return "entry/registration";
    }

    @PostMapping("/registration")
    public String registrationPost(@Valid User user, BindingResult bindingResult) {
        User userExists = userService.findByUserName(user.getUsername());
        if (userExists != null) {
            bindingResult.rejectValue("username", "error.user",
                    "A user with that name already exists!");
        }
        if (user.getName().isEmpty()) {
            bindingResult.rejectValue("name","error.user","Field cannot be empty!");
        }
        if (user.getUsername().isEmpty()) {
            bindingResult.rejectValue("username","error.user","Field cannot be empty!");
        }
        if (user.getPassword().isEmpty()) {
            bindingResult.rejectValue("password","error.user","Field cannot be empty!");
        }
        if (bindingResult.hasErrors()) {
            return "entry/registration";
        } else {
            userService.saveUser(user);
            return "redirect:/login";
        }
    }
}
