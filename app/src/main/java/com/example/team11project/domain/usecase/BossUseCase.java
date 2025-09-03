package com.example.team11project.domain.usecase;

import com.example.team11project.domain.repository.BossRepository;
import com.example.team11project.domain.repository.LevelInfoRepository;

public class BossUseCase {

    private final BossRepository bossRepository;
    private final LevelInfoRepository levelInfoRepository;

    public BossUseCase(BossRepository bossRepository, LevelInfoRepository levelInfoRepository)
    {
        this.bossRepository = bossRepository;
        this.levelInfoRepository = levelInfoRepository;
    }


}
