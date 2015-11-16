/*
 * TagStripper.java
 * Copyright (c) 2004, KTH NADA.
 *
 * This file is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software; see the file COPYING.  If not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 *
 * Martin Hassel, 2004-aug-16
 * http://www.nada.kth.se/~xmartin/
 */
package moj.util;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;

/**
 * A small utility class that contains methods for stripping tags from 
 * HTML documents. It tries to retain the structure of the original text 
 * by converting HTML line breaks, paragraphs, headings etc to ordinary 
 * single and double line breaks.
 * 
 * The design for this class is heavily influenced by an example in
 * "Web Client Programming with Java" by Elliotte Rusty Harold.
 *
 * @author  Martin Hassel
 * @version 2004-aug-04
 */
public class TagStripper {
	/**
	 * Strips HTML tags from the given string and returns it while trying to 
	 * retain the structure of the original text by converting HTML line breaks, 
	 * paragraphs, headings etc to ordinary single and double line breaks.
	 * 
	 * @param htmltext the string that is to be stripped from HTML tags
	 * @return the given string stripped from HTML tags
	 */
	public String stripTags(String htmltext) {
		// Create a StreamReader you can read the HTML text with.
		return this.stripTags(new StringReader(htmltext));
	}

	/**
	 * Strips HTML tags from the given stream and returns a string while trying to 
	 * retain the structure of the original text by converting HTML line breaks, 
	 * paragraphs, headings etc to ordinary single and double line breaks.
	 * 
	 * @param reader the stream that is to be stripped from HTML tags
	 * @return the given stream stripped from HTML tags returned as a string
	 */
	public String stripTags(Reader reader) {
		// StringBuffer to collect the HTML stripped text in.
		final StringBuffer strbuf = new StringBuffer();
		// Begin by retrieving a parser using the ParserGetter class:
		ParserGetter kit = new ParserGetter();
		HTMLEditorKit.Parser parser = kit.getParser();
		// Next, construct an instance of your callback class like this:
		HTMLEditorKit.ParserCallback callback = new LineBreakingTagStripper(strbuf);
		try {
			// Finally, pass the Reader and HTMLEditorKit.ParserCallback to 
			// the HTMLEditorKit.Parser's parse() method, like this:
			parser.parse(reader, callback, false);
		}
		catch(IOException ex) {
			System.err.println(ex); 
		}
		return strbuf.toString();
	}

	private class ParserGetter extends HTMLEditorKit {
		// Purely to make this method public
		public HTMLEditorKit.Parser getParser(){
			return super.getParser();
		}
	}

	private class LineBreakingTagStripper extends HTMLEditorKit.ParserCallback {
		private StringBuffer strbuf;
		private String lineSeparator;

		public LineBreakingTagStripper(StringBuffer out) {
			this(out, System.getProperty("line.separator", "\r\n")); 
		}  

		public LineBreakingTagStripper(StringBuffer out, String lineSeparator) {
			this.strbuf = out;
			this.lineSeparator = lineSeparator;
		}  

		public void handleText(char[] text, int position) {
			strbuf.append(text);
		}

		public void handleEndTag(HTML.Tag tag, int position) {
			if(tag.isBlock()) {
				strbuf.append(lineSeparator);
				strbuf.append(lineSeparator);
			} else if(tag.breaksFlow()) {
				strbuf.append(lineSeparator);
			}
		}

		public void handleSimpleTag(HTML.Tag tag, MutableAttributeSet attributes, int position) {
			if(tag.isBlock()) {
				strbuf.append(lineSeparator);
				strbuf.append(lineSeparator);
			} else if (tag.breaksFlow()) {
				strbuf.append(lineSeparator);
			} else {
				strbuf.append(' '); 
			}
		}
	}
}
