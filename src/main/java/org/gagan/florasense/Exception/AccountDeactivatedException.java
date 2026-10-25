package org.gagan.florasense.Exception;

public class AccountDeactivatedException extends  RuntimeException{
    public AccountDeactivatedException(String message){
        super(message);
    }
}
