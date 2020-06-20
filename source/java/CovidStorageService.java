package org.covid;

import java.io.File;
import java.util.LinkedList;
import java.util.zip.*;
import org.apache.log4j.Logger;
import org.rsna.ctp.Configuration;
import org.rsna.ctp.objects.FileObject;
import org.rsna.ctp.objects.ZipObject;
import org.rsna.ctp.pipeline.AbstractPipelineStage;
import org.rsna.ctp.pipeline.PipelineStage;
import org.rsna.ctp.pipeline.StorageService;
import org.rsna.ctp.servlets.SummaryLink;
import org.rsna.server.User;
import org.rsna.util.FileUtil;
import org.rsna.util.StringUtil;
import org.rsna.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A class to store objects in a directory system with no index.
 */
public class CovidStorageService extends AbstractPipelineStage implements StorageService {

	static final Logger logger = Logger.getLogger(CovidStorageService.class);

	File lastFileStored = null;
	long lastTime = 0;
	File lastFileIn;
    volatile int totalCount = 0;
    volatile int acceptedCount = 0;
    volatile int storedCount = 0;
    int maxEntries = 100;
    File indexDir = null;
    File storeDir = null;

	/**
	 * Construct a CovidStorageService for ZipObjects that include Covid datasets.
	 * @param element the XML element from the configuration file
	 * specifying the configuration of the stage.
	 */
	public CovidStorageService(Element element) {
		super(element);
		lastFileIn = null;
		indexDir = new File(root, "index");
		storeDir = new File(root, "store");
		indexDir.mkdirs();
		storeDir.mkdirs();
		CovidDB.getInstance(indexDir);
	}
	
	/**
	 * Start the pipeline stage. When this method is called, the Configuration object
	 * and all the stages have been instantiated.
	 */
	public void start() {
		super.start();
	}

	/**
	 * Stop the stage.
	 */
	public synchronized void shutdown() {
		CovidDB.getInstance().close();
		super.shutdown();
	}

	/**
	 * Store an object if the object is a ZipObject containing both a manifest and a metadata file.
	 * If the storage attempt fails, quarantine the input object if a quarantine
	 * was defined in the configuration, and return null to stop further processing.
	 * @param fileObject the object to process.
	 * @return the original FileObject, or null if the object could not be stored.
	 */
	public synchronized FileObject store(FileObject fileObject) {
		logger.debug("File received for storage: "+fileObject.getFile());

		//Count all the files
		totalCount++;

		if (fileObject instanceof ZipObject) {
			ZipObject zob = (ZipObject)fileObject;
			Document manifestXML = zob.getManifestDocument();
			String patientID = zob.getPatientID();
			Document metadataXML = null;
			for (ZipEntry ze : zob.getEntries()) {
				File zeFile = new File(ze.getName());
				String zeName = zeFile.getName();
				if (zeName.equals("metadata.xml")) {
					try { 
						String s = zob.extractFileText(ze, FileUtil.utf8);
						metadataXML = XmlUtil.getDocument(s);
						break;
					}
					catch (Exception skip) { }
				}
			}
			
			if ((manifestXML != null) && (metadataXML != null) && !patientID.equals("")) {
				//Okay, accept the object.
				acceptedCount++;
				
				//Get a place to store it.
				File caseFile = null;
				
				//First see if it has been stored before.
				//If so, this is an update.
				CovidDB db = CovidDB.getInstance();
				CovidDBEntry entry = db.get(patientID);
				if (entry != null) {
					//Old case
					caseFile = entry.getFile();
				}
				else {
					//New case
					File caseDir = null;
					File[] storeDirs = storeDir.listFiles();
					if (storeDirs.length == 0) {
						caseDir = new File(storeDir, "000000");
					}
					else caseDir = storeDirs[storeDirs.length - 1];
					caseDir.mkdirs();
					File[] caseDirFiles = caseDir.listFiles();
					if (caseDirFiles.length >= maxEntries) {
						int n = Integer.parseInt(caseDir.getName()) +1;
						caseDir = new File(storeDir, String.format("%06d", n));
						caseDir.mkdirs();
					}
					caseFile = new File(caseDir, patientID+".zip");
				}
				zob.copyTo(caseFile);

				//Now update the database
				Metadata metadata = new Metadata(metadataXML);
				entry = new CovidDBEntry(patientID, metadata, caseFile, System.currentTimeMillis());
				db.put(patientID, entry);
				storedCount++;
				lastFileStored = caseFile;
				lastTime = System.currentTimeMillis();
			}
		}
		lastFileOut = lastFileStored;
		lastTimeOut = lastTime;
		return fileObject;
	}

	/**
	 * Get HTML text displaying the current status of the stage.
	 * @return HTML text displaying the current status of the stage.
	 */
	public synchronized String getStatusHTML() {
		StringBuffer sb = new StringBuffer();
		sb.append("<h3>"+name+"</h3>");
		sb.append("<table border=\"1\" width=\"100%\">");
		sb.append("<tr><td width=\"20%\">Files received for storage:</td>"
			+ "<td>" + totalCount + "</td></tr>");
		sb.append("<tr><td width=\"20%\">Files accepted for storage:</td>"
			+ "<td>" + acceptedCount + "</td></tr>");
		sb.append("<tr><td width=\"20%\">Files actually stored:</td>"
			+ "<td>" + storedCount + "</td></tr>");
		sb.append("<tr><td width=\"20%\">Last file stored:</td>");
		if (lastTime != 0) {
			sb.append("<td>"+lastFileStored+"</td></tr>");
			sb.append("<tr><td width=\"20%\">Last file stored at:</td>");
			sb.append("<td>"+StringUtil.getDateTime(lastTime,"&nbsp;&nbsp;&nbsp;")+"</td></tr>");
		}
		else sb.append("<td>No activity</td></tr>");
		sb.append("</table>");
		return sb.toString();
	}

	/**
	 * Get the list of links for display on the summary page.
	 * @param user the requesting user.
	 * @return the list of links for display on the summary page.
	 */
	public synchronized LinkedList<SummaryLink> getLinks(User user) {
		LinkedList<SummaryLink> links = super.getLinks(user);
		return links;
	}

}
