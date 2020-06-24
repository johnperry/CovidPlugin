package org.covid;

import java.io.File;
import org.apache.log4j.Logger;
import org.rsna.util.FileUtil;
import org.rsna.util.StringUtil;
import org.rsna.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A singleton class to encapsulate an XML index of contributing sites.
 */
public class SiteIndex {

	static final Logger logger = Logger.getLogger(SiteIndex.class);

	static SiteIndex instance = null;
	static final String filename = "SiteIndex.xml";
	File indexFile = null;
	long lastModified = 0;
	Document indexXML = null;
	Element root = null;
	
	protected SiteIndex(File indexDir) throws Exception {
		indexDir.mkdirs();
		this.indexFile = new File(indexDir, filename);
		if (!indexFile.exists()) {
			indexXML = XmlUtil.getDocument();
			root = indexXML.createElement("SiteIndex");
			indexXML.appendChild(root);
			save();
		}
		else load();
	}
	
	private void load() {
		try {
			indexXML = XmlUtil.getDocument(indexFile);
			root = indexXML.getDocumentElement();
			lastModified = indexFile.lastModified();
		}
		catch (Exception ex) {
			logger.warn("Unable to parse the site index", ex);
		}
	}
	
	private boolean isStale() {
		return (indexFile.lastModified() - lastModified) > 1000;
	}
	
	/**
	 * Get the singleton instance, creating it only if it does not exist.
	 * @param indexFile the XML file containing the site index
	 * @return the instance
	 */
	public static SiteIndex getInstance(File indexDir) {
		if (instance == null) {
			try { instance = new SiteIndex(indexDir); }
			catch (Exception unable) {
				logger.warn("Unable to get the SiteIndex instance", unable);
			}
		}
		return instance;
	}
	
	/**
	 * Get the singleton instance.
	 * @return the instance
	 */
	public static SiteIndex getInstance() {
		return instance;
	}
	
	/**
	 * Add a new Site to the index, or update the email
	 * address of an existing site;
	 * @param siteID the id of the site to create or update
	 * @param email the email address
	 * @param siteName the name of the site
	 */
	public void add(String siteID, String username, String email, String phone, String sitename, String adrs1, String adrs2, String adrs3) {
		if (isStale()) load();
		Element site = null;
		Node child = root.getFirstChild();
		while (child != null) {
			if (child instanceof Element) {
				Element eChild = (Element)child;
				if (eChild.getNodeName().equals("Site")
						&& eChild.getAttribute("id").equals(siteID)) {
					site = eChild;
					break;
				}
			}
			child = child.getNextSibling();
		}
		if (site == null) {
			site = indexXML.createElement("Site");
			site.setAttribute("id", siteID);
			root.appendChild(site);
		}
		site.setAttribute("user", username);
		site.setAttribute("email", email);
		site.setAttribute("phone", phone);
		site.setAttribute("site", sitename);
		site.setAttribute("adrs1", adrs1);
		site.setAttribute("adrs2", adrs2);
		site.setAttribute("adrs3", adrs3);
		save();
	}

	/**
	 * Check whether a site with a specific siteID exists in the index.
	 * @param siteID the id of the site
	 * @return true if the site exists in the index; false otherwise
	 */
	public boolean contains(String siteID) {
		if (isStale()) load();
		Node child = root.getFirstChild();
		while (child != null) {
			if (child instanceof Element) {
				Element eChild = (Element)child;
				if (eChild.getNodeName().equals("Site")
						&& eChild.getAttribute("id").equals(siteID)) {
					return true;
				}
			}
			child = child.getNextSibling();
		}
		return false;
	}
	
	/**
	 * Get an XML document containing the index.
	 * @return the index
	 */
	public Document getXML() {
		if (isStale()) load();
		return indexXML;
	}
	
	private synchronized void save() {
		FileUtil.setText(indexFile, XmlUtil.toPrettyString(indexXML));
		lastModified = indexFile.lastModified();
	}
}
