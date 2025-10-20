package com.samjhadoo.service.friendlytalk;

import com.samjhadoo.dto.friendlytalk.FriendlyTalkQueueDTO;
import com.samjhadoo.dto.friendlytalk.FriendlyTalkSessionDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.model.friendlytalk.FriendlyTalkQueue;
import com.samjhadoo.model.friendlytalk.FriendlyTalkSession;
import com.samjhadoo.model.enums.friendlytalk.MoodType;
import com.samjhadoo.repository.friendlytalk.FriendlyTalkQueueRepository;
import com.samjhadoo.repository.friendlytalk.FriendlyTalkSessionRepository;
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
public class QueueServiceImpl implements QueueService {

    private final FriendlyTalkQueueRepository queueRepository;
    private final FriendlyTalkSessionRepository sessionRepository;
    private final MoodService moodService;

    @Override
    public FriendlyTalkQueueDTO joinQueue(User user, MoodType moodType, int intensity,
                                         boolean anonymous, String preferredTopics,
                                         String avoidTopics, int maxWaitMinutes) {
        // Check if user is already in queue
        FriendlyTalkQueue existingQueue = queueRepository.findByUser(user).orElse(null);
        if (existingQueue != null && existingQueue.isActive()) {
            throw new IllegalArgumentException("User is already in queue");
        }

        // Remove existing queue entry if present
        if (existingQueue != null) {
            queueRepository.delete(existingQueue);
        }

        FriendlyTalkQueue queue = FriendlyTalkQueue.builder()
                .user(user)
                .status(FriendlyTalkQueue.QueueStatus.WAITING)
                .moodType(moodType)
                .intensity(intensity)
                .anonymous(anonymous)
                .preferredTopics(preferredTopics)
                .avoidTopics(avoidTopics)
                .maxWaitMinutes(maxWaitMinutes)
                .estimatedWaitMinutes(calculateEstimatedWaitTime(moodType, intensity))
                .matchingCriteria(String.format("mood:%s,intensity:%d,topics:%s",
                        moodType, intensity, preferredTopics))
                .retryCount(0)
                .build();

        FriendlyTalkQueue savedQueue = queueRepository.save(queue);

        log.info("User {} joined queue with mood {} (intensity: {})",
                user.getId(), moodType, intensity);

        return convertToDTO(savedQueue);
    }

