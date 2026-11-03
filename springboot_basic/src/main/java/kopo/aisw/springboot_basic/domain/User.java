package kopo.aisw.springboot_basic.domain;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
public class User {
    private String userId;
    private String userPassword;
    private String userName;
    private String userAuth;
    private LocalDateTime regDate;
    private LocalDateTime modDate;
}
