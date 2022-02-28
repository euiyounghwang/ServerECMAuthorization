package Server.DB;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;



import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;


public class HttpAuthorizationDB extends Thread
{
	  // 파일 요청이 없을 경우의 기본 파일
//	  private static final String DEFAULT_FILE_PATH = "index.html";
	 
	  private HttpServer server;	
	 
	  
	  /**
	   * <pre>
	   * 기본 생성자
	   * </pre>
	   * 
	   * @param connectionSocket 클라이언트와의 통신을 위한 소켓
	   */
	  public HttpAuthorizationDB(HttpServer server) throws IOException
	  {
		  this.server = server;
	  }
	  
	
 
  
	  /* (non-Javadoc)
	   * @see java.lang.Thread#run()
	   */
	  @Override
	  public void run()
	  {
		  System.out.println("\n\nWebServer Listening ..");
		  this.server.createContext("/", new MyHandler());
		  this.server.setExecutor(null); // creates a default executor
		  this.server.start();
	  }
}

class MyHandler implements HttpHandler {
	
    public void handle(HttpExchange t) throws IOException {
    	
     System.out.println("\n\nWebServer Thread Created");
    	  
    
      System.out.println("###### Header #### ");
      Headers reqHeaders = t.getRequestHeaders();
//      System.out.println(t.getRequestHeaders().);
      
      
      System.out.println(t.getRequestHeaders().values());
      

      BufferedReader in=new BufferedReader(new InputStreamReader(t.getRequestBody()));
      String line="";
      StringBuffer buff=new StringBuffer();
      while ((line=in.readLine()) != null) {
        buff.append(line);
      }
      in.close();
      
      Date thisdate = new Date ();
      System.out.println("###" + thisdate.toString() + " >> [" + System.currentTimeMillis()+ "] >> " + buff.toString());

      
//      HttpClient _Httpclient = new DefaultHttpClient();
//      HttpPost _Httppost = new HttpPost("http://192.168.18.131:9200/ecm/posco/_search");
//
//      String result ="";
//      try 
//		{
//			HttpEntity entity = new ByteArrayEntity(buff.toString().getBytes("UTF-8"));
//			_Httppost.setEntity(entity);
//			 HttpResponse response1 = _Httpclient.execute(_Httppost);
//			  result = EntityUtils.toString(response1.getEntity());
//	
//		
//			
//		}
//		catch (MalformedURLException e) {
//			System.out.println(e.getMessage());
//	    } catch (IOException e) {
//	    	System.out.println(e.getMessage());
//	    }
//		
//		 
//      
//       System.out.println("### result ### >> " + result);
//       
//       Headers headers = t.getResponseHeaders();
//       headers.add("Content-length", String.valueOf(result.length()));
//       headers.add("Content-Type", "application/json; charset=UTF-8");
//       headers.add("Access-Control-Allow-Credentials", "true");
//       t.sendResponseHeaders(200, 0);
//		
//      OutputStream os = t.getResponseBody();
//      os.write(result.getBytes());
//      os.close();
    }
  }