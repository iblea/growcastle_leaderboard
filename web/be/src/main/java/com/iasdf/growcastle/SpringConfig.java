package com.iasdf.growcastle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.iasdf.growcastle.repository.PlayerLeaderboardRepository;
import com.iasdf.growcastle.service.PlayerLeaderboardService;

@Configuration
public class SpringConfig {

    private final PlayerLeaderboardRepository playerLeaderboardRepository;

    @Autowired
    public SpringConfig(PlayerLeaderboardRepository playerLeaderboardRepository) {
        this.playerLeaderboardRepository = playerLeaderboardRepository;
    }

    @Bean
    public PlayerLeaderboardService playerLeaderboardService() {
        return new PlayerLeaderboardService(playerLeaderboardRepository);
        // return new PlayerLeaderboardService(playerLeaderboardRepository());
    }
    // public PlayerLeaderboardRepository playerLeaderboardRepository() {
    //     return new PlayerLeaderboardRepositoryTmp();
    // }
}
