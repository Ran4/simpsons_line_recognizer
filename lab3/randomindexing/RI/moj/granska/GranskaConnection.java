/*
 * GranskaConnection.java
 * Copyright (c) 2004, KTH NADA.
 *
 * This file is part of SweSum^2 (see http://swesum.nada.kth.se/),
 * and is free software; you can redistribute it and/or
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
 * Martin Hassel, 2004-feb-15
 * http://www.nada.kth.se/~xmartin/
 *
 */

package moj.granska;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * GranskaConnection handles all communication with a Granska server. So far,
 * only tokenizing, lemmatizing, tagging, parsing and grammar checking is
 * only available for Swedish text input.
 * Parts of this class where derived from the Grim project with special
 * thanks to Stefan Westlund.
 *
 * @author  Martin Hassel
 * @version 2004-may-21
 */
public class GranskaConnection {
    private final URL _servletsBaseURL;
    private final URL _inflectorURL;
    private final URL _granskaURL;
    private final URL _gtaURL;

    /**
     * Get default Granska connection properties for connecting to the Granska 
     * server at KTH NADA.
     * @return properties object containing the default Granska connection info
     *         for connecting to the Granska server at KTH NADA.
     */
    public static Properties getDefaultProperties() {
    	Properties props = new Properties();
		props.setProperty("protocol", "http");
		props.setProperty("host", "skrutten.nada.kth.se");
		props.setProperty("port", "80");
		props.setProperty("path", "/grimservlets/");
		props.setProperty("inflector_path", "inflect");
		props.setProperty("granska_path", "check");
		props.setProperty("gta_path", "analyze");
    	return props;
    }

    /**
     * Create a new GranskaConnection to the given <code>host</code> at the
     * given <code>port</code> and <code>path</code>.
     * @param host Granska server host to connect to.
     * @param port port on Granska server to connect to.
     * @param path path to the servlets on the Granska server.
     */
    public GranskaConnection(String host, int port, String path)
        throws java.net.MalformedURLException {
    	Properties props = new Properties();
        try {
            props.load(new FileInputStream("Granska.properties"));
		} catch (Exception ex) {
			props = GranskaConnection.getDefaultProperties();
		}
        _servletsBaseURL = new URL("http", host, port, path);
        _inflectorURL = new URL(_servletsBaseURL, props.getProperty("inflector_path"));
        _granskaURL = new URL(_servletsBaseURL, props.getProperty("granska_path"));
        _gtaURL = new URL(_servletsBaseURL, props.getProperty("gta_path"));
    }

    /**
     * Create a new GranskaConnection to the Granska Server given in Granska.properties.
     */
    public GranskaConnection()
        throws java.net.MalformedURLException {
    	Properties props = new Properties();
        try {
            props.load(new FileInputStream("Granska.properties"));
		} catch (Exception ex) {
			System.err.println("Can't load Granska.properties, will try to connect to the Granska server at KTH NADA");
			props = GranskaConnection.getDefaultProperties();
        }
		_servletsBaseURL = new URL(props.getProperty("protocol"),
				props.getProperty("host"),
				Integer.parseInt(props.getProperty("port")),
				props.getProperty("path"));
        _inflectorURL = new URL(_servletsBaseURL, props.getProperty("inflector_path"));
        _granskaURL = new URL(_servletsBaseURL, props.getProperty("granska_path"));
        _gtaURL = new URL(_servletsBaseURL, props.getProperty("gta_path"));
    }

