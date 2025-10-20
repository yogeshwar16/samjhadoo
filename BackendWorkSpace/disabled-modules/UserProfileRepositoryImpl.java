package com.samjhadoo.repository;

import com.samjhadoo.dto.user.ProfileSearchCriteria;
import com.samjhadoo.model.user.UserProfile;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserProfileRepositoryImpl implements UserProfileRepositoryCustom {

    private final EntityManager em;

    @Override
    public Page<UserProfile> searchProfiles(ProfileSearchCriteria criteria, Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<UserProfile> cq = cb.createQuery(UserProfile.class);
        Root<UserProfile> userProfile = cq.from(UserProfile.class);

        List<Predicate> predicates = new ArrayList<>();

        if (criteria.getQuery() != null && !criteria.getQuery().isBlank()) {
            String searchQuery = "%" + criteria.getQuery().toLowerCase() + "%";
            predicates.add(cb.or(
                    cb.like(cb.lower(userProfile.get("displayName")), searchQuery),
                    cb.like(cb.lower(userProfile.get("headline")), searchQuery),
                    cb.like(cb.lower(userProfile.get("bio")), searchQuery)
            ));
        }

        if (criteria.getSkills() != null && !criteria.getSkills().isEmpty()) {
            predicates.add(userProfile.join("skills").get("name").in(criteria.getSkills()));
        }

        // Add more criteria as needed...

        cq.where(predicates.toArray(new Predicate[0]));

        TypedQuery<UserProfile> query = em.createQuery(cq);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<UserProfile> resultList = query.getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        countQuery.select(cb.count(countQuery.from(UserProfile.class)));
        countQuery.where(predicates.toArray(new Predicate[0]));
        Long count = em.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(resultList, pageable, count);
    }
}
