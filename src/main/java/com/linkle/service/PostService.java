package com.linkle.service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.linkle.domain.dto.PostDTO;
import com.linkle.domain.entity.Linker;
import com.linkle.domain.entity.Post;
import com.linkle.repository.LinkerRepository;
import com.linkle.repository.PostRepository;
import com.linkle.domain.entity.User;
import com.linkle.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LinkerRepository linkerRepository;
    private final S3Client s3Client;

    // minio 저장소 활용
    @Value("${minio.bucket}")
    private String bucketName;

    public List<PostDTO> findByLinkerLinkerId(Long linkerId){
        if (linkerId == null) {
            return Collections.emptyList(); // Return empty list for null linkerId
        }
        List<Post> posts = postRepository.findByLinkerLinkerId(linkerId);
        return PostDTO.fromEntityList(posts);
    }


    public PostDTO createPost(PostDTO dto, MultipartFile imageFile) throws IOException {
        String imageKey = uploadPostImage(imageFile, dto.getLinkerId());

        User user = userRepository.findById(dto.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        Linker linker = linkerRepository.findById(dto.getLinkerId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 링커입니다."));

        Post post = Post.builder()
            .image(imageKey)
            .memo(dto.getMemo()).
            createdDate(java.time.LocalDateTime.now())
            .user(user)
            .linker(linker)
            .build();
        Post saved = postRepository.save(post);

        return PostDTO.fromEntity(saved);
    }

    //이미지 업로드
    public String uploadPostImage(MultipartFile file, Long linkerId) throws IOException {
        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        //중간에 / 넣으면 외부저장소에 폴더가 생김
        String key = "Post" + linkerId + "/" +UUID.randomUUID() +"." + ext;
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

    public PostDTO findById(Long postId) {
        if (postId == null) return null;
        Optional<Post> opt = postRepository.findById(postId);
        //User같이 보내기
        opt.get().getUser();



        return opt.map(PostDTO::fromEntity).orElse(null);
    }

    public Long  deletePost(Long postId) {
        postRepository.deleteById(postId);

        Long result = postRepository.findById(postId).stream().count();

        return result;
    }


    @Transactional
    public Long updatePost(Long postId, String content, MultipartFile file) throws IOException {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post가 없습니다"));

        // 텍스트(memo) 업데이트
        if (content != null && !content.trim().isEmpty()) {
            post.setMemo(content);
        }

        // 이미지 업데이트
        if (file != null && !file.isEmpty()) {
            String imageUrl = uploadPostImage(file, post.getLinker().getLinkerId());
            post.setImage(imageUrl);
        }

        // JPA는 엔티티 변경사항을 자동 감지 → save() 생략해도 됨
        postRepository.save(post);

        return post.getPostId();
    }

}
