package com.linkle.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.linkle.domain.dto.SearchLinkerResponseDTO;
import com.linkle.domain.dto.SearchUserResponseDTO;
import com.linkle.domain.entity.Friend;
import com.linkle.domain.entity.Linker;
import com.linkle.domain.entity.User;
import com.linkle.repository.FriendRepository;
import com.linkle.repository.ParticipateRepository;
import com.linkle.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final ParticipateRepository participateRepository;

    public List<SearchUserResponseDTO> searchUsersWithFriendStatus(String word, Long currentUserId) {
        List<User> users = friendRepository.searchByNicknameOrName(word);

        return users.stream()
            .map(user -> {
                Friend friend = friendRepository.findByUserPair(currentUserId, user.getUserId());
                return SearchUserResponseDTO.from(user, friend);
            })
            .toList();
    }

    public List<SearchLinkerResponseDTO> getAllLinkersByName(String word) {

        List<Object[]> results = participateRepository.findAllWithCountsRaw(word);

        return results.stream()
            .map(row -> {
                // 🔒 row 순서: linker_id, name, category_id, memo, chatRoomCount, postCount

                Long linkerId = ((Number) row[0]).longValue();    // linker_id
                String name = (String) row[1];                     // name
                Long categoryId = ((Number) row[2]).longValue();   // category_id
                String memo = (String) row[3];                     // memo
                Long chatRoomCount = ((Number) row[4]).longValue();
                Long postCount = ((Number) row[5]).longValue();

                // 가상의 Linker 객체 생성 (id, name, memo만 사용)
                Linker linker = new Linker();
                linker.setLinkerId(linkerId);
                linker.setName(name);
                linker.setCategoryId(categoryId);
                linker.setMemo(memo);

                return SearchLinkerResponseDTO.from(linker, chatRoomCount, postCount);
            })
            .toList();
    }

}
