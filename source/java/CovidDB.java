package org.covid;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import jdbm.RecordManager;
import jdbm.htree.HTree;
import jdbm.helper.FastIterator;
import org.apache.log4j.Logger;
import org.rsna.util.FileUtil;
import org.rsna.util.JdbmUtil;
import org.rsna.util.StringUtil;
import org.rsna.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A class to encapsulate an index of Covid cases.
 */
public class CovidDB {

	static final Logger logger = Logger.getLogger(CovidDB.class);

	static CovidDB instance = null;
	
	RecordManager recman = null;
	HTree index = null;
	
	protected CovidDB(File indexFile) {
		getIndex(indexFile.getAbsolutePath());
	}
	
	public static CovidDB getInstance(File indexDir) {
		if (instance == null) {
			indexDir.mkdirs();
			File indexFile = new File(indexDir, "index");
			instance = new CovidDB(indexFile);
		}
		return instance;
	}
	
	public static CovidDB getInstance() {
		return instance;
	}
	
	public boolean put(String patientID, CovidDBEntry entry) {
		try { 
			index.put(patientID, entry);
			recman.commit();
		}
		catch (Exception ex) { 
			logger.warn("Exception putting entry in index: "+entry.getPatientID(), ex);
			return false; }
		return true;
	}
	
	public CovidDBEntry get(String patientID) {
		try { return (CovidDBEntry)index.get(patientID); }
		catch (Exception ex) { return null; }
	}
	
	public boolean remove(String patientID) {
		try {
			index.remove(patientID);
			return true;
		}
		catch (Exception ex) { return false; }
	}
	
	public CovidDBEntry[] getDBEntries() {
		try {
			FastIterator fit = index.keys();
			String key;
			LinkedList<CovidDBEntry> list = new LinkedList<CovidDBEntry>();
			while ((key=(String)fit.next()) != null) {
				CovidDBEntry entry = (CovidDBEntry)index.get(key);
				list.add(entry);
			}
			CovidDBEntry[] entries = list.toArray(new CovidDBEntry[list.size()]);
			Arrays.sort(entries);
			return entries;
		}
		catch (Exception ex) { 
			logger.warn("Exception getting CovidDBEntries", ex);
			return new CovidDBEntry[0];
		}
	}
	
	//Load the index HTrees
	private void getIndex(String indexPath) {
		recman	= JdbmUtil.getRecordManager( indexPath );
		index	= JdbmUtil.getHTree(recman, "index");
	}

	public synchronized void close() {
		//Commit and close the database
		if (recman != null) {
			try {
				recman.commit();
				recman.close();
			}
			catch (Exception ex) {
				logger.warn("Unable to commit and close the database.");
			}
		}
	}
}
