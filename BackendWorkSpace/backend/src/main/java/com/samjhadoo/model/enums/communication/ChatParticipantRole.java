package com.samjhadoo.model.enums.communication;

public enum ChatParticipantRole {
    OWNER,          // Room creator/owner
    ADMIN,          // Administrator with moderation rights
    MODERATOR,      // Moderator with limited admin rights
    MEMBER,         // Regular participant
    GUEST,          // Guest with limited access
    MUTED,          // Muted participant
    BANNED          // Banned participant
}
