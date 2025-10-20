package com.samjhadoo.service.friendlytalk;

import com.samjhadoo.dto.friendlytalk.FriendlyTalkRoomDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.model.friendlytalk.FriendlyTalkRoom;
import com.samjhadoo.model.enums.friendlytalk.RoomStatus;
import com.samjhadoo.repository.friendlytalk.FriendlyTalkRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RoomServiceImpl implements RoomService {

    private final FriendlyTalkRoomRepository roomRepository;

    @Override
    public FriendlyTalkRoomDTO createRoom(User creator, String name, String description,
                                         int maxParticipants, boolean anonymous,
                                         String topicTags, String moodFocus) {
        // Validate max participants
        if (maxParticipants < 2 || maxParticipants > 10) {
            throw new IllegalArgumentException("Max participants must be between 2 and 10");
        }

        FriendlyTalkRoom room = FriendlyTalkRoom.builder()
                .roomId(UUID.randomUUID().toString())
                .name(name)
                .description(description)
                .creator(creator)
                .status(RoomStatus.WAITING)
                .maxParticipants(maxParticipants)
                .currentParticipants(0)
                .anonymous(anonymous)
                .recorded(false) // Default to not recorded for privacy
                .allowScreenShare(false) // Default to no screen share
                .requireModeratorApproval(false) // Default to no approval required
                .topicTags(topicTags)
                .moodFocus(moodFocus)
                .build();

        FriendlyTalkRoom savedRoom = roomRepository.save(room);

        log.info("Created room {} for user {}: {} (max: {})",
                savedRoom.getRoomId(), creator.getId(), name, maxParticipants);

        return convertToDTO(savedRoom, creator);
    }

    @Override
    public FriendlyTalkRoomDTO joinRoom(User user, String roomId) {
        FriendlyTalkRoom room = roomRepository.findByRoomId(roomId).orElse(null);
        if (room == null) {
            throw new IllegalArgumentException("Room not found");
        }

        if (room.getStatus() == RoomStatus.CLOSED) {
            throw new IllegalArgumentException("Room is closed");
        }

        if (room.isFull()) {
            throw new IllegalArgumentException("Room is full");
        }

        // Check if user is already in the room
        if (room.getParticipants().contains(user)) {
            return convertToDTO(room, user);
        }

        // Add user to room
        room.addParticipant(user);
        roomRepository.save(room);

        log.info("User {} joined room {}", user.getId(), roomId);

        return convertToDTO(room, user);
    }

    @Override
    public boolean leaveRoom(User user, String roomId) {
        FriendlyTalkRoom room = roomRepository.findByRoomId(roomId).orElse(null);
        if (room == null) {
            return false;
        }

        boolean removed = room.removeParticipant(user);
        if (removed) {
            roomRepository.save(room);
            log.info("User {} left room {}", user.getId(), roomId);
        }

        return removed;
    }

    @Override
    @Transactional(readOnly = true)
    public FriendlyTalkRoomDTO getRoom(String roomId, User user) {
        FriendlyTalkRoom room = roomRepository.findByRoomId(roomId).orElse(null);
        if (room == null) {
            return null;
        }

        return convertToDTO(room, user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendlyTalkRoomDTO> getActiveRooms(int limit) {
        return roomRepository.findActiveRooms().stream()
                .limit(limit)
                .map(room -> convertToDTO(room, null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendlyTalkRoomDTO> getAvailableRooms(int limit) {
        return roomRepository.findAvailableRooms().stream()
                .limit(limit)
                .map(room -> convertToDTO(room, null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendlyTalkRoomDTO> getRoomsByMoodFocus(String moodFocus, int limit) {
        return roomRepository.findRoomsByMoodFocus(moodFocus).stream()
                .limit(limit)
                .map(room -> convertToDTO(room, null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendlyTalkRoomDTO> getRoomsByTopic(String topic, int limit) {
        return roomRepository.findRoomsByTopic(topic).stream()
                .limit(limit)
                .map(room -> convertToDTO(room, null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendlyTalkRoomDTO> getUserRooms(User user) {
        return roomRepository.findByCreator(user).stream()
                .map(room -> convertToDTO(room, user))
                .collect(Collectors.toList());
    }

    @Override
    public boolean closeRoom(String roomId, User user) {
        FriendlyTalkRoom room = roomRepository.findByRoomId(roomId).orElse(null);
        if (room == null) {
            return false;
        }

        // Check if user is creator or moderator
        if (!room.getCreator().getId().equals(user.getId()) &&
            (room.getModerator() == null || !room.getModerator().getId().equals(user.getId()))) {
            throw new IllegalArgumentException("Only creator or moderator can close the room");
        }

        room.closeRoom();
        roomRepository.save(room);

        log.info("Room {} closed by user {}", roomId, user.getId());

        return true;
    }

    @Override
    public boolean assignModerator(String roomId, User moderator) {
        FriendlyTalkRoom room = roomRepository.findByRoomId(roomId).orElse(null);
        if (room == null) {
            return false;
        }

        room.setModerator(moderator);
        roomRepository.save(room);

        log.info("Assigned moderator {} to room {}", moderator.getId(), roomId);

        return true;
    }

    @Override
    public boolean removeParticipant(String roomId, Long participantId, User moderator) {
        FriendlyTalkRoom room = roomRepository.findByRoomId(roomId).orElse(null);
        if (room == null) {
            return false;
        }

        // Check if user is moderator
        if (room.getModerator() == null || !room.getModerator().getId().equals(moderator.getId())) {
            throw new IllegalArgumentException("Only moderator can remove participants");
        }

        // Find participant user (in real implementation, you'd fetch by ID)
        User participant = room.getParticipants().stream()
                .filter(p -> p.getId().equals(participantId))
                .findFirst()
                .orElse(null);

        if (participant == null) {
            return false;
        }

        boolean removed = room.removeParticipant(participant);
        if (removed) {
            roomRepository.save(room);
            log.info("Moderator {} removed participant {} from room {}",
                    moderator.getId(), participantId, roomId);
        }

        return removed;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getRoomStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long totalRooms = roomRepository.count();
        long activeRooms = roomRepository.countActiveRooms();

        stats.put("totalRooms", totalRooms);
        stats.put("activeRooms", activeRooms);

        // Room status distribution
        Map<String, Long> statusDistribution = new HashMap<>();
        for (RoomStatus status : RoomStatus.values()) {
            long count = roomRepository.findByStatus(status).size();
            statusDistribution.put(status.name(), count);
        }
        stats.put("statusDistribution", statusDistribution);

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendlyTalkRoomDTO> getLeastCrowdedRooms(int limit) {
        return roomRepository.findLeastCrowdedRooms().stream()
                .limit(limit)
                .map(room -> convertToDTO(room, null))
                .collect(Collectors.toList());
    }

    @Override
    public FriendlyTalkRoomDTO updateRoom(String roomId, User user, String name,
                                         String description, String topicTags) {
        FriendlyTalkRoom room = roomRepository.findByRoomId(roomId).orElse(null);
        if (room == null) {
            throw new IllegalArgumentException("Room not found");
        }

        // Check if user is creator
        if (!room.getCreator().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Only creator can update the room");
        }

        room.setName(name);
        room.setDescription(description);
        room.setTopicTags(topicTags);

        FriendlyTalkRoom savedRoom = roomRepository.save(room);

        log.info("Updated room {} by user {}", roomId, user.getId());

        return convertToDTO(savedRoom, user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUserInRoom(User user, String roomId) {
        FriendlyTalkRoom room = roomRepository.findByRoomId(roomId).orElse(null);
        return room != null && room.getParticipants().contains(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendlyTalkRoomDTO> getModeratedRooms(User user) {
        return roomRepository.findRoomsModeratedBy(user).stream()
                .map(room -> convertToDTO(room, user))
                .collect(Collectors.toList());
    }

    private FriendlyTalkRoomDTO convertToDTO(FriendlyTalkRoom room, User requestingUser) {
        List<String> participantNames = room.getParticipants().stream()
                .map(p -> room.isAnonymous() ? "Anonymous" :
                         p.getFirstName() + " " + p.getLastName())
                .collect(Collectors.toList());

        boolean userIsParticipant = requestingUser != null && room.getParticipants().contains(requestingUser);
        boolean userIsModerator = requestingUser != null &&
                                 room.getModerator() != null &&
                                 room.getModerator().getId().equals(requestingUser.getId());

        return FriendlyTalkRoomDTO.builder()
                .id(room.getId())
                .roomId(room.getRoomId())
                .name(room.getName())
                .description(room.getDescription())
                .creatorName(room.isAnonymous() ? "Anonymous" :
                           room.getCreator().getFirstName() + " " + room.getCreator().getLastName())
                .status(room.getStatus())
                .maxParticipants(room.getMaxParticipants())
                .currentParticipants(room.getCurrentParticipants())
                .anonymous(room.isAnonymous())
                .recorded(room.isRecorded())
                .allowScreenShare(room.isAllowScreenShare())
                .requireModeratorApproval(room.isRequireModeratorApproval())
                .moderatorName(room.getModerator() != null ?
                              (room.isAnonymous() ? "Anonymous" :
                               room.getModerator().getFirstName() + " " + room.getModerator().getLastName()) : null)
                .createdAt(room.getCreatedAt())
                .startedAt(room.getStartedAt())
                .endedAt(room.getEndedAt())
                .durationMinutes(room.getDurationMinutes())
                .topicTags(room.getTopicTags())
                .moodFocus(room.getMoodFocus())
                .participantNames(participantNames)
                .userIsParticipant(userIsParticipant)
                .userIsModerator(userIsModerator)
                .build();
    }
}
