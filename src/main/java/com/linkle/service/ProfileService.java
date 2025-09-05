package com.linkle.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.linkle.domain.dto.ProfileLinkerCountDTO;
import com.linkle.domain.dto.ProfileParticipateLinkerDTO;
import com.linkle.domain.dto.ProfileEditRequestDTO;
import com.linkle.domain.dto.ProfileEditResponseDTO;
import com.linkle.domain.entity.User;
import com.linkle.repository.ParticipateRepository;
import com.linkle.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final ParticipateRepository participateRepository;
    private final S3Client s3Client;

    // minio 저장소 활용
    @Value("${minio.bucket}")
    private String bucketName;

    // 유저 조회
    public ProfileEditResponseDTO getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
        return ProfileEditResponseDTO.fromEntity(user);
    }

    //유저 링커 참여 내역 조회
    public List<ProfileParticipateLinkerDTO> getUserLineker(Long userId) {
        return participateRepository.findParticipationsByUserId(userId);
    }

    // 유저 링커 참여 통계 조회
    public List<ProfileLinkerCountDTO> getUserCount(Long userId) {
        return participateRepository.countUserParticipationByCategory(userId);
    }

    // 유저 수정
    public ProfileEditResponseDTO updateUserProfile(Long userId, ProfileEditRequestDTO dto) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        if (dto.getName() != null) user.setName(dto.getName());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getPassword() != null) user.setPassword(dto.getPassword());
        if (dto.getNickname() != null) user.setNickname(dto.getNickname());
        if (dto.getGender() != null) user.setGender(dto.getGender());
        if (dto.getImage() != null) user.setImage(dto.getImage());
        if (dto.getBackground() != null) user.setBackground(dto.getBackground());
        if (dto.getMemo() != null) user.setMemo(dto.getMemo());
        if (dto.getAccountNumber() != null) user.setAccountNumber(dto.getAccountNumber());

        userRepository.save(user);
        return ProfileEditResponseDTO.fromEntity(user);
    }

    // 유저 삭제
    public void deleteUserProfile(Long userId) {
        userRepository.deleteById(userId);
    }

    // 유저 삽입
    public ProfileEditResponseDTO insertUser(ProfileEditRequestDTO dto) {
        User user = User.builder()
            .name(dto.getName())
            .email(dto.getEmail())
            .password(dto.getPassword())
            .nickname(dto.getNickname())
            .gender(dto.getGender())
            .balance(0L)
            .createdDate(LocalDate.now())
            .build();

        userRepository.save(user);
        return ProfileEditResponseDTO.fromEntity(user);
    }

    // 모든 유저 조회
    public List<ProfileEditResponseDTO> getAllUsers() {
        List<User> userEntities = userRepository.findAll();
        List<ProfileEditResponseDTO> userDtoList = new ArrayList<>();
        for (User userEntity : userEntities) {
            ProfileEditResponseDTO userDto = ProfileEditResponseDTO.fromEntity(userEntity);
            userDtoList.add(userDto);
        }
        return userDtoList;
    }

    // 이미지 업로드
    public String uploadProfileImage(MultipartFile file, Long userId) throws IOException {
        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String key = "profile_" + userId + "." + ext;
        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .build(),
            RequestBody.fromInputStream(file.getInputStream(), file.getSize())
        );
        return key; // 최종 key 리턴
    }

    // 배경이미지 업로드
    public String uploadBackgroundImage(MultipartFile file, Long userId) throws IOException {
        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String key = "background_" + userId + "." + ext;
        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .build(),
            RequestBody.fromInputStream(file.getInputStream(), file.getSize())
        );
        return key;
    }

    public byte[] downloadFile(String key) throws IOException {
        try (ResponseInputStream<GetObjectResponse> obj =
                 s3Client.getObject(GetObjectRequest.builder()
                     .bucket(bucketName)
                     .key(key)
                     .build())) {
            return obj.readAllBytes();
        }
    }

}