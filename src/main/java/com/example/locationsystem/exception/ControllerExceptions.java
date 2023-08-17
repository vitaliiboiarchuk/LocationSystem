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

    public static class LocationNotFoundException extends RuntimeException {

        public LocationNotFoundException(String message) {

            super(message);
        }
    }

    public static class UserNotFoundException extends RuntimeException {

        public UserNotFoundException(String message) {

            super(message);
        }
    }

    public static class LocationOrUserNotFoundException extends RuntimeException {

        public LocationOrUserNotFoundException(String message) {

            super(message);
        }
    }

    public static class UserAccessNotFoundException extends RuntimeException {

        public UserAccessNotFoundException(String message) {

            super(message);
        }
    }

    public static class NotLoggedInException extends RuntimeException {

        public NotLoggedInException(String message) {

            super(message);
        }
    }

    public static class UserSaveException extends RuntimeException {

        public UserSaveException(String message) {

            super(message);
        }
    }

    public static class LocationSaveException extends RuntimeException {

        public LocationSaveException(String message) {

            super(message);
        }
    }

    public static class UserAccessSaveException extends RuntimeException {

        public UserAccessSaveException(String message) {

            super(message);
        }
    }
}
