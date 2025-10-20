package com.samjhadoo.dto.user;

import com.samjhadoo.model.enums.Role;
import com.samjhadoo.model.enums.VerificationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserDTO {

    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private Role role;
    private VerificationStatus verificationStatus;
    private LocalDateTime createdAt;
}
