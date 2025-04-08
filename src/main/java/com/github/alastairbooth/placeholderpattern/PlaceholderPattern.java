package com.github.alastairbooth.placeholderpattern;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class PlaceholderPattern {

    private static final String ANYTHING_NOT_IN_BRACES = "(?:(?<=^)|(?<=\\}))([^{}]+)(?=\\{|$)";
    private static final String ANYTHING_IN_BRACES = "\\{[^{}]*\\}";
    private static final String BRACED_STRING = "^\\{.+\\}$";
    private static final String STARTS_WITH_VAR = "^\\{.*";

    private final String pattern;
    private final UnaryOperator<String> nullReplacer;
    @Getter(AccessLevel.PRIVATE)
    private final boolean startsWithVar;
    private final List<String> keys;
    private final List<String> accompanyingText;
    private List<String> workingKeys;
    private List<String> workingAccompanyingText;

    public PlaceholderPattern(String pattern) {
        this(pattern, s -> {
            throw new PlaceholderPatternException("null replacer has not been defined and is needed");
        });
    }

    public PlaceholderPattern(String pattern, UnaryOperator<String> nullReplacer) {
        this.pattern = pattern;
        this.nullReplacer = nullReplacer;
        this.keys = allMatches(pattern, ANYTHING_IN_BRACES);
        this.accompanyingText = allMatches(pattern, ANYTHING_NOT_IN_BRACES);
        this.workingKeys = new ArrayList<>(keys);
        this.workingAccompanyingText = new ArrayList<>(accompanyingText);
        this.startsWithVar = pattern.matches(STARTS_WITH_VAR);
    }

    private List<String> allMatches(String string, String regex) {
        List<String> matches = new ArrayList<>();
        Matcher m = Pattern.compile(regex).matcher(string);
        while (m.find()) {
            matches.add(m.group());
        }
        return matches;
    }

    public PlaceholderPattern withReplacements(String... replacements) {
        if (replacements.length % 2 != 0) {
            throw new PlaceholderPatternException("Replacements must be in key/value pairs");
        }
        for (int i = 0; i < replacements.length; i += 2) {
            String key = replacements[i];
            if (!key.matches(BRACED_STRING)) {
                key = wrap(key, BraceType.CURLY);
            }
            int index = keys.indexOf(key);
            if (index < 0) {
                throw new PlaceholderPatternException("unknown key: " + key);
            }
            workingKeys.set(index, replacements[i + 1]);
        }
        return this;
    }

    public String getWithReplacements(String... replacements) {
        return withReplacements(replacements).toString();
    }

    public PlaceholderPattern withAccompanyingTextOperator(UnaryOperator<String> operator) {
        workingAccompanyingText = workingAccompanyingText.stream().map(operator).toList();
        return this;
    }

    public String getWithAccompanyingTextOperator(UnaryOperator<String> operator) {
        return withAccompanyingTextOperator(operator).toString();
    }

    public PlaceholderPattern withKeyOperator(UnaryOperator<String> operator) {
        validateWorkingKeys();
        workingKeys = workingKeys.stream().map(operator).toList();
        return this;
    }

    public String getWithKeyOperator(UnaryOperator<String> operator) {
        return withKeyOperator(operator).toString();
    }

    private String validateForOutput(String key) {
        if (key.matches(BRACED_STRING)) {
            return nullReplacer.apply(key);
        } else {
            return key;
        }
    }

    private void validateWorkingKeys() {
        workingKeys = workingKeys.stream().map(this::validateForOutput).toList();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (startsWithVar) {
            for (int i = 0; i < keys.size(); i++) {
                sb.append(validateForOutput(workingKeys.get(i)));
                if (accompanyingText.size() > i) {
                    sb.append(validateForOutput(workingAccompanyingText.get(i)));
                }
            }
        } else {
            for (int i = 0; i < accompanyingText.size(); i++) {
                sb.append(workingAccompanyingText.get(i));
                if (keys.size() > i) {
                    sb.append(validateForOutput(workingKeys.get(i)));
                }
            }
        }
        this.workingKeys = new ArrayList<>(keys);
        this.workingAccompanyingText = new ArrayList<>(accompanyingText);
        return sb.toString();
    }

    @SuppressWarnings("unused")
    public static String wrap(@NonNull String string, @NonNull BraceType braceType) {
        return braceType.getStart() + string + braceType.getEnd();
    }

    @SuppressWarnings("unused")
    public static String wrap(@NonNull String string, char c) {
        return c + string + c;
    }
}
