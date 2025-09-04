package com.example.team11project.domain.usecase;

import com.example.team11project.domain.model.Boss;
import com.example.team11project.domain.model.BossBattle;
import com.example.team11project.domain.model.Equipment;
import com.example.team11project.domain.model.LevelInfo;
import com.example.team11project.domain.model.User;
import com.example.team11project.domain.repository.BossBattleRepository;
import com.example.team11project.domain.repository.BossRepository;
import com.example.team11project.domain.repository.BossRewardRepository;
import com.example.team11project.domain.repository.LevelInfoRepository;
import com.example.team11project.domain.repository.RepositoryCallback;

import java.util.ArrayList;
import java.util.List;

public class BossUseCase {

    private final BossRepository bossRepository;
    private final BossBattleRepository bossBattleRepository;

    private final BossRewardRepository bossRewardRepository;

    public BossUseCase(BossRepository bossRepository, BossBattleRepository battleRepository, BossRewardRepository bossRewardRepository)
    {
        this.bossRepository = bossRepository;
        this.bossBattleRepository = battleRepository;
        this.bossRewardRepository = bossRewardRepository;
    }

    public void findUndefeatedBossRecursive(String userId, int level, RepositoryCallback<Boss> callback) {
        int minLevel = 1;
        if (minLevel > level) {
            // Nema nepobeđenih boss-ova
            callback.onSuccess(null);
            return;
        }

        bossRepository.getBossByUserIdAndLevel(userId, minLevel, new RepositoryCallback<Boss>() {
            @Override
            public void onSuccess(Boss boss) {
                if (boss != null && !boss.isDefeated()) {
                    // Našli smo nepobeđenog boss-a
                    callback.onSuccess(boss);
                } else {
                    // Proveravamo niži nivo
                    findUndefeatedBossRecursive(userId, minLevel + 1, callback);
                }
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public void createBoss(User user, LevelInfo levelInfo, RepositoryCallback<Boss> callback) {
        // Proveravamo da li već postoji boss za ovaj nivo
        bossRepository.getBossByUserIdAndLevel(user.getId(), levelInfo.getLevel(), new RepositoryCallback<Boss>() {
            @Override
            public void onSuccess(Boss existingBoss) {
                if (existingBoss != null) {
                    // Boss već postoji za ovaj nivo
                    callback.onSuccess(existingBoss);
                } else {
                    if (levelInfo.getLevel() == 0)
                    {
                        callback.onSuccess(null);
                    }
                    else {
                        // Kreiramo novi boss jer ne postoji za ovaj nivo
                        Boss newBoss = createNewBoss(user, levelInfo);

                        // Čuvamo boss u bazi koristeći addBoss metodu
                        bossRepository.addBoss(newBoss, new RepositoryCallback<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                // ID je sada postavljen od strane remote data source-a
                                callback.onSuccess(newBoss);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                callback.onFailure(e);
                            }
                        });
                    }

                }
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    private Boss createNewBoss(User user, LevelInfo levelInfo) {
        Boss boss = new Boss();
        boss.setUserId(user.getId());
        boss.setLevel(levelInfo.getLevel());

        // Podesavamo boss statistike na osnovu nivoa
        int baseHP = calculateBossHP(levelInfo.getLevel());
        boss.setMaxHP(baseHP);
        boss.setCurrentHP(baseHP);
        boss.setDefeated(false);
        boss.setCoinsReward(calculateCoinsReward(levelInfo.getLevel()));

        return boss;
    }

    private int calculateBossHP(int level) {
        int hp = 200; // Level 1 = 200 HP
        for (int i = 2; i <= level; i++) {
            hp = hp * 2 + hp / 2;
        }
        return hp;
    }

    private int calculateCoinsReward(int level) {
        double reward = 200.0; // Prvi boss
        for (int i = 2; i <= level; i++) {
            reward *= 1.2;
        }
        return (int) reward;
    }

    public void getOrCreateBossBattle(User user, Boss boss, int level, RepositoryCallback<BossBattle> callback) {
        // Proveravamo da li već postoji aktivna bitka protiv ovog boss-a
        bossBattleRepository.getBattleByUserAndBossAndLevel(user.getId(), boss.getId(), level, new RepositoryCallback<BossBattle>() {
            @Override
            public void onSuccess(BossBattle existingBattle) {
                if (existingBattle != null && !existingBattle.isBossDefeated()) {
                    // Već postoji aktivna bitka
                    callback.onSuccess(existingBattle);
                } else {
                    BossBattle newBattle = createNewBossBattle(user, boss, level);
                    bossBattleRepository.addBattle(newBattle, new RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            callback.onSuccess(newBattle);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            callback.onFailure(e);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    private BossBattle createNewBossBattle(User user, Boss boss, int level) {
        BossBattle battle = new BossBattle();
        battle.setUserId(user.getId());
        battle.setBossId(boss.getId());
        battle.setLevel(level);
        battle.setAttacksUsed(0); // Počinje sa 0 napada (maksimalno 5)
        battle.setDamageDealt(0);

        // Šansa za pogodak se računa na osnovu uspešnosti rešavanja zadataka u etapi
        //battle.setHitChance(calculateHitChanceFromTaskSuccess(user));

        battle.setHitChance(0.67);

        List<String> equipments= new ArrayList<String>(); // za sada nema nista u equipment
        battle.setActiveEquipment(equipments);

        int totalPP = user.getLevelInfo().getPp();
        battle.setUserPP(totalPP);

        battle.setBossDefeated(false);

        return battle;
    }




}
