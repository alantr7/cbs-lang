package com.github.alantr7.codebots.cbslang.low.tokenizer;

import java.util.LinkedList;
import java.util.List;

public class Tokenizer {

    public static String[][] tokenize(String input) {
        String[] lines = input.split("\n");
        List<String[]> tokenizedLines = new LinkedList<>();

        for (String line : lines) {
            String trimmed = line.trim();
            int separator = trimmed.indexOf(" ");

            if (trimmed.startsWith(";"))
                continue;

            if (separator == -1) {
                tokenizedLines.add(new String[]{trimmed});
                continue;
            }

            String command = trimmed.substring(0, separator);

            List<String> tokens = new LinkedList<>();
            tokens.add(command);

            trimmed = trimmed.substring(separator + 1);
            while (trimmed.contains(",")) {
                String operand = trimmed.substring(0, trimmed.indexOf(",")).trim();
                tokens.add(operand);

                trimmed = trimmed.substring(trimmed.indexOf(",") + 1).trim();
            }
            if (trimmed.contains(";")) {
                trimmed = trimmed.substring(0, trimmed.indexOf(";")).trim();
            }
            if (!trimmed.isEmpty())
                tokens.add(trimmed);

            tokenizedLines.add(tokens.toArray(String[]::new));
        }

        return tokenizedLines.toArray(String[][]::new);
    }

}
