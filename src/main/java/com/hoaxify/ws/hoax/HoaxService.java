package com.hoaxify.ws.hoax;

import com.hoaxify.ws.file.FileAttachment;
import com.hoaxify.ws.file.FileAttachmentRepository;
import com.hoaxify.ws.file.FileService;
import com.hoaxify.ws.hoax.vm.HoaxSubmitVM;
import com.hoaxify.ws.user.User;
import com.hoaxify.ws.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class HoaxService {

    private final HoaxRepository hoaxRepository;
    private UserService userService;
    public final FileAttachmentRepository fileAttachmentRepository;
    private final FileService fileService;

    @Autowired
    public HoaxService(final HoaxRepository hoaxRepository,
                       UserService userService,
                       final FileAttachmentRepository fileAttachmentRepository,
                       final FileService fileService) {
        this.hoaxRepository = hoaxRepository;
        this.fileAttachmentRepository = fileAttachmentRepository;
        this.fileService = fileService;
        this.userService = userService;
    }

    public void save(final HoaxSubmitVM hoaxSubmitVM, User user) {
        Hoax hoax = new Hoax();
        hoax.setContent(hoaxSubmitVM.getContent());
        hoax.setTimestamp(new Date());
        hoax.setUser(user);
        hoaxRepository.save(hoax);
        final Optional<FileAttachment> optionalFileAttachment = fileAttachmentRepository.findById(hoaxSubmitVM.getAttachmentId());
        if (optionalFileAttachment.isPresent()) {
            final FileAttachment fileAttachment = optionalFileAttachment.get();
            fileAttachment.setHoax(hoax);
            fileAttachmentRepository.save(fileAttachment);
        }
    }

    public Page<Hoax> getHoaxes(final Pageable page) {
        return hoaxRepository.findAll(page);
    }

    public Page<Hoax> getHoaxesOfUser(final String username,
                                      final Pageable page) {
        User inDB = userService.getByUsername(username);
        return hoaxRepository.findByUser(inDB, page);
    }

    public Page<Hoax> getOldHoaxes(final long id, final String username, final Pageable page) {
        Specification<Hoax> specification = idLessThan(id);
        if (username != null) {
            User inDB = userService.getByUsername(username);
            specification = specification.and(userIs(inDB));
        }
        return hoaxRepository.findAll(specification, page);
    }

    public long getNewHoaxesCount(final long id, final String username) {
        Specification<Hoax> specification = idGreaterThan(id);
        if (username != null) {
            User inDB = userService.getByUsername(username);
            specification = specification.and(userIs(inDB));
        }
        return hoaxRepository.count(specification);
    }

    public List<Hoax> getNewHoaxes(final long id, final String username, final Sort sort) {
        Specification<Hoax> specification = idGreaterThan(id);
        if (username != null) {
            User inDB = userService.getByUsername(username);
            specification = specification.and(userIs(inDB));
        }
        return hoaxRepository.findAll(specification, sort);
    }

    Specification<Hoax> idLessThan(long id) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.lessThan(root.get("id"), id);
    }

    Specification<Hoax> userIs(User user) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get("user"), user);
    }

    Specification<Hoax> idGreaterThan(long id) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.greaterThan(root.get("id"), id);
    }

    public void delete(final long id) {
        Hoax inDB = hoaxRepository.getOne(id);
        if (inDB.getFileAttachment() != null) {
            final String fileName = inDB.getFileAttachment().getName();
            fileService.deleteAttachmentFile(fileName);
        }
        hoaxRepository.deleteById(id);
    }

//    public void deleteHoaxesOfUser(String username) {
//        User inDB = userService.getByUsername(username);
//        Specification<Hoax> userOwned = userIs(inDB);
//        final List<Hoax> hoaxesToBeRemoved = hoaxRepository.findAll(userOwned);
//        hoaxRepository.deleteAll(hoaxesToBeRemoved);
//    }
}