package com.linkle.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.linkle.domain.dto.ParticipateLinkerResponseDTO;
import com.linkle.domain.dto.SearchUserResponseDTO;
import com.linkle.domain.entity.Friend;
import com.linkle.domain.entity.Linker;
import com.linkle.domain.entity.LinkerState;
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

    public Page<SearchUserResponseDTO> searchUsersWithFriendStatus(
        String word,
        Long currentUserId,
        Pageable pageable
    ) {
        Page<User> users = friendRepository.searchByNicknameOrName(word, pageable);

        return users.map(user -> {
            Friend friend = friendRepository.findByUserPair(currentUserId, user.getUserId());
            return SearchUserResponseDTO.from(user, friend);
        });
    }

    public Page<ParticipateLinkerResponseDTO> getAllLinkersByName(String word, Pageable pageable) {
        Page<Object[]> results = participateRepository.findAllWithCountsRaw(word, pageable);

        return results.map(row -> {
            Long linkerId = ((Number) row[0]).longValue();
            String name = (String) row[1];
            Long categoryId = ((Number) row[2]).longValue();
            String memo = (String) row[3];
            Long chatRoomCount = ((Number) row[4]).longValue();
            Long postCount = ((Number) row[5]).longValue();
            LinkerState state = (LinkerState) row[6];
            String address = (String) row[7];

            Linker linker = new Linker();
            linker.setLinkerId(linkerId);
            linker.setName(name);
            linker.setCategoryId(categoryId);
            linker.setMemo(memo);
            linker.setState(state);
            linker.setAddress(address);
            System.out.println("검색어 word = "+ word); // word가 "%ㄱ%" 형태인지 확인
            return ParticipateLinkerResponseDTO.from(linker, chatRoomCount, postCount);
        });
    }


}
