package com.linkle.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.linkle.domain.dto.SearchResponseDTO;
import com.linkle.domain.entity.Friend;
import com.linkle.domain.entity.User;
import com.linkle.repository.FriendRepository;
import com.linkle.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final UserRepository userRepository;
    private final FriendRepository friendRepository;

    public List<SearchResponseDTO> searchUsersWithFriendStatus(String word, Long currentUserId) {
        List<User> users = friendRepository.searchByNicknameOrName(word);

        return users.stream()
            .map(user -> {
                Friend friend = friendRepository.findByUserPair(currentUserId, user.getUserId());
                return SearchResponseDTO.from(user, friend);
            })
            .toList();
    }
}
