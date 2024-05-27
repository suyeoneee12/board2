package com.study.board2.controller;

import com.study.board2.dto.Board2DTO;
import com.study.board2.dto.CommentDTO;
import com.study.board2.service.BoardService;
import com.study.board2.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class Board2Controller {
    private final BoardService boardService;
    private final CommentService commentService;

    @GetMapping("/save")
    public String saveForm() { return "save"; }

    @PostMapping("/save")
    public String save(@ModelAttribute Board2DTO boardDTO) throws IOException {
        System.out.println("boardDTO = " + boardDTO);
        boardService.save(boardDTO);

        return "index";
    }

    @GetMapping("/")
    public String findAll(Model model) {
        // DB에서 전체 게시글 데이터를 가져와서 list.html에 보여준다.
        List<Board2DTO> boardDTOList = boardService.findAll();
        model.addAttribute("boardList",boardDTOList);
        return "list";
    }

    @GetMapping("/{id}")
    public String findById(@PathVariable(name="id") Long id, Model model,
                           @PageableDefault(page = 1) Pageable pageable) {
        /*
            해당 게시글의 조회수를 하나 올리고
            게시글 데이터를 가져와서 detail.html에 출력
         */
        boardService.updateHits(id);
        Board2DTO board2DTO = boardService.findById(id);
        /* 댓글 목록 가져오기 */
        List<CommentDTO>  commentDTOList = commentService.findAll(id);
        model.addAttribute("commentList", commentDTOList);

        model.addAttribute("board", board2DTO);
        model.addAttribute("page", pageable.getPageNumber());
        return "detail";
    }

    @GetMapping("update/{id}")
    public String updateForm(@PathVariable(name = "id") Long id, Model model) {
        Board2DTO board2DTO = boardService.findById(id);
        model.addAttribute("boardUpdate", board2DTO);
        return "update";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute Board2DTO board2DTO, Model model) {
        Board2DTO board = boardService.update(board2DTO);
        model.addAttribute("board", board);
        return "detail";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        boardService.delete(id);
        return "redirect:/board/";
    }

    // board/paging/page=1
    @GetMapping("/paging")
    public String Paging(@PageableDefault(page = 1)Pageable pageable, Model model) {
        //pageable.getPageNumber();
        Page<Board2DTO> boardList = boardService.paging(pageable);
        int blockLimit = 3;
        int startPage = (((int)(Math.ceil((double)pageable.getPageNumber() / blockLimit))) - 1) * blockLimit + 1; // 1 4 7 10 ~~
        int endPage = ((startPage + blockLimit - 1) < boardList.getTotalPages()) ? startPage + blockLimit - 1 : boardList.getTotalPages();

        // page 갯수 20개
        // 현재 사용자가 3페이지
        // 1 2 3
        // 현재 사용자가 7페이지
        // 7 8 9
        // 보여지는 이지 갯수 3개
        // 총 페이지 갯수 8개

        model.addAttribute("boardList", boardList);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        return "paging";
    }
}
