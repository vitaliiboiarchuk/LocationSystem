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

        return locationService.findLocationByNameAndUserId(location.getName(), userId)
            .thenCompose(locExists -> {
                if (locExists.isPresent()) {
                    log.warn("Add location failed. Location with the same name already exists. Location name={}",
                        location.getName());
                    throw new AlreadyExistsException("Location with that name already exists");
                }
                return locationService.saveLocation(location, userId)
                    .thenApply(ResponseEntity::ok);
            });
    }

    @GetAndValidUserId
    @GetMapping("/{locationId}/")
    public CompletableFuture<ResponseEntity<List<User>>> showFriendsOnLocation(
        Long userId, @PathVariable Long locationId
    ) {

        return locationService.findLocationInUserLocations(userId, locationId)
            .thenCompose(location -> userService.findAllUsersOnLocation(locationId, userId))
            .thenApply(ResponseEntity::ok);
    }

    @GetAndValidUserId
    @PostMapping("/share")
    public CompletableFuture<ResponseEntity<UserAccess>> shareLocation(
        Long userId,
        @RequestBody UserAccess userAccess
    ) {

        return locationService.findNotSharedToUserLocation(userId, userAccess.getLocationId(), userAccess.getUserId())
            .thenCompose(location -> userAccessService.saveUserAccess(userAccess)
                .thenApply(ResponseEntity::ok));
    }

    @GetAndValidUserId
    @PutMapping("/change")
    public CompletableFuture<ResponseEntity<UserAccess>> changeAccess(
        Long userId, @RequestBody UserAccess userAccess
    ) {

        return userAccessService.findUserAccess(userAccess, userId)
            .thenCompose(access -> userAccessService.changeUserAccess(userAccess)
                .thenCompose(result ->
                    userAccessService.findUserAccess(userAccess, userId)
                        .thenApply(ResponseEntity::ok)
                ));
    }

    @GetAndValidUserId
    @DeleteMapping("/delete/{name}/")
    public CompletableFuture<ResponseEntity<Void>> deleteLocation(
        Long userId, @PathVariable String name
    ) {

        return userService.findLocationOwner(name, userId)
            .thenCompose(owner -> locationService.deleteLocation(name, userId)
                .thenApply(deleted -> ResponseEntity.ok().build()));
    }
}


