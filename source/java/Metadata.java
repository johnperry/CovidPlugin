package org.covid;

import java.io.File;
import java.io.Serializable;
import org.w3c.dom.Document;

/**
 * A class to encapsulate the metadata in a Covid case submission.
 */
public class Metadata implements Serializable {
	
	final Document metadata;
	
	public Metadata(Document metadata) {
		this.metadata = metadata;
	}
	
	public Document getXML() {
		return metadata;
	}
}
	
