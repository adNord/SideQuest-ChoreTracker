package com.ChoreTracker.ChoreTracker;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.ChoreTracker.ChoreTracker.repositories.HouseholdRepository;
import com.ChoreTracker.ChoreTracker.service.CodeWordProvider;
import com.ChoreTracker.ChoreTracker.service.InviteCodeService;

public class InviteCodeServiceTest {
    
    @Mock
    private CodeWordProvider codeWordProvider;
    
    @Mock
    private HouseholdRepository householdRepository;
    
    private InviteCodeService inviteCodeService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        inviteCodeService = new InviteCodeService(codeWordProvider, householdRepository);
    }
    
    @Test
    void generateInviteCode_ShouldReturnUniqueCode() {
        when(codeWordProvider.getAdjectives()).thenReturn(Arrays.asList("glad", "solig", "ljus"));
        when(codeWordProvider.getNouns()).thenReturn(Arrays.asList("katt", "hund", "fågel"));
        when(householdRepository.existsByInviteCode(anyString())).thenReturn(false);
        
        String inviteCode = inviteCodeService.generateInviteCode();
        
        assertNotNull(inviteCode);
        assertFalse(inviteCode.isEmpty());
        
        verify(householdRepository, atLeastOnce()).existsByInviteCode(anyString());
    }
}
