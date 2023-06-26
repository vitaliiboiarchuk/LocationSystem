package com.example.locationsystem.location;

import com.example.locationsystem.user.User;
import com.example.locationsystem.user.UserService;
import com.example.locationsystem.userAccess.UserAccess;
import com.example.locationsystem.userAccess.UserAccessService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.example.locationsystem.user.UserControllerExceptions.*;
import com.example.locationsystem.location.LocationControllerExceptions.*;

@RestController
@RequestMapping("/location")
@Log4j2
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

    Long getUserIdFromRequest(HttpServletRequest request) {

        Cookie cookie = WebUtils.getCookie(request, "user");
        if (cookie == null) {
            log.warn("User not logged in");
            throw new NotLoggedInException("Not logged in");
        }
        log.info("User logged in");
        return Long.valueOf(cookie.getValue());
    }

    @GetMapping("")
    public CompletableFuture<ResponseEntity<List<Location>>> showLocations(HttpServletRequest request) {

        log.info("Show locations request received");
        Long userId = getUserIdFromRequest(request);

        return locationService.findAllMyLocations(userId)
            .thenApplyAsync(locations -> {
                log.info("Show locations successful");
                return ResponseEntity.ok(locations);
            });
    }

    @GetMapping("/{locationId}/")
    public CompletableFuture<ResponseEntity<List<User>>> showFriendsOnLocation(
        HttpServletRequest request, @PathVariable Long locationId
    ) {

        log.info("Show friends on location request received. Location ID: {}", locationId);
        Long userId = getUserIdFromRequest(request);

        return locationService.findAllMyLocations(userId)
            .thenComposeAsync(locations -> {
                boolean containsLocWithId = locations.stream().anyMatch(loc -> loc.getId().equals(locationId));
                if (!containsLocWithId) {
                    log.warn("No location found with id: {}", locationId);
                    throw new NoLocationFoundException("No location found");
                }
                log.info("Location found with id: {}", locationId);
                return userService.findAllUsersWithAccessOnLocation(locationId, userId);
            })
            .thenApplyAsync(users -> {
                log.info("Show friends on location successful. Location ID: {}", locationId);
                return ResponseEntity.ok(users);
            });
    }

    @PutMapping("/change/{locationId}/{uId}/")
    public CompletableFuture<ResponseEntity<String>> changeAccess(
        HttpServletRequest request,
        @PathVariable Long locationId,
        @PathVariable Long uId
    ) {

        log.info("Change access request received. Location ID: {}, User ID: {}", locationId, uId);
        Long userId = getUserIdFromRequest(request);

        CompletableFuture<User> locationOwnerFuture = userService.findLocationOwner(locationId, userId);
        CompletableFuture<UserAccess> userAccessFuture = userAccessService.findUserAccess(locationId, uId);

        return locationOwnerFuture.thenCombine(userAccessFuture, (owner, userAccess) -> {
                if (owner == null) {
                    log.warn("Change access failed. Location owner not found. Location ID: {}", locationId);
                    throw new LocationOwnerNotFoundException("Failed to change access");
                }
                if (userAccess == null) {
                    log.warn("Change access failed. User access not found. Location ID: {}, User ID: {}",
                        locationId, uId);
                    throw new UserAccessNotFoundException("User access not found");
                }
                return null;
            })
            .thenComposeAsync(voidResult -> CompletableFuture.runAsync(() -> {
                userAccessService.changeUserAccess(locationId, uId);
            }))
            .thenApplyAsync(voidResult2 -> {
                log.info("Change access successful. Location ID: {}, User ID: {}", locationId, uId);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.add("message", "Access changed successfully");
                return ResponseEntity.ok().headers(headers).build();
            });
    }

    @PostMapping("/add")
    public CompletableFuture<ResponseEntity<Location>> addLocation(
        HttpServletRequest request,
        @RequestBody Location location
    ) {

        log.info("Add location request received");
        Long userId = getUserIdFromRequest(request);

        CompletableFuture<Location> locExistsFuture =
            locationService.findLocationByNameAndUserId(location.getName(),
                userId);

        return locExistsFuture.thenComposeAsync(locExists -> {
            if (locExists != null) {
                log.warn("Add location failed. Location with the same name already exists. Location Name: {}",
                    location.getName());
                throw new AlreadyExistsException("Location with that name already exists");
            } else if (location.getName().isEmpty() || location.getAddress().isEmpty()) {
                log.warn("Add location failed. Empty fields");
                throw new EmptyFieldException("Fields can not be empty");
            }

            return userService.findById(userId)
                .thenApplyAsync(user -> {
                    location.setUser(user);
                    locationService.saveLocation(location);
                    log.info("Add location successful");
                    return ResponseEntity.ok(location);
                });
        });
    }

    @PostMapping("/share/{locationId}/{uId}/")
    public CompletableFuture<ResponseEntity<UserAccess>> shareLocation(
        HttpServletRequest request,
        @PathVariable Long locationId,
        @PathVariable Long uId,
        @RequestBody UserAccess userAccess
    ) {

        log.info("Share location request received. Location ID: {}, User ID: {}", locationId, uId);
        Long userId = getUserIdFromRequest(request);

        CompletableFuture<List<Location>> locationsFuture = locationService.findNotSharedToUserLocations(userId, uId);
        CompletableFuture<User> userToShareFuture = userService.findById(uId);

        return locationsFuture.thenCombine(userToShareFuture, (locations, userToShare) -> {
            boolean containsLocWithId = locations.stream().anyMatch(loc -> loc.getId().equals(locationId));
            if (!containsLocWithId) {
                log.warn("Share location failed. No location to share with id: {}", locationId);
                throw new NoLocationFoundException("No location to share");
            }

            if (userToShare == null) {
                log.warn("Share location failed. User to share not found with id: {}", uId);
                throw new NoUserToShareException("No user to share");
            }

            userAccess.setUser(userToShare);

            CompletableFuture<Location> locFuture = locationService.findById(locationId);
            userAccess.setLocation(locFuture.join());

            CompletableFuture<Void> saveUserAccessFuture =
                    CompletableFuture.runAsync(() -> {
                        userAccessService.saveUserAccess(userAccess);
                    });

            return saveUserAccessFuture.thenApplyAsync(result -> {
                log.info("Share location successful. Location ID: {}, User ID: {}", locationId, uId);
                return ResponseEntity.ok(userAccess);
            });
        }).thenComposeAsync(Function.identity());
    }

    @DeleteMapping("/delete/{locationId}/")
    public CompletableFuture<ResponseEntity<String>> deleteLocation(
        HttpServletRequest request,
        @PathVariable Long locationId
    ) {

        log.info("Delete location request received. Location ID: {}", locationId);
        Long userId = getUserIdFromRequest(request);

        CompletableFuture<User> userFuture = userService.findLocationOwner(locationId, userId);

        return userFuture.thenApplyAsync(owner -> {
            if (owner == null) {
                log.warn("Delete location failed. Not location owner. Location ID: {}", locationId);
                throw new LocationOwnerNotFoundException("Failed to delete location");
            }
            locationService.deleteLocation(locationId, userId);
            log.info("Delete location successful. Location ID: {}", locationId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("message", "Location deleted successfully");
            return ResponseEntity.ok().headers(headers).build();
        });
    }
}

