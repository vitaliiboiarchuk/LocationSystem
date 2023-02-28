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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@Controller
@Secured("ROLE_USER")
public class LocationController {

    private final UserRepository userRepository;
    private final LocationRepository locationRepository;

    public LocationController(UserRepository userRepository, LocationRepository locationRepository) {
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
    }

    @GetMapping("/addLocation")
    public String addLocation(@AuthenticationPrincipal CurrentUser currentUser, Model model) {
        User entityUser = currentUser.getUser();
        model.addAttribute("user", userRepository.getReferenceById(entityUser.getId()));
        model.addAttribute("location", new Location());
        return "addLocation";
    }

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

    @GetMapping("/myLocations")
    public String myLocations(@AuthenticationPrincipal CurrentUser currentUser, Model model) {
        User entityUser = currentUser.getUser();
        model.addAttribute("locations",locationRepository.findAllMyLocations(entityUser.getId()));
        model.addAttribute("readOnlyLocations",locationRepository.findAllMyReadOnlyLocations(entityUser.getId()));
        model.addAttribute("adminLocations",locationRepository.findAllMyAdminLocations(entityUser.getId()));
        return "myLocations";
    }

    @GetMapping("/shareLocation")
    public String shareLocation(@AuthenticationPrincipal CurrentUser currentUser, Model model) {
        User entityUser = currentUser.getUser();
        model.addAttribute("users",userRepository.findAllWhereIdNotLike(entityUser.getId()));
        return "shareLocation";
    }

    @GetMapping("/shareReadOnly/{id}/")
    public String shareReadOnly(@AuthenticationPrincipal CurrentUser currentUser, @PathVariable long id, Model model) {
        User entityUser = currentUser.getUser();
        model.addAttribute("user",userRepository.getReferenceById(id));
        List<Location> allMyLocations = locationRepository.findAllMyLocations(entityUser.getId());
        List<Location> allMyAdminLocations = locationRepository.findAllMyAdminLocations(entityUser.getId());
        List<Location> forAdminAccess = Stream.of(allMyLocations, allMyAdminLocations).flatMap(Collection::stream).toList();
        model.addAttribute("locations",forAdminAccess);
        return "shareReadOnly";
    }

    @PostMapping("/shareReadOnly")
    public String shareReadOnly(@Valid User user) {
        userRepository.save(user);
        return "redirect:/";
    }

    @GetMapping("/shareAdmin/{id}/")
    public String shareAdmin(@AuthenticationPrincipal CurrentUser currentUser, @PathVariable long id, Model model) {
        User entityUser = currentUser.getUser();
        model.addAttribute("user",userRepository.getReferenceById(id));
        List<Location> allMyLocations = locationRepository.findAllMyLocations(entityUser.getId());
        List<Location> allMyAdminLocations = locationRepository.findAllMyAdminLocations(entityUser.getId());
        List<Location> forAdminAccess = Stream.of(allMyLocations, allMyAdminLocations).flatMap(Collection::stream).toList();
        model.addAttribute("locations",forAdminAccess);
        return "shareAdmin";
    }

    @PostMapping("/shareAdmin")
    public String shareAdmin(@Valid User user) {
        userRepository.save(user);
        return "redirect:/";
    }


}
