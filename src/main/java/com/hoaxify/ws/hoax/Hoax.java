package com.hoaxify.ws.hoax;

import com.hoaxify.ws.file.FileAttachment;
import com.hoaxify.ws.user.User;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
public class Hoax {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(length = 2000)
    private String content;

    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    @ManyToOne
    private User user;

//    (orphanRemoval = true , cascade = CascadeType.REMOVE) ikisi aynı işi yapar
    @OneToOne(mappedBy = "hoax", cascade = CascadeType.ALL)
    private FileAttachment fileAttachment;
}
