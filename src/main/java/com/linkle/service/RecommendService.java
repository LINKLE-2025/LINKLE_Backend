package com.linkle.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import com.linkle.repository.LinkerRepository;

@Service
public class RecommendService {

    LinkerRepository linkerRepository;

    RestTemplate restTemplate;

    @Value("${app.recommend.base-url}")
    private String recommendURL;

    public List<Long> getRecommendedLinker(long userId){
        String url = recommendURL + userId;
        List<Long> list = new ArrayList<>();
        return list;
    }
}
