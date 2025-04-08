package com.github.alastairbooth.placeholderpattern;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlaceholderPatternTest {

    private static final UnaryOperator<String> OPERATOR = string -> '\'' + string + '\'';

    private static Stream<Arguments> provideForReplace() {
        return Stream.of(
                Arguments.of("{key}", "replacement"),
                Arguments.of("{key} at start", "replacement at start"),
                Arguments.of("with a {key} in the middle", "with a replacement in the middle"),
                Arguments.of("ends with {key}", "ends with replacement")
        );
    }

    @ParameterizedTest
    @MethodSource(value = "provideForReplace")
    void testReplace(String input, String expectedOutput) {
        PlaceholderPattern placeholderPattern = new PlaceholderPattern(input);
        String result = placeholderPattern.getWithReplacements("key", "replacement");
        assertEquals(expectedOutput, result);
    }

    private static Stream<Arguments> provideForKeyOperator() {
        return Stream.of(
                Arguments.of("{key}", "'replacement'"),
                Arguments.of("{key} at start", "'replacement' at start"),
                Arguments.of("with a {key} in the middle", "with a 'replacement' in the middle"),
                Arguments.of("ends with {key}", "ends with 'replacement'")
        );
    }

    @ParameterizedTest
    @MethodSource(value = "provideForKeyOperator")
    void testWithKeyOperator(String input, String expectedOutput) {
        PlaceholderPattern placeholderPattern = new PlaceholderPattern(input);
        String result = placeholderPattern
                .withReplacements("key", "replacement")
                .getWithKeyOperator(OPERATOR);
        assertEquals(expectedOutput, result);
    }

    private static Stream<Arguments> provideForAccompanyingTextOperator() {
        return Stream.of(
                Arguments.of("{key}", "replacement"),
                Arguments.of("{key} at start", "replacement' at start'"),
                Arguments.of("with a {key} in the middle", "'with a 'replacement' in the middle'"),
                Arguments.of("ends with {key}", "'ends with 'replacement")
        );
    }

    @ParameterizedTest
    @MethodSource(value = "provideForAccompanyingTextOperator")
    void testWithAccompanyingTextOperator(String input, String expectedOutput) {
        PlaceholderPattern placeholderPattern = new PlaceholderPattern(input);
        String result = placeholderPattern
                .withReplacements("key", "replacement")
                .getWithAccompanyingTextOperator(OPERATOR);
        assertEquals(expectedOutput, result);
    }

}