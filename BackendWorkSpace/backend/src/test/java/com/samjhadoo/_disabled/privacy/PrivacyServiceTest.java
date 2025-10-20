package com.samjhadoo.service.privacy;

import com.samjhadoo.dto.privacy.ConsentDTO;
import com.samjhadoo.dto.privacy.DataExportDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.model.privacy.ConsentLog;
import com.samjhadoo.model.privacy.DataExportRequest;
import com.samjhadoo.repository.privacy.ConsentLogRepository;
import com.samjhadoo.repository.privacy.DataExportRequestRepository;
import com.samjhadoo.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrivacyServiceTest {

    @Mock
    private ConsentLogRepository consentLogRepository;

    @Mock
    private DataExportRequestRepository exportRequestRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private PrivacyServiceImpl privacyService;

    private User testUser;
    private ConsentLog testConsent;
    private DataExportRequest testExportRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testConsent = new ConsentLog();
        testConsent.setId(1L);
        testConsent.setUser(testUser);
        testConsent.setConsentType(ConsentLog.ConsentType.TERMS_AND_CONDITIONS);
        testConsent.setGranted(true);
        testConsent.setVersion("1.0");

        testExportRequest = new DataExportRequest();
        testExportRequest.setId("export-123");
        testExportRequest.setUser(testUser);
        testExportRequest.setStatus(DataExportRequest.ExportStatus.PENDING);
    }

    @Test
    void recordConsent_NewConsent_ShouldSaveAndReturnConsent() {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(consentLogRepository.save(any(ConsentLog.class))).thenReturn(testConsent);

        // Act
        ConsentDTO result = privacyService.recordConsent(
            testUser, 
            ConsentLog.ConsentType.TERMS_AND_CONDITIONS, 
            true, 
            "1.0", 
            "127.0.0.1", 
            "Test User Agent"
        );

        // Assert
        assertNotNull(result);
        assertEquals(testConsent.getId(), result.getId());
        verify(consentLogRepository, times(1)).save(any(ConsentLog.class));
    }

    @Test
    void getUserConsents_ShouldReturnUserConsents() {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(consentLogRepository.findByUserOrderByCreatedAtDesc(testUser))
            .thenReturn(Arrays.asList(testConsent));

        // Act
        List<ConsentDTO> result = privacyService.getUserConsents(testUser);

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(testConsent.getId(), result.get(0).getId());
    }

    @Test
    void revokeConsent_ExistingConsent_ShouldUpdateConsent() {
        // Arrange
        when(consentLogRepository.findByUserAndConsentTypeAndGrantedTrue(testUser, ConsentLog.ConsentType.TERMS_AND_CONDITIONS))
            .thenReturn(Optional.of(testConsent));
        when(consentLogRepository.save(any(ConsentLog.class))).thenReturn(testConsent);

        // Act
        privacyService.revokeConsent(testUser, ConsentLog.ConsentType.TERMS_AND_CONDITIONS, "User request");

        // Assert
        assertFalse(testConsent.isGranted());
        assertNotNull(testConsent.getRevokedAt());
        assertEquals("User request", testConsent.getRevocationReason());
        verify(consentLogRepository, times(1)).save(testConsent);
    }

    @Test
    void requestDataExport_NewRequest_ShouldCreateExportRequest() {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(exportRequestRepository.save(any(DataExportRequest.class))).thenReturn(testExportRequest);

        // Act
        DataExportDTO result = privacyService.requestDataExport(testUser, "JSON");

        // Assert
        assertNotNull(result);
        assertEquals(testExportRequest.getId(), result.getId());
        verify(exportRequestRepository, times(1)).save(any(DataExportRequest.class));
    }

    @Test
    void getExportRequest_ValidId_ShouldReturnExportRequest() {
        // Arrange
        when(exportRequestRepository.findByIdAndUser("export-123", testUser))
            .thenReturn(Optional.of(testExportRequest));

        // Act
        DataExportDTO result = privacyService.getExportRequest("export-123", testUser);

        // Assert
        assertNotNull(result);
        assertEquals("export-123", result.getId());
    }

    @Test
    void generateComplianceReport_ShouldReturnReport() {
        // Arrange
        when(consentLogRepository.countByUser(testUser)).thenReturn(3L);
        when(exportRequestRepository.countByUser(testUser)).thenReturn(2L);
        when(consentLogRepository.findFirstByUserAndConsentTypeOrderByCreatedAtDesc(
            testUser, ConsentLog.ConsentType.PRIVACY_POLICY))
            .thenReturn(Optional.of(testConsent));

        // Act
        var report = privacyService.generateComplianceReport(testUser);

        // Assert
        assertNotNull(report);
        assertEquals(3L, report.get("consentCount"));
        assertEquals(2L, report.get("exportRequestCount"));
        assertTrue(report.containsKey("lastPrivacyConsent"));
    }
}
