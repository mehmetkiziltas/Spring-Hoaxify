package com.hoaxify.ws.shared;

import com.hoaxify.ws.file.FileService;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.stream.Collectors;

public class FileTypeValidator implements ConstraintValidator<FileType, String> {

    private FileService fileService;
    String[] types;

    @Autowired
    public FileTypeValidator(final FileService fileService) {
        this.fileService = fileService;
    }

    @Override
    public void initialize(final FileType constraintAnnotation) {
        this.types = constraintAnnotation.types();
    }

    @Override
    public boolean isValid(final String s, final ConstraintValidatorContext context) {
        if (s == null || s.isEmpty()) {
            return true;
        }
        String fileType = fileService.detectType(s);
        for (String supportedType : this.types) {
            if (fileType.contains(supportedType)) {
                return true;
            }
        }
        String supportedTypes = Arrays.stream(this.types).collect(Collectors.joining(", "));
        context.disableDefaultConstraintViolation();
        final HibernateConstraintValidatorContext hibernateConstraintValidatorContext = context.unwrap(HibernateConstraintValidatorContext.class);
        hibernateConstraintValidatorContext.addMessageParameter("types",supportedTypes);
        hibernateConstraintValidatorContext.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate()).addConstraintViolation();
        return false;
    }
}
