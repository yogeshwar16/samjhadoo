package com.samjhadoo.service.friendlytalk;

import com.samjhadoo.dto.friendlytalk.FavoriteDTO;

import java.util.List;

public interface FavoriteService {

    FavoriteDTO addFavorite(FavoriteDTO.CreateRequest request, Long userId);

    FavoriteDTO updateFavorite(Long favoriteId, FavoriteDTO.UpdateRequest request, Long userId);

    void removeFavorite(Long favoriteId, Long userId);

    void removeFavoriteByUser(Long favoriteUserId, Long userId);

    FavoriteDTO getFavoriteById(Long favoriteId);

    List<FavoriteDTO> getUserFavorites(Long userId);

    List<FavoriteDTO> getFavoritesByTag(Long userId, String tag);

    List<String> getUserFavoriteTags(Long userId);

    List<FavoriteDTO> getMutualFavorites(Long userId);

    boolean isFavorite(Long userId, Long favoriteUserId);

    long getFavoriteCount(Long userId);

    void checkMutualFavorites(Long userId, Long favoriteUserId);
}
