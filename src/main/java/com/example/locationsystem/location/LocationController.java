package com.example.locationsystem.location;

import com.example.locationsystem.user.User;
import com.example.locationsystem.user.UserService;
import com.example.locationsystem.userAccess.UserAccess;
import com.example.locationsystem.userAccess.UserAccessService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Controller
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

    @RequestMapping("")
    public String showLocations(@CookieValue(value = "user") String userId, Model model) {
        model.addAttribute("locations", locationService.findAllAddedLocations(Long.valueOf(userId)));
        model.addAttribute("adminAccess", locationService.findAllLocationsWithAccess(Long.valueOf(userId), "ADMIN"));
        model.addAttribute("readAccess", locationService.findAllLocationsWithAccess(Long.valueOf(userId), "READ"));
        return "location/locations";
    }

    @RequestMapping("/{locationId}/")
    public String showFriendsOnLocation(@CookieValue(value = "user") String userId, @PathVariable Long locationId, Model model) {
        model.addAttribute("adminAccess", userService.findAllUsersWithAccessOnLocation(locationId, "ADMIN", Long.valueOf(userId)));
        model.addAttribute("readAccess", userService.findAllUsersWithAccessOnLocation(locationId, "READ", Long.valueOf(userId)));
        User owner = userService.findLocationOwner(locationId, Long.valueOf(userId));
        if (owner != null) {
            model.addAttribute("showOwner", true);
            model.addAttribute("owner", owner);
        } else {
            model.addAttribute("showOwnerActions", true);
        }
        return "location/friends";
    }


    @RequestMapping("/{locationId}/{uId}/")
    public String changeAccess(@CookieValue(value = "user") String userId, @PathVariable Long locationId, @PathVariable Long uId) {
        User owner = userService.findLocationOwner(locationId, Long.valueOf(userId));
        if (owner == null) {
            userAccessService.changeUserAccess(locationId, uId);
        }
        return "redirect:/";
    }

    @GetMapping("/add")
    public String addLocation(@CookieValue(value = "user") String userId, Model model) {
        model.addAttribute("user", userService.findById(Long.valueOf(userId)));
        model.addAttribute("location", new Location());
        return "location/add";
    }

    @PostMapping("/add")
    public String addLocation(@CookieValue(value = "user") String userId, @Valid Location location) {
        Location locExists = locationService.findLocationByNameAndUserId(location.getName(), Long.valueOf(userId));
        if (locExists != null || location.getName().isEmpty() || location.getAddress().isEmpty()) {
            return "redirect:/location/add?error=true";
        }
        locationService.saveLocation(location);
        return "redirect:/";
    }

    @RequestMapping("/share")
    public String shareLocation(@CookieValue(value = "user") String userId, Model model) {
        model.addAttribute("users", userService.findUsersToShare(Long.valueOf(userId)));
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
    public String shareLocation(@CookieValue(value = "user") String userId, @PathVariable Long id, Model model) {
        List<Location> locations = locationService.findNotSharedToUserLocations(Long.valueOf(userId), id);
        if (locations.isEmpty()) {
            model.addAttribute("notAvailable", true);
        } else {
            model.addAttribute("available", true);
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

    @RequestMapping("/{locationId}/delete")
    public String deleteLocation(@CookieValue(value = "user") String userId, @PathVariable Long locationId) {
        User owner = userService.findLocationOwner(locationId, Long.valueOf(userId));
        if (owner == null) {
            locationService.deleteLocation(locationId);
        }
        return "redirect:/";
    }
}

