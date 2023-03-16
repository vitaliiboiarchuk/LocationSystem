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

    @RequestMapping("")
    public String showLocations(@AuthenticationPrincipal CurrentUser currentUser, Model model) {
        User entityUser = currentUser.getUser();
        model.addAttribute("locations",locationService.findAllAddedLocations(entityUser.getId()));
        model.addAttribute("adminAccess",locationService.findAllLocationsWithAccess(entityUser.getId(),"ADMIN"));
        model.addAttribute("readAccess",locationService.findAllLocationsWithAccess(entityUser.getId(),"READ"));
        return "location/locations";
    }

    @RequestMapping("/{locationId}/")
    public String showFriendsOnLocation(@AuthenticationPrincipal CurrentUser currentUser, @PathVariable long locationId, Model model) {
        User entityUser = currentUser.getUser();
        model.addAttribute("adminAccess",userService.findAllUsersWithAccessOnLocation(locationId,"ADMIN", entityUser.getId()));
        model.addAttribute("readAccess",userService.findAllUsersWithAccessOnLocation(locationId,"READ", entityUser.getId()));
        User owner = userService.findLocationOwner(locationId, entityUser.getId());
        if (owner != null) {
            model.addAttribute("showOwner",true);
            model.addAttribute("owner", owner);
        } else {
            model.addAttribute("showOwnerActions",true);
        }
        return "location/friends";
    }

    @RequestMapping("/{locationId}/{userId}/")
    public String changeAccess(@AuthenticationPrincipal CurrentUser currentUser, @PathVariable long locationId, @PathVariable long userId) {
        User entityUser = currentUser.getUser();
        User owner = userService.findLocationOwner(locationId, entityUser.getId());
        if (owner == null) {
            userAccessService.changeUserAccess(locationId,userId);
        }
        return "redirect:/";
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

    @RequestMapping("/{locationId}/delete")
    public String deleteLocation(@AuthenticationPrincipal CurrentUser currentUser, @PathVariable Long locationId) {
        User entityUser = currentUser.getUser();
        User owner = userService.findLocationOwner(locationId, entityUser.getId());
        if (owner == null) {
            locationService.deleteLocation(locationId);
        }
        return "redirect:/";
    }
}

