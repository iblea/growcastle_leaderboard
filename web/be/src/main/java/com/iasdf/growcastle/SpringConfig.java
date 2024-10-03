package com.iasdf.growcastle;

import org.springframework.context.annotation.Bean;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.iasdf.growcastle.repository.PlayerLeaderboardRepository;
import com.iasdf.growcastle.service.PlayerLeaderboardService;

@Configuration
public class SpringConfig {

    @Bean
    public PlayerLeaderboardService playerLeaderboardService() {
        return new PlayerLeaderboardService(playerLeaderboardRepository());
    }

    public PlayerLeaderboardRepository playerLeaderboardRepository() {
        return new PlayerLeaderboardRepository();
    }

}
