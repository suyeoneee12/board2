package com.study.board2.dto;

import com.study.board2.entity.BaseEntity;
import com.study.board2.entity.BoardEntity;
import com.study.board2.entity.BoardFileEntity;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// DTO(Data Transfer Object), VO, Bean, Entity
@Getter
@Setter
@ToString
@NoArgsConstructor // 기본 생성자
@AllArgsConstructor // 모든 필드를 매개변수로 하는 생성자
public class Board2DTO {
    private Long id;
    private String boardWriter;
    private String boardPass;
    private String boardTitle;
    private String boardContents;
    private int boardHits;
    private LocalDateTime boardCreatedTime;
    private LocalDateTime boardUpdatedTime;

    private List<MultipartFile> boardFile; //save.html -> Controller 파일 담는 용도
    private List<String> originalFileName; // 원본 파일 이름
    private List<String> storedFileName; // 서버 저장용 파일 이름
    private int fileAttached; // 파일 첨부 여부 (첨부:1, 미첨부:0)

    public Board2DTO(Long id, String boardWriter, String boardTitle, int boardHits, LocalDateTime boardCreatedTime) {
        this.id = id;
        this.boardWriter = boardWriter;
        this.boardTitle = boardTitle;
        this.boardHits = boardHits;
        this.boardCreatedTime = boardCreatedTime;
    }

    public static Board2DTO toBoardDTO(BoardEntity boardEntity) {
        Board2DTO board2DTO = new Board2DTO();
        board2DTO.setId(boardEntity.getId());
        board2DTO.setBoardWriter(boardEntity.getBoardWriter());
        board2DTO.setBoardPass(boardEntity.getBoardPass());
        board2DTO.setBoardTitle(boardEntity.getBoardTitle());
        board2DTO.setBoardContents(boardEntity.getBoardContents());
        board2DTO.setBoardHits(boardEntity.getBoardHits());
        board2DTO.setBoardCreatedTime(boardEntity.getCreatedTime());
        board2DTO.setBoardUpdatedTime(boardEntity.getUpdatedTime());
        if(boardEntity.getFileAttached() == 0) {
            board2DTO.setFileAttached(boardEntity.getFileAttached()); // 0
        } else {
            List<String> originalFileNameList = new ArrayList<>();
            List<String> storedFileNameList = new ArrayList<>();
            board2DTO.setFileAttached(boardEntity.getFileAttached()); // 1
            for (BoardFileEntity boardFileEntity: boardEntity.getBoardFileEntityList()) {
                originalFileNameList.add(boardFileEntity.getOriginalFileName());
                storedFileNameList.add(boardFileEntity.getStoredFileName());
            }
            board2DTO.setOriginalFileName(originalFileNameList);
            board2DTO.setStoredFileName(storedFileNameList);
        }
        return board2DTO;
    }
}
