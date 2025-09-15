package com.linkle.service;

import java.util.List;

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

    public List<SearchUserResponseDTO> searchUsersWithFriendStatus(String word, Long currentUserId) {
        List<User> users = friendRepository.searchByNicknameOrName(word);

        return users.stream()
            .map(user -> {
                Friend friend = friendRepository.findByUserPair(currentUserId, user.getUserId());
                return SearchUserResponseDTO.from(user, friend);
            })
            .toList();
    }

    public List<ParticipateLinkerResponseDTO> getAllLinkersByName(String word) {

        List<Object[]> results = participateRepository.findAllWithCountsRaw(word);

        return results.stream()
            .map(row -> {
                Long linkerId = ((Number) row[0]).longValue();
                String name = (String) row[1];
                Long categoryId = ((Number) row[2]).longValue();
                String memo = (String) row[3];
                Long chatRoomCount = ((Number) row[4]).longValue();
                Long postCount = ((Number) row[5]).longValue();
                LinkerState state = (LinkerState) row[6];
                String address = ((String) row[7]);

                Linker linker = new Linker();
                linker.setLinkerId(linkerId);
                linker.setName(name);
                linker.setCategoryId(categoryId);
                linker.setMemo(memo);
                linker.setState((state));
                linker.setAddress((address));

                return ParticipateLinkerResponseDTO.from(linker, chatRoomCount, postCount);
            })
            .toList();
    }

}
