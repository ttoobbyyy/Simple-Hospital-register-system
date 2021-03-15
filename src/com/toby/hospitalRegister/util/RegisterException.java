package com.toby.hospitalRegister.util;

public class RegisterException extends Exception{
    public enum ErrorCode{
        noError,
        registerCategoryNotFound,
        registerIdExceeded,
        patientNotExist,
        sqlException,
        retryTimeExceeded,
    }
    public ErrorCode error;
    public RegisterException(String reason, ErrorCode err){
        super(reason);
        this.error = err;
    }
}
