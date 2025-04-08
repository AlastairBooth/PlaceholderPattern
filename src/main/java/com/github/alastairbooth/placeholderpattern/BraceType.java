package com.github.alastairbooth.placeholderpattern;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BraceType {
    NORMAL('(', ')'),
    SQUARE('[', ']'),
    CURLY('{', '}');

    private final char start;
    private final char end;

}
