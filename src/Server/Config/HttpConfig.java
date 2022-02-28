package Server.Config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import Server.Cached.ECMAuthorizationCached;


public class HttpConfig  
{
	 /**
	 * ElasticSearch 검색엔진 IP 정보 및
	 * ECM 권한 체크 Tomcat Service URL
	 * 
	 */
	
//	  Properties prop = getProperties((System.getProperty("user.dir") + "\\Server.properties"));
	  
	  public static String SearchEngineHostIP = "http://10.132.17.100:9200";
	  public static String ConnectorLateHostIP = "http://10.132.18.50:8080/connector-manager/authorization";
//	  public static String ConnectorLateHostIP = "http://10.132.12.89:8080/connector-manager/authorization";
	  
	  public static String fields = "[\"KEY\",\"TITLE\",\"TARGET_URL\",\"MAIL_ID\",\"SECURITY_LEVEL\",\"USER_NO\",\"USER_NAME\",\"INPUTDATE\",\"UPDATED_USER_NO\",\"UPDATED_DATE\",\"COMPANY_CODE\",\"COMPANY_NAME\",\"DEPT_CODE\",\"DEPT_NAME\",\"SYSTEM_SUB_CODE\",\"SYSTEM_SUB_NAME\",\"VERSION\",\"USER_INFO\",\"SYSTEM_TAG\",\"EXPT_RCMD_CNT\",\"GNR_RCMD_CNT\",\"SUM_RCMD_CNT\",\"SORT_RCMD\",\"EXPT_YN\",\"FILE_FORMAT\",\"FILE_SIZE\",\"DATE_CREATE\",\"DATE_MODIFY\",\"COWORKER_ID\",\"ECM_CONTENT_SOURCE\",\"ECM_IS_PRIDE\",\"ECM_FOLDER_ID\",\"ECM_DOC_TYPE\",\"ECM_OPEN_FLAG\",\"DEPT_CODE_PATH\",\"DEPT_NAME_PATH\"]";
	  public static String highlight2 = "\"highlight\":{\"require_field_match\":false,\"pre_tags\":[\"<b>\"],\"post_tags\":[\"<\\/b>\"],\"fields\":{\"CONTENT\":{\"number_of_fragments\":3,\"fragment_size\":120},\"TITLE\":{}}},";
	  public static String highlight = "\"highlight\":{\"require_field_match\":false,\"pre_tags\":[\"<b>\"],\"post_tags\":[\"</b>\"],\"fields\":{\"CONTENT\":{\"number_of_fragments\":3,\"fragment_size\":120},\"TITLE\":{}}},";
	  public static String fielddata_fields = "\"fielddata_fields\":[\"CONTENT.truncated\"],";
	  
	  Logger logger = Logger.getLogger(this.getClass());
	  
	  // Documentum 권한 체크를 위해 ElasticSearch Size 값을 변경
	  public static int MaxPagingNum =100;
	  
	  public static int CrawlRetryTimes = 10;
	  
	  private HashMap<String, String> documentumServiceURL = new HashMap<String, String>();
	  
	  public static long start = 0;
	  public static long end = 0;
	  public static long total = 0;
	  
	  
//	  PropertyConfigurator.configure(prop.getProperty("feed.home") + prop.getProperty("feed.log.config.file"));
	  
	  public HttpConfig() {
//		  SearchEngineHostIP  = "http://" + prop.getProperty("server.elasticsearch.address");
//		  System.out.println(prop.getProperty("server.elasticsearch.address"));
//		  ConnectorLateHostIP = "http://" + prop.getProperty("server.connectorauth.address");
		  _Initialized();
	  }
	  
	  private void _Initialized() {
		  documentumServiceURL.put("30", "newdocumentumtest");
		  documentumServiceURL.put("01", "newdocumentumict");
	  }
	  
	
	  public HashMap<String, String> getDocumentumServiceURL() {
				return documentumServiceURL;
	  }
	
	  public void setDocumentumServiceURL(HashMap<String, String> documentumServiceURL) {
			this.documentumServiceURL = documentumServiceURL;
	  }
	  
	  public Properties getProperties(String filePath) {
			InputStream fis = null;
			Properties prop = null;
			try {
				fis = new FileInputStream(filePath);

				prop = new Properties();
				prop.load(fis);
				
			} catch (IOException e) {
				logger.error(e);
			} finally {
				if (fis != null) try { fis.close(); } catch (Exception e) {};
			}
			return prop;
		}
}