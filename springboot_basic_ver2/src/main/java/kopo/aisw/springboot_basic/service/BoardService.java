package kopo.aisw.springboot_basic.service;

import kopo.aisw.springboot_basic.domain.Board;
import kopo.aisw.springboot_basic.mapper.BoardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import kopo.aisw.springboot_basic.domain.Criteria;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardMapper boardMapper;

    // 게시글 저장
    public void saveBoard(Board board) {
        boardMapper.save(board);
    }

    // 게시글 상세 조회
    public Board getBoardById(Long boardId) {
        // 조회수 증가
        boardMapper.updateViewCount(boardId);
        return boardMapper.findById(boardId);
    }

    // 게시글 목록 조회
    public List<Board> getAllBoards() {
        return boardMapper.findAll();
    }

    // 게시글 수정
    public void updateBoard(Board board) {
        boardMapper.update(board);
    }

    // 게시글 삭제
    public void deleteBoard(Long boardId) {
        boardMapper.delete(boardId);
    }
    // 페이징된 게시글 목록
    public List<Board> getListWithPaging(Criteria cri) {
        return boardMapper.getListWithPaging(cri);
    }

    // 검색 결과 전체 개수
    public int getTotalCount(Criteria cri) {
        return boardMapper.getTotalCount(cri);
    }
}