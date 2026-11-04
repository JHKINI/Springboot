package kopo.aisw.springboot_basic.controller;

import kopo.aisw.springboot_basic.domain.Board;
import kopo.aisw.springboot_basic.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    // 게시글 목록 페이지
    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("boards", boardService.getAllBoards());
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