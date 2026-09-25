package com.dogukan.appointment.handler;

import com.dogukan.appointment.exception.BaseException;
import com.dogukan.appointment.exception.MessageType;
import com.dogukan.appointment.model.RootEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<RootEntity<Object>> handleBaseException(BaseException exception) {
        HttpStatus status = statusFor(exception.getMessageType());
        return ResponseEntity.status(status).body(RootEntity.error(exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RootEntity<Object>> handleValidationException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(RootEntity.error(message));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<RootEntity<Object>> handleAuthenticationException(AuthenticationException exception) {
        String message;
        if (exception instanceof LockedException) {
            message = "Çok fazla hatalı giriş denemesi nedeniyle hesabınız geçici olarak kilitlendi.";
        } else if (exception instanceof DisabledException) {
            message = "Hesabınız doğrulanmamış veya pasif durumda.";
        } else {
            message = "E-posta veya şifre hatalı.";
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RootEntity.error(message));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<RootEntity<Object>> handleAccessDeniedException(AccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(RootEntity.error("Bu işlem için yetkiniz yok."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RootEntity<Object>> handleUnexpectedException(Exception exception) {
        log.error("Beklenmeyen hata", exception);
        return ResponseEntity.internalServerError().body(RootEntity.error("Beklenmeyen bir hata oluştu."));
    }

    private HttpStatus statusFor(MessageType messageType) {
        if (messageType == null) {
            return HttpStatus.BAD_REQUEST;
        }
        return switch (messageType) {
            case USER_NOT_FOUND, DOCTOR_NOT_FOUND, APPOINTMENT_NOT_FOUND, DEPARTMENT_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case EMAIL_ALREADY_EXISTS, TCKNO_ALREADY_EXISTS -> HttpStatus.CONFLICT;
            case INVALID_TOKEN, TOKEN_EXPIRED -> HttpStatus.UNAUTHORIZED;
            case UNAUTHORIZED_REQUEST -> HttpStatus.FORBIDDEN;
            case APPOINTMENT_NOT_CANCELLABLE, DOCTOR_NOT_AVAILABLE -> HttpStatus.CONFLICT;
        };
    }
}
