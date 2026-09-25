package com.dogukan.appointment.exception;

public class BaseException extends RuntimeException {

    private final MessageType messageType;

    public BaseException(ErrorMessage message) {
        super(message.prepareErrorMessage());
        this.messageType = message.getMessageType();
    }

    public MessageType getMessageType() {
        return messageType;
    }
}
