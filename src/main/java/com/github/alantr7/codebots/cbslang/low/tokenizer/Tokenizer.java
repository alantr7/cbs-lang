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
            if (trimmed.contains(";")) {
                trimmed = trimmed.substring(0, trimmed.indexOf(";")).trim();
            }

            String word = "";
            boolean isQuotes = false;

            for (int i = 0; i < trimmed.length(); i++) {
                char ch = trimmed.charAt(i);
                if (ch == '"') {
                    isQuotes = !isQuotes;
                } else if (!isQuotes) {
                    if (ch == ' ') {
                        tokens.add(word);
                        word = "";
                        continue;
                    } else if (ch == ',') {
                        continue;
                    }
                }

                word += ch;
            }

            if (!word.isEmpty())
                tokens.add(word);

            tokenizedLines.add(tokens.toArray(String[]::new));
        }

        return tokenizedLines.toArray(String[][]::new);
    }

}
