package com.example.locationsystem.location;

public class LocationControllerExceptions {

    public static class LocationOwnerNotFoundException extends RuntimeException {

        public LocationOwnerNotFoundException(String message) {

            super(message);
        }
    }

    public static class NoLocationFoundException extends RuntimeException {

        public NoLocationFoundException(String message) {

            super(message);
        }
    }

    public static class UserAccessNotFoundException extends RuntimeException {

        public UserAccessNotFoundException(String message) {

            super(message);
        }
    }

    public static class NoUserToShareException extends RuntimeException {

        public NoUserToShareException(String message) {
            super(message);
        }
    }

}
