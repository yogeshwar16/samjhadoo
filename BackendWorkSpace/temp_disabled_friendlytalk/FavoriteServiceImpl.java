package com.samjhadoo.service.friendlytalk;

import com.samjhadoo.dto.friendlytalk.FavoriteDTO;
import com.samjhadoo.exception.ResourceNotFoundException;
import com.samjhadoo.exception.UnauthorizedException;
import com.samjhadoo.model.User;
import com.samjhadoo.model.friendlytalk.Favorite;
import com.samjhadoo.repository.UserRepository;
import com.samjhadoo.repository.friendlytalk.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public FavoriteDTO addFavorite(FavoriteDTO.CreateRequest request, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        User favoriteUser = userRepository.findById(request.getFavoriteUserId())
            .orElseThrow(() -> new ResourceNotFoundException("Favorite user not found"));

        if (userId.equals(request.getFavoriteUserId())) {
            throw new IllegalArgumentException("Cannot add yourself as a favorite");
        }

        if (favoriteRepository.existsByUserAndFavoriteUser(user, favoriteUser)) {
            throw new IllegalStateException("User is already in favorites");
        }

        Favorite favorite = Favorite.builder()
            .user(user)
            .favoriteUser(favoriteUser)
            .tag(request.getTag())
            .notes(request.getNotes())
            .notifyWhenOnline(request.getNotifyWhenOnline() != null ? request.getNotifyWhenOnline() : true)
            .build();

        Favorite savedFavorite = favoriteRepository.save(favorite);
        log.info("User {} added user {} to favorites", userId, request.getFavoriteUserId());

        // Check if it's mutual
        checkMutualFavorites(userId, request.getFavoriteUserId());

        return convertToDTO(savedFavorite);
    }

    @Override
    @Transactional
    public FavoriteDTO updateFavorite(Long favoriteId, FavoriteDTO.UpdateRequest request, Long userId) {
        Favorite favorite = favoriteRepository.findById(favoriteId)
            .orElseThrow(() -> new ResourceNotFoundException("Favorite not found"));

        if (!favorite.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only update your own favorites");
        }

        if (request.getTag() != null) favorite.setTag(request.getTag());
        if (request.getNotes() != null) favorite.setNotes(request.getNotes());
        if (request.getNotifyWhenOnline() != null) favorite.setNotifyWhenOnline(request.getNotifyWhenOnline());

        Favorite updatedFavorite = favoriteRepository.save(favorite);
        log.info("Updated favorite: {}", favoriteId);

        return convertToDTO(updatedFavorite);
    }

    @Override
    @Transactional
    public void removeFavorite(Long favoriteId, Long userId) {
        Favorite favorite = favoriteRepository.findById(favoriteId)
            .orElseThrow(() -> new ResourceNotFoundException("Favorite not found"));

        if (!favorite.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only remove your own favorites");
        }

        Long favoriteUserId = favorite.getFavoriteUser().getId();
        favoriteRepository.delete(favorite);
        log.info("User {} removed user {} from favorites", userId, favoriteUserId);

        // Update mutual status
        checkMutualFavorites(userId, favoriteUserId);
    }

    @Override
    @Transactional
    public void removeFavoriteByUser(Long favoriteUserId, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        User favoriteUser = userRepository.findById(favoriteUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Favorite user not found"));

        favoriteRepository.deleteByUserAndFavoriteUser(user, favoriteUser);
        log.info("User {} removed user {} from favorites", userId, favoriteUserId);

        // Update mutual status
        checkMutualFavorites(userId, favoriteUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public FavoriteDTO getFavoriteById(Long favoriteId) {
        Favorite favorite = favoriteRepository.findById(favoriteId)
            .orElseThrow(() -> new ResourceNotFoundException("Favorite not found"));
        return convertToDTO(favorite);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FavoriteDTO> getUserFavorites(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return favoriteRepository.findByUser(user)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FavoriteDTO> getFavoritesByTag(Long userId, String tag) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return favoriteRepository.findByUserAndTag(user, tag)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getUserFavoriteTags(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return favoriteRepository.findDistinctTagsByUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FavoriteDTO> getMutualFavorites(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return favoriteRepository.findMutualFavorites(user)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFavorite(Long userId, Long favoriteUserId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        User favoriteUser = userRepository.findById(favoriteUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Favorite user not found"));

        return favoriteRepository.existsByUserAndFavoriteUser(user, favoriteUser);
    }

    @Override
    @Transactional(readOnly = true)
    public long getFavoriteCount(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return favoriteRepository.countFavoritesByUser(user);
    }

    @Override
    @Transactional
    public void checkMutualFavorites(Long userId, Long favoriteUserId) {
        User user = userRepository.findById(userId).orElse(null);
        User favoriteUser = userRepository.findById(favoriteUserId).orElse(null);

        if (user == null || favoriteUser == null) {
            return;
        }

        // Check if both users have favorited each other
        boolean userFavoritedOther = favoriteRepository.existsByUserAndFavoriteUser(user, favoriteUser);
        boolean otherFavoritedUser = favoriteRepository.existsByUserAndFavoriteUser(favoriteUser, user);

        boolean isMutual = userFavoritedOther && otherFavoritedUser;

        // Update mutual status for both favorites
        favoriteRepository.findByUserAndFavoriteUser(user, favoriteUser)
            .ifPresent(fav -> {
                fav.setIsMutual(isMutual);
                favoriteRepository.save(fav);
            });

        favoriteRepository.findByUserAndFavoriteUser(favoriteUser, user)
            .ifPresent(fav -> {
                fav.setIsMutual(isMutual);
                favoriteRepository.save(fav);
            });

        if (isMutual) {
            log.info("Mutual favorite detected between users {} and {}", userId, favoriteUserId);
            notifyMutualFavorite(user, favoriteUser);
        }
    }

    private FavoriteDTO convertToDTO(Favorite favorite) {
        return FavoriteDTO.builder()
            .id(favorite.getId())
            .user(FavoriteDTO.UserInfo.builder()
                .id(favorite.getUser().getId())
                .name(favorite.getUser().getName())
                .email(favorite.getUser().getEmail())
                .build())
            .favoriteUser(FavoriteDTO.UserInfo.builder()
                .id(favorite.getFavoriteUser().getId())
                .name(favorite.getFavoriteUser().getName())
                .email(favorite.getFavoriteUser().getEmail())
                .build())
            .tag(favorite.getTag())
            .notes(favorite.getNotes())
            .notifyWhenOnline(favorite.getNotifyWhenOnline())
            .isMutual(favorite.getIsMutual())
            .createdAt(favorite.getCreatedAt())
            .build();
    }

    private void notifyMutualFavorite(User user1, User user2) {
        try {
            messagingTemplate.convertAndSendToUser(
                user1.getEmail(),
                "/queue/notifications",
                new MutualFavoriteMessage(user2.getId(), user2.getName() + " has also added you as a favorite!")
            );

            messagingTemplate.convertAndSendToUser(
                user2.getEmail(),
                "/queue/notifications",
                new MutualFavoriteMessage(user1.getId(), user1.getName() + " has also added you as a favorite!")
            );
        } catch (Exception e) {
            log.error("Failed to send mutual favorite notification: {}", e.getMessage());
        }
    }

    private record MutualFavoriteMessage(Long userId, String message) {}
}
