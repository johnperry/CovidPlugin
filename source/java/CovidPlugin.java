package org.covid;

import java.io.File;
import java.io.FileOutputStream;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.rsna.ctp.plugin.AbstractPlugin;
import org.rsna.ctp.Configuration;
import org.w3c.dom.Element;
import org.rsna.server.*;
import org.rsna.util.Cache;
import org.rsna.util.FileUtil;

/**
 * A plugin to add covid servlets into the CTP server.
 */
public class CovidPlugin extends AbstractPlugin {
	
	static final Logger logger = Logger.getLogger(CovidPlugin.class);
	
	/**
	 * IMPORTANT: When the constructor is called, neither the
	 * pipelines nor the HttpServer have necessarily been
	 * instantiated. Any actions that depend on those objects
	 * must be deferred until the start method is called.
	 * @param element the XML element from the configuration file
	 * specifying the configuration of the plugin.
	 */
	public CovidPlugin(Element element) {
		super(element);
		Logger.getLogger("org.covid").setLevel( Level.toLevel("INFO") );
		logger.info(getID()+" Plugin instantiated");
	}
	
	public void start() {
		//Instantiate the SiteIndex
		SiteIndex.getInstance(root);
		
		//Add the servlet
		ServletSelector selector = Configuration.getInstance().getServer().getServletSelector();
		selector.addServlet("/", CovidServlet.class);
		selector.addServlet("qic", CovidServlet.class);
		selector.addServlet("qicadmin", CovidAdminServlet.class);
		
		//Set the name of the session cookie
		Authenticator.getInstance().setSessionCookieName("QICSESSION");
	
		File serverROOT = Configuration.getInstance().getServer().getServletSelector().getRoot();
		try {
			//Put the QIC home page in the serverROOT as index.html
			String qicHomePage = FileUtil.getText( FileUtil.getStream("/qichome.html"), FileUtil.utf8); 
			FileUtil.setText(new File(serverROOT, "index.html"), qicHomePage);
			//Force it into the cache to make sure it is found there
			FileUtil.setText(new File(Cache.getInstance().getDirectory(), "index.html"), qicHomePage);
			
			//Put the CTP home page in the serverROOT as ctp.html 
			String ctpHomePage = FileUtil.getText( FileUtil.getStream("/ctphome.html"), FileUtil.utf8 ); 
			FileUtil.setText(new File(serverROOT, "ctp.html"), ctpHomePage);
			
			//Put the logo in the serverROOT
			byte[] logo = FileUtil.getBytes( FileUtil.getStream("/logo.PNG") );
			FileOutputStream out = new FileOutputStream( new File(serverROOT, "logo.PNG") );
			out.write(logo); out.flush();
			FileUtil.close(out);			
		}
		catch (Exception ex) { logger.warn("Unable to install home pages", ex); }
	}

}