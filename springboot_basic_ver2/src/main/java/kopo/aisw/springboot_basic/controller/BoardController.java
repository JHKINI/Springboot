package kopo.aisw.springboot_basic.controller;

import kopo.aisw.springboot_basic.domain.Board;
import kopo.aisw.springboot_basic.domain.Criteria;
import kopo.aisw.springboot_basic.domain.PageDTO;
import kopo.aisw.springboot_basic.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    // 게시글 목록 페이지

    @GetMapping("/list")
    public String list(Criteria cri, Model model) {

        // 페이징된 게시글 조회
        List<Board> boardList = boardService.getListWithPaging(cri);

        // 검색 조건에 맞는 전체 게시글 수
        int total = boardService.getTotalCount(cri);

        // 화면에 전달
        model.addAttribute("boards", boardList);
        model.addAttribute("pageMaker", new PageDTO(cri, total));

        return "board/list";
    }


    // 게시글 작성 페이지
    @GetMapping("/write")
    public String writeForm() {
        return "board/write";
    }

    // 게시글 저장
    @PostMapping("/write")
    public String write(Board board, Authentication auth) {
        board.setUserId(auth.getName());
        boardService.saveBoard(board);
        return "redirect:/board/list";
    }

    // 게시글 상세 페이지
    @GetMapping("/view/{boardId}")
    public String view(@PathVariable Long boardId, Model model) {
        model.addAttribute("board", boardService.getBoardById(boardId));
        return "board/view";
    }

    // 게시글 수정 페이지
    @GetMapping("/edit/{boardId}")
    public String editForm(@PathVariable Long boardId, Model model) {
        model.addAttribute("board", boardService.getBoardById(boardId));
        return "board/edit";
    }

    // 게시글 수정
    @PostMapping("/edit/{boardId}")
    public String edit(@PathVariable Long boardId, Board board) {
        board.setBoardId(boardId);
        boardService.updateBoard(board);
        return "redirect:/board/view/" + boardId;
    }

    // 게시글 삭제
    @PostMapping("/delete/{boardId}")
    public String delete(@PathVariable Long boardId) {
        boardService.deleteBoard(boardId);
        return "redirect:/board/list";
    }

}