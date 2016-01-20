package org.alicebot.ab;

import junit.framework.TestCase;

import org.junit.Test;

// http://stackoverflow.com/questions/4757800/configuring-intellij-idea-for-unit-testing-with-junit
/**
 * Created by User on 3/31/14.
 */
public class InflectorTest extends TestCase {
	@Test
	public void testPluralize() throws Exception {
		final Inflector inflector = new Inflector();
		final String pairs[][] = { { "dog", "dogs" }, { "person", "people" }, { "cats", "cats" } };
		for (final String[] pair : pairs) {
			final String singular = pair[0];
			final String expected = pair[1];
			final String actual = inflector.pluralize(singular);
			assertEquals("Pluralize " + pairs[0][0], expected, actual);
		}

	}

	@Test
	public void testSingularize() throws Exception {

	}
}
