package com.atlantbh.nutch.filter.xpath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;

import org.apache.avro.util.Utf8;
import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.metadata.Metadata;
import org.apache.nutch.parse.Parse;
import org.apache.nutch.protocol.Content;
import org.apache.nutch.storage.WebPage;
import org.apache.nutch.util.Bytes;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Content.class, Parse.class, WebPage.class})
public class XPathHtmlParserFilterTest {

	private XPathHtmlParserFilter xmlHtmlParser;
	private byte[] rawXmlContent;
	private byte[] rawHtmlContent;
	
	// Test data
	private static final String[] testStringArray = {"Harry Potter", "Learning XML"};
	private static final Float[] testFloatArray = {29.99f, 39.95f};
	
	@Before
	public void init() {
		xmlHtmlParser = new XPathHtmlParserFilter();	
	}
	
	@Test
	public void testGetParse() throws IOException {
		
		InputStream xmlInputStream = XPathHtmlParserFilterTest.class.getResourceAsStream("example-content.xml");
		rawXmlContent = new byte[xmlInputStream.available()];
		xmlInputStream.read(rawXmlContent);
		
		// Mock objects
		WebPage page = PowerMockito.mock(WebPage.class);	
		Content content = PowerMockito.mock(Content.class);	
		Parse parse = mock(Parse.class);
		Configuration configuration = mock(Configuration.class);
		
		// Mock data
		when(configuration.get(anyString())).thenReturn("");
		when(page.getContentType()).thenReturn(new Utf8("application/xml"));
		when(page.getContent()).thenReturn(ByteBuffer.wrap(rawXmlContent));
		
		when(configuration.getConfResourceAsReader(anyString())).thenReturn(new InputStreamReader(XPathIndexingFilterTest.class.getResourceAsStream("example-xpathfilter-conf.xml")));
		
		xmlHtmlParser.setConf(configuration);
		Parse parseReturn = xmlHtmlParser.filter("http://www.test.com/", page, parse, null, null);
		
		int stringValueIndexCount = 0;
		
		ByteBuffer metadata = page.getFromMetadata(new Utf8("testString"));
		if(metadata != null) {
			int index = Arrays.binarySearch(testStringArray, Bytes.toString(metadata.array()));
			stringValueIndexCount += index;
			assertTrue("String value not found!", stringValueIndexCount >= 0);
		}
		
		assertEquals("Not all String values parsed!", 1, stringValueIndexCount);
	}
	
	@Test
	public void testHtmlCleanupAndParse() throws IOException {
		
		InputStream htmlInputStream = XPathHtmlParserFilterTest.class.getResourceAsStream("example-content.html");
		rawHtmlContent = new byte[htmlInputStream.available()];
		htmlInputStream.read(rawHtmlContent);
		
		// Mock objects
		WebPage page = PowerMockito.mock(WebPage.class);	
		Content content = PowerMockito.mock(Content.class);	
		Parse parse = mock(Parse.class);
		Configuration configuration = mock(Configuration.class);
		
		// Mock data
		when(page.getContentType()).thenReturn(new Utf8("text/html"));
		when(page.getContent()).thenReturn(ByteBuffer.wrap(rawHtmlContent));
		when(page.getFromMetadata(new Utf8(Metadata.ORIGINAL_CHAR_ENCODING)))
			.thenReturn(ByteBuffer.wrap("utf-8".getBytes()));

		when(configuration.get(anyString())).thenReturn("");
		when(configuration.get("parser.character.encoding.default", "UTF-8")).thenReturn("UTF-8");
		when(configuration.getConfResourceAsReader(anyString())).thenReturn(new InputStreamReader(XPathIndexingFilterTest.class.getResourceAsStream("example-xpathfilter-conf2.xml")));
		
		xmlHtmlParser.setConf(configuration);
		Parse parseReturn = xmlHtmlParser.filter(content.getUrl(), page, parse, null, null);
		
		ByteBuffer meta = null;

		meta = page.getFromMetadata(new Utf8("articleAuthor"));
		if(meta != null)
			assertEquals("Error parsing html", "Samir ELJAZOVIĆ", Bytes.toString(meta.array()));
		
		meta = page.getFromMetadata(new Utf8("articleTitle"));
		if(meta != null)
			assertEquals("Error parsing html", "Amazon Elastic MapReduce – Part 2 (Amazon S3 Input Format)", Bytes.toString(meta.array()));
	}
	
}
