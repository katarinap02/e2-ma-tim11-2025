package com.example.team11project.domain.usecase;

import android.util.Log;

import com.example.team11project.data.datasource.remote.RemoteDataSource;
import com.example.team11project.domain.model.Boss;
import com.example.team11project.domain.model.BossBattle;
import com.example.team11project.domain.model.BossReward;
import com.example.team11project.domain.model.Clothing;
import com.example.team11project.domain.model.Equipment;
import com.example.team11project.domain.model.EquipmentType;
import com.example.team11project.domain.model.LevelInfo;
import com.example.team11project.domain.model.Potion;
import com.example.team11project.domain.model.User;
import com.example.team11project.domain.model.Weapon;
import com.example.team11project.domain.repository.BossBattleRepository;
import com.example.team11project.domain.repository.BossRepository;
import com.example.team11project.domain.repository.BossRewardRepository;
import com.example.team11project.domain.repository.EquipmentRepository;
import com.example.team11project.domain.repository.LevelInfoRepository;
import com.example.team11project.domain.repository.RepositoryCallback;
import com.example.team11project.domain.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BossUseCase {

    private final BossRepository bossRepository;
    private final BossBattleRepository bossBattleRepository;

    private final BossRewardRepository bossRewardRepository;

    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;

    public BossUseCase(BossRepository bossRepository, BossBattleRepository battleRepository, BossRewardRepository bossRewardRepository, EquipmentRepository equipmentRepository, UserRepository userRepository)
    {
        this.bossRepository = bossRepository;
        this.bossBattleRepository = battleRepository;
        this.bossRewardRepository = bossRewardRepository;
        this.equipmentRepository = equipmentRepository;
        this.userRepository = userRepository;
    }

    public void findUndefeatedBossRecursive(String userId, int maxLevel, RepositoryCallback<Boss> callback) {
        findUndefeatedBossFromLevel(userId, 1, maxLevel, callback);
    }

    private void findUndefeatedBossFromLevel(String userId, int currentLevel, int maxLevel, RepositoryCallback<Boss> callback) {
        if (currentLevel > maxLevel) {
            // Nema nepobeđenih boss-ova
            callback.onSuccess(null);
            return;
        }

        bossRepository.getBossByUserIdAndLevel(userId, currentLevel, new RepositoryCallback<Boss>() {
            @Override
            public void onSuccess(Boss boss) {
                if (boss != null && !boss.isDefeated()) {
                    // Našli smo nepobeđenog boss-a
                    callback.onSuccess(boss);
                } else {
                    // Proveravamo sledeći nivo
                    findUndefeatedBossFromLevel(userId, currentLevel + 1, maxLevel, callback);
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

                        calculateCoinsReward(user.getId(), levelInfo.getLevel(), new RepositoryCallback<Integer>() {
                            @Override
                            public void onSuccess(Integer reward) {
                                newBoss.setCoinsReward(reward);

                                // Kada se coinsReward izračuna, tek tada čuvamo u bazi
                                bossRepository.addBoss(newBoss, new RepositoryCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        callback.onSuccess(newBoss);
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        callback.onFailure(e);
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Exception e) {
                                callback.onFailure(e); // ako ne može da se izračuna reward
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
        //boss.setCoinsReward(calculateCoinsReward(user.getId(),levelInfo.getLevel()));

        return boss;
    }

    private int calculateBossHP(int level) {
        int hp = 200; // Level 1 = 200 HP
        for (int i = 2; i <= level; i++) {
            hp = hp * 2 + hp / 2;
        }
        return hp;
    }

    private void calculateCoinsReward(String userId, int level, RepositoryCallback<Integer> callback) {
        if (level == 1) {
            callback.onSuccess(200);
            return;
        }

        bossRepository.getBossByUserIdAndLevel(userId, level - 1, new RepositoryCallback<Boss>() {
            @Override
            public void onSuccess(Boss previousBoss) {
                int reward = (int) (previousBoss.getCoinsReward() * 1.2);
                callback.onSuccess(reward);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(new Exception("Nije moguće učitati prethodnog bossa: " + e.getMessage()));
            }
        });
    }

    public void getOrCreateBossBattle(User user, Boss boss, ArrayList<String> activeEquipmentImages, double successRate, RepositoryCallback<BossBattle> callback) {
       bossBattleRepository.getBattleByUserAndBossAndLevel(
                user.getId(),
                boss.getId(),
                user.getLevelInfo().getLevel(),
                new RepositoryCallback<BossBattle>() {
                    @Override
                    public void onSuccess(BossBattle existingBattle) {
                        if (existingBattle != null && !existingBattle.isBossDefeated()) {
                            callback.onSuccess(existingBattle);
                        } else {
                            BossBattle newBattle = createNewBossBattle(user, boss, activeEquipmentImages, successRate);
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


    private BossBattle createNewBossBattle(User user, Boss boss, ArrayList<String> activeEquipmentImages, double successRate) {
        BossBattle battle = new BossBattle();
        battle.setUserId(user.getId());
        battle.setBossId(boss.getId());
        battle.setLevel(user.getLevelInfo().getLevel());
        battle.setAttacksUsed(0); // Počinje sa 0 napada (maksimalno 5)
        battle.setDamageDealt(0);


        // Šansa za pogodak se računa na osnovu uspešnosti rešavanja zadataka u etapi
        //battle.setHitChance(calculateHitChanceFromTaskSuccess(user));

        battle.setHitChance(successRate);
        battle.setActiveEquipment(activeEquipmentImages);
        int totalPP = user.getLevelInfo().getPp();
        battle.setUserPP(totalPP);

        battle.setBossDefeated(false);

        return battle;
    }

    public void performAttack(BossBattle battle, Boss boss, RepositoryCallback<Boolean> callback) {
        if (battle.getAttacksUsed() >= 5) {
            callback.onFailure(new Exception("Maksimalan broj napada je već izvršen"));
            return;
        }

        // Proveravamo da li je napad uspešan na osnovu hitChance
        boolean isHit = Math.random() < battle.getHitChance();

        if (isHit) {
            // Napad je uspešan - oduzimamo PP od boss HP-a
            int damage = battle.getUserPP();
            int newHP = Math.max(0, boss.getCurrentHP() - damage);

            boss.setCurrentHP(newHP);
            battle.setDamageDealt(battle.getDamageDealt() + damage);

            // Proveravamo da li je boss poražen
            if (newHP == 0) {
                boss.setDefeated(true);
                battle.setBossDefeated(true);
            }
        }

        // Ažuriramo broj korišćenih napada
        battle.setAttacksUsed(battle.getAttacksUsed() + 1);

        // Proveravamo da li je ovo poslednji napad
        boolean isFinalAttack = battle.getAttacksUsed() >= 5;

        if (isFinalAttack || boss.isDefeated()) {
            // Završavamo borbu i računamo nagrade
            processBattleEnd(battle, boss, new RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    // Ažuriramo boss i battle u bazi
                    updateBattleAndBoss(battle, boss, new RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            callback.onSuccess(isHit); // true ako je poslednji napad pogodio
                        }

                        @Override
                        public void onFailure(Exception e) {
                            callback.onFailure(e);
                        }
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    callback.onFailure(e);
                }
            });
        } else {
            // Ažuriramo battle u bazi
            updateBattleAndBoss(battle, boss, new RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    callback.onSuccess(isHit);
                }

                @Override
                public void onFailure(Exception e) {
                    callback.onFailure(e);
                }
            });
        }
    }

    private void updateBattleAndBoss(BossBattle battle, Boss boss, RepositoryCallback<Void> callback) {
        // Ažuriraj battle
        bossBattleRepository.updateBattle(battle, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                // Ažuriraj boss
                bossRepository.updateBoss(boss, new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        callback.onSuccess(null);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        callback.onFailure(e);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    private void processBattleEnd(BossBattle battle, Boss boss, RepositoryCallback<Void> callback) {
        int coinsEarned = 0;

        if (battle.isBossDefeated()) {
            // Boss je poražen - puna nagrada
            coinsEarned = boss.getCoinsReward();

            // 20% šanse za opremu
            if (Math.random() < 0.2) {
                handleEquipmentGeneration(battle, boss, coinsEarned, callback);
            } else {
                // Nema opreme, samo novac
                finalizeBattleReward(battle, boss, coinsEarned, null, callback);
            }
        } else {
            // Boss nije poražen - proveravamo da li je umanjen za 50% HP
            double damagePercent = (double) battle.getDamageDealt() / boss.getMaxHP();

            if (damagePercent >= 0.5) {
                // Umanjen je za 50% ili više - polovina nagrade
                coinsEarned = boss.getCoinsReward() / 2;

                // 10% šanse za opremu
                if (Math.random() < 0.1) {
                    handleEquipmentGeneration(battle, boss, coinsEarned, callback);
                } else {
                    // Nema opreme, samo novac
                    finalizeBattleReward(battle, boss, coinsEarned, null, callback);
                }
            } else {
                // Nema nagrade
                finalizeBattleReward(battle, boss, 0, null, callback);
            }
        }
    }

    private void handleEquipmentGeneration(BossBattle battle, Boss boss, int coinsEarned, RepositoryCallback<Void> callback) {
        generateRandomEquipmentId(new EquipmentGenerationCallback() {
            @Override
            public void onSuccess(String equipmentId) {
                finalizeBattleReward(battle, boss, coinsEarned, equipmentId, callback);
            }

            @Override
            public void onFailure(Exception e) {
                // Ako generisanje opreme ne uspe, daj samo novac
                finalizeBattleReward(battle, boss, coinsEarned, null, callback);
            }
        });
    }

    private void generateRandomEquipmentId(EquipmentGenerationCallback callback) {
        // 95% šanse za odeću, 5% za oružje
        EquipmentType equipmentType = Math.random() < 0.95 ? EquipmentType.CLOTHING : EquipmentType.WEAPON;

        // Dobijanje sve opreme iz repozitorijuma
        equipmentRepository.getAllEquipment(new RemoteDataSource.DataSourceCallback<List<Equipment>>() {
            @Override
            public void onSuccess(List<Equipment> allEquipment) {
                List<Equipment> filteredEquipment = new ArrayList<>();

                // Filtriranje opreme po tipu
                for (Equipment equipment : allEquipment) {
                    if (equipmentType == EquipmentType.CLOTHING &&
                            "clothing".equalsIgnoreCase(equipment.getType().name()) &&
                            equipment instanceof Clothing) {
                        filteredEquipment.add(equipment);
                    } else if (equipmentType == EquipmentType.WEAPON &&
                            "weapon".equalsIgnoreCase(equipment.getType().name()) &&
                            equipment instanceof Weapon) {
                        filteredEquipment.add(equipment);
                    }
                }

                if (filteredEquipment.isEmpty()) {
                    callback.onFailure(new Exception("Nema dostupne opreme za tip: " + equipmentType));
                    return;
                }

                // Random izbor opreme
                Equipment randomEquipment = filteredEquipment.get(
                        (int) (Math.random() * filteredEquipment.size())
                );

                // Vraćamo ID opreme (Firebase document ID)
                String equipmentId = randomEquipment.getImage();
                callback.onSuccess(equipmentId);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(new Exception("Neuspešno učitavanje opreme: " + e.getMessage()));
            }
        });
    }

    private void finalizeBattleReward(BossBattle battle, Boss boss, int coinsEarned, String equipmentId, RepositoryCallback<Void> callback) {
        // Kreiraj boss reward ako ima nagrade
        if (coinsEarned >= 0 || equipmentId != null) {
            BossReward reward = new BossReward();
            reward.setBossId(boss.getId());
            reward.setUserId(battle.getUserId());
            reward.setLevel(battle.getLevel());
            reward.setCoinsEarned(coinsEarned);
            reward.setEquipmentId(equipmentId);
            bossRewardRepository.addReward(reward, callback);
        } else {
            callback.onSuccess(null);
        }
    }

    public interface EquipmentGenerationCallback {
        void onSuccess(String equipmentId);
        void onFailure(Exception e);
    }


        public boolean isBattleFinished(BossBattle battle) {
        return battle.getAttacksUsed() >= 5 || battle.isBossDefeated();
    }

    public void getBattleStatus(BossBattle battle, Boss boss, RepositoryCallback<String> callback) {
        StringBuilder status = new StringBuilder();

        status.append("Napad: ").append(battle.getAttacksUsed()).append("/5\n");
        status.append("Boss HP: ").append(boss.getCurrentHP()).append("/").append(boss.getMaxHP()).append("\n");
        status.append("Šteta nanešena: ").append(battle.getDamageDealt()).append("\n");

        if (battle.isBossDefeated()) {
            status.append("Boss je poražen!");
        } else if (battle.getAttacksUsed() >= 5) {
            status.append("Borba završena - boss nije poražen");
        }

        callback.onSuccess(status.toString());
    }


}
