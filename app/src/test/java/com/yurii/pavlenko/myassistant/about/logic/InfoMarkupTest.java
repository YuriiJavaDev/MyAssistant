package com.yurii.pavlenko.myassistant.about.logic;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.List;

public class InfoMarkupTest {

    @Test
    public void marksTellHeadingsBulletsAndParagraphs() {
        List<InfoBlock> blocks = InfoMarkup.parse(new String[]{"# Tasks", "Write what to do.", "- Filter", "- Sort"});
        assertEquals(4, blocks.size());
        assertEquals(InfoBlock.Type.HEADING, blocks.get(0).getType());
        assertEquals("Tasks", blocks.get(0).getText());
        assertEquals(InfoBlock.Type.PARAGRAPH, blocks.get(1).getType());
        assertEquals("Write what to do.", blocks.get(1).getText());
        assertEquals(InfoBlock.Type.BULLET, blocks.get(2).getType());
        assertEquals("Filter", blocks.get(2).getText());
        assertEquals("Sort", blocks.get(3).getText());
    }

    @Test
    public void aDashWithoutASpaceOrInTheMiddleIsPlainText() {
        assertEquals(InfoBlock.Type.PARAGRAPH, InfoMarkup.typeOf("-5 degrees"));
        assertEquals(InfoBlock.Type.PARAGRAPH, InfoMarkup.typeOf("Weather - sun"));
        assertEquals(InfoBlock.Type.PARAGRAPH, InfoMarkup.typeOf("#hashtag"));
    }

    @Test
    public void surroundingSpacesAreIgnored() {
        assertEquals(InfoBlock.Type.HEADING, InfoMarkup.typeOf("  # Steps  "));
        assertEquals("Steps", InfoMarkup.parseItem("# Steps  ").getText());
    }

    @Test
    public void emptyArrayGivesNoBlocks() {
        assertEquals(0, InfoMarkup.parse(new String[0]).size());
    }
}
