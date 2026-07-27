package com.entelect.ranger.json;

import java.util.*;

/** Small standards-compliant JSON parser for the contest's dependency-free input format. */
public final class JsonParser {
    private final String text;
    private int index;

    private JsonParser(String text) { this.text = text; }

    public static Object parse(String text) {
        JsonParser parser = new JsonParser(text);
        Object value = parser.readValue();
        parser.skipWhitespace();
        if (parser.index != text.length()) parser.fail("Unexpected trailing content");
        return value;
    }

    private Object readValue() {
        skipWhitespace();
        if (index >= text.length()) fail("Expected a JSON value");
        return switch (text.charAt(index)) {
            case '{' -> readObject(); case '[' -> readArray(); case '"' -> readString();
            case 't' -> readLiteral("true", Boolean.TRUE); case 'f' -> readLiteral("false", Boolean.FALSE);
            case 'n' -> readLiteral("null", null); default -> readNumber();
        };
    }

    private Map<String, Object> readObject() {
        expect('{'); Map<String, Object> result = new LinkedHashMap<>(); skipWhitespace();
        if (consume('}')) return result;
        do { skipWhitespace(); String key = readString(); skipWhitespace(); expect(':'); result.put(key, readValue()); skipWhitespace(); }
        while (consume(','));
        expect('}'); return result;
    }

    private List<Object> readArray() {
        expect('['); List<Object> result = new ArrayList<>(); skipWhitespace();
        if (consume(']')) return result;
        do { result.add(readValue()); skipWhitespace(); } while (consume(','));
        expect(']'); return result;
    }

    private String readString() {
        expect('"'); StringBuilder result = new StringBuilder();
        while (index < text.length()) {
            char c = text.charAt(index++);
            if (c == '"') return result.toString();
            if (c == '\\') {
                if (index >= text.length()) fail("Unterminated escape");
                char escaped = text.charAt(index++);
                switch (escaped) {
                    case '"', '\\', '/' -> result.append(escaped);
                    case 'b' -> result.append('\b'); case 'f' -> result.append('\f'); case 'n' -> result.append('\n');
                    case 'r' -> result.append('\r'); case 't' -> result.append('\t');
                    case 'u' -> { if (index + 4 > text.length()) fail("Invalid unicode escape"); result.append((char) Integer.parseInt(text.substring(index, index += 4), 16)); }
                    default -> fail("Invalid escape sequence");
                }
            } else { if (c < 0x20) fail("Control character in string"); result.append(c); }
        }
        fail("Unterminated string"); return null;
    }

    private Number readNumber() {
        int start = index;
        if (consume('-')) { }
        if (consume('0')) { } else { requireDigits(); }
        if (consume('.')) requireDigits();
        if (consume('e') || consume('E')) { consume('+'); consume('-'); requireDigits(); }
        String token = text.substring(start, index);
        try { return (token.contains(".") || token.contains("e") || token.contains("E")) ? Double.parseDouble(token) : Long.parseLong(token); }
        catch (NumberFormatException error) { throw new IllegalArgumentException("Invalid number '" + token + "'", error); }
    }

    private Object readLiteral(String literal, Object value) { if (!text.startsWith(literal, index)) fail("Expected " + literal); index += literal.length(); return value; }
    private void requireDigits() { int start = index; while (index < text.length() && Character.isDigit(text.charAt(index))) index++; if (start == index) fail("Expected digit"); }
    private boolean consume(char wanted) { if (index < text.length() && text.charAt(index) == wanted) { index++; return true; } return false; }
    private void expect(char wanted) { skipWhitespace(); if (!consume(wanted)) fail("Expected '" + wanted + "'"); }
    private void skipWhitespace() { while (index < text.length() && Character.isWhitespace(text.charAt(index))) index++; }
    private void fail(String message) { throw new IllegalArgumentException(message + " at character " + index); }
}
