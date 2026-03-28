package com.landgo.coreservice.exception;
import org.springframework.http.HttpStatus;
public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException(String message) { super(message, HttpStatus.NOT_FOUND, "NOT_FOUND"); }
    public ResourceNotFoundException(String resource, String field, Object value) { super(resource + " not found with " + field + ": " + value, HttpStatus.NOT_FOUND, "NOT_FOUND"); }
}
