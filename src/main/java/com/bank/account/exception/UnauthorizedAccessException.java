package com.bank.account.exception;

/**
 * Exception thrown when user tries to access unauthorized resources
 */
public class UnauthorizedAccessException extends RuntimeException {

    private final String operation;
    private final String resourceId;
    private final String userId;

    public UnauthorizedAccessException(String message) {
        super(message);
        this.operation = null;
        this.resourceId = null;
        this.userId = null;
    }

    public UnauthorizedAccessException(String operation, String resourceId, String userId) {
        super(String.format("User %s is not authorized to perform %s on resource %s", userId, operation, resourceId));
        this.operation = operation;
        this.resourceId = resourceId;
        this.userId = userId;
    }

    public String getOperation() {
        return operation;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getUserId() {
        return userId;
    }
}