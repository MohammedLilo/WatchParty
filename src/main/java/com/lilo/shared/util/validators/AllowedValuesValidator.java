package com.lilo.shared.util.validators;

import com.lilo.shared.annotations.AllowedValues;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;

public class AllowedValuesValidator implements ConstraintValidator<AllowedValues, String> {

    private List<String> allowedValues;
    @Override
    public void initialize(AllowedValues constraintAnnotation) {
        this.allowedValues = Arrays.asList(constraintAnnotation.values());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return allowedValues.contains(value);
    }
}