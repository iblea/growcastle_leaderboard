package com.iasdf.growcastle;

import org.springframework.context.annotation.Bean;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.iasdf.growcastle.repository.PlayerRepository;
import com.iasdf.growcastle.service.PlayerService;

@Configuration
public class SpringConfig {

    @Bean
    public PlayerService playerService() {
        return new PlayerService(playerRepository());
    }

    public PlayerRepository playerRepository() {
        return new PlayerRepository();
    }

}
