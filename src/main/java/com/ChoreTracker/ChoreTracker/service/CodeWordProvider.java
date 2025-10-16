package com.ChoreTracker.ChoreTracker.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class CodeWordProvider {
    private final List<String> adjectives;
    private final List<String> nouns;

    public CodeWordProvider() {
        this.adjectives = Collections.unmodifiableList(loadLinesFromResource("invitecode-words/adjectives.txt"));
        this.nouns = Collections.unmodifiableList(loadLinesFromResource("invitecode-words/nouns.txt"));
    }

    //Läser in rader från textfil och returnerar som lista av strängar
    private List<String> loadLinesFromResource(String path) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ClassPathResource(path).getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().map(String::trim).filter(line -> !line.isEmpty()).collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public List<String> getAdjectives() {
        return adjectives;
    }

    public List<String> getNouns() {
        return nouns;
    }
}
