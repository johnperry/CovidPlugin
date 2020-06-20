package org.covid;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.*;
import org.apache.log4j.Logger;
import org.rsna.ctp.objects.DicomObject;
import org.rsna.server.HttpRequest;
import org.rsna.server.HttpResponse;
import org.rsna.server.Path;
import org.rsna.servlets.Servlet;
import org.rsna.util.FileUtil;
import org.rsna.util.HtmlUtil;
import org.rsna.util.StringUtil;
import org.rsna.util.XmlUtil;
import org.w3c.dom.*;

/**
 * CovidServlet.
 * This servlet provides a browser-accessible user interface for
 * viewing the repository database.
 */
public class CovidServlet extends Servlet {

	static final Logger logger = Logger.getLogger(CovidServlet.class);

	/**
	 * Construct a CovidServlet.
	 * @param root the root directory of the server.
	 * @param context the path identifying the servlet.
	 */
	public CovidServlet(File root, String context) {
		super(root, context);
	}

	/**
	 * The servlet method that responds to an HTTP GET.
	 *
	 * If called with no query parameters, this method returns an
	 * HTML page listing the patients in the database in reverse
	 * order of submission date. 
	 *
	 * If called with a query parameter
	 * identifying a patient, this method returns the a page listing
	 * the contents of the case metadata.
	 *
	 * @param req The HttpServletRequest provided by the servlet container.
	 * @param res The HttpServletResponse provided by the servlet container.
	 */
	public void doGet( HttpRequest req, HttpResponse res ) {
		res.setContentEncoding(req);
		res.disableCaching();
		
		//Get the database
		CovidDB db = CovidDB.getInstance();
		
		Path path = req.getParsedPath();
		
		if (path.length() == 1) {
			//this is the servlet home page
			CovidDBEntry[] entries = db.getDBEntries();
			Document doc = null;
			try {
				doc = XmlUtil.getDocument();
				Element root = doc.createElement("Patients");
				doc.appendChild(root);
				for (CovidDBEntry entry : entries) {
					Metadata md = entry.getMetadata();
					Document mdDoc = md.getXML();
					Element mdRoot = mdDoc.getDocumentElement();
					Element patient = XmlUtil.getFirstNamedChild(mdRoot, "Patient");
					patient = (Element)doc.importNode(patient, true);
					root.appendChild(patient);
				}
				Document xsl = XmlUtil.getDocument( FileUtil.getStream( "/CovidPatientList.xsl" ) );
				String[] params = new String[] {
					"admin", (req.userHasRole("admin") ? "yes" : "no")
				};
				res.write( XmlUtil.getTransformedText( doc, xsl, params ) );
				res.setContentType("html");
			}
			catch (Exception ex) { res.setResponseCode(res.servererror); }
		}
		
		else if (path.length() == 2) {
			//this is the list of the metadata for a case
			try {
				String patientID = path.element(1);
				CovidDBEntry entry = (CovidDBEntry)db.get(patientID);
				Metadata md = entry.getMetadata();
				//res.write(XmlUtil.toPrettyString(md.getXML()));				
				Document xsl = XmlUtil.getDocument( FileUtil.getStream( "/CovidMetadataList.xsl" ) );
				res.write( XmlUtil.getTransformedText( md.getXML(), xsl, null ) );
				res.setContentType("html");
			}
			catch (Exception ex) { res.setResponseCode(res.notfound); }
		}
		
		else if (path.length() == 3) {
			if (path.element(2).equals("export")) {
				//this is the download of a case
				try {
					String patientID = path.element(1);
					CovidDBEntry entry = (CovidDBEntry)db.get(patientID);
					File file = entry.getFile();
					res.write(file);
					res.setContentDisposition(file);
					res.setContentType("zip");
				}
				catch (Exception ex) { res.setResponseCode(res.notfound); }
			}
			else if (path.element(2).equals("list")) {
				//this is the list of the objects in the zip file for a case
				try {
					String patientID = path.element(1);
					CovidDBEntry entry = (CovidDBEntry)db.get(patientID);
					File file = entry.getFile();
					Document doc = getZipDocument(file);
					Document xsl = XmlUtil.getDocument( FileUtil.getStream( "/CovidStudyList.xsl" ) );
					res.write( XmlUtil.getTransformedText( doc, xsl, null ) );
					res.setContentType("html");
				}
				catch (Exception ex) { 
					res.setResponseCode(res.notfound);
					logger.warn("list failure", ex);
				}
			}
			else if (path.element(2).equals("elements")) {
				//this is the list of the DICOM elements in a specific image
				try {
					String patientID = path.element(1);
					CovidDBEntry entry = (CovidDBEntry)db.get(patientID);
					File file = entry.getFile();
					File imgFile = getImageFile(file, req.getParameter("path"));
					DicomObject dob = new DicomObject(imgFile);
					res.write(dob.getElementTablePage(false));
					res.setContentType("html");
				}
				catch (Exception ex) { 
					res.setResponseCode(res.notfound);
					logger.warn("element list failure for "+req.getParameter("path"), ex);
				}
			}
			else if (path.element(2).equals("image")) {
				//this is the display of a specific image
				try {
					String patientID = path.element(1);
					CovidDBEntry entry = (CovidDBEntry)db.get(patientID);
					File file = entry.getFile();
					File imageFile = getImageFile(file, req.getParameter("path"));
					DicomObject dob = new DicomObject(imageFile);
					File jpg = File.createTempFile("TEMP-",".jpg");
					dob.saveAsJPEG(jpg, 0, 1024, 256, 100);
					res.write(jpg);
					res.setContentType("jpg");
				}
				catch (Exception ex) { 
					res.setResponseCode(res.notfound);
					logger.warn("element list failure for "+req.getParameter("path"), ex);
				}
			}
			else if (path.element(2).equals("delete")) {
				//this is the delete of a patient
				try {
					String patientID = path.element(1);
					CovidDBEntry entry = (CovidDBEntry)db.get(patientID);
					File file = entry.getFile();
					if ( db.remove(patientID) ) file.delete();
					res.redirect("/"+context);
				}
				catch (Exception ex) { 
					res.setResponseCode(res.notfound);
					logger.warn("delete failure for "+req.getParameter("path"), ex);
				}
			}
		}
		res.send();
	}
	
