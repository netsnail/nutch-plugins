package com.atlantbh.nutch.index.book;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashSet;

import org.apache.avro.util.Utf8;
import org.apache.hadoop.conf.Configuration;
import org.apache.log4j.Logger;
import org.apache.nutch.indexer.IndexingException;
import org.apache.nutch.indexer.IndexingFilter;
import org.apache.nutch.indexer.NutchDocument;
import org.apache.nutch.storage.WebPage;
import org.apache.nutch.storage.WebPage.Field;
import org.apache.nutch.util.Bytes;

/**
 * Second stage of {@link XPathHtmlParserFilter} the IndexingFilter.
 * It takes the prepared data located in the metadata and indexes
 * it to solr.
 * 
 * 
 * @author Emir Dizdarevic
 * @version 1.4
 * @since Apache Nutch 1.4
 *
 */
public class BookIndexingFilter implements IndexingFilter {

	// Constants
	private static final Logger log = Logger.getLogger(BookIndexingFilter.class);
	
	// Configuration
	private Configuration configuration;
	
	private static final Collection<WebPage.Field> FIELDS = new HashSet<WebPage.Field>();

	static {
		  FIELDS.add(WebPage.Field.METADATA);
	}	
	
	public BookIndexingFilter() {}
	
	private void initConfig() {
		
	}
	
	@Override
	public Configuration getConf() {
		return configuration;
	}

	@Override
	public void setConf(Configuration configuration) {
		this.configuration = configuration;
		initConfig();
	}

	@Override
	public NutchDocument filter(NutchDocument doc, String url, WebPage page)
			throws IndexingException {

		ByteBuffer buffer = page.getFromMetadata(new Utf8("Author"));
		String stringValue = "";
		if(buffer != null) 
			stringValue = Bytes.toString(buffer.array());
			
		doc.add("author", stringValue);
		
		return doc;
	}

	@Override
	public Collection<Field> getFields() {
		return FIELDS;
	}
}
