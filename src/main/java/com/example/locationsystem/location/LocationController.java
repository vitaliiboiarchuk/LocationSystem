package com.example.locationsystem.location;

import com.example.locationsystem.user.CurrentUser;
import com.example.locationsystem.user.User;
import com.example.locationsystem.user.UserService;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

@Controller
@Secured("ROLE_USER")
@RequestMapping("/location")
public class LocationController {

    private final UserService userService;
    private final LocationService locationService;

    public LocationController(UserService userService, LocationService locationService) {
        this.userService = userService;
        this.locationService = locationService;
    }

    @GetMapping("/add")
    public String addLocation(@AuthenticationPrincipal CurrentUser currentUser, Model model) {
        User entityUser = currentUser.getUser();
        model.addAttribute("user", userService.findById(entityUser.getId()));
        model.addAttribute("location", new Location());
        return "location/add";
    }

    @PostMapping("/add")
    public String addLocation(@AuthenticationPrincipal CurrentUser currentUser, @Valid Location location, BindingResult result, Model model) {
        User entityUser = currentUser.getUser();
        Location locExists = locationService.findLocationByName(location.getName());
        if (locExists != null) {
            result.rejectValue("name", "error.user",
                    "Location with that name already exists!");
        }
        if (result.hasErrors()) {
            model.addAttribute("user", userService.findById(entityUser.getId()));
            return "location/add";
        }
        locationService.saveLocation(location);
        return "redirect:/";
    }
}

