package com.ChoreTracker.ChoreTracker;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ChoreTracker.ChoreTracker.service.CodeWordProvider;

public class CodeWordProviderTest {
    
    private CodeWordProvider codeWordProvider;
    
    @BeforeEach
    void setUp() {
        codeWordProvider = new CodeWordProvider();
    }
    
    @Test
    void loadLinesFromResource_ShouldReturnNonEmptyList() {
        List<String> adjectives = codeWordProvider.getAdjectives();
        List<String> nouns = codeWordProvider.getNouns();
        
        assertNotNull(adjectives, "Adjectives should not be null");
        assertNotNull(nouns, "Nouns should not be null");
        assertFalse(adjectives.isEmpty(), "Adjectives should not be empty");
        assertFalse(nouns.isEmpty(), "Nouns should not be empty");
        
        adjectives.forEach(word -> {
            assertNotNull(word, "Adjective should not be null");
            assertFalse(word.trim().isEmpty(), "Adjective should not be empty");
        });
        
        nouns.forEach(word -> {
            assertNotNull(word, "Noun should not be null");
            assertFalse(word.trim().isEmpty(), "Noun should not be empty");
        });
    }
}
