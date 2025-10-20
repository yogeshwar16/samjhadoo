package com.samjhadoo.model.enums;

/**
 * Topic categories for organization and filtering
 */
public enum TopicCategory {
    EDUCATION("Education", "Academic and learning topics"),
    CAREER("Career", "Professional development and job-related"),
    LIFESTYLE("Lifestyle", "Personal life and hobbies"),
    AGRICULTURE("Agriculture", "Farming and agricultural guidance"),
    WELLNESS("Wellness", "Mental and physical health"),
    TECHNOLOGY("Technology", "Tech skills and software"),
    BUSINESS("Business", "Entrepreneurship and business"),
    ARTS("Arts & Crafts", "Creative and artistic topics"),
    OTHER("Other", "Miscellaneous topics");

    private final String displayName;
    private final String description;

    TopicCategory(String displayName, String description) {
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
