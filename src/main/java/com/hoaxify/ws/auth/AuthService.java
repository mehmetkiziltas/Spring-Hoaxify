package com.hoaxify.ws.auth;

import com.hoaxify.ws.user.User;
import com.hoaxify.ws.user.UserRepository;
import com.hoaxify.ws.user.vm.UserVM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;

    @Autowired
    public AuthService(final UserRepository userRepository,
                       final PasswordEncoder passwordEncoder,
                       final TokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenRepository = tokenRepository;
    }

    public AuthResponse authenticate(final Credentials credentials) {
        User inDB = userRepository.findByUsername(credentials.getUsername());
        if (inDB == null) {
            throw new AuthException();
        }
        final boolean matches = passwordEncoder.matches(credentials.getPassword(), inDB.getPassword());
        if (!matches) {
            throw new AuthException();
        }
        final UserVM userVM = new UserVM(inDB);
        final String token = generateRandomToken();
        final Token tokenEntity = new Token();
        tokenEntity.setToken(token);
        tokenEntity.setUser(inDB);
        tokenRepository.save(tokenEntity);
        final AuthResponse response = new AuthResponse();
        response.setUser(userVM);
        response.setToken(token);
        return response;
    }

    public UserDetails getUserDetails(final String token) {
        final Optional<Token> optionalToken = tokenRepository.findById(token);
        return optionalToken.<UserDetails>map(Token::getUser).orElse(null);
    }

    public String generateRandomToken() {
        return UUID.randomUUID().toString().replaceAll(",","");
    }

    public void clearToken(final String token) {
        tokenRepository.deleteById(token);
    }
}
