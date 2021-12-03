package com.example.teampandanback.service;

import com.example.teampandanback.domain.note.Note;
import com.example.teampandanback.domain.note.NoteRepository;
import com.example.teampandanback.exception.ApiRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

@RequiredArgsConstructor
@Service
public class LockManagerService {
    private final NoteRepository noteRepository;
    /*
    @PersistenceContext : 영속성 컨텍스트 -> 엔티티를 영구히 저장하는 환경
    EntityManager : Entity를 관리하는 역할/ 엔티티 매니저 내부에 영속성 컨텍스트를 두어 엔티티들을 관리
    영속성 : https://perfectacle.github.io/2018/01/14/jpa-entity-manager-factory/

     */
    @PersistenceContext
    EntityManager em;

    /*
    em.clear()를 하면 DB에 데이터를 반영하고, 영속성 컨텍스트를 지우는 역할
     */
    @Transactional
    public void preProcess(Long noteId){
        em.clear();
        Note note = noteRepository.findById(noteId).orElseThrow(
                () -> new ApiRequestException("해당 노트가 없습니다.")
        );
//        note.setLocked(true);
        note.setWriting(true);
    }

    @Transactional
    public void deLock(Long noteId){
        em.clear();
        Note note = noteRepository.findById(noteId).orElseThrow(
                ()-> new ApiRequestException("해당 노트가 없습니다.")
        );
        note.setLocked(false);
        note.setWriting(false);
        note.setWriterId(null);
    }

    @Transactional
    public Boolean isAnyoneWriting(Long noteId){
        em.clear();
        Note note = noteRepository.findById(noteId).orElseThrow(
                ()-> new ApiRequestException("해당 노트가 없습니다.")
        );
        if(note.getWriting() == true){
            return true;
        }else{
            return false;
        }
    }

    @Transactional
    public void assumeThatNobodyIsWriting(Long noteId){
        em.clear();
        Note note = noteRepository.findById(noteId).orElseThrow(
                ()-> new ApiRequestException("해당 노트가 없습니다.")
        );
        note.setWriting(false);
    }
}
