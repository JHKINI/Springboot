package kopo.aisw.springboot_basic.domain;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class Comment {
    private Long commentId;
    private Long boardId;
    private String userId;
    private String content;
    private LocalDateTime regDate;
    private LocalDateTime modDate;
}