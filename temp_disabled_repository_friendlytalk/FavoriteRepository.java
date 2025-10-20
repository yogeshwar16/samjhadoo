package com.samjhadoo.repository.friendlytalk;

import com.samjhadoo.model.User;
import com.samjhadoo.model.friendlytalk.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    List<Favorite> findByUser(User user);

    List<Favorite> findByFavoriteUser(User favoriteUser);

    Optional<Favorite> findByUserAndFavoriteUser(User user, User favoriteUser);

    List<Favorite> findByUserAndTag(User user, String tag);

    boolean existsByUserAndFavoriteUser(User user, User favoriteUser);

    void deleteByUserAndFavoriteUser(User user, User favoriteUser);

    @Query("SELECT COUNT(f) FROM Favorite f WHERE f.favoriteUser = :user")
    long countFavoritesByUser(@Param("user") User user);

    @Query("SELECT f FROM Favorite f WHERE f.user = :user AND f.notifyWhenOnline = true")
    List<Favorite> findByUserWithNotificationsEnabled(@Param("user") User user);

    @Query("SELECT f FROM Favorite f WHERE f.user = :user AND f.isMutual = true")
    List<Favorite> findMutualFavorites(@Param("user") User user);

    @Query("SELECT DISTINCT f.tag FROM Favorite f WHERE f.user = :user AND f.tag IS NOT NULL")
    List<String> findDistinctTagsByUser(@Param("user") User user);
}
