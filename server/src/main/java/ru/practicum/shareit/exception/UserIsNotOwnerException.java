package ru.practicum.shareit.exception;

public class UserIsNotOwnerException extends RuntimeException {
    public UserIsNotOwnerException(String msg) {
        super(msg);
    }
}
