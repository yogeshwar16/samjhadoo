package com.samjhadoo.mapper;

import com.samjhadoo.dto.community.CommunityDTO;
import com.samjhadoo.dto.community.CommunityMemberDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.model.community.Community;
import com.samjhadoo.model.community.CommunityMember;
import com.samjhadoo.model.enums.MemberRole;
import com.samjhadoo.model.enums.MemberStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CommunityMapper {

    CommunityMapper INSTANCE = Mappers.getMapper(CommunityMapper.class);

    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "toInstant")
    @Mapping(target = "memberCount", expression = "java(community.getMembers() != null ? (long) community.getMembers().size() : 0L)")
    @Mapping(target = "createdBy", source = "createdBy.id")
    @Mapping(target = "isMember", constant = "false")
    @Mapping(target = "memberRole", ignore = true)
    @Mapping(target = "memberStatus", ignore = true)
    CommunityDTO toDTO(Community community);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "members", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "createdBy", source = "createdBy")
    Community toEntity(CreateCommunityRequest request, User createdBy);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "fullName", source = "user.fullName")
    @Mapping(target = "title", source = "user.title")
    @Mapping(target = "company", source = "user.company")
    @Mapping(target = "mentorRating", source = "user.mentorRating")
    @Mapping(target = "menteeRating", source = "user.menteeRating")
    @Mapping(target = "joinedAt", source = "joinedAt", qualifiedByName = "toInstant")
    @Mapping(target = "lastActive", source = "lastActive", qualifiedByName = "toInstant")
    CommunityMemberDTO toMemberDTO(CommunityMember member);

    default List<CommunityMemberDTO> toMemberDTOs(List<CommunityMember> members) {
        if (members == null) {
            return null;
        }
        return members.stream()
                .map(this::toMemberDTO)
                .collect(Collectors.toList());
    }

    @Named("toInstant")
    default Instant toInstant(Long epochMillis) {
        return epochMillis != null ? Instant.ofEpochMilli(epochMillis) : null;
    }

    @Named("toEpochMillis")
    default Long toEpochMillis(Instant instant) {
        return instant != null ? instant.toEpochMilli() : null;
    }
}
