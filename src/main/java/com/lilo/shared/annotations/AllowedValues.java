package com.lilo.shared.annotations;

import com.lilo.shared.util.validators.AllowedValuesValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AllowedValuesValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface AllowedValues {

    // The list of allowed string values
    String[] values() default {};

    // Standard validation message properties
    String message() default "Invalid value. Must be one of {values}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
