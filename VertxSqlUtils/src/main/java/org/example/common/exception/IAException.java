package org.example.common.exception;

/**
 * 非法访问异常
 * @date 2021-02-02
 */
public class IAException extends RuntimeException{

    public IAException(String message){
        super(message);
    }

}
