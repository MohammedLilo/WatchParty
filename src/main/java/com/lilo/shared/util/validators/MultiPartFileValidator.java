package com.lilo.shared.util.validators;

import com.lilo.shared.annotations.ValidMultipartFile;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.tika.Tika;
import org.springframework.util.InvalidMimeTypeException;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

public class MultiPartFileValidator implements ConstraintValidator<ValidMultipartFile, MultipartFile> {

    private static final Tika TIKA = new Tika();

    private Set<String> allowedTypes;
    private long maxFileSize;
    private String invalidFileTypeMessage;
    private String invalidSizeMessage;

    @Override
    public void initialize(ValidMultipartFile constraintAnnotation) {
        this.maxFileSize = constraintAnnotation.maxFileSize();
        this.allowedTypes = Set.of(constraintAnnotation.allowedTypes());
        this.invalidFileTypeMessage = constraintAnnotation.invalidFileTypeMessage();
        this.invalidSizeMessage = constraintAnnotation.invalidSizeMessage();
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        // Null or empty check upfront
        if (file == null) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        StringBuilder combinedMessage = new StringBuilder();
        boolean isValid = true;

        // 1. Check Size
        if (file.getSize() > maxFileSize) {
            combinedMessage.append(invalidSizeMessage);
            isValid = false;
        }

        // 2. Check Type
        try {
            String detectedMimeType = TIKA.detect(file.getBytes());

            // Check if detected type matches ANY of the allowed patterns
            boolean typeIsAllowed = allowedTypes.stream()
                    .anyMatch(pattern -> isMimeTypeMatch(pattern, detectedMimeType));

            if (!typeIsAllowed) {
                if (!isValid) combinedMessage.append(" & ");
                combinedMessage.append(invalidFileTypeMessage);
                isValid = false;
            }
        } catch (IOException e) {
            if (!isValid) combinedMessage.append(" & ");
            combinedMessage.append("Unable to detect file type");
            isValid = false;
        }

        // If any check failed, report the combined message
        if (!isValid) {
            context.buildConstraintViolationWithTemplate(combinedMessage.toString())
                    .addConstraintViolation();
        }

        return isValid;
    }

    private boolean isMimeTypeMatch(String pattern, String detected) {
        try {
            // MimeTypeUtils handles wildcards like */* and image/* correctly
            return MimeTypeUtils.parseMimeType(pattern).includes(MimeTypeUtils.parseMimeType(detected));
        } catch (InvalidMimeTypeException e) {
            return false;
        }
    }
}
