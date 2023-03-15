package com.example.locationsystem.location;

import com.example.locationsystem.user.CurrentUser;
import com.example.locationsystem.user.User;
import com.example.locationsystem.user.UserService;
import com.example.locationsystem.userAccess.UserAccess;
import com.example.locationsystem.userAccess.UserAccessService;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Controller
@Secured("ROLE_USER")
@RequestMapping("/location")
public class LocationController {

    private final UserService userService;
    private final LocationService locationService;
    private final UserAccessService userAccessService;

    public LocationController(UserService userService, LocationService locationService, UserAccessService userAccessService) {
        this.userService = userService;
        this.locationService = locationService;
        this.userAccessService = userAccessService;
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

    @RequestMapping("/share")
    public String shareLocation(@AuthenticationPrincipal CurrentUser currentUser, Model model) {
        User entityUser = currentUser.getUser();
        model.addAttribute("users",userService.findUsersToShare(entityUser.getId()));
        return "location/users";
    }

    @ModelAttribute("accessTitles")
    public List<String> accessTitles() {
        List<String> titles = new ArrayList<>();
        titles.add("ADMIN");
        titles.add("READ");
        return titles;
    }

    @GetMapping("/share/{id}/")
    public String shareLocation(@AuthenticationPrincipal CurrentUser currentUser, @PathVariable long id, Model model) {
        User entityUser = currentUser.getUser();
        List<Location> locations = locationService.findNotSharedToUserLocations(entityUser.getId(),id);
        if (locations.isEmpty()) {
            model.addAttribute("notAvailable",true);
        } else {
            model.addAttribute("available",true);
            model.addAttribute("userAccess", new UserAccess());
            model.addAttribute("user", userService.findById(id));
            model.addAttribute("locations", locations);
        }
        return "location/share";
    }

    @PostMapping("/share")
    public String shareLocation(@Valid UserAccess userAccess) {
        userAccessService.saveUserAccess(userAccess);
        return "redirect:/";
    }
}

