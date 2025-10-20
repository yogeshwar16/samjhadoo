package com.samjhadoo.model;

import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.communication.MessageType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a chat message in a chat room.
 */
@Entity
@Table(name = "chat_messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String messageId; // UUID for external reference

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type;

    @Lob
    @Column
    private String content; // Text content for text messages

    @Column(name = "file_url")
    private String fileUrl; // URL for file/image attachments

    @Column(name = "file_name")
    private String fileName; // Original file name

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "file_type")
    private String fileType; // MIME type

    @Column(name = "thumbnail_url")
    private String thumbnailUrl; // Thumbnail for images/videos

    @Column(name = "duration_seconds")
    private Integer durationSeconds; // For voice notes/videos

    @Column(name = "voice_transcript")
    private String voiceTranscript; // AI-generated transcript for voice messages

    @Column(name = "location_latitude")
    private Double locationLatitude;

    @Column(name = "location_longitude")
    private Double locationLongitude;

    @Column(name = "location_name")
    private String locationName;

    @Column(name = "contact_name")
    private String contactName; // For contact sharing

    @Column(name = "contact_number")
    private String contactNumber;

    @Column(name = "is_edited", nullable = false)
    private boolean edited;

    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by_id")
    private Long deletedById;

    @Column(name = "is_system_message", nullable = false)
    private boolean systemMessage;

    @Column(name = "is_pinned", nullable = false)
    private boolean pinned;

    @Column(name = "reply_to_message_id")
    private String replyToMessageId; // ID of message being replied to

    @Column(name = "thread_id")
    private String threadId; // For threaded conversations

    @Lob
    @Column(name = "reactions")
    private String reactions; // JSON object of emoji reactions

    @Column(name = "read_by_count", nullable = false)
    private int readByCount;

    @Column(name = "delivered_to_count", nullable = false)
    private int deliveredToCount;

    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    @Column(name = "is_encrypted", nullable = false)
    private boolean encrypted;

    @Lob
    @Column(name = "metadata")
    private String metadata; // Additional message metadata

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (readByCount == 0) {
            readByCount = 0;
        }
        if (deliveredToCount == 0) {
            deliveredToCount = 0;
        }
        if (systemMessage) {
            systemMessage = false; // Default to user message
        }
        if (pinned) {
            pinned = false; // Default to not pinned
        }
        if (encrypted) {
            encrypted = false; // Default to not encrypted
        }
        // default unread
        if (isRead) {
            isRead = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();

        if (edited && editedAt == null) {
            editedAt = LocalDateTime.now();
        }

        if (deleted && deletedAt == null) {
            deletedAt = LocalDateTime.now();
        }
    }

    /**
     * Marks the message as edited.
     */
    public void markEdited() {
        this.edited = true;
        this.editedAt = LocalDateTime.now();
    }

    /**
     * Marks the message as deleted.
     * @param deletedBy The user who deleted it
     */
    public void markDeleted(User deletedBy) {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedById = deletedBy.getId();
    }

    /**
     * Increments the read count.
     */
    public void incrementReadCount() {
        this.readByCount++;
    }

    /**
     * Increments the delivered count.
     */
    public void incrementDeliveredCount() {
        this.deliveredToCount++;
    }

    /**
     * Pins or unpins the message.
     * @param pinned Whether to pin
     */
    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    /**
     * Adds a reaction to the message.
     * @param emoji The emoji reaction
     * @param userId The user who reacted
     */
    public void addReaction(String emoji, Long userId) {
        // In a real implementation, this would update the reactions JSON
        // For now, we'll just log it
    }

    /**
     * Removes a reaction from the message.
     * @param emoji The emoji reaction
     * @param userId The user who reacted
     */
    public void removeReaction(String emoji, Long userId) {
        // In a real implementation, this would update the reactions JSON
        // For now, we'll just log it
    }

    /**
     * Checks if the message has been read by a specific user.
     * @param userId The user ID
     * @return true if read by the user
     */
    public boolean isReadByUser(Long userId) {
        // In a real implementation, this would check a read receipts table
        // For now, we'll return false as this requires more complex logic
        return false;
    }

    /**
     * Gets the message read rate.
     * @return Read rate as percentage (0-100)
     */
    public double getReadRate() {
        if (deliveredToCount == 0) {
            return 0;
        }
        return ((double) readByCount / deliveredToCount) * 100;
    }

    /**
     * Checks if the message is a media message.
     * @return true if contains media
     */
    public boolean isMediaMessage() {
        return type == MessageType.IMAGE ||
               type == MessageType.FILE ||
               type == MessageType.VOICE_NOTE ||
               type == MessageType.VIDEO;
    }

    /**
     * Checks if the message is a system message.
     * @return true if system message
     */
    public boolean isSystemMessage() {
        return systemMessage || type == MessageType.SYSTEM;
    }

    /**
     * Gets the message age in minutes.
     * @return Age in minutes
     */
    public long getAgeInMinutes() {
        if (createdAt != null) {
            return java.time.Duration.between(createdAt, LocalDateTime.now()).toMinutes();
        }
        return 0;
    }

    /**
     * Checks if the message should be auto-deleted based on retention policy.
     * @return true if should be deleted
     */
    public boolean shouldAutoDelete() {
        if (chatRoom.getMessageRetentionDays() == null) {
            return false;
        }

        return createdAt.isBefore(LocalDateTime.now().minusDays(chatRoom.getMessageRetentionDays()));
    }

    /**
     * Gets the message priority for delivery.
     * @return Priority score (higher = more important)
     */
    public int getPriorityScore() {
        int score = 1; // Base priority

        // System messages get higher priority
        if (isSystemMessage()) {
            score += 10;
        }

        // Media messages get higher priority
        if (isMediaMessage()) {
            score += 5;
        }

        // Pinned messages get higher priority
        if (pinned) {
            score += 8;
        }

        // Recent messages get higher priority
        if (getAgeInMinutes() < 5) {
            score += 3;
        }

        return score;
    }

    /**
     * Checks if the message contains sensitive content.
     * @return true if contains sensitive content
     */
    public boolean containsSensitiveContent() {
        if (content == null) {
            return false;
        }

        // Check for sensitive keywords (in a real implementation, this would use AI)
        String[] sensitiveWords = {"password", "ssn", "credit card", "bank account"};
        String lowerContent = content.toLowerCase();

        for (String word : sensitiveWords) {
            if (lowerContent.contains(word)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Gets the message content preview (first 100 characters).
     * @return Content preview
     */
    public String getContentPreview() {
        if (content == null) {
            return "";
        }

        if (content.length() <= 100) {
            return content;
        }

        return content.substring(0, 97) + "...";
    }

    /**
     * Checks if the message is from a bot or automated system.
     * @return true if likely automated
     */
    public boolean isLikelyAutomated() {
        return systemMessage ||
               (content != null && content.toLowerCase().contains("bot")) ||
               sender.getEmail().contains("bot") ||
               sender.getEmail().contains("system");
    }

    /**
     * Gets the message engagement score.
     * @return Engagement score based on reads and reactions
     */
    public int getEngagementScore() {
        int score = 0;

        // Read count bonus
        score += Math.min(50, readByCount * 2);

        // Reactions bonus (mock calculation)
        if (reactions != null && !reactions.trim().isEmpty()) {
            score += 10;
        }

        // Media content bonus
        if (isMediaMessage()) {
            score += 15;
        }

        // Reply bonus
        if (replyToMessageId != null) {
            score += 5;
        }

        return Math.min(100, score);
    }

    /**
     * Checks if the message should trigger notifications.
     * @return true if should notify
     */
    public boolean shouldTriggerNotification() {
        // Don't notify for system messages
        if (isSystemMessage()) {
            return false;
        }

        // Don't notify for edited messages
        if (edited) {
            return false;
        }

        // Don't notify for old messages
        if (getAgeInMinutes() > 60) {
            return false;
        }

        return true;
    }

    /**
     * Gets the message file size in MB.
     * @return File size in MB, or 0 if no file
     */
    public double getFileSizeMB() {
        if (fileSizeBytes == null) {
            return 0;
        }
        return fileSizeBytes / (1024.0 * 1024.0);
    }

    /**
     * Checks if the message file is large (>10MB).
     * @return true if large file
     */
    public boolean isLargeFile() {
        return fileSizeBytes != null && fileSizeBytes > 10 * 1024 * 1024;
    }

    /**
     * Gets the message type display name.
     * @return Human-readable message type
     */
    public String getTypeDisplayName() {
        return switch (type) {
            case TEXT -> "Text";
            case IMAGE -> "Image";
            case FILE -> "File";
            case VOICE -> "Voice";
            case VOICE_NOTE -> "Voice Note";
            case VIDEO -> "Video";
            case SYSTEM -> "System";
            case EMOJI -> "Emoji";
            case LOCATION -> "Location";
            case CONTACT -> "Contact";
            case STICKER -> "Sticker";
        };
    }

    /**
     * Checks if the message is a reply to another message.
     * @return true if reply
     */
    public boolean isReply() {
        return replyToMessageId != null;
    }

    /**
     * Checks if the message is part of a thread.
     * @return true if threaded
     */
    public boolean isThreaded() {
        return threadId != null;
    }

    /**
     * Gets the message delivery status for a specific user.
     * @param userId The user ID
     * @return Delivery status
     */
    public String getDeliveryStatusForUser(Long userId) {
        if (isReadByUser(userId)) {
            return "READ";
        } else if (deliveredToCount > 0) {
            return "DELIVERED";
        } else {
            return "SENT";
        }
    }

    /**
     * Updates the voice transcript.
     * @param transcript The transcript text
     */
    public void updateVoiceTranscript(String transcript) {
        this.voiceTranscript = transcript;
    }

    /**
     * Sets location information.
     * @param latitude Latitude
     * @param longitude Longitude
     * @param name Location name
     */
    public void setLocation(double latitude, double longitude, String name) {
        this.locationLatitude = latitude;
        this.locationLongitude = longitude;
        this.locationName = name;
        this.type = MessageType.LOCATION;
    }

    /**
     * Sets contact information.
     * @param name Contact name
     * @param number Contact number
     */
    public void setContact(String name, String number) {
        this.contactName = name;
        this.contactNumber = number;
        this.type = MessageType.CONTACT;
    }

    /**
     * Checks if the message is eligible for auto-deletion.
     * @return true if should be auto-deleted
     */
    public boolean isEligibleForAutoDeletion() {
        return chatRoom.isAutoDeleteMessages() && shouldAutoDelete();
    }

    /**
     * Gets the message search score for relevance ranking.
     * @param searchTerm The search term
     * @return Relevance score (higher = more relevant)
     */
    public double getSearchRelevanceScore(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return 0;
        }

        double score = 0;
        String lowerSearch = searchTerm.toLowerCase();
        String lowerContent = (content != null ? content.toLowerCase() : "");

        // Exact match in content
        if (lowerContent.contains(lowerSearch)) {
            score += 10;
        }

        // Match in file name
        if (fileName != null && fileName.toLowerCase().contains(lowerSearch)) {
            score += 5;
        }

        // Match in voice transcript
        if (voiceTranscript != null && voiceTranscript.toLowerCase().contains(lowerSearch)) {
            score += 7;
        }

        // Recent messages get bonus
        if (getAgeInMinutes() < 60) {
            score += 2;
        }

        return score;
    }
}
