
package kopo.aisw.springboot_basic.domain;

import lombok.Getter;

@Getter
public class PageDTO {

    private int startPage;
    private int endPage;
    private boolean prev;
    private boolean next;
    private int total;
    private Criteria cri;

    public PageDTO(Criteria cri, int total) {
        this.cri = cri;
        this.total = total;

        this.endPage = (int) (Math.ceil(cri.getPageNum() / 10.0)) * 10;
        this.startPage = this.endPage - 9;

        int realEnd = (int) Math.ceil((total * 1.0) / cri.getAmount());

        if (realEnd < this.endPage) {
            this.endPage = realEnd;
        }

        // 검색 결과가 0건이면 realEnd가 0이 되어 페이지 버튼이 "1, 0"으로 만들어지므로 최소 1로 보정
        if (this.endPage < 1) {
            this.endPage = 1;
        }

        this.prev = this.startPage > 1;
        this.next = this.endPage < realEnd;
    }
}
