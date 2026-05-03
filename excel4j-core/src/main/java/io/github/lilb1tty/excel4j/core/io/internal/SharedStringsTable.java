package io.github.lilb1tty.excel4j.core.io.internal;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class SharedStringsTable {

    private final List<String> strings = new ArrayList<>();
    private final Map<String, Integer> index = new HashMap<>();

    public int add(String s) {
        return index.computeIfAbsent(s, k -> {
            int idx = strings.size();
            strings.add(k);
            return idx;
        });
    }

    public String get(int idx) {
        return strings.get(idx);
    }

    public int size() {
        return strings.size();
    }

    public List<String> all() {
        return List.copyOf(strings);
    }

    public static List<String> read(ZipFile zip) throws IOException, XMLStreamException {
        List<String> result = new ArrayList<>();
        ZipEntry entry = zip.getEntry("xl/sharedStrings.xml");
        if (entry == null) return result;
        XMLInputFactory factory = XMLInputFactory.newInstance();
        try (InputStream in = zip.getInputStream(entry)) {
            XMLStreamReader r = factory.createXMLStreamReader(in);
            StringBuilder current = null;
            while (r.hasNext()) {
                int event = r.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if ("si".equals(r.getLocalName())) current = new StringBuilder();
                } else if (event == XMLStreamConstants.CHARACTERS && current != null) {
                    current.append(r.getText());
                } else if (event == XMLStreamConstants.END_ELEMENT) {
                    if ("si".equals(r.getLocalName()) && current != null) {
                        result.add(current.toString());
                        current = null;
                    }
                }
            }
        }
        return result;
    }
}
