package com.example.locationsystem.location;

import com.example.locationsystem.user.User;
import com.example.locationsystem.user.UserService;
import com.example.locationsystem.userAccess.UserAccess;
import com.example.locationsystem.userAccess.UserAccessService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/location")
public class LocationController {

    private final UserService userService;
    private final LocationService locationService;
    private final UserAccessService userAccessService;

    public LocationController(
        UserService userService,
        LocationService locationService,
        UserAccessService userAccessService
    ) {

        this.userService = userService;
        this.locationService = locationService;
        this.userAccessService = userAccessService;
    }

    @GetMapping("")
    public CompletableFuture<ResponseEntity<String>> showLocations(@CookieValue(value = "user") String userId) {

        CompletableFuture<List<Location>> addedLocationsFuture =
            locationService.findAllAddedLocations(Long.valueOf(userId));
        CompletableFuture<List<Location>> adminAccessFuture =
            locationService.findAllLocationsWithAccess(Long.valueOf(userId), "ADMIN");
        CompletableFuture<List<Location>> readAccessFuture =
            locationService.findAllLocationsWithAccess(Long.valueOf(userId), "READ");

        return CompletableFuture.allOf(addedLocationsFuture, adminAccessFuture, readAccessFuture).thenApplyAsync((Void) -> {
            List<Location> addedLocations = addedLocationsFuture.join();
            List<Location> adminAccessLocations = adminAccessFuture.join();
            List<Location> readAccessLocations = readAccessFuture.join();

            return ResponseEntity.ok().body("Added locations: " + addedLocations.toString() + "\n Locations with " +
                "admin:" +
                " access " + adminAccessLocations.toString() + "\n Locations with read access: " + readAccessLocations.toString());
        });
    }

    @GetMapping("/{locationId}/")
    public CompletableFuture<ResponseEntity<String>> showFriendsOnLocation(
        @CookieValue(value = "user") String userId, @PathVariable Long locationId
    ) {

        CompletableFuture<List<User>> adminAccessFuture = userService.findAllUsersWithAccessOnLocation(locationId,
            "ADMIN", Long.valueOf(userId));
        CompletableFuture<List<User>> readAccessFuture = userService.findAllUsersWithAccessOnLocation(locationId,
            "READ", Long.valueOf(userId));
        CompletableFuture<User> ownerFuture = userService.findLocationOwner(locationId, Long.valueOf(userId));

        return CompletableFuture.allOf(adminAccessFuture, readAccessFuture, ownerFuture).thenApplyAsync((Void) -> {
            List<User> usersWithAdminAccess = adminAccessFuture.join();
            List<User> usersWithReadAccess = readAccessFuture.join();
            User owner = ownerFuture.join();
            if (owner != null) {
                return ResponseEntity.ok().body("Friends with admin access: " + usersWithAdminAccess.toString() + "\n" +
                    "Friends with read access: " + usersWithReadAccess.toString() + "\n Owner: " + owner);
            } else {
                return ResponseEntity.ok().body("Friends with admin access: " + usersWithAdminAccess.toString() + "\n" +
                    "Friends with read access: " + usersWithReadAccess.toString());
            }
        });
    }

    @PutMapping("/changeAccess/{locationId}/{uId}/")
    public CompletableFuture<ResponseEntity<String>> changeAccess(
        @CookieValue(value = "user") String userId,
        @PathVariable Long locationId,
        @PathVariable Long uId
    ) {

        CompletableFuture<User> userFuture = userService.findLocationOwner(locationId, Long.valueOf(userId));

        return userFuture.thenApplyAsync(owner -> {
            if (owner != null) {
                return ResponseEntity.badRequest().body("Failed to change access");
            }
            userAccessService.changeUserAccess(locationId, uId);
            return ResponseEntity.ok("User access changed successfully");
        });
    }

    @PostMapping("/add")
    public CompletableFuture<ResponseEntity<String>> addLocation(
        @CookieValue(value = "user") String userId,
        @RequestBody Location location
    ) {

        CompletableFuture<Location> locExistsFuture = locationService.findLocationByNameAndUserId(location.getName(),
            Long.valueOf(userId));

        return locExistsFuture.thenApplyAsync(locExists -> {
            if (locExists != null) {
                return ResponseEntity.badRequest().body("Location with that name already exists");
            } else if (location.getName().isEmpty() || location.getAddress().isEmpty()) {
                return ResponseEntity.badRequest().body("Fields can not be empty");
            } else {
                CompletableFuture<User> userFuture = userService.findById(Long.valueOf(userId));
                location.setUser(userFuture.join());
                locationService.saveLocation(location);
                return ResponseEntity.ok("Location added successfully");
            }
        });
    }

    @PostMapping("/share/{locationId}/{uId}/")
    public CompletableFuture<ResponseEntity<String>> shareLocation(
        @CookieValue(value = "user") String userId,
        @PathVariable Long locationId,
        @PathVariable Long uId,
        @RequestBody UserAccess userAccess
    ) {

        CompletableFuture<List<Location>> locationsFuture =
            locationService.findNotSharedToUserLocations(Long.valueOf(userId), uId);
        return locationsFuture.thenApplyAsync(locs -> {
            boolean containsLocWithId = locs.stream().anyMatch(loc -> loc.getId().equals(locationId));
            if (containsLocWithId) {
                CompletableFuture<User> user = userService.findById(uId);
                userAccess.setUser(user.join());
                CompletableFuture<Location> loc = locationService.findById(locationId);
                userAccess.setLocation(loc.join());
                userAccessService.saveUserAccess(userAccess);
                return ResponseEntity.ok("Location shared successfully");
            } else {
                return ResponseEntity.badRequest().body("User already has access to all of your locations, or you " +
                    "have no location to share.");
            }
        });
    }

    @DeleteMapping("/delete/{locationId}/")
    public CompletableFuture<ResponseEntity<String>> deleteLocation(
        @CookieValue(value = "user") String userId,
        @PathVariable Long locationId
    ) {

        CompletableFuture<User> userFuture = userService.findLocationOwner(locationId, Long.valueOf(userId));

        return userFuture.thenApplyAsync(owner -> {
            if (owner != null) {
                return ResponseEntity.badRequest().body("Failed to delete location");
            }
            locationService.deleteLocation(locationId, Long.valueOf(userId));
            return ResponseEntity.ok("Location deleted successfully");
        });
    }
}

