package org.gagan.florasense.Exception;

public class InvalidEmailOrPasswordException extends RuntimeException{
    public InvalidEmailOrPasswordException(String message){
        super(message);
    }
}
