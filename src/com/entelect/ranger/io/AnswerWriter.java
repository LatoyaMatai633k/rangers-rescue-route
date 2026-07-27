package com.entelect.ranger.io;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

public final class AnswerWriter {
    public void write(Path output, List<String> route) throws IOException {
        Files.createDirectories(output.getParent());
        String json = "{\n  \"route\": [" + route.stream().map(this::quote).reduce((left, right) -> left + ", " + right).orElse("") + "]\n}\n";
        Files.writeString(output, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
    private String quote(String value) { return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""; }
}
