package com.example.locationsystem.location;

import com.example.locationsystem.annotation.GetAndValidUserId;
import com.example.locationsystem.user.User;
import com.example.locationsystem.user.UserService;
import com.example.locationsystem.userAccess.UserAccess;
import com.example.locationsystem.userAccess.UserAccessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.example.locationsystem.exception.ControllerExceptions.*;

@RestController
@RequestMapping("/location")
@Log4j2
@RequiredArgsConstructor
public class LocationController {

    private final UserService userService;
    private final LocationService locationService;
    private final UserAccessService userAccessService;

    @GetAndValidUserId
    @GetMapping("")
    public CompletableFuture<ResponseEntity<List<Location>>> showLocations(Long userId) {

        return locationService.findAllUserLocations(userId)
            .thenApply(ResponseEntity::ok);
    }

    @GetAndValidUserId
    @PostMapping("/add")
    public CompletableFuture<ResponseEntity<Location>> addLocation(
        Long userId,
        @Valid @RequestBody Location location
    ) {

        CompletableFuture<Location> locExistsFuture =
            locationService.findLocationByNameAndUserId(location.getName(),
                userId);

        return locExistsFuture.thenCompose(locExists -> {
            if (locExists != null) {
                log.warn("Add location failed. Location with the same name already exists. Location Name={}",
                    location.getName());
                throw new AlreadyExistsException("Location with that name already exists");
            }
            return locationService.saveLocation(location, userId).thenApply(savedLocation -> {
                log.info("Add location successful");
                return ResponseEntity.ok(savedLocation);
            });
        });
    }

    @GetAndValidUserId
    @GetMapping("/{locationId}/")
    public CompletableFuture<ResponseEntity<List<User>>> showFriendsOnLocation(
        Long userId, @PathVariable Long locationId
    ) {

        return locationService.findLocationInUserLocations(userId, locationId)
            .thenCompose(location -> {
                if (location == null) {
                    log.warn("No location found with id={}", locationId);
                    throw new LocationNotFoundException("No location found");
                }
                log.info("Location found with id={}", locationId);
                return userService.findAllUsersOnLocation(locationId, userId);
            })
            .thenApply(users -> {
                log.info("Show friends on location successful. Location ID={}", locationId);
                return ResponseEntity.ok(users);
            });
    }

    @GetAndValidUserId
    @PostMapping("/share")
    public CompletableFuture<ResponseEntity<UserAccess>> shareLocation(
        Long userId,
        @RequestBody UserAccess userAccess
    ) {

        CompletableFuture<Location> locationFuture = locationService.findNotSharedToUserLocation(userId,
            userAccess.getLocationId(),
            userAccess.getUserId());
        CompletableFuture<User> userToShareFuture = userService.findUserById(userAccess.getUserId());

        return locationFuture.thenCombine(userToShareFuture, (location, userToShare) -> {
            if (location == null) {
                log.warn("Share location failed. No location to share with id={}", userAccess.getLocationId());
                throw new LocationNotFoundException("No location to share");
            }
            if (userToShare == null) {
                log.warn("Share location failed. No user to share with id={}", userAccess.getUserId());
                throw new NoUserToShareException("No user to share");
            }
            if (userAccess.getUserId().equals(userId)) {
                log.warn("Share location failed. Can't share location to yourself. User id={}", userId);
                throw new SelfShareException("Can't share to yourself");
            }

            return userAccessService.saveUserAccess(userAccess).thenApply(result -> {
                log.info("Share location successful. Location ID={}, User ID={}", userAccess.getLocationId(),
                    userAccess.getUserId());
                return ResponseEntity.ok(result);
            });
        }).thenCompose(Function.identity());
    }

    @GetAndValidUserId
    @PutMapping("/change")
    public CompletableFuture<ResponseEntity<UserAccess>> changeAccess(
        Long userId, @RequestBody UserAccess userAccess
    ) {

        CompletableFuture<User> locationOwnerFuture = userService.findLocationOwner(userAccess.getLocationId());
        CompletableFuture<UserAccess> userAccessFuture = userAccessService.findUserAccess(userAccess);

        return locationOwnerFuture.thenCombine(userAccessFuture, (owner, access) -> {
            if (owner == null || !owner.getId().equals(userId)) {
                log.warn("Change access failed. Location owner not found. Location ID={}",
                    userAccess.getLocationId());
                throw new LocationOwnerNotFoundException("Location owner not found");
            }
            if (access == null) {
                log.warn("Change access failed. User access not found. Location ID={}, User ID={}",
                    userAccess.getLocationId(), userAccess.getUserId());
                throw new UserAccessNotFoundException("User access not found");
            }
            return userAccessService.changeUserAccess(userAccess)
                .thenCompose(result ->
                    userAccessService.findUserAccess(userAccess)
                        .thenApply(updatedUserAccess -> {
                            log.info("Change access successful. Location ID={}, User ID={}",
                                userAccess.getLocationId(), userAccess.getUserId());
                            return ResponseEntity.ok(updatedUserAccess);
                        })
                );
        }).thenCompose(Function.identity());
    }

    @GetAndValidUserId
    @DeleteMapping("/delete/{name}/")
    public CompletableFuture<ResponseEntity<Void>> deleteLocation(
        Long userId, @PathVariable String name
    ) {

        CompletableFuture<Location> locationFuture = locationService.findLocationByNameAndUserId(name, userId);

        return locationFuture.thenCompose(location -> {
            if (location == null) {
                log.warn("Location not found. Location name={}", name);
                throw new LocationNotFoundException("No location found");
            }
            return locationService.deleteLocation(location.getId(), userId)
                .thenApply(deleted -> {
                    log.info("Delete location successful. Location ID={}", location.getId());
                    return ResponseEntity.ok().build();
                });
        });
    }
}


