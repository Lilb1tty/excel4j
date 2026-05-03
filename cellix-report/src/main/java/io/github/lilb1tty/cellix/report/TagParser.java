package io.github.lilb1tty.cellix.report;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TagParser {

    private static final Pattern TAG_PATTERN = Pattern.compile(
        "<#(value|band)\\s+([a-zA-Z_][a-zA-Z0-9_.]*)>|<\\/band>"
    );

    private TagParser() {}

    public static List<Tag> parse(String text) {
        List<Tag> tags = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return tags;
        }
        Matcher m = TAG_PATTERN.matcher(text);
        while (m.find()) {
            if (m.group(1) == null) {
                tags.add(new Tag.BandEndTag());
            } else if ("value".equals(m.group(1))) {
                tags.add(new Tag.ValueTag(m.group(2)));
            } else {
                tags.add(new Tag.BandStartTag(m.group(2)));
            }
        }
        return tags;
    }
}
