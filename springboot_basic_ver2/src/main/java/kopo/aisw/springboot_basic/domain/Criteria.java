
package kopo.aisw.springboot_basic.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Criteria {

    private int pageNum;
    private int amount;

    private String type;
    private String keyword;

    public Criteria() {
        this(1, 10);
    }

    public Criteria(int pageNum, int amount) {
        this.pageNum = pageNum;
        this.amount = amount;
    }

    // 건너뛸 게시글 수
    public int getOffset() {
        return (pageNum - 1) * amount;
    }

    // 검색 유형을 배열로 변환
    public String[] getTypeArr() {
        return type == null ? new String[]{} : type.split("");
    }
}
