package com.hoaxify.ws.hoax;

import com.hoaxify.ws.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class HoaxSecurityService {

    private final HoaxRepository hoaxRepository;

    @Autowired
    public HoaxSecurityService(final HoaxRepository hoaxRepository) {
        this.hoaxRepository = hoaxRepository;
    }

    public boolean isAllowedToDelete(long id, User loggedInUser) {
        final Optional<Hoax> optionalHoax = hoaxRepository.findById(id);
        if (!optionalHoax.isPresent()) {
            return false;
        }
        Hoax hoax = optionalHoax.get();
        if (hoax.getUser().getId() != loggedInUser.getId()) {
            return false;
        }
        return true;
    }
}
