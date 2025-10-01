package com.example.team11project.data.datasource.remote;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;


import com.example.team11project.domain.model.Alliance;
import com.example.team11project.domain.model.AllianceInvite;
import com.example.team11project.domain.model.AllianceMessage;
import com.example.team11project.domain.model.AllianceMission;
import com.example.team11project.domain.model.AllianceMissionReward;
import com.example.team11project.domain.model.Boss;
import com.example.team11project.domain.model.BossBattle;
import com.example.team11project.domain.model.BossReward;
import com.example.team11project.domain.model.Clothing;
import com.example.team11project.domain.model.Equipment;
import com.example.team11project.domain.model.LevelInfo;
import com.example.team11project.domain.model.MemberProgress;
import com.example.team11project.domain.model.Potion;
import com.example.team11project.domain.model.TaskInstance;
import com.example.team11project.domain.model.User;
import com.example.team11project.domain.model.UserTitle;
import com.example.team11project.domain.model.Weapon;
import com.example.team11project.domain.repository.RepositoryCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.team11project.domain.model.Category;
import com.example.team11project.domain.model.Task;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RemoteDataSource {

    private FirebaseFirestore db;
    private static final String USERS_COLLECTION = "users";
    private static final String TASKS_COLLECTION = "tasks";
    private static final String CATEGORIES_COLLECTION = "categories";
    private static final String INSTANCES_COLLECTION = "task_instances";

    private static final String BOSSES_COLLECTION = "bosses";
    private static final String BOSS_BATTLES_COLLECTION = "bossBattles";
    private static final String BOSS_REWARDS_COLLECTION = "bossRewards";
    private static final String EQUIPMENT_COLLECTION = "equipment";
    private static final String ALLIANCE_COLLECTION = "alliances";
    private static final String ALLIANCE_INVITATION_COLLECTION = "alliance_invitations";
    private static final String MESSAGE_COLLECTION = "messages";
    private static final String ALLIANCE_BOSS_COLLECTION = "alliance_boss";
    private static final String ALLIANCE_MISSION_COLLECTION = "alliance_mission";
    private static final String ALLIANCE_REWARD_COLLECTION = "alliance_reward";
    private static final String MEMBER_PROGRESS_COLLECTION = "member_progress";


    public RemoteDataSource() {
        this.db = FirebaseFirestore.getInstance();
    }

    public interface DataSourceCallback<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }

    public void addTask(Task task, final DataSourceCallback<String> callback) {
        // Struktura: /users/{userId}/tasks/{taskId}
        db.collection(USERS_COLLECTION).document(task.getUserId())
                .collection(TASKS_COLLECTION)
                .add(task) // Firestore automatski konvertuje POJO klasu
                .addOnSuccessListener(documentReference -> callback.onSuccess(documentReference.getId()))
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    public void setTaskWithId(Task task, String taskId, final DataSourceCallback<String> callback) {
        // Koristi set() umesto add() da zadržite specifičan ID
        db.collection(USERS_COLLECTION).document(task.getUserId())
                .collection(TASKS_COLLECTION)
                .document(taskId) // Koristi specifičan ID
                .set(task)
                .addOnSuccessListener(aVoid -> callback.onSuccess(taskId))
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    public void getAllTasks(String userId, final DataSourceCallback<List<Task>> callback) {
        getTasksCollection(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Task> taskList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Task t = document.toObject(Task.class);
                            t.setId(document.getId()); // Ručno postavljamo ID dokumenta
                            taskList.add(t);
                        }
                        callback.onSuccess(taskList);
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    public void deleteTask(String taskId, String userId, final DataSourceCallback<Void> callback) {
        getTasksCollection(userId).document(taskId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e));
    }
    public void updateTask(Task task, final DataSourceCallback<Void> callback) {
        getTasksCollection(task.getUserId()).document(task.getId())
                .set(task) // set() će ili ažurirati postojeći ili kreirati novi ako ne postoji
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    public void getTasksInPeriod(String userId, Date startDate, Date endDate, final DataSourceCallback<List<Task>> callback) {
        CollectionReference tasksRef = db.collection(USERS_COLLECTION)
                .document(userId)
                .collection(TASKS_COLLECTION);

        if (endDate == null) {
            callback.onFailure(new Exception("End date must not be null"));
            return;
        }
        Query query = tasksRef;

        if (startDate != null) {
            query = query.whereGreaterThanOrEqualTo("executionTime", startDate);
        }
        query = query.whereLessThanOrEqualTo("executionTime", endDate);

        // orderBy na kraju (ili ga ukloni ako nije potrebno)
        query = query.orderBy("executionTime");

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Task> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            Task t = doc.toObject(Task.class);
                            t.setId(doc.getId());
                            list.add(t);
                        } catch (Exception e) {
                            // Log greške ali nastavi sa ostalim taskovima
                            Log.w("FirestoreDataSource", "Error parsing task: " + doc.getId(), e);
                        }
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreDataSource", "Error fetching tasks in period", e);
                    callback.onFailure(e);
                });
    }


    public void addCategory(Category category, final DataSourceCallback<String> callback) {
        // Struktura: /users/{userId}/categories/{categoryId}
        db.collection(USERS_COLLECTION).document(category.getUserId())
                .collection(CATEGORIES_COLLECTION)
                .add(category)
                .addOnSuccessListener(documentReference -> callback.onSuccess(documentReference.getId()))
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    public void getAllCategories(String userId, final DataSourceCallback<List<Category>> callback) {
        getCategoriesCollection(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Category> categoryList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Category c = document.toObject(Category.class);
                            c.setId(document.getId()); // Ručno postavljamo ID
                            categoryList.add(c);
                        }
                        callback.onSuccess(categoryList);
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    public void updateCategory(Category category, final DataSourceCallback<Void> callback) {
        getCategoriesCollection(category.getUserId()).document(category.getId())
                .set(category)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    public void deleteCategory(String categoryId, String userId, final DataSourceCallback<Void> callback) {
        getCategoriesCollection(userId).document(categoryId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    private CollectionReference getTasksCollection(String userId) {
        return db.collection(USERS_COLLECTION).document(userId).collection(TASKS_COLLECTION);
    }

    private CollectionReference getCategoriesCollection(String userId) {
        return db.collection(USERS_COLLECTION).document(userId).collection(CATEGORIES_COLLECTION);
    }


    public void addUser(User user, final DataSourceCallback<String> callback) {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            String uid = firebaseUser.getUid();
                            user.setId(uid);

                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(user.getUsername())
                                    .build();
                            firebaseUser.updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileTask -> {
                                        firebaseUser.sendEmailVerification()
                                                .addOnCompleteListener(verificationTask -> {
                                                    if (verificationTask.isSuccessful()) {
                                                        // Upis korisnika
                                                        user.setLevelInfo(new LevelInfo(0, 200, 0, 0, 0, UserTitle.POČETNIK, 0, new Date(), null));
                                                        if (user.getClothing() == null) {
                                                            user.setClothing(new ArrayList<>());
                                                        }

                                                        if (user.getWeapons() == null) {
                                                            user.setWeapons(new ArrayList<>());
                                                        }

                                                        if (user.getPotions() == null) {
                                                            user.setPotions(new ArrayList<>());
                                                        }
                                                        user.setCoins(0);
                                                        db.collection(USERS_COLLECTION)
                                                                .document(uid)
                                                                .set(user)
                                                                .addOnFailureListener(callback::onFailure);

                                                    } else {
                                                            callback.onFailure(verificationTask.getException());
                                                            firebaseUser.delete();
                                                        }
                                                    });
                                    });
                        } else {
                            callback.onFailure(new Exception("FirebaseUser je null nakon kreiranja."));
                        }
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    public void refreshUserVerificationStatus(User user, final DataSourceCallback<Void> callback) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            firebaseUser.reload().addOnCompleteListener(task -> {
                if (firebaseUser.isEmailVerified()) {
                    user.setVerified(true);
                    db.collection(USERS_COLLECTION)
                            .document(user.getId())
                            .set(user, SetOptions.merge())
                            .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                            .addOnFailureListener(callback::onFailure);
                } else {
                    callback.onFailure(new Exception("User email not verified yet"));
                }
            });
        } else {
            callback.onFailure(new Exception("FirebaseUser is null"));
        }
    }

    public void login(String email, String password, final DataSourceCallback<User> callback) {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        Log.d("LoginDebug", "Searching for email: '" + email + "'");

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(authTask -> {
                    if (authTask.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();

                        if (firebaseUser == null) {
                            callback.onFailure(new Exception("Greška prilikom prijave, pokušajte ponovo."));
                            return;
                        }

                        // 2. Sada proveri da li je email verifikovan
                        if (firebaseUser.isEmailVerified()) {
                            // 3. Ako jeste, dohvati ostatak podataka o korisniku iz Firestore baze
                            db.collection(USERS_COLLECTION)
                                    .document(firebaseUser.getUid()) // Koristi jedinstveni UID korisnika
                                    .get()
                                    .addOnCompleteListener(firestoreTask -> {
                                        if (firestoreTask.isSuccessful() && firestoreTask.getResult() != null) {
                                            User user = firestoreTask.getResult().toObject(User.class);
                                            if (user != null) {
                                                user.setId(firebaseUser.getUid()); // Osiguraj da je ID postavljen
                                                callback.onSuccess(user);
                                            } else {
                                                callback.onFailure(new Exception("Korisnički podaci nisu pronađeni."));
                                            }
                                        } else {
                                            callback.onFailure(firestoreTask.getException());
                                        }
                                    });
                        } else {
                            // 4. Ako email NIJE verifikovan, vrati tačnu poruku o grešci
                            auth.signOut(); // Izloguj korisnika jer nije verifikovan
                            callback.onFailure(new Exception("Morate prvo da aktivirate nalog putem linka u mejlu"));
                        }
                    } else {
                        // Ako prijava ne uspe (npr. pogrešna lozinka), Firebase će vratiti odgovarajuću grešku
                        callback.onFailure(authTask.getException());
                    }
                });
    }

    public void updateUser(User user, final DataSourceCallback<Void> callback) {
        if (user.getId() == null) {
            callback.onFailure(new Exception("User ID je null, ne može se update-ovati."));
            return;
        }

        if (user.getClothing() == null) user.setClothing(new ArrayList<>());
        if (user.getWeapons() == null) user.setWeapons(new ArrayList<>());
        if (user.getPotions() == null) user.setPotions(new ArrayList<>());

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("clothing", user.getClothing());
        userMap.put("weapons", user.getWeapons());
        userMap.put("potions", user.getPotions());
        userMap.put("currentAlliance", convertAllianceToMap(user.getCurrentAlliance()));

        db.collection(USERS_COLLECTION)
                .document(user.getId())
                .set(userMap, SetOptions.merge())
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    // Pomoćna metoda koja konvertuje Alliance u mapu
    private Map<String, Object> convertAllianceToMap(Alliance alliance) {
        if (alliance == null) return null;
        Map<String, Object> map = new HashMap<>();
        map.put("id", alliance.getId());
        map.put("name", alliance.getName());
        map.put("leader", alliance.getLeader());
        map.put("members", alliance.getMembers() != null ? alliance.getMembers() : new ArrayList<>());
        map.put("missionActive", alliance.isMissionActive());
        return map;
    }


    public void getUserById(String userId, DataSourceCallback<User> callback) {
        if (userId == null || userId.isEmpty()) {
            callback.onFailure(new Exception("User ID je null ili prazan."));
            return;
        }

        db.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        callback.onFailure(new Exception("User ne postoji"));
                        return;
                    }

                    User user = documentSnapshot.toObject(User.class);

                    if (user.getLevelInfo() != null) {
                        String titleStr = documentSnapshot.getString("levelInfo.title");
                        UserTitle titleEnum;
                        try {
                            titleEnum = UserTitle.valueOf(titleStr);
                        } catch (Exception e) {
                            titleEnum = UserTitle.POČETNIK; // fallback
                        }
                        user.getLevelInfo().setTitle(titleEnum);
                    } else {
                        user.setLevelInfo(new LevelInfo(0, 200, 0, 0, 0, UserTitle.POČETNIK, 0, new Date(), null)); // default LevelInfo
                    }

                    callback.onSuccess(user);
                })
                .addOnFailureListener(callback::onFailure);

    }

    public void addTaskInstance(TaskInstance instance, final DataSourceCallback<String> callback) {
        if (instance.getUserId() == null) {
            callback.onFailure(new Exception("UserID je null."));
            return;
        }

        db.collection(USERS_COLLECTION).document(instance.getUserId())
                .collection(INSTANCES_COLLECTION)
                .add(instance)
                .addOnSuccessListener(documentReference -> callback.onSuccess(documentReference.getId()))
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    public void getAllTaskInstances(String userId, String originalTaskId, final DataSourceCallback<List<TaskInstance>> callback) {
        if (userId == null || originalTaskId == null) {
            callback.onFailure(new Exception("UserID ili OriginalTaskID je null."));
            return;
        }
        db.collection(USERS_COLLECTION).document(userId)
                .collection(INSTANCES_COLLECTION)
                .whereEqualTo("originalTaskId", originalTaskId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<TaskInstance> instances = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            TaskInstance instance = document.toObject(TaskInstance.class);
                            instance.setId(document.getId());
                            instances.add(instance);
                        }
                        callback.onSuccess(instances);
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }
    public void addEquipmentToCollection(Equipment equipment, final DataSourceCallback<String> callback) {
        db.collection(EQUIPMENT_COLLECTION)
                .add(equipment)
                .addOnSuccessListener(documentReference -> callback.onSuccess(documentReference.getId()))
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    public void updateEquipmentInCollection(Equipment equipment, final DataSourceCallback<Void> callback) {
        db.collection(EQUIPMENT_COLLECTION).document(equipment.getId())
                .set(equipment)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    public void deleteEquipmentFromCollection(String equipmentId, final DataSourceCallback<Void> callback) {
        db.collection(EQUIPMENT_COLLECTION).document(equipmentId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    public void getAllEquipment(final DataSourceCallback<List<Equipment>> callback) {
        db.collection(EQUIPMENT_COLLECTION)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Equipment> list = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String type = doc.getString("type"); // polje u Firestore dokumentu
                        Equipment equipment = null;

                        if ("weapon".equalsIgnoreCase(type)) {
                            equipment = doc.toObject(Weapon.class);
                        } else if ("clothing".equalsIgnoreCase(type)) {
                            equipment = doc.toObject(Clothing.class);
                        } else if ("potion".equalsIgnoreCase(type)) {
                            equipment = doc.toObject(Potion.class);
                        }

                        if (equipment != null) {
                            list.add(equipment);
                        }
                    }

                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onFailure(e));
    }



    public void updateTaskInstance(TaskInstance instance, final DataSourceCallback<Void> callback) {
        if (instance.getUserId() == null || instance.getId() == null) {
            callback.onFailure(new Exception("UserID ili InstanceID je null."));
            return;
        }
        db.collection(USERS_COLLECTION).document(instance.getUserId())
                .collection(INSTANCES_COLLECTION)
                .document(instance.getId())
                .set(instance, SetOptions.merge()) // Koristi merge da ne prebrišeš polja ako ih ne šalješ sve
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    public void deleteTaskInstance(String instanceId, String userId, final DataSourceCallback<Void> callback) {
        if (userId == null || instanceId == null) {
            callback.onFailure(new Exception("UserID ili InstanceID je null."));
            return;
        }
        db.collection(USERS_COLLECTION).document(userId)
                .collection(INSTANCES_COLLECTION)
                .document(instanceId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    private CollectionReference getTaskInstanceCollection(String userId) {
        return db.collection(USERS_COLLECTION).document(userId).collection(INSTANCES_COLLECTION);
    }

    public void getAllUsers(final DataSourceCallback<List<User>> callback) {
        db.collection(USERS_COLLECTION)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<User> users = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            User u = document.toObject(User.class);
                            u.setId(document.getId()); // Ručno postavljamo ID
                            users.add(u);
                        }
                        callback.onSuccess(users);
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    public void updatePassword(String userId, String newPassword, final DataSourceCallback<Void> callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(userId)
                .update("password", newPassword)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) {
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                });
    }

    //*********************DEO SA BOSOM *******************************//

    public void addBoss(Boss boss, final DataSourceCallback<String> callback) {
        // /users/{userId}/bosses/{bossId}
        db.collection(USERS_COLLECTION).document(boss.getUserId())
                .collection(BOSSES_COLLECTION)
                .add(boss)
                .addOnSuccessListener(documentReference -> callback.onSuccess(documentReference.getId()))
                .addOnFailureListener(callback::onFailure);
    }

    public void getAllBosses(String userId, final DataSourceCallback<List<Boss>> callback) {
        db.collection(USERS_COLLECTION).document(userId).collection(BOSSES_COLLECTION)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Boss> bossList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Boss b = document.toObject(Boss.class);
                            b.setId(document.getId());
                            bossList.add(b);
                        }
                        callback.onSuccess(bossList);
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    public void getBossById(String userId, String bossId, final DataSourceCallback<Boss> callback) {
        db.collection(USERS_COLLECTION).document(userId)
                .collection(BOSSES_COLLECTION).document(bossId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Boss boss = document.toObject(Boss.class);
                            boss.setId(document.getId());
                            callback.onSuccess(boss);
                        } else {
                            callback.onFailure(new Exception("Boss not found"));
                        }
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }


    public void updateBoss(Boss boss, final DataSourceCallback<Void> callback) {
        db.collection(USERS_COLLECTION).document(boss.getUserId())
                .collection(BOSSES_COLLECTION).document(boss.getId())
                .set(boss)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    public void addBossBattle(BossBattle battle, final DataSourceCallback<String> callback) {
        db.collection(USERS_COLLECTION).document(battle.getUserId())
                .collection(BOSS_BATTLES_COLLECTION)
                .add(battle)
                .addOnSuccessListener(docRef -> callback.onSuccess(docRef.getId()))
                .addOnFailureListener(callback::onFailure);
    }

    public void getAllBossBattles(String userId, final DataSourceCallback<List<BossBattle>> callback) {
        db.collection(USERS_COLLECTION).document(userId).collection(BOSS_BATTLES_COLLECTION)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<BossBattle> battleList = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            BossBattle b = doc.toObject(BossBattle.class);
                            b.setId(doc.getId());
                            battleList.add(b);
                        }
                        callback.onSuccess(battleList);
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    public void updateBossBattle(BossBattle battle, final DataSourceCallback<Void> callback) {
        db.collection(USERS_COLLECTION).document(battle.getUserId())
                .collection(BOSS_BATTLES_COLLECTION).document(battle.getId())
                .set(battle)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    public void addBossReward(BossReward reward, final DataSourceCallback<String> callback) {
        db.collection(USERS_COLLECTION).document(reward.getUserId())
                .collection(BOSS_REWARDS_COLLECTION)
                .add(reward)
                .addOnSuccessListener(docRef -> callback.onSuccess(docRef.getId()))
                .addOnFailureListener(callback::onFailure);
    }

    public void getAllBossRewards(String userId, final DataSourceCallback<List<BossReward>> callback) {
        db.collection(USERS_COLLECTION).document(userId).collection(BOSS_REWARDS_COLLECTION)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<BossReward> rewardList = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            BossReward r = doc.toObject(BossReward.class);
                            r.setId(doc.getId());
                            rewardList.add(r);
                        }
                        callback.onSuccess(rewardList);
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    public void updateBossReward(BossReward reward, final DataSourceCallback<Void> callback) {
        db.collection(USERS_COLLECTION).document(reward.getUserId())
                .collection(BOSS_REWARDS_COLLECTION).document(reward.getId())
                .set(reward)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    public void getBossByUserIdAndLevel(String userId, int level, final DataSourceCallback<Boss> callback) {
        // Tražimo boss-a sa određenim level-om za korisnika
        db.collection(USERS_COLLECTION).document(userId)
                .collection(BOSSES_COLLECTION)
                .whereEqualTo("level", level)
                .limit(1) // Očekujemo samo jedan boss po nivou
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // Nema boss-a za ovaj nivo
                        callback.onSuccess(null);
                    } else {
                        // Pronađen je boss
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        Boss boss = document.toObject(Boss.class);
                        if (boss != null) {
                            boss.setId(document.getId()); // Postavimo ID iz dokumenta
                        }
                        callback.onSuccess(boss);
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    public void getBattleByUserAndBossAndLevel(String userId, String bossId, int level, final DataSourceCallback<BossBattle> callback) {
        // Tražimo boss battle sa određenim parametrima
        db.collection(USERS_COLLECTION).document(userId)
                .collection(BOSS_BATTLES_COLLECTION)
                .whereEqualTo("bossId", bossId)
                .whereEqualTo("level", level)  // NOVO - direktno int umesto levelInfoId string
                .limit(1) // Očekujemo samo jednu bitku po kombinaciji
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // Nema boss battle-a za ovu kombinaciju
                        callback.onSuccess(null);
                    } else {
                        // Pronađen je boss battle
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        BossBattle bossBattle = document.toObject(BossBattle.class);
                        if (bossBattle != null) {
                            bossBattle.setId(document.getId()); // Postavimo ID iz dokumenta
                        }
                        callback.onSuccess(bossBattle);
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    public void getRewardByUserAndBossAndLevel(String userId, String bossId, int level, final DataSourceCallback<BossReward> callback) {
        // Tražimo boss reward sa određenim parametrima
        db.collection(USERS_COLLECTION).document(userId)
                .collection(BOSS_REWARDS_COLLECTION)
                .whereEqualTo("bossId", bossId)
                .whereEqualTo("level", level)
                .limit(1) // Očekujemo samo jednu nagradu po kombinaciji
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        callback.onSuccess(null);
                    } else {
                        // Pronađen je boss reward
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        BossReward bossReward = document.toObject(BossReward.class);
                        if (bossReward != null) {
                            bossReward.setId(document.getId());
                        }
                        callback.onSuccess(bossReward);
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e));
    }


    public void getPotionsByUserId(String userId, final DataSourceCallback<List<Potion>> callback) {
        db.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            User user = document.toObject(User.class);
                            if (user != null && user.getPotions() != null) {
                                callback.onSuccess(user.getPotions());
                            } else {
                                callback.onSuccess(new ArrayList<>()); // nema napitaka
                            }
                        } else {
                            callback.onSuccess(new ArrayList<>()); // user ne postoji
                        }
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    public void getClothingByUserId(String userId, final DataSourceCallback<List<Clothing>> callback) {
        db.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            User user = document.toObject(User.class);
                            if (user != null && user.getClothing() != null) {
                                callback.onSuccess(user.getClothing());
                            } else {
                                callback.onSuccess(new ArrayList<>()); // nema odece
                            }
                        } else {
                            callback.onSuccess(new ArrayList<>()); // user ne postoji
                        }
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    public void getWeaponByUserId(String userId, final DataSourceCallback<List<Weapon>> callback) {
        db.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            User user = document.toObject(User.class);
                            if (user != null && user.getWeapons() != null) {
                                callback.onSuccess(user.getWeapons());
                            } else {
                                callback.onSuccess(new ArrayList<>()); // nema oruzja
                            }
                        } else {
                            callback.onSuccess(new ArrayList<>()); // user ne postoji
                        }
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    public void addAlliance(Alliance alliance, final DataSourceCallback<String> callback) {
        db.collection(USERS_COLLECTION).document(alliance.getLeader())
                .collection(ALLIANCE_COLLECTION)
                .add(alliance)
                .addOnSuccessListener(documentReference -> callback.onSuccess(documentReference.getId()))
                .addOnFailureListener(callback::onFailure);
    }

    public void setAllianceWithId(Alliance alliance, String allianceId, final DataSourceCallback<String> callback) {
        db.collection(USERS_COLLECTION).document(alliance.getLeader())
                .collection(ALLIANCE_COLLECTION)
                .document(allianceId)
                .set(alliance)
                .addOnSuccessListener(aVoid -> callback.onSuccess(allianceId))
                .addOnFailureListener(callback::onFailure);
    }

    public void getAllAlliances(String userId, final DataSourceCallback<List<Alliance>> callback) {
        db.collection(USERS_COLLECTION).document(userId)
                .collection(ALLIANCE_COLLECTION)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Alliance> allianceList = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Alliance a = doc.toObject(Alliance.class);
                            a.setId(doc.getId());
                            allianceList.add(a);
                        }
                        callback.onSuccess(allianceList);
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    public void deleteAlliance(String allianceId, String userId, final DataSourceCallback<Void> callback) {
        db.collection(USERS_COLLECTION).document(userId)
                .collection(ALLIANCE_COLLECTION)
                .document(allianceId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    public void addAllianceInvite(AllianceInvite invite, final DataSourceCallback<String> callback) {
        // Ako invite.id još nije setovan, generiši UUID i koristi ga kao docId
        if (invite.getId() == null || invite.getId().isEmpty()) {
            invite.setId(UUID.randomUUID().toString());
        }

        db.collection(USERS_COLLECTION)
                .document(invite.getToUser().getId())
                .collection(ALLIANCE_INVITATION_COLLECTION)
                .document(invite.getId()) // koristi invite.id kao docId
                .set(invite)
                .addOnSuccessListener(aVoid -> callback.onSuccess(invite.getId()))
                .addOnFailureListener(callback::onFailure);
    }


    public void setAllianceInviteWithId(AllianceInvite invite, String inviteId, final DataSourceCallback<String> callback) {
        db.collection(USERS_COLLECTION).document(invite.getToUser().getId())
                .collection(ALLIANCE_INVITATION_COLLECTION)
                .document(inviteId)
                .set(invite)
                .addOnSuccessListener(aVoid -> callback.onSuccess(inviteId))
                .addOnFailureListener(callback::onFailure);
    }

    public void getAllAllianceInvites(String userId, final DataSourceCallback<List<AllianceInvite>> callback) {
        db.collection(USERS_COLLECTION).document(userId)
                .collection(ALLIANCE_INVITATION_COLLECTION)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<AllianceInvite> inviteList = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            AllianceInvite invite = doc.toObject(AllianceInvite.class);
                            invite.setId(doc.getId());
                            inviteList.add(invite);
                            Log.d("InviteDebug", "invite: " + invite.getId()
                                    + ", alliance=" + (invite.getAlliance() != null ? invite.getAlliance().getName() : "NULL")
                                    + ", fromUser=" + (invite.getFromUser() != null ? invite.getFromUser().getUsername() : "NULL"));

                        }
                        callback.onSuccess(inviteList);
                    } else {
                        callback.onFailure(task.getException());
                    }
                });

    }

    public void deleteAllianceInvite(String inviteId, String userId, final DataSourceCallback<Void> callback) {
        db.collection(USERS_COLLECTION).document(userId)
                .collection(ALLIANCE_INVITATION_COLLECTION)
                .document(inviteId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    public void getAllianceById(String userId, String allianceId, final DataSourceCallback<Alliance> callback) {
        db.collection(USERS_COLLECTION).document(userId)
                .collection(ALLIANCE_COLLECTION)
                .document(allianceId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Alliance alliance = document.toObject(Alliance.class);
                            if (alliance != null) {
                                alliance.setId(document.getId());
                                callback.onSuccess(alliance);
                            } else {
                                callback.onFailure(new Exception("Alliance object is null"));
                            }
                        } else {
                            callback.onFailure(new Exception("Alliance not found"));
                            Log.d("AllianceRepositoryImpl", "Remote fetch failed: ");

                        }
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    public void updateAlliance(Alliance alliance, final DataSourceCallback<Void> callback) {
        db.collection(USERS_COLLECTION).document(alliance.getLeader())
                .collection(ALLIANCE_COLLECTION)
                .document(alliance.getId())
                .set(alliance)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    public void updateAllianceInvite(AllianceInvite invite, final DataSourceCallback<Void> callback) {
        db.collection(USERS_COLLECTION)
                .document(invite.getToUser().getId())
                .collection(ALLIANCE_INVITATION_COLLECTION)
                .document(invite.getId())
                .update(
                        "accepted", invite.isAccepted(),
                        "responded", invite.isResponded()
                )
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }


    public void getPendingInvites(String userId, final DataSourceCallback<List<AllianceInvite>> callback) {
        db.collection(USERS_COLLECTION)
                .document(userId)
                .collection("alliance_invitations")
                .whereEqualTo("responded", false)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<AllianceInvite> invites = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        AllianceInvite invite = doc.toObject(AllianceInvite.class);
                        invites.add(invite);
                    }
                    callback.onSuccess(invites);
                })
                .addOnFailureListener(callback::onFailure);
    }


    public void acceptInvite(String userId, String inviteId, RepositoryCallback<Void> callback) {
        db.collection(USERS_COLLECTION)
                .document(userId)
                .collection(ALLIANCE_INVITATION_COLLECTION)
                .document(inviteId)
                .update("accepted", true, "responded", true)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    public void rejectInvite(String userId, String inviteId, RepositoryCallback<Void> callback) {
        HashMap<String, Object> updates = new HashMap<>();
        updates.put("accepted", false);
        updates.put("responded", true);

        db.collection(USERS_COLLECTION)
                .document(userId)
                .collection(ALLIANCE_INVITATION_COLLECTION)
                .document(inviteId)
                .set(updates, SetOptions.merge()) // merge: kreira dokument ako ne postoji
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    public void updateAllianceInviteByFieldId(AllianceInvite invite, final DataSourceCallback<Void> callback) {
        db.collection(USERS_COLLECTION)
                .document(invite.getToUser().getId())
                .collection(ALLIANCE_INVITATION_COLLECTION)
                .whereEqualTo("id", invite.getId()) // polje id unutar dokumenta
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // uzimamo prvi dokument koji odgovara
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        db.collection(USERS_COLLECTION)
                                .document(invite.getToUser().getId())
                                .collection(ALLIANCE_INVITATION_COLLECTION)
                                .document(doc.getId()) // stvarni documentId u Firestore
                                .set(invite) // update-ujemo ceo dokument ili samo potrebna polja
                                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                                .addOnFailureListener(callback::onFailure);
                    } else {
                        callback.onFailure(new Exception("Invite not found for field id: " + invite.getId()));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }


    public void getAllianceInviteById(String userId, String inviteId, final DataSourceCallback<AllianceInvite> callback) {
        db.collection(USERS_COLLECTION)
                .document(userId)
                .collection(ALLIANCE_INVITATION_COLLECTION)
                .document(inviteId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot doc = task.getResult();
                        if (doc.exists()) {
                            AllianceInvite invite = doc.toObject(AllianceInvite.class);
                            if (invite != null) {
                                invite.setId(doc.getId());
                                callback.onSuccess(invite);
                            } else {
                                callback.onFailure(new Exception("Invite object is null"));
                            }
                        } else {
                            callback.onFailure(new Exception("Invite not found"));
                        }
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }
    public void addMessage(String allianceLeaderId, String allianceId, AllianceMessage message,
                           final RepositoryCallback<String> callback) {
        getUserById(message.getSenderId(), new DataSourceCallback<User>() {
            @Override
            public void onSuccess(User user) {

                message.setSenderUsername(user.getUsername());
                db.collection(USERS_COLLECTION)
                        .document(allianceLeaderId)
                        .collection(ALLIANCE_COLLECTION)
                        .document(allianceId)
                        .collection(MESSAGE_COLLECTION)
                        .add(message)
                        .addOnSuccessListener(docRef -> {
                            message.setId(docRef.getId());
                            callback.onSuccess(docRef.getId());
                        })
                        .addOnFailureListener(e -> {
                            callback.onFailure(e);
                        });
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }



    public void getAllMessages(String allianceLeaderId, String allianceId,
                               final RepositoryCallback<List<AllianceMessage>> callback) {
        db.collection(USERS_COLLECTION)
                .document(allianceLeaderId)
                .collection(ALLIANCE_COLLECTION)
                .document(allianceId)
                .collection(MESSAGE_COLLECTION)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<AllianceMessage> messages = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            AllianceMessage m = doc.toObject(AllianceMessage.class);
                            m.setId(doc.getId());
                            messages.add(m);
                        }
                        callback.onSuccess(messages);
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    public void createAllianceMission(AllianceMission mission, final DataSourceCallback<String> callback) {
        if (mission.getId() == null || mission.getId().isEmpty()) {
            mission.setId(UUID.randomUUID().toString());
        }

        db.collection(ALLIANCE_MISSION_COLLECTION)
                .document(mission.getId())
                .set(mission)
                .addOnSuccessListener(aVoid -> callback.onSuccess(mission.getId()))
                .addOnFailureListener(callback::onFailure);
    }

    public void getActiveMissionByAllianceId(String allianceId, final DataSourceCallback<AllianceMission> callback) {
        db.collection(ALLIANCE_MISSION_COLLECTION)
                .whereEqualTo("allianceId", allianceId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            AllianceMission mission = doc.toObject(AllianceMission.class);
                            mission.setId(doc.getId());

                            // Proveri da li je misija aktivna
                            Date now = new Date();
                            if (mission.getBoss().getCurrentHp() > 0 && now.before(mission.getEndDate())) {
                                callback.onSuccess(mission);
                                return;
                            }
                        }
                        callback.onSuccess(null); // Nema aktivne misije
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    public void getAllianceMissionById(String missionId, final DataSourceCallback<AllianceMission> callback) {
        db.collection(ALLIANCE_MISSION_COLLECTION)
                .document(missionId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        AllianceMission mission = task.getResult().toObject(AllianceMission.class);
                        mission.setId(task.getResult().getId());
                        callback.onSuccess(mission);
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    public void updateBossHp(String missionId, int newHp, final DataSourceCallback<Void> callback) {
        db.collection(ALLIANCE_MISSION_COLLECTION)
                .document(missionId)
                .update("boss.currentHp", newHp)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    public void updateAllianceMission(AllianceMission mission, final DataSourceCallback<Void> callback) {
        db.collection(ALLIANCE_MISSION_COLLECTION)
                .document(mission.getId())
                .set(mission)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    public void updateMemberProgress(MemberProgress progress, final DataSourceCallback<Void> callback) {
        // Proveri da li progress ima missionId
        if (progress.getMissionId() == null || progress.getMissionId().isEmpty()) {
            callback.onFailure(new Exception("MissionId je obavezan"));
            return;
        }

        // Dobavi misiju
        getAllianceMissionById(progress.getMissionId(), new DataSourceCallback<AllianceMission>() {
            @Override
            public void onSuccess(AllianceMission mission) {
                // Pronađi i ažuriraj progress za korisnika
                List<MemberProgress> progressList = mission.getMemberProgressList();
                boolean found = false;

                for (int i = 0; i < progressList.size(); i++) {
                    if (progressList.get(i).getUserId().equals(progress.getUserId())) {
                        progressList.set(i, progress);
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    progressList.add(progress);
                }

                mission.setMemberProgressList(progressList);
                updateAllianceMission(mission, callback);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public void getMemberProgressByUserId(String missionId, String userId, final DataSourceCallback<MemberProgress> callback) {
        getAllianceMissionById(missionId, new DataSourceCallback<AllianceMission>() {
            @Override
            public void onSuccess(AllianceMission mission) {
                for (MemberProgress progress : mission.getMemberProgressList()) { // ISPRAVI: bilo je getMemberProgressMap()
                    if (progress.getUserId().equals(userId)) {
                        callback.onSuccess(progress);
                        return;
                    }
                }
                callback.onSuccess(null);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public void createAllianceMissionReward(AllianceMissionReward reward, final DataSourceCallback<String> callback) {
        if (reward.getUserId() == null || reward.getUserId().isEmpty()) {
            callback.onFailure(new Exception("UserId je obavezan"));
            return;
        }

        String rewardId = UUID.randomUUID().toString();

        db.collection(USERS_COLLECTION)
                .document(reward.getUserId())
                .collection(ALLIANCE_REWARD_COLLECTION)
                .document(rewardId)
                .set(reward)
                .addOnSuccessListener(aVoid -> callback.onSuccess(rewardId))
                .addOnFailureListener(callback::onFailure);
    }

    public void getAllRewardsByUserId(String userId, final DataSourceCallback<List<AllianceMissionReward>> callback) {
        db.collection(USERS_COLLECTION)
                .document(userId)
                .collection(ALLIANCE_REWARD_COLLECTION)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<AllianceMissionReward> rewards = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            AllianceMissionReward reward = doc.toObject(AllianceMissionReward.class);
                            rewards.add(reward);
                        }
                        callback.onSuccess(rewards);
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    public void getTotalBadgeCount(String userId, final DataSourceCallback<Integer> callback) {
        getAllRewardsByUserId(userId, new DataSourceCallback<List<AllianceMissionReward>>() {
            @Override
            public void onSuccess(List<AllianceMissionReward> rewards) {
                int totalBadges = 0;
                for (AllianceMissionReward reward : rewards) {
                    totalBadges += reward.getBadgeCount();
                }
                callback.onSuccess(totalBadges);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

}
