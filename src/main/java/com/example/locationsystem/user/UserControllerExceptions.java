package com.example.locationsystem.user;

public class UserControllerExceptions {

    public static class AlreadyExistsException extends RuntimeException {

        public AlreadyExistsException(String message) {

            super(message);
        }
    }

    public static class EmptyFieldException extends RuntimeException {

        public EmptyFieldException(String message) {

            super(message);
        }
    }

    public static class InvalidEmailException extends RuntimeException {

        public InvalidEmailException(String message) {

            super(message);
        }
    }

    public static class InvalidLoginOrPasswordException extends RuntimeException {

        public InvalidLoginOrPasswordException(String message) {

            super(message);
        }
    }

    public static class NotLoggedInException extends RuntimeException {

        public NotLoggedInException(String message) {

            super(message);
        }
    }
}
