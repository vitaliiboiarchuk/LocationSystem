package com.example.locationsystem.exception;

public class ControllerExceptions {

    public static class AlreadyExistsException extends RuntimeException {

        public AlreadyExistsException(String message) {

            super(message);
        }
    }

    public static class InvalidLoginOrPasswordException extends RuntimeException {

        public InvalidLoginOrPasswordException(String message) {

            super(message);
        }
    }

    public static class LocationOwnerNotFoundException extends RuntimeException {

        public LocationOwnerNotFoundException(String message) {

            super(message);
        }
    }

    public static class LocationNotFoundException extends RuntimeException {

        public LocationNotFoundException(String message) {

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

    public static class SelfShareException extends RuntimeException {

        public SelfShareException(String message) {

            super(message);
        }
    }

    public static class NotLoggedInException extends RuntimeException {

        public NotLoggedInException(String message) {

            super(message);
        }
    }
}
