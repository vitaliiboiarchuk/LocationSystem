package com.example.locationsystem.location;

import com.example.locationsystem.user.CurrentUser;
import com.example.locationsystem.user.User;
import com.example.locationsystem.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class LocationController {

    private final UserRepository userRepository;
    private final LocationRepository locationRepository;

    public LocationController(UserRepository userRepository, LocationRepository locationRepository) {
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
    }

    @Secured("ROLE_USER")
    @GetMapping("/addLocation")
    public String addLocation(@AuthenticationPrincipal CurrentUser currentUser, Model model) {
        User entityUser = currentUser.getUser();
        model.addAttribute("user", userRepository.getReferenceById(entityUser.getId()));
        model.addAttribute("location", new Location());
        return "addLocation";
    }

    @Secured("ROLE_USER")
    @PostMapping("/addLocation")
    public String addLocation(@AuthenticationPrincipal CurrentUser currentUser, @Valid Location location, BindingResult result, Model model) {
        User entityUser = currentUser.getUser();
        if (result.hasErrors()) {
            model.addAttribute("user", userRepository.getReferenceById(entityUser.getId()));
            return "addLocation";
        }
        locationRepository.save(location);
        return "redirect:/";
    }

    @Secured("ROLE_USER")
    @GetMapping("/myLocations")
    public String myLocations(@AuthenticationPrincipal CurrentUser currentUser, Model model) {
        User entityUser = currentUser.getUser();
        model.addAttribute("locations",locationRepository.findAllMyLocations(entityUser.getId()));
        return "myLocations";
    }
}
