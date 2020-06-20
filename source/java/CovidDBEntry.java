package org.covid;

import java.io.File;
import java.io.Serializable;

/**
 * A class to encapsulate an entry in the Covid database.
 */
public class CovidDBEntry implements Comparable<CovidDBEntry>, Serializable  {
	
	public final String patientID;
	public final Metadata metadata;
	public final File file;
	public final long time;
	
	public CovidDBEntry(String patientID, Metadata metadata, File file, long time) {
		this.patientID = patientID;
		this.metadata = metadata;
		this.file = file;
		this.time = time;
	}
	
	public String getPatientID() {
		return patientID;
	}
	
	public File getFile() {
		return file;
	}
	
	public Metadata getMetadata() {
		return metadata;
	}
	
	//Cmparable interface: set up for reverse chronolgical order
	public int compareTo(CovidDBEntry entry) {
		if (time > entry.time) return -1;
		if (time < entry.time) return 1;
		return 0;
	}
}
	
