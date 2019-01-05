package ca.pfv.spmf.welwindow;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ca.pfv.spmf.gui.PreferencesManager;

public class Plugins {

//	private static String url = "http://www.philippe-fournier-viger.com/spmf/plugins/{pluginname}/info.txt";
//	static String url1 = "http://www.philippe-fournier-viger.com/spmf/plugins/{pluginname}/{pluginname}.jar";
//	static String url2 = "http://www.philippe-fournier-viger.com/spmf/plugins/{pluginname}/documentation.php";
//	static String[][] pluginNames = new String[][] {
//			{ "removelongtransactions", "UTF-8" },
//			{ "removeshorttransactions", "UTF-8" } };
	public static List<Plugin> listPlugin = new ArrayList<Plugin>();
	public static List<String> pluginNames;

	public static Plugin SendGET(String url, String EncodingFormat, String param) {
		String result="";
		BufferedReader read = null;
		Plugin p = new Plugin();
		
		// Save the url of the repository from which this plugin is found
		// This can be useful if we want to update from the same repository later.

		try {
			URL realurl = new URL(url + "?" + param);
			URLConnection connection = realurl.openConnection();
			connection.connect();
			Map<String, List<String>> map = connection.getHeaderFields();
			for (String key : map.keySet()) {
			}
			read = new BufferedReader(new InputStreamReader(
					connection.getInputStream(), EncodingFormat));
			String line;
			while ((line = read.readLine()) != null) {
				if (line.indexOf("#NAME") == 0) {
					p.setName(line.substring(7));
					
				}
				if (line.indexOf("#IMPLEMENTATION_AUTHOR") == 0) {
					p.setAuthor(line.substring(23));
				
				}
				
				if (line.indexOf("#CATEGORY") == 0) {
					p.setCategory(line.substring(10));
					
				}
				if (line.indexOf("#PLUGIN_VERSION") == 0) {
					p.setVersion(line.substring(17));
				
				}
				if (line.indexOf("#DESCRIPTION") == 0) {
					p.setDescription(line.substring(14));
				
				}
				
				if (line.indexOf("#URL_OF_DOCUMENTATION") == 0) {
					p.setUrlOfDocumentation(line.substring(23));
				
				}
				result += line;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (read != null) {
				try {
					read.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return p;
	}


	public static void pluginInit() {
		// FIRST, get the list of plugins
		// =================================
		// Create the URL:
		// "http://www.philippe-fournier-viger.com/spmf/plugins/list_of_plugins.txt"
		String urlPluginNames = PreferencesManager.getInstance()
				.getRepositoryURL() + "/list_of_plugins.txt";
		pluginNames = SendGETPluginNames(urlPluginNames, "UTF8", null);
		
		// EMPTY THE LIST OF PLUGIN
		listPlugin.clear();
		
		// Load the plugins
		for (String pluginName : pluginNames) {
			// =================================
			// Create the URL:
			// "http://www.philippe-fournier-viger.com/spmf/plugins/{pluginname}/info.txt"
			String url = PreferencesManager.getInstance()
					.getRepositoryURL() + pluginName // plugin name
					+ "/info.txt";
			// =================================
			Plugin plugin = SendGET(url, "UTF8", null);
			plugin.setRepositoryURL(PreferencesManager.getInstance().getRepositoryURL());
			plugin.setRepositoryPluginFolder(pluginName);
			listPlugin.add(plugin);
		}
		
//		if (listPlugin.size() == 0) {
//			
//		}
	}


	private static List<String> SendGETPluginNames(String url, String EncodingFormat, String param) {
		List<String> pluginNames = new ArrayList<String>();
		String result="";
		BufferedReader read = null;
		Plugin p = new Plugin();
		try {
			URL realurl = new URL(url + "?" + param);
			URLConnection connection = realurl.openConnection();
			connection.connect();
			Map<String, List<String>> map = connection.getHeaderFields();
//			for (String key : map.keySet()) {
//			}
			read = new BufferedReader(new InputStreamReader(
					connection.getInputStream(), EncodingFormat));
			String line;
			while ((line = read.readLine()) != null) {
				if (line.length() >= 1) {
					pluginNames.add(line);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (read != null) {
				try {
					read.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return pluginNames;
	}
	
	public static boolean checkIfURLisARepository(String URLName){
		// FIRST, get the list of plugins
		// =================================
		// Create the URL:
		// "http://www.philippe-fournier-viger.com/spmf/plugins/list_of_plugins.txt"
		String urlPluginNames = URLName + "/list_of_plugins.txt";
		// =================================
		
	    try {
		
	      HttpURLConnection.setFollowRedirects(false);
	      // note : you may also need
	      //        HttpURLConnection.setInstanceFollowRedirects(false)
	      HttpURLConnection con =
	         (HttpURLConnection) new URL(urlPluginNames).openConnection();
	      con.setRequestMethod("HEAD");
	      return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
	    }
	    catch (Exception e) {
	       return false;
	    }
	  }
}
	
		