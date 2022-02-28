package ServerMain;

import Server.IF.HttpServer.HttpCacheManager;
import Server.IF.HttpServer.HttpElasticSearch;

import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;



public class HttpAuthorizationServer
{
	public static void main(String args[]) throws Exception
    {
		HttpServer server = HttpServer.create(new InetSocketAddress(7010), 500000);
//		new HttpElasticSearch(server);
		HttpElasticSearch serverThread;
		serverThread = new HttpElasticSearch(server);
		serverThread.start();
		
		// Caching 체크 Thread
		HttpCacheManager CacheThread;
		CacheThread = new HttpCacheManager();
		CacheThread.start();
	}
}
