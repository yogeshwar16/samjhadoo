# Backend Startup Troubleshooting Log

This document tracks the errors encountered during the Spring Boot application startup and the steps taken to resolve them.

---

### Error History

1.  **Error:** `UnsatisfiedDependencyException` -> `AnnotationException`: `mappedBy` property `placement` not found in `AdCampaign`.
    *   **Root Cause:** An incorrect `@OneToMany` relationship was defined in `AdPlacement` pointing to a `placement` field that did not exist in `AdCampaign`.
    *   **Fix:** Removed the incorrect `campaigns` list and the `@OneToMany` mapping from the `AdPlacement` entity.

2.  **Error:** `QueryCreationException` -> `UnknownPathException`: Could not resolve attribute `shouldAutoCleanup` of `com.samjhadoo.model.ChatRoom`.
    *   **Root Cause:** The repository query was trying to access `shouldAutoCleanup` as if it were a database column, but it is a transient Java method (`getShouldAutoCleanup()`). JPQL cannot directly use logic from entity methods in `WHERE` clauses.
    *   **Fix:** Re-wrote the `findRoomsForCleanup` query in `ChatRoomRepository` to directly implement the cleanup logic (`lastActivityAt < :cutoffDate AND currentParticipants = 0 AND isPublic = false`).

3.  **Error:** `QueryCreationException` -> `UnknownPathException`: Could not resolve attribute `healthScore` of `com.samjhadoo.model.ChatRoom`.
    *   **Root Cause:** Similar to the previous error, `healthScore` is a computed value from a `getHealthScore()` method in the `ChatRoom` entity, not a persisted field.
    *   **Fix:** Commented out the `findHealthyRooms` and `findUnhealthyRooms` queries in `ChatRoomRepository` as they require a more complex implementation (e.g., native query or service-layer calculation).

4.  **Error:** `QueryCreationException` -> `SemanticException`: Cannot compare left expression of type `java.lang.Long` with right expression of type `java.lang.String`.
    *   **Root Cause:** The `PaymentRepository` defined methods like `findByUserId(String userId)`, but the `user` field in the `Payment` entity joins to the `User` entity, whose primary key (`id`) is a `Long`.
    *   **Fix:** Changed the parameter type for `userId` from `String` to `Long` in all relevant methods in `PaymentRepository`.

5.  **Error:** `QueryCreationException` -> `UnknownPathException`: Could not resolve attribute `clickThroughRate` of `com.samjhadoo.model.ads.Ad`.
    *   **Root Cause:** `clickThroughRate` is a calculated property in the `Ad` entity, not a database column. JPQL cannot use it directly in queries.
    *   **Fix:** Commented out the `findHighPerformingAds` and `getAverageClickThroughRate` methods in `AdRepository` as they require a custom implementation.

6.  **Error:** `BeanCreationException`: Could not safely identify store assignment for repository candidate `SurgeRuleRepository`.
    *   **Root Cause:** The project uses both JPA (for H2/Postgres) and MongoDB, but Spring couldn't determine which datastore `SurgeRuleRepository` was supposed to use, as it was in a package scanned by both.
    *   **Fix:**
        1.  Created a dedicated `com.samjhadoo.repository.mongo` package.
        2.  Moved `SurgeRuleRepository` into this new package.
        3.  Annotated `SamjhadooApplication` with `@EnableJpaRepositories` and `@EnableMongoRepositories`, pointing them to their respective base packages to resolve the ambiguity.
        4.  Converted the `SurgeRule` entity from a JPA `@Entity` to a MongoDB `@Document`.
        5.  Changed `SurgeRuleRepository` to extend `MongoRepository` instead of `JpaRepository`.
        6.  Fixed resulting compilation errors by adding missing imports (`@Data`, `@Id`, `BigDecimal`, `LocalDateTime`) to `SurgeRule.java`.

7.  **Error:** `QueryCreationException` -> `UnknownPathException`: Could not resolve attribute `clickThroughRate` of `com.samjhadoo.model.ads.AdPlacement`.
    *   **Root Cause:** The `AdPlacementRepository` contains a query attempting to use `clickThroughRate`, which is a computed method in the `AdPlacement` entity, not a database field.
    *   **Fix:** Commented out the `getAverageClickThroughRate` method in `AdPlacementRepository` as it cannot be resolved by JPQL.

---
