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
 * CovidAdminServlet.
 * This servlet provides an AJAX interface for querying the SiteIndex and creating new site entries.
 */
public class CovidAdminServlet extends Servlet {

	static final Logger logger = Logger.getLogger(CovidAdminServlet.class);

	/**
	 * Construct a CovidAdminServlet.
	 * @param root the root directory of the server.
	 * @param context the path identifying the servlet.
	 */
	public CovidAdminServlet(File root, String context) {
		super(root, context);
	}

	/**
	 * The servlet method that responds to an HTTP GET.
	 * @param req The HttpServletRequest provided by the servlet container.
	 * @param res The HttpServletResponse provided by the servlet container.
	 */
	public void doGet( HttpRequest req, HttpResponse res ) {
		res.setContentEncoding(req);
		res.disableCaching();
		
		//Get the index
		SiteIndex index = SiteIndex.getInstance();
		
		Path path = req.getParsedPath();
		res.setContentType("xml");
		
		if (path.length() == 1) {
			//This is a request for the list of sites.
			//Only allow this option for admins.
			if (!req.userHasRole("admin")) {
				res.setResponseCode(res.forbidden);
				res.send();
				return;
			}
			//Okay, return the list of sites
			Document doc = index.getXML();
			try {
				Document xsl = XmlUtil.getDocument( FileUtil.getStream( "/CovidSiteList.xsl" ) );
				res.write( XmlUtil.getTransformedText( doc, xsl, null ) );
				res.setContentType("html");
			}
			catch (Exception ex) { res.setResponseCode(res.servererror); }
		}
		
		else if ((path.length() == 2) && path.element(1).equals("check")) {
			//This is the test for an existing site
			String siteID = req.getParameter("siteID", "");
			res.setResponseCode( index.contains(siteID) ? res.ok : res.notfound );
		}

		else if ((path.length() == 2) && path.element(1).equals("create")) {
			//This is the creation of a new site or the update of an old one
			String siteID = req.getParameter("siteID", "").trim();
			String email = req.getParameter("email", "").trim();
			String sitename = req.getParameter("sitename", "").trim();
			String username = req.getParameter("username", "").trim();
			
			if ((siteID.length() == 6) && siteID.matches("\\d{6}")
						&& (email.length() > 0) && isValid(email)) {
				index.add(siteID, email, sitename, username);
				res.setResponseCode(res.ok);
			}
			else res.setResponseCode(res.notfound);
		}
		
		else res.setResponseCode(res.notfound);
		res.send();
	}
	
	private boolean isValid(String email) {
      String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
      return email.matches(regex);
	}
}