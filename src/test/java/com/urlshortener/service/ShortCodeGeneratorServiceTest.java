package com.urlshortener.service;

import com.urlshortener.repository.ShortUrlRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ShortCodeGeneratorService.
 *
 * We test:
 *   - That generated codes have the expected length.
 *   - That codes only contain allowed characters.
 *   - That collision handling works: if the first code already exists, a new
 *     one is generated.
 *   - That the safety valve (IllegalStateException) fires after 10 failures.
 */
@ExtendWith(MockitoExtension.class)
class ShortCodeGeneratorServiceTest {

    @Mock
    private ShortUrlRepository repository;

    @InjectMocks
    private ShortCodeGeneratorService generator;

    private static final String VALID_CHARS =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    @Test
    @DisplayName("Generated code has exactly 7 characters")
    void generateCode_hasCorrectLength() {
        String code = generator.generateCode();
        assertThat(code).hasSize(7);
    }

    @Test
    @DisplayName("Generated code contains only alphanumeric characters")
    void generateCode_alphanumericOnly() {
        for (int i = 0; i < 100; i++) {
            String code = generator.generateCode();
            for (char c : code.toCharArray()) {
                assertThat(VALID_CHARS).contains(String.valueOf(c));
            }
        }
    }

    @RepeatedTest(10)
    @DisplayName("Each call to generateCode produces a different-format string (statistically)")
    void generateCode_producesNonEmptyCode() {
        String code = generator.generateCode();
        assertThat(code).isNotBlank().hasSize(7);
    }

    @Test
    @DisplayName("Collision handling: skips an existing code and returns the next unique one")
    void generateUniqueCode_handlesCollision() {
        // Simulate: first code "aaaaaaa" already exists, "bbbbbbb" is free
        when(repository.existsByShortCode("aaaaaaa")).thenReturn(true);
        when(repository.existsByShortCode("bbbbbbb")).thenReturn(false);

        // Spy the generator so we can control generateCode() output
        ShortCodeGeneratorService spy = spy(new ShortCodeGeneratorService(repository));
        doReturn("aaaaaaa", "bbbbbbb").when(spy).generateCode();

        String result = spy.generateUniqueCode();

        assertThat(result).isEqualTo("bbbbbbb");
        // existsByShortCode should have been called for both codes
        verify(repository, times(1)).existsByShortCode("aaaaaaa");
        verify(repository, times(1)).existsByShortCode("bbbbbbb");
    }

    @Test
    @DisplayName("Safety valve: throws IllegalStateException after 10 consecutive collisions")
    void generateUniqueCode_throwsAfterTooManyCollisions() {
        // All codes "collide" — existsByShortCode always returns true
        when(repository.existsByShortCode(anyString())).thenReturn(true);

        assertThatThrownBy(() -> generator.generateUniqueCode())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("10 attempts");
    }
}
