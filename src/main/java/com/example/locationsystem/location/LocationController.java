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
import java.util.concurrent.CompletableFuture;

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
    public CompletableFuture<String> showLocations(@CookieValue(value = "user") String userId, Model model) {
        CompletableFuture<List<Location>> addedLocationsFuture = locationService.findAllAddedLocations(Long.valueOf(userId));
        CompletableFuture<List<Location>> adminAccessFuture = locationService.findAllLocationsWithAccess(Long.valueOf(userId), "ADMIN");
        CompletableFuture<List<Location>> readAccessFuture = locationService.findAllLocationsWithAccess(Long.valueOf(userId), "READ");

        return CompletableFuture.allOf(addedLocationsFuture, adminAccessFuture, readAccessFuture).thenApplyAsync((Void) -> {
            model.addAttribute("locations", addedLocationsFuture.join());
            model.addAttribute("adminAccess", adminAccessFuture.join());
            model.addAttribute("readAccess", readAccessFuture.join());
            return "location/locations";
        });
    }

    @RequestMapping("/{locationId}/")
    public CompletableFuture<String> showFriendsOnLocation(@CookieValue(value = "user") String userId, @PathVariable Long locationId, Model model) {
        CompletableFuture<List<User>> adminAccessFuture = userService.findAllUsersWithAccessOnLocation(locationId,"ADMIN",Long.valueOf(userId));
        CompletableFuture<List<User>> readAccessFuture = userService.findAllUsersWithAccessOnLocation(locationId,"READ",Long.valueOf(userId));
        CompletableFuture<User> ownerFuture = userService.findLocationOwner(locationId,Long.valueOf(userId));

        return CompletableFuture.allOf(adminAccessFuture,readAccessFuture,ownerFuture).thenApplyAsync((Void) -> {
            model.addAttribute("adminAccess",adminAccessFuture.join());
            model.addAttribute("readAccess",readAccessFuture.join());
            User owner = ownerFuture.join();
            if (owner != null) {
                model.addAttribute("showOwner", true);
                model.addAttribute("owner", owner);
            } else {
                model.addAttribute("showOwnerActions", true);
            }
            return "location/friends";
        });
    }


    @RequestMapping("/{locationId}/{uId}/")
    public CompletableFuture<String> changeAccess(@CookieValue(value = "user") String userId, @PathVariable Long locationId, @PathVariable Long uId) {
        return userService.findLocationOwner(locationId, Long.valueOf(userId)).thenComposeAsync(owner -> {
            if (owner == null) {
                return userAccessService.changeUserAccess(locationId, uId).thenApplyAsync((Void) -> "redirect:/");
            }
            return CompletableFuture.completedFuture("redirect:/");
        });
    }

    @GetMapping("/add")
    public CompletableFuture<String> addLocation(@CookieValue(value = "user") String userId, Model model) {
        CompletableFuture<User> userFuture = userService.findById(Long.valueOf(userId));

        return userFuture.thenApplyAsync(user -> {
            model.addAttribute("user", user);
            model.addAttribute("location", new Location());
            return "location/add";
        });
    }

    @PostMapping("/add")
    public CompletableFuture<String> addLocation(@CookieValue(value = "user") String userId, @Valid Location location) {
        CompletableFuture<Location> locExistsFuture = locationService.findLocationByNameAndUserId(location.getName(), Long.valueOf(userId));

        return locExistsFuture.thenApplyAsync(locExists -> {
            if (locExists != null || location.getName().isEmpty() || location.getAddress().isEmpty()) {
                return "redirect:/location/add?error=true";
            } else {
                locationService.saveLocation(location);
                return "redirect:/";
            }
        });
    }

    @RequestMapping("/share")
    public CompletableFuture<String> shareLocation(@CookieValue(value = "user") String userId, Model model) {
        CompletableFuture<List<User>> usersFuture = userService.findUsersToShare(Long.valueOf(userId));

        return usersFuture.thenApplyAsync(users -> {
            model.addAttribute("users", users);
            return "location/users";
        });
    }

    @ModelAttribute("accessTitles")
    public List<String> accessTitles() {
        List<String> titles = new ArrayList<>();
        titles.add("ADMIN");
        titles.add("READ");
        return titles;
    }

    @GetMapping("/share/{id}/")
    public CompletableFuture<String> shareLocation(@CookieValue(value = "user") String userId, @PathVariable Long id, Model model) {
        CompletableFuture<List<Location>> locationsFuture = locationService.findNotSharedToUserLocations(Long.valueOf(userId), id);

        return locationsFuture.thenComposeAsync(locations -> {
            if (locations.isEmpty()) {
                model.addAttribute("notAvailable", true);
            } else {
                model.addAttribute("available", true);
                model.addAttribute("userAccess", new UserAccess());
                model.addAttribute("user", userService.findById(id).join());
                model.addAttribute("locations", locations);
            }
            return CompletableFuture.completedFuture("location/share");
        });
    }

    @PostMapping("/share")
    public CompletableFuture<String> shareLocation(@Valid UserAccess userAccess) {
        return userAccessService.saveUserAccess(userAccess).thenApplyAsync((Void) -> "redirect:/");
    }

    @RequestMapping("/{locationId}/delete")
    public CompletableFuture<String> deleteLocation(@CookieValue(value = "user") String userId, @PathVariable Long locationId) {
        CompletableFuture<Void> deleteLocationFuture = locationService.deleteLocation(locationId, Long.valueOf(userId));
        return deleteLocationFuture.thenApplyAsync((Void) -> "redirect:/");
    }
}

