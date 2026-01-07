package com.lilo.shared.annotations;

import com.lilo.shared.util.validators.MultiPartFileValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.springframework.http.MediaType;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MultiPartFileValidator.class)
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidMultipartFile {

    String[] allowedTypes() default {MediaType.ALL_VALUE};

    // The maximum size allowed (default 5MB)
    long maxFileSize() default Long.MAX_VALUE;

    // Error messages
    String invalidFileTypeMessage() default "Invalid file type";

    String invalidSizeMessage() default "File size exceeds the allowed limit";

    // Standard Bean Validation fields
    String message() default "Invalid file";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
