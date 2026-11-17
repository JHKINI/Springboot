package kopo.aisw.springboot_basic.mapper;

import kopo.aisw.springboot_basic.domain.Board;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import kopo.aisw.springboot_basic.domain.Criteria;

@Mapper
public interface BoardMapper {
    void save(Board board);
    Board findById(Long boardId);
    List<Board> findAll();
    void update(Board board);
    void delete(Long boardId);
    void updateViewCount(Long boardId);
    // 페이징 + 검색
    List<Board> getListWithPaging(Criteria cri);

    // 검색 결과 전체 개수
    int getTotalCount(Criteria cri);
}
