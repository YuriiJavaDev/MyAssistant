package com.yurii.pavlenko.myassistant.localization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;

/** Every translation has the same texts as the English source, the same placeholders and the plural forms of its language. */
public class TranslationsTest {

    private static final File RES = new File("src/main/res");
    private static final Pattern PLACEHOLDER = Pattern.compile("%(?:\\d+\\$)?[-#+ 0,(]*\\d*(?:\\.\\d+)?[sdf]|%%");
    private static final List<String> FOUR_FORMS = new ArrayList<>(java.util.Arrays.asList("one", "few", "many", "other"));

    @Test
    public void everyLanguageTranslatesEverythingTheSameWay() throws Exception {
        Map<String, Resource> source = read(new File(RES, "values"));
        File[] folders = RES.listFiles((dir, name) -> name.matches("values-[a-z]{2,3}(-r[A-Z]{2})?"));
        assertTrue("No translations found, is the working directory the app module?", folders != null && folders.length > 0);
        for (File folder : folders) {
            Map<String, Resource> translation = read(folder);
            assertEquals(folder.getName() + " keys", source.keySet(), translation.keySet());
            for (Map.Entry<String, Resource> entry : source.entrySet()) {
                Resource english = entry.getValue();
                Resource mine = translation.get(entry.getKey());
                String where = folder.getName() + " " + entry.getKey();
                if (english.quantities.isEmpty()) {
                    assertEquals(where + " items", english.texts.size(), mine.texts.size());
                } else {
                    assertTrue(where + " needs the forms of its language", mine.quantities.containsAll(FOUR_FORMS));
                }
                for (int i = 0; i < mine.texts.size(); i++) {
                    String reference = english.texts.get(english.quantities.isEmpty() ? i : english.quantities.indexOf("other"));
                    assertEquals(where + " placeholders", placeholders(reference), placeholders(mine.texts.get(i)));
                }
            }
        }
    }

    private static List<String> placeholders(String text) {
        List<String> found = new ArrayList<>();
        Matcher matcher = PLACEHOLDER.matcher(text);
        while (matcher.find()) {
            found.add(matcher.group());
        }
        Collections.sort(found);
        return found;
    }

    private static final class Resource {
        final List<String> texts = new ArrayList<>();
        final List<String> quantities = new ArrayList<>();
    }

    /** The translatable resources of a language by kind and name. */
    private static Map<String, Resource> read(File folder) throws Exception {
        Map<String, Resource> result = new TreeMap<>();
        File[] files = folder.listFiles((dir, name) -> name.startsWith("strings") && name.endsWith(".xml"));
        assertTrue(folder + " has no strings", files != null);
        for (File file : files) {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
            NodeList nodes = document.getDocumentElement().getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (!(node instanceof Element) || "false".equals(((Element) node).getAttribute("translatable"))) {
                    continue;
                }
                Element element = (Element) node;
                Resource resource = new Resource();
                if (element.getTagName().equals("string")) {
                    resource.texts.add(element.getTextContent());
                } else {
                    NodeList items = element.getElementsByTagName("item");
                    for (int j = 0; j < items.getLength(); j++) {
                        Element item = (Element) items.item(j);
                        resource.texts.add(item.getTextContent());
                        if (element.getTagName().equals("plurals")) {
                            resource.quantities.add(item.getAttribute("quantity"));
                        }
                    }
                }
                result.put(element.getTagName() + ":" + element.getAttribute("name"), resource);
            }
        }
        return result;
    }
}