    /**
     * Sends the text <code>text</code> to a Granska/GTA/Inflector servlet and 
     * returns an InputSource "pointing" at the scrutinized text. The returned 
     * XML-document contains information about lemmas and pos-tags of words as 
     * well as possible grammar errors.
     * @param text the text that is to be scrutinized by the Granska server.
     * @param servlet servlet to connect to, i.e. Granska/GTA/Inflector.
     * @return an InputSource, for example to be passed to a XMLReader.
     */
    private InputSource granskaConnect(String text, URL servlet) {
        try {
            HttpURLConnection servletConnection = (HttpURLConnection)servlet.openConnection();
            servletConnection.setDoOutput(true);
            servletConnection.setDoInput(true);
            BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(servletConnection.getOutputStream(), "ISO-8859-1"));
            bw.write( "text="+text);
            bw.flush();
            bw.close();
            return new InputSource(new BufferedReader(
                new InputStreamReader(servletConnection.getInputStream(), "ISO-8859-1")));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Sends the text <code>text</code> to the Granska server and returns an
     * InputSource "pointing" at the scrutinized text. The returned XML-document
     * contains information about lemmas and pos-tags of words as well as
     * possible grammar errors.
     * @param text the text that is to be scrutinized by the Granska server.
     * @return an InputSource, for example to be passed to a XMLReader.
     */
    public InputSource scrutinize(String text) {
        return granskaConnect(text, _granskaURL);
    }

    /**
     * Sends the text <code>text</code> to the Granska server and returns an
     * InputSource "pointing" at the parsed text. The returned XML-document
     * contains information about lemmas and pos-tags of words as well as
     * phrase structure information.
     * @param text the text that is to be parsed by the Granska server.
     * @return an InputSource, for example to be passed to a XMLReader.
     */
    public InputSource parse(String text) {
        return granskaConnect(text, _gtaURL);
    }

    /**
     * Tokenizes the given text. The tokens (words, delimiters
     * etc.) in the returned string are separated by space. For example, the text
     * "Han springer fortare!" would yield the response "Han springer fortare ! ".
     * Note: Processes the text until first encountered newline, following text is skipped!
     * @param text the text that is to be tokenized.
     * @return the text tokenized, or <code>null</code> if <code>tokenize</code>
     *         fails.
     */
    public String tokenize(String text) {
        try {
            InputSource source = granskaConnect(text, _gtaURL);
            XMLReader parser =
                XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
            tokenizeHandler handler = new tokenizeHandler();
            parser.setContentHandler(handler);
            parser.parse(source);
            return handler.text;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Tokenizes and lemmatizes the given text. The tokens (words, delimiters
     * etc.) in the returned string are separated by space. For example, the text
     * "Han springer fortare!" would yield the response "han springa fort ! ".
     * Note: Processes the text until first encountered newline, following text is skipped!
     * @param text the text that is to be tokenized and lemmatized.
     * @return the text tokenized and lemmatized, or <code>null</code> if
     *         <code>lemmatize</code> fails.
     */
    public String lemmatize(String text) {
        try {
            InputSource source = granskaConnect(text, _gtaURL);
            XMLReader parser =
                XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
            lemmaHandler handler = new lemmaHandler();
            parser.setContentHandler(handler);
            parser.parse(source);
            return handler.text;
        } catch (Exception ex) {
            //ex.printStackTrace();
        }
        return null;
    }

    /**
     * Tokenizes, lemmatizes and pos-tags the given text. The tokens (words,
     * delimiters etc.) in the returned string are separated by space. For
     * example, the text "Han springer fortare!" would yield the response
     * "han_pn springa_vb fort_ab !_mad ".
     * Note: Processes the text until first encountered newline, following text is skipped!
     * @param text the text that is to be tokenized, lemmatized and pos-tagged.
     * @return the text tokenized, lemmatized and pos-tagged - or <code>null</code>
     *         if <code>lemmaTag</code> fails.
     */
    public String lemmaTag(String text) {
        try {
            InputSource source = granskaConnect(text, _gtaURL);
            XMLReader parser =
                XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
            lemmaTagHandler handler = new lemmaTagHandler();
            parser.setContentHandler(handler);
            parser.parse(source);
            return handler.text;
        } catch (Exception ex) {
            //ex.printStackTrace();
        }
        return null;
    }

    /**
     * Pos-tags the given text. The tokens (words, delimiters etc.) in the
     * returned string are separated by space. For example, the text "Han
     * springer fortare!" would yield the response "Han_pn.utr.sin.def.sub
     * springer_vb.prs.akt fortare_ab.kom !_mad ".
     * Note: Processes the text until first encountered newline, following text is skipped!
     * @param text the text that is to be pos-tagged.
     * @return the text pos-tagged - or <code>null</code> if
     *         <code>simpleTag</code> fails.
     */
    public String simpleTag(String text) {
        try {
            InputSource source = granskaConnect(text, _gtaURL);
            XMLReader parser =
                XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
            simpleTagHandler handler = new simpleTagHandler();
            parser.setContentHandler(handler);
            parser.parse(source);
            return handler.text;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Inflects the given word.
     * @param word the word that is to be inflected.
     * @return all inflections of the given word, no matter wordclass.
     *         Inflections are separated by newline (\n).
     */
    public String inflect(String word) {
        return inflect(word, "");
    }

    /**
     * Inflects the given word according to the paradigm of the given wordclass.
     * @param word the word that is to be inflected.
     * @param wordclass the wordclass of the word that is to be inflected.
     * @return all inflections of the given word according to the paradigm of
     *         the given wordclass. Inflections are separated by newline (\n).
     */
    public String inflect(String word, String wordclass) {
        try {
            InputSource source = granskaConnect(word+"&wc="+wordclass, _inflectorURL);
            XMLReader parser =
                XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
            inflectHandler handler = new inflectHandler();
            parser.setContentHandler(handler);
            parser.parse(source);
            return handler.text;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }


    // Nested classes to handle XML parsing
    /**
     * lemmaHandler is a ContentHandler that extracts lemmas as the XML is
     * parsed. The result is a tokenized and lemmatized text in the public
     * String <code>text</code>.
     */
    private class lemmaHandler extends DefaultHandler {
        private StringBuffer textbuffer = new StringBuffer();
        public String text = "";

        public void startElement(String namespaceURI, String localName,
                String qualifiedName, Attributes atts) throws SAXException {
            if(localName.equals("w")) {
            	if(atts.getValue("lemma") != null)
            		textbuffer.append(atts.getValue("lemma")+" ");
            }
        }

        public void endDocument() {
            text = textbuffer.toString();
        }
    } // End nested class

    private class lemmaTagHandler extends DefaultHandler {
        private StringBuffer textbuffer = new StringBuffer();
        public String text = "";

        public void startElement(String namespaceURI, String localName,
                String qualifiedName, Attributes atts) throws SAXException {
            if(localName.equals("w")) {
            	if(atts.getValue("lemma") != null) {
                    textbuffer.append(atts.getValue("lemma")+"_");
                    String tag = atts.getValue("tag");
                    if(tag.indexOf(".") > 1)
                        textbuffer.append(tag.substring(0,tag.indexOf("."))+" ");
                    else
                        textbuffer.append(tag+" ");
                }
            }
        }

        public void endDocument() {
            text = textbuffer.toString();
        }
    } // End nested class

    private class simpleTagHandler extends DefaultHandler {
        private StringBuffer outbuffer = new StringBuffer();
        private StringBuffer buffer = null;
        public String text = "", _tag = "";

        public void startElement(String namespaceURI, String localName,
                String qualifiedName, Attributes atts) throws SAXException {
            buffer = new StringBuffer();
            if(localName.equals("w")) {
                _tag = atts.getValue("tag");
            }
        }

        public void endElement(String namespaceURI, String localName,
                String qualifiedName) throws SAXException {
            if(localName.equals("w")) {
                outbuffer.append(buffer+"_"+_tag+" ");
            }
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
            if(buffer != null)
                buffer.append(ch, start, length);
        }

        public void endDocument() {
            text = outbuffer.toString();
        }
    } // End nested class

    private class tokenizeHandler extends DefaultHandler {
        private StringBuffer outbuffer = new StringBuffer();
        private StringBuffer buffer = null;
        public String text = "";

        public void startElement(String namespaceURI, String localName,
                String qualifiedName, Attributes atts) throws SAXException {
            buffer = new StringBuffer();
        }

        public void endElement(String namespaceURI, String localName,
                String qualifiedName) throws SAXException {
            if(localName.equals("w")) {
                outbuffer.append(buffer+" ");
            }
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
            if(buffer != null)
                buffer.append(ch, start, length);
        }

        public void endDocument() {
            text = outbuffer.toString();
        }
    } // End nested class

    private class inflectHandler extends DefaultHandler {
        private StringBuffer outbuffer = new StringBuffer();
        private StringBuffer buffer = null;
        public String text = "";
        boolean inProposal = false;

        public void startElement(String namespaceURI, String localName,
                String qualifiedName, Attributes atts) throws SAXException {
            buffer = new StringBuffer();
            if(localName.equals("proposal"))
                inProposal = true;
        }

        public void endElement(String namespaceURI, String localName,
                String qualifiedName) throws SAXException {
            if(inProposal) {
                if(localName.equals("word"))
                    outbuffer.append(buffer);
                if(localName.equals("tag"))
                    outbuffer.append(" "+buffer+"\n");
                inProposal = false;
            }
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
            if(buffer != null)
                buffer.append(ch, start, length);
        }

        public void endDocument() {
            text = outbuffer.toString();
        }
    } // End nested class(es)

	/**
	 * Some example code that showcases some usages of the Granska package.
	 * @param args no arguments taken
	 */
    public static void main (String[] args) {
        try {
            GranskaConnection granska = new GranskaConnection();
            System.out.println("Han springer fortare!");
            System.out.println(granska.tokenize("Han springer fortare!"));
            System.out.println(granska.lemmatize("Han springer fortare!"));
            System.out.println(granska.lemmaTag("Han springer fortare!"));
            System.out.println(granska.simpleTag("Han springer fortare!"));
            System.out.println(granska.inflect("springa"));
            System.out.println(granska.inflect("springa","vb"));
            System.out.println(granska.inflect("springa","nn"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
