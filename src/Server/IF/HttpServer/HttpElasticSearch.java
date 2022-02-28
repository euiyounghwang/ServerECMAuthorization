package Server.IF.HttpServer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import Server.Cached.ECMAuthorizationCached;
import Server.Config.HttpConfig;
import Server.ECM.HttpAuthorizationFilterECM;
import Server.ElasticSearch.HttpSearchEngineChannel;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class HttpElasticSearch extends Thread {
	// �뚯씪 �붿껌���놁쓣 寃쎌슦��湲곕낯 �뚯씪
	// private static final String DEFAULT_FILE_PATH = "index.html";

	Logger logger = Logger.getLogger(this.getClass());
	private HttpServer server;

	/**
	 * @param HttpServer
	 *            �대씪�댁뼵�몄����듭떊���꾪븳 �뚯폆
	 */
	public HttpElasticSearch(HttpServer server) throws IOException {
		this.server = server;
		// this.run();
	}

	/**
	 * Server Listen
	 */
	// @Override
	public void run() {
		logger.info("\n\nThe Server is running ..");
		System.out.println("\n\nThe Server is running ..");
		this.server.createContext("/", new MyHandler());
		// this.server.createContext("/_plugin", new InfoHandler());
		this.server.setExecutor(null); // creates a default executor
		this.server.start();
	}

	// http://localhost:8000/info
	// static class InfoHandler implements HttpHandler {
	// public void handle(HttpExchange httpExchange) throws IOException {
	// String response =
	// "http://192.168.18.131:9200/_plugin/marvel/sense/vendor/ace/worker-json.js";
	// HttpElasticSearch.writeResponse(httpExchange, response.toString());
	// }
	// }

}



/**
 * WebServer Thread Handler
*/
class MyHandler implements HttpHandler {
	Logger logger = Logger.getLogger(this.getClass());

	BufferedReader in = null;
	String X_Username = null, CompanyCode = null;
	int CurrentPageNum = 0, PageSize = 0;
	long total = 0, start = 0, end = 0;
	Map<String,String> UserResponseIDCacheArrayList = new LinkedHashMap<String,String>();

	public MyHandler() {
		PropertyConfigurator.configure("log4j.properties");
	}

	/**
	 * ########################################## Return Target HTTP Server
	 * ##########################################
	 */
	public static void writeResponse(HttpExchange httpExchange, String response)
			throws IOException {
		httpExchange.sendResponseHeaders(200, response.length());
		OutputStream os = httpExchange.getResponseBody();
		os.write(response.getBytes());
		os.close();
	}

	/**
	 * Http Header
	 * WebServer Thread Created
	[INFO] (HttpElasticSearch.java) - Content-type=[application/x-www-form-urlencoded]
	[INFO] (HttpElasticSearch.java) - Host=[10.132.12.75:7010]
	[INFO] (HttpElasticSearch.java) - Content-length=[935]
	[INFO] (HttpElasticSearch.java) - Companycode=[30]
	[INFO] (HttpElasticSearch.java) - Connection=[Keep-Alive]
	[INFO] (HttpElasticSearch.java) - X-username=[pd292816]
	[INFO] (HttpElasticSearch.java) - Sitenms=[posco_ecm_test]
	[INFO] (HttpElasticSearch.java) - User-agent=[Java1.6.0_30]
	[INFO] (HttpElasticSearch.java) - Accept=[text/html, image/gif, image/jpeg, q=.2]
	[INFO] (HttpElasticSearch.java) - From=[0]
	[INFO] (HttpElasticSearch.java) - Size=[15]
	Initial >> pd292816
	*/
	public void handle(HttpExchange t) throws IOException {
		HttpConfig.start = System.currentTimeMillis();

		logger.info("\n\nWebServer Thread Created");

		StringBuffer RequestBuffer = new StringBuffer();
//		StringBuffer ResponseBuffer = new StringBuffer();
		try {
			// logger.info("###### Header #### ");

			// logger.info(t.getRequestHeaders().getFirst("X-Username"));

			Headers headers = t.getRequestHeaders();
			Set<Map.Entry<String, List<String>>> entries = headers.entrySet();

			for (Map.Entry<String, List<String>> entry : entries) {
				logger.info(entry.toString());
				if (entry.toString().contains("X")) {
					X_Username = entry.toString().replace("[", "").replace("]", "").split("=")[1];
				}
				
				if (entry.toString().toLowerCase().contains("com")) {
					CompanyCode = entry.toString().replace("[", "").replace("]", "").split("=")[1];
				}
				
				if (entry.toString().toLowerCase().contains("from")) {
					CurrentPageNum = Integer.parseInt(entry.toString().replace("[", "").replace("]", "").split("=")[1]);
				}
				
				if (entry.toString().toLowerCase().contains("size")) {
					PageSize = Integer.parseInt(entry.toString().replace("[", "").replace("]", "").split("=")[1]);
				}
			}
			
			CurrentPageNum = CurrentPageNum/PageSize;
			
			logger.info("Initial X_Username >> " + X_Username);
			logger.info("Initial CurrentPageNum >> " + CurrentPageNum);
			logger.info("Initial PageSize >> " + PageSize);

			in = new BufferedReader(new InputStreamReader(t.getRequestBody()));

			String line = "";

			while ((line = in.readLine()) != null) {
				RequestBuffer.append(line);
			}

			logger.info(RequestBuffer);
		} catch (Exception e) {
		} finally {
			in.close();
		}

//		new Thread(new HttpSearchEngineChannel(new ECMAuthorizationCached(X_Username, CompanyCode, RequestBuffer, ResponseBuffer, CurrentPageNum, PageSize), t)).start();
		new Thread(new HttpSearchEngineChannel(new ECMAuthorizationCached(X_Username, CompanyCode, RequestBuffer, UserResponseIDCacheArrayList, CurrentPageNum, PageSize), t)).start();
	}
}