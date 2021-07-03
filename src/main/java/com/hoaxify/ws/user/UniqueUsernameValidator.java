package com.hoaxify.ws.user;

import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UniqueUsernameValidator implements ConstraintValidator<UniqueUsername, String> {

    private UserRepository userRepository;

    @Autowired
    public UniqueUsernameValidator(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean isValid(final String username, final ConstraintValidatorContext context) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            return false;
        }
        return true;
    }
}
