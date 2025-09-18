package com.linkle.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.linkle.domain.dto.FriendDTO;
import com.linkle.domain.dto.FriendResponseDTO;
import com.linkle.domain.entity.Friend;
import com.linkle.domain.entity.FriendState;
import com.linkle.domain.entity.User;
import com.linkle.repository.FriendRepository;
import com.linkle.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FriendService {
    private final FriendRepository friendRepository;
    private final UserRepository userRepository;

    // 친구 요청 보내기(insert)
    public FriendDTO createFriend(FriendDTO dto) {
        User user1 = userRepository.findById(dto.getUserId1())
            .orElseThrow(() -> new RuntimeException("user1 없음"));
        User user2 = userRepository.findById(dto.getUserId2())
            .orElseThrow(() -> new RuntimeException("user2 없음"));

        dto.setState(FriendState.REQUESTED);

        Friend friend = dto.toEntity(user1, user2);
        Friend saved = friendRepository.save(friend);

        return FriendDTO.fromEntity(saved);
    }

    // 받은 요청 조회
    public List<Friend> getReceivedRequests(Long userId) {
        return friendRepository.findReceivedFriendRequests(userId);
    }

    // 보낸 요청 조회
    public List<Friend> getSentRequests(Long userId) {
        return friendRepository.findSentFriendRequests(userId);
    }

    // 요청 수락(update)
    @Transactional
    public FriendDTO acceptFriend(Long friendId) {
        Friend friend = friendRepository.findReceivedFriend(friendId)
            .orElseThrow(() -> new RuntimeException("친구 요청 없음"));

        friend.setState(FriendState.ACCEPTED);

        Friend saved = friendRepository.save(friend);
        return FriendDTO.fromEntity(saved);
    }

    // 요청 거절(delete)
    @Transactional
    public void refusalFriend(FriendDTO dto) {
        Friend friend = friendRepository.findFriendRelation(dto.getUserId1(), dto.getUserId2())
            .orElseThrow(() -> new RuntimeException("친구 관계 없음"));

        friendRepository.delete(friend);
    }


    // 친구 목록 조회
    public List<Long> getFriendList(Long userId) {
        return friendRepository.findAllFriendIds(userId);
    }

    // 친구 목록 조회 (Friend 엔티티 그대로 반환)
    // public List<FriendResponseDTO> getFriendEntities(Long userId) { return friendRepository.findAcceptedFriends(userId); }

    public List<FriendResponseDTO> getFriendEntities(Long userId) {
        return friendRepository.findAcceptedFriends(userId)
            .stream()
            .map(friend -> FriendResponseDTO.fromEntity(friend, userId))
            .toList();
    }
    // 친구 삭제
    public void deleteFriend(Long friendId){
        friendRepository.deleteById(friendId);
    }

    // 친구 관계 조회
    public Friend getFriendRelationship(Long userId1, Long userId2) {
        return friendRepository.findByUserPair(userId1, userId2);
    }
}