	private Document getZipDocument(File file) throws Exception {
		ZipFile zipFile = new ZipFile(file);
		Document doc = XmlUtil.getDocument();
		Element root = doc.createElement("Patient");
		doc.appendChild(root);
		String name = file.getName();
		name = name.substring(0, name.length()-4);
		root.setAttribute("name", name);
		Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
		Element study = null;
		Element series = null;
		while (zipEntries.hasMoreElements()) {
			ZipEntry ze = zipEntries.nextElement();
			String path = ze.getName().replace('\\','/');
			int n = countSlashes(path);
			if (!path.contains("metadata.xml") && (n > 1)) {
				if (n == 2) {
					//this is a study
					study = doc.createElement("Study");
					study.setAttribute("path", path);
					study.setAttribute("name", getName(path));
					root.appendChild(study);
				}
				else if ((n == 3) && path.endsWith("/")) {
					//this is a series
					series = doc.createElement("Series");
					series.setAttribute("path", path);
					series.setAttribute("name", getName(path));
					study.appendChild(series);
				}
				else {
					//this is an image
					Element image = doc.createElement("Image");
					image.setAttribute("path", path);
					image.setAttribute("name", getName(path));
					series.appendChild(image);
				}
			}
		}
		zipFile.close();
		return doc;
	}
	
	private File getImageFile(File file, String path) throws Exception {
		ZipFile zipFile = new ZipFile(file);
		Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
		while (zipEntries.hasMoreElements()) {
			ZipEntry ze = zipEntries.nextElement();
			String zpath = ze.getName().replace('\\','/');
			if (zpath.equals(path)) {
				BufferedOutputStream out = null;
				BufferedInputStream in = null;
				File outFile = File.createTempFile("TEMP-",".dcm");
				out = new BufferedOutputStream(new FileOutputStream(outFile));
				in = new BufferedInputStream(zipFile.getInputStream(ze));
				FileUtil.copy(in, out, -1);
				FileUtil.close(in);
				FileUtil.close(out);
				FileUtil.close(zipFile);
				return outFile;
			}
		}
		logger.warn("getImageFile returning null for "+path);
		return null;
	}
	
	private String getName(String s) {
		if (s.endsWith("/")) s = s.substring(0, s.length()-1);
		return new File(s).getName();
	}
	
	private int countSlashes(String s) {
		int n = 0;
		int k = -1;
		while ( (k = s.indexOf('/', k+1)) != -1) n++;
		return n;
	}
	
}











