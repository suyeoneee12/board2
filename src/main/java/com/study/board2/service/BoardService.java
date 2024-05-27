package com.study.board2.service;

import com.study.board2.dto.Board2DTO;
import com.study.board2.entity.BoardFileEntity;
import com.study.board2.entity.BoardEntity;
import com.study.board2.repository.BoardFileRepository;
import com.study.board2.repository.BoardRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// DTO -> Entity (Entity Class)
// Entity -> DTO (DTO Class)

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final BoardFileRepository boardFileRepository;

    public void save(Board2DTO boardDTO) throws IOException {
        // 파일 첨부 여부에 따라 로직 분리
        if (boardDTO.getBoardFile().isEmpty()) {
            // 첨부 파일 없음.
            BoardEntity boardEntity = BoardEntity.toSaveEntity(boardDTO);
            boardRepository.save(boardEntity);
        } else {
            // 첨부 파일 있음.
            /*
                1. DTO에 담긴 파일을 꺼냄
                2. 파일의 이름 가져옴
                3. 서버 저장용 이름을 만듦
                // 내사진.jpg => 839798375892_내사진.jpg
                4. 저장 경로 설정
                5. 해당 경로에 파일 저장
                6. board_table에 해당 데이터 save 처리
                7. board_file_table에 해당 데이터 save 처리
             */
            BoardEntity boardEntity = BoardEntity.toSaveFileEntity(boardDTO);
            Long savedId = boardRepository.save(boardEntity).getId();
            BoardEntity board = boardRepository.findById(savedId).get();
            for(MultipartFile boardFile: boardDTO.getBoardFile()) {
                String originalFilename = boardFile.getOriginalFilename(); // 2.
                String storedFileName = System.currentTimeMillis() + "_" + originalFilename; // 3.
                String savePath = "C:/springboot_img/" + storedFileName; // 4. C:/springboot_img/9802398403948_내사진.jpg
                boardFile.transferTo(new File(savePath)); // 5.
                BoardFileEntity boardFileEntity = BoardFileEntity.toBoardFileEntity(board, originalFilename, storedFileName);
                boardFileRepository.save(boardFileEntity);
            }
        }

    }

    @Transactional
    public List<Board2DTO> findAll() {
        List<BoardEntity> boardEntityList = boardRepository.findAll();
        List<Board2DTO> board2DTOList = new ArrayList<>();
        for(BoardEntity boardEntity: boardEntityList) {
            board2DTOList.add(Board2DTO.toBoardDTO(boardEntity));
        }
        return board2DTOList;
    }

    @Transactional
    public void updateHits(Long id) {
        boardRepository.updatehits(id);
    }

    @Transactional
    public Board2DTO findById(Long id) {
        Optional<BoardEntity> optionalBoardEntity = boardRepository.findById(id);
        if(optionalBoardEntity.isPresent()) {
            BoardEntity boardEntity = optionalBoardEntity.get();
            Board2DTO board2DTO = Board2DTO.toBoardDTO(boardEntity);
            return board2DTO;
        } else {
            return null;
        }
    }

    public Board2DTO update(Board2DTO board2DTO) {
        BoardEntity boardEntity = BoardEntity.toUpdateEntity(board2DTO);
        boardRepository.save(boardEntity);
        return findById(board2DTO.getId());
    }

    public void delete(Long id) {
        boardRepository.deleteById(id);
    }

    public Page<Board2DTO> paging(Pageable pageable) {
        int page = pageable.getPageNumber() - 1;
        int pageLimit = 3;  // 한 페이지에 보여줄 글 갯수
        // 한 페이지당 3개씩 글을 보여주고 정렬 기준은 id 기준으로 내림차순 정렬
        // page 위치에 있는 값은 0부터 시작
        Page<BoardEntity> boardEntities =
                boardRepository.findAll(PageRequest.of(page, pageLimit, Sort.by(Sort.Direction.DESC,"id")));

        System.out.println("boardEntities.getContent() = " + boardEntities.getContent()); // 요청 페이지에 해당하는 글
        System.out.println("boardEntities.getTotalElements() = " + boardEntities.getTotalElements()); // 전체 글 개수
        System.out.println("boardEntities.getNumber() = " + boardEntities.getNumber()); // DB로 요청한 페이지 번호
        System.out.println("boardEntities.getTotalPages() = " + boardEntities.getTotalPages()); // 전체 페이지 갯수
        System.out.println("boardEntities.getSize() = " + boardEntities.getSize()); // 한 페이지에 보여지는 글 갯수
        System.out.println("boardEntities.getPrevious() = " + boardEntities.hasPrevious()); // 이전 페이지 존재 여부
        System.out.println("boardEntities.isFirst() = " +  boardEntities.isFirst()); // 첫 페이지 여부
        System.out.println("boardEntities.isLast() = " + boardEntities.isLast()); // 마지막 페이지 여부

        // 목록: id, writer, title, hits, createdTime
        Page<Board2DTO> board2DTOS = boardEntities.map(board -> new Board2DTO(board.getId(), board.getBoardWriter(), board.getBoardTitle(), board.getBoardHits(), board.getCreatedTime()));
        return board2DTOS;
    }
}