    @Override
    public boolean leaveQueue(User user) {
        FriendlyTalkQueue queue = queueRepository.findByUser(user).orElse(null);
        if (queue == null) {
            return false;
        }

        queue.cancel();
        queueRepository.save(queue);

        log.info("User {} left queue", user.getId());

        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public FriendlyTalkQueueDTO getUserQueueStatus(User user) {
        return queueRepository.findByUser(user)
                .filter(FriendlyTalkQueue::isActive)
                .map(this::convertToDTO)
                .orElse(null);
    }

    @Override
    public FriendlyTalkSessionDTO findMatch(User user) {
        FriendlyTalkQueue userQueue = queueRepository.findByUser(user)
                .filter(FriendlyTalkQueue::isActive)
                .orElse(null);

        if (userQueue == null) {
            return null;
        }

        // Get other active queue entries
        List<FriendlyTalkQueue> otherQueues = queueRepository.findActiveQueue().stream()
                .filter(q -> !q.getUser().getId().equals(user.getId()))
                .collect(Collectors.toList());

        // Find best match
        FriendlyTalkQueue bestMatch = null;
        int bestScore = 0;

        for (FriendlyTalkQueue otherQueue : otherQueues) {
            if (areCompatibleForMatching(userQueue, otherQueue)) {
                int score = calculateMatchingScore(userQueue, otherQueue);
                if (score > bestScore) {
                    bestScore = score;
                    bestMatch = otherQueue;
                }
            }
        }

        if (bestMatch != null) {
            // Create session
            return createSessionFromQueues(userQueue, bestMatch);
        }

        return null;
    }

    @Override
    public int processQueueMatching() {
        List<FriendlyTalkQueue> activeQueues = queueRepository.findActiveQueue();
        int matchesCreated = 0;

        // Simple matching algorithm - pair users with similar moods and intensities
        List<FriendlyTalkQueue> unmatchedQueues = activeQueues.stream()
                .filter(q -> q.getMatchedWith() == null)
                .collect(Collectors.toList());

        for (int i = 0; i < unmatchedQueues.size() - 1; i += 2) {
            FriendlyTalkQueue queue1 = unmatchedQueues.get(i);
            FriendlyTalkQueue queue2 = unmatchedQueues.get(i + 1);

            if (areCompatibleForMatching(queue1, queue2)) {
                createSessionFromQueues(queue1, queue2);
                matchesCreated++;
            }
        }

        return matchesCreated;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendlyTalkQueueDTO> getActiveQueue(int limit) {
        return queueRepository.findActiveQueue().stream()
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendlyTalkQueueDTO> getQueueByMoodType(MoodType moodType, int limit) {
        return queueRepository.findByMoodTypeInQueue(moodType).stream()
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendlyTalkQueueDTO> getHighIntensityQueue(int minIntensity, int limit) {
        return queueRepository.findHighIntensityInQueue(minIntensity).stream()
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public int expireOldQueueEntries() {
        List<FriendlyTalkQueue> expiredQueues = queueRepository.findExpiredQueue(LocalDateTime.now());
        int count = expiredQueues.size();

        if (!expiredQueues.isEmpty()) {
            queueRepository.deleteAll(expiredQueues);
            log.info("Expired {} old queue entries", count);
        }

        return count;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getQueueStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long totalActive = queueRepository.countActiveQueue();
        stats.put("totalActiveQueue", totalActive);

        // Mood distribution in queue
        Map<String, Long> moodDistribution = new HashMap<>();
        for (MoodType moodType : MoodType.values()) {
            long count = queueRepository.findByMoodTypeInQueue(moodType).size();
            moodDistribution.put(moodType.name(), count);
        }
        stats.put("moodDistribution", moodDistribution);

        // Average wait time
        Double avgWait = queueRepository.getAverageEstimatedWait();
        stats.put("averageEstimatedWait", avgWait != null ? avgWait : 0);

        return stats;
    }

    @Override
    public int calculateEstimatedWaitTime(MoodType moodType, int intensity) {
        // Base wait time based on popularity of mood type
        int baseWaitTime = switch (moodType) {
            case LONELY, ANXIOUS -> 5; // High demand moods
            case HAPPY, EXCITED -> 15; // Lower demand moods
            default -> 10; // Medium demand moods
        };

        // Adjust based on intensity (higher intensity = faster matching)
        int intensityAdjustment = Math.max(0, 5 - (intensity / 2));

        return baseWaitTime - intensityAdjustment;
    }

    @Override
    public boolean areCompatibleForMatching(FriendlyTalkQueue queue1, FriendlyTalkQueue queue2) {
        // Check if moods are compatible
        if (queue1.getMoodType() == queue2.getMoodType()) {
            return true; // Same mood types are compatible
        }

        // Check intensity compatibility (within 2 points)
        if (Math.abs(queue1.getIntensity() - queue2.getIntensity()) <= 2) {
            return true;
        }

        // Check topic compatibility if specified
        if (queue1.getPreferredTopics() != null && queue2.getPreferredTopics() != null) {
            String[] topics1 = queue1.getPreferredTopics().split(",");
            String[] topics2 = queue2.getPreferredTopics().split(",");
            for (String topic1 : topics1) {
                for (String topic2 : topics2) {
                    if (topic1.trim().equalsIgnoreCase(topic2.trim())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendlyTalkQueueDTO> getQueueNeedingRetry() {
        return queueRepository.findQueueWithRetries().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean updateMatchingCriteria(User user, String preferredTopics, String avoidTopics) {
        FriendlyTalkQueue queue = queueRepository.findByUser(user)
                .filter(FriendlyTalkQueue::isActive)
                .orElse(null);

        if (queue == null) {
            return false;
        }

        queue.setPreferredTopics(preferredTopics);
        queue.setAvoidTopics(avoidTopics);
        queueRepository.save(queue);

        log.info("Updated matching criteria for user {}", user.getId());

        return true;
    }

    @Override
    public boolean cancelQueueEntry(User user) {
        FriendlyTalkQueue queue = queueRepository.findByUser(user).orElse(null);
        if (queue == null) {
            return false;
        }

        queue.cancel();
        queueRepository.save(queue);

        log.info("Cancelled queue entry for user {}", user.getId());

        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendlyTalkQueueDTO> getRecentlyMatched(int limit) {
        return queueRepository.findRecentlyMatched().stream()
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private FriendlyTalkSessionDTO createSessionFromQueues(FriendlyTalkQueue queue1, FriendlyTalkQueue queue2) {
        // Determine initiator and receiver (first in queue becomes initiator)
        FriendlyTalkQueue initiatorQueue = queue1;
        FriendlyTalkQueue receiverQueue = queue2;

        FriendlyTalkSession session = FriendlyTalkSession.builder()
                .initiator(initiatorQueue.getUser())
                .receiver(receiverQueue.getUser())
                .status(FriendlyTalkSession.SessionStatus.REQUESTED)
                .initiatorMood(initiatorQueue.getMoodType())
                .receiverMood(receiverQueue.getMoodType())
                .anonymous(initiatorQueue.isAnonymous() || receiverQueue.isAnonymous())
                .topic("General conversation")
                .build();

        FriendlyTalkSession savedSession = sessionRepository.save(session);

        // Mark queues as matched
        initiatorQueue.markAsMatched(receiverQueue.getUser());
        receiverQueue.markAsMatched(initiatorQueue.getUser());
        queueRepository.save(initiatorQueue);
        queueRepository.save(receiverQueue);

        log.info("Created session {} between users {} and {}",
                savedSession.getId(), initiatorQueue.getUser().getId(), receiverQueue.getUser().getId());

        return convertSessionToDTO(savedSession);
    }

    private int calculateMatchingScore(FriendlyTalkQueue queue1, FriendlyTalkQueue queue2) {
        int score = 0;

        // Mood type compatibility
        if (queue1.getMoodType() == queue2.getMoodType()) {
            score += 50;
        }

        // Intensity compatibility
        int intensityDiff = Math.abs(queue1.getIntensity() - queue2.getIntensity());
        score += Math.max(0, 30 - intensityDiff * 5);

        // Topic compatibility
        if (queue1.getPreferredTopics() != null && queue2.getPreferredTopics() != null) {
            String[] topics1 = queue1.getPreferredTopics().split(",");
            String[] topics2 = queue2.getPreferredTopics().split(",");
            for (String topic1 : topics1) {
                for (String topic2 : topics2) {
                    if (topic1.trim().equalsIgnoreCase(topic2.trim())) {
                        score += 20;
                        break;
                    }
                }
            }
        }

        return score;
    }

    private FriendlyTalkQueueDTO convertToDTO(FriendlyTalkQueue queue) {
        return FriendlyTalkQueueDTO.builder()
                .id(queue.getId())
                .userName(queue.isAnonymous() ? "Anonymous" :
                         queue.getUser().getFirstName() + " " + queue.getUser().getLastName())
                .status(queue.getStatus().name())
                .moodType(queue.getMoodType())
                .intensity(queue.getIntensity())
                .anonymous(queue.isAnonymous())
                .preferredTopics(queue.getPreferredTopics())
                .avoidTopics(queue.getAvoidTopics())
                .maxWaitMinutes(queue.getMaxWaitMinutes())
                .joinedAt(queue.getJoinedAt())
                .matchedAt(queue.getMatchedAt())
                .expiresAt(queue.getExpiresAt())
                .estimatedWaitMinutes(queue.getEstimatedWaitMinutes())
                .matchedWithName(queue.getMatchedWith() != null ?
                               (queue.isAnonymous() ? "Anonymous" :
                                queue.getMatchedWith().getFirstName() + " " + queue.getMatchedWith().getLastName()) : null)
                .matchingCriteria(queue.getMatchingCriteria())
                .retryCount(queue.getRetryCount())
                .lastRetryAt(queue.getLastRetryAt())
                .waitingTimeMinutes(queue.getWaitingTimeMinutes())
                .active(queue.isActive())
                .build();
    }

    private FriendlyTalkSessionDTO convertSessionToDTO(FriendlyTalkSession session) {
        return FriendlyTalkSessionDTO.builder()
                .id(session.getId())
                .initiatorName(session.isAnonymous() ? "Anonymous" :
                              session.getInitiator().getFirstName() + " " + session.getInitiator().getLastName())
                .receiverName(session.isAnonymous() ? "Anonymous" :
                             session.getReceiver().getFirstName() + " " + session.getReceiver().getLastName())
                .status(session.getStatus().name())
                .initiatorMood(session.getInitiatorMood())
                .receiverMood(session.getReceiverMood())
                .anonymous(session.isAnonymous())
                .topic(session.getTopic())
                .notes(session.getNotes())
                .startedAt(session.getStartedAt())
                .endedAt(session.getEndedAt())
                .durationMinutes(session.getDurationMinutes())
                .satisfactionRating(session.getSatisfactionRating())
                .feedback(session.getFeedback())
                .reportedAt(session.getReportedAt())
                .reportReason(session.getReportReason())
                .moderatorName(session.getModerator() != null ?
                              (session.isAnonymous() ? "Anonymous" :
                               session.getModerator().getFirstName() + " " + session.getModerator().getLastName()) : null)
                .moderationNotes(session.getModerationNotes())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .build();
    }
}
