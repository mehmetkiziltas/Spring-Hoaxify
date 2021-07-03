package com.hoaxify.ws.user;

import com.hoaxify.ws.error.NotFoundException;
import com.hoaxify.ws.file.FileService;
import com.hoaxify.ws.hoax.HoaxService;
import com.hoaxify.ws.user.vm.UserUpdateVM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.Temporal;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final FileService fileService;

    @Autowired
    public UserService(final UserRepository userRepository,
                       final PasswordEncoder passwordEncoder,
                       final FileService fileService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileService = fileService;
    }

    public void save(final User user) {
        String encryptedPassword = this.passwordEncoder.encode(user.getPassword());
        user.setPassword(encryptedPassword);
        userRepository.save(user);
    }

    public Page<User> getUsers(Pageable page, final User user) {
        if (user != null) {
            return userRepository.findByUsernameNot(user.getUsername(), page);
        }
        return userRepository.findAll(page);
    }

    public User getByUsername(final String username) {
        User inDB = userRepository.findByUsername(username);
        if (inDB == null) {
            throw new NotFoundException();
        }
        return inDB;
    }

    public User updateUser(final String username, final UserUpdateVM updatedUser) {
        User inDB = getByUsername(username);
        inDB.setDisplayName(updatedUser.getDisplayName());
        if (updatedUser.getImage() != null) {
            final String oldImageName = inDB.getImage();
            final String storedFileName = fileService.writeBase64EncodedStringToFile(updatedUser.getImage());
            inDB.setImage(storedFileName);
            fileService.deleteProfileImage(oldImageName);
        }

        return userRepository.save(inDB);
    }

    public void deleteUser(final String username) {
        User inDB = userRepository.findByUsername(username);
        fileService.deleteAllStoredFilesForUser(inDB);
        userRepository.delete(inDB);
    }
}
