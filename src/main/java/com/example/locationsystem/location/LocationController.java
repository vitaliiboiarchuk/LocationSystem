package com.example.locationsystem.location;

import com.example.locationsystem.user.User;
import com.example.locationsystem.user.UserService;
import com.example.locationsystem.userAccess.UserAccess;
import com.example.locationsystem.userAccess.UserAccessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.example.locationsystem.exception.ControllerExceptions.*;

@RestController
@RequestMapping("/location")
public class LocationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocationController.class);

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
            LOGGER.warn("User not logged in");
            throw new NotLoggedInException("Not logged in");
        }
        LOGGER.info("User logged in");
        return Long.valueOf(cookie.getValue());
    }

    @GetMapping("")
    public CompletableFuture<ResponseEntity<List<Location>>> showLocations(HttpServletRequest request) {

        LOGGER.info("Show locations request received");
        Long userId = getUserIdFromRequest(request);

        return locationService.findAllMyLocations(userId)
            .thenApplyAsync(locations -> {
                LOGGER.info("Show locations successful");
                return ResponseEntity.ok(locations);
            });
    }

    @GetMapping("/{locationId}/")
    public CompletableFuture<ResponseEntity<List<User>>> showFriendsOnLocation(
        HttpServletRequest request, @PathVariable Long locationId
    ) {

        LOGGER.info("Show friends on location request received. Location ID: {}", locationId);
        Long userId = getUserIdFromRequest(request);

        return locationService.findAllMyLocations(userId)
            .thenComposeAsync(locations -> {
                boolean containsLocWithId = locations.stream().anyMatch(loc -> loc.getId().equals(locationId));
                if (!containsLocWithId) {
                    LOGGER.warn("No location found with id: {}", locationId);
                    throw new NoLocationFoundException("No location found");
                }
                LOGGER.info("Location found with id: {}", locationId);
                return userService.findAllUsersWithAccessOnLocation(locationId, userId);
            })
            .thenApplyAsync(users -> {
                LOGGER.info("Show friends on location successful. Location ID: {}", locationId);
                return ResponseEntity.ok(users);
            });
    }

    @PutMapping("/change/{locationId}/{uId}/")
    public CompletableFuture<ResponseEntity<Void>> changeAccess(
        HttpServletRequest request,
        @PathVariable Long locationId,
        @PathVariable Long uId
    ) {

        LOGGER.info("Change access request received. Location ID: {}, User ID: {}", locationId, uId);
        Long userId = getUserIdFromRequest(request);

        CompletableFuture<User> locationOwnerFuture = userService.findLocationOwner(locationId, userId);
        CompletableFuture<UserAccess> userAccessFuture = userAccessService.findUserAccess(locationId, uId);

        return locationOwnerFuture.thenCombine(userAccessFuture, (owner, userAccess) -> {
                if (owner == null) {
                    LOGGER.warn("Change access failed. Location owner not found. Location ID: {}", locationId);
                    throw new LocationOwnerNotFoundException("Location owner not found");
                }
                if (userAccess == null) {
                    LOGGER.warn("Change access failed. User access not found. Location ID: {}, User ID: {}",
                        locationId, uId);
                    throw new UserAccessNotFoundException("User access not found");
                }
                return null;
            })
            .thenComposeAsync(voidResult -> CompletableFuture.runAsync(() -> {
                userAccessService.changeUserAccess(locationId, uId);
            }))
            .thenApplyAsync(voidResult2 -> {
                LOGGER.info("Change access successful. Location ID: {}, User ID: {}", locationId, uId);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.add("message", "Access changed successfully");
                return ResponseEntity.ok().headers(headers).build();
            });
    }

    @PostMapping("/add")
    public CompletableFuture<ResponseEntity<Location>> addLocation(
        HttpServletRequest request,
        @Valid @RequestBody Location location
    ) {

        LOGGER.info("Add location request received");
        Long userId = getUserIdFromRequest(request);

        CompletableFuture<Location> locExistsFuture =
            locationService.findLocationByNameAndUserId(location.getName(),
                userId);

        return locExistsFuture.thenComposeAsync(locExists -> {
            if (locExists != null) {
                LOGGER.warn("Add location failed. Location with the same name already exists. Location Name: {}",
                    location.getName());
                throw new AlreadyExistsException("Location with that name already exists");
            }
            return userService.findById(userId)
                .thenApplyAsync(user -> {
                    location.setUser(user);
                    locationService.saveLocation(location);
                    LOGGER.info("Add location successful");
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

        LOGGER.info("Share location request received. Location ID: {}, User ID: {}", locationId, uId);
        Long userId = getUserIdFromRequest(request);

        CompletableFuture<List<Location>> locationsFuture = locationService.findNotSharedToUserLocations(userId, uId);
        CompletableFuture<User> userToShareFuture = userService.findById(uId);

        return locationsFuture.thenCombine(userToShareFuture, (locations, userToShare) -> {
            boolean containsLocWithId = locations.stream().anyMatch(loc -> loc.getId().equals(locationId));
            if (!containsLocWithId) {
                LOGGER.warn("Share location failed. No location to share with id: {}", locationId);
                throw new NoLocationFoundException("No location to share");
            }

            if (userToShare == null) {
                LOGGER.warn("Share location failed. No user to share with id: {}", uId);
                throw new NoUserToShareException("No user to share");
            }

            if (Objects.equals(uId, userId)) {
                LOGGER.warn("Share location failed. Can't share location to yourself. User id: {}", userId);
                throw new SelfShareException("Can't share to yourself");
            }

            userAccess.setUser(userToShare);

            CompletableFuture<Location> locationFuture = locationService.findById(locationId);
            CompletableFuture<User> ownerFuture = userService.findById(userId);

            CompletableFuture<Location> locWithOwnerFuture = CompletableFuture.allOf(locationFuture, ownerFuture)
                .thenApplyAsync(result -> {
                    Location location = locationFuture.join();
                    User user = ownerFuture.join();
                    location.setUser(user);
                    return location;
                });

            Location locationWithOwner = locWithOwnerFuture.join();
            userAccess.setLocation(locationWithOwner);

            CompletableFuture<Void> saveUserAccessFuture =
                CompletableFuture.runAsync(() -> {
                    userAccessService.saveUserAccess(userAccess);
                });

            return saveUserAccessFuture.thenApplyAsync(result -> {
                LOGGER.info("Share location successful. Location ID: {}, User ID: {}", locationId, uId);
                return ResponseEntity.ok(userAccess);
            });
        }).thenComposeAsync(Function.identity());
    }

    @DeleteMapping("/delete/{locationId}/")
    public CompletableFuture<ResponseEntity<Void>> deleteLocation(
        HttpServletRequest request,
        @PathVariable Long locationId
    ) {

        LOGGER.info("Delete location request received. Location ID: {}", locationId);
        Long userId = getUserIdFromRequest(request);

        CompletableFuture<User> userFuture = userService.findLocationOwner(locationId, userId);

        return userFuture.thenApplyAsync(owner -> {
            if (owner == null) {
                LOGGER.warn("Delete location failed. Not location owner. Location ID: {}", locationId);
                throw new LocationOwnerNotFoundException("Location owner not found");
            }
            locationService.deleteLocation(locationId, userId);
            LOGGER.info("Delete location successful. Location ID: {}", locationId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("message", "Location deleted successfully");
            return ResponseEntity.ok().headers(headers).build();
        });
    }
}

