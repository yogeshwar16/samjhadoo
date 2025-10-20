package com.samjhadoo.model.enums;

/**
 * Community tags for targeted pricing and personalization
 */
public enum CommunityTag {
    STUDENT("Student", "Students pursuing education"),
    EMPLOYEE("Employee/Professional", "Working professionals"),
    FARMER("Farmer", "Agricultural workers"),
    WOMAN("Woman/Girl", "Women and girls"),
    SENIOR_CITIZEN("Senior Citizen", "Elderly individuals"),
    OTHER("Other", "Other category"),
    PREFER_NOT_TO_SAY("Prefer not to say", "User prefers not to disclose");

    private final String displayName;
    private final String description;

    CommunityTag(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
