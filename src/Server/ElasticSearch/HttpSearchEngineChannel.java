package Server.ElasticSearch;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import Server.Cached.ECMAuthorizationCached;
import Server.Common.Parsing.HttpCommonParsing;
import Server.Config.HttpConfig;
import Server.ECM.HttpAuthorizationFilterECM;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;


/**
 * WAS, ElasticSearch, Documentum I/F Channel Class
 */
public class HttpSearchEngineChannel implements Runnable {
	Logger logger = Logger.getLogger(this.getClass());
	
	ECMAuthorizationCached AuthorObject = null;
	private String result =null, aclresult =null, PermitDocIDList=null;
	HttpExchange t;

	public HttpSearchEngineChannel(ECMAuthorizationCached AuthorObject, HttpExchange t) {
		this.t = t;
		this.AuthorObject = AuthorObject;
	}

	/**
	 * ElasticSearch POST 諛⑹떇�쇰줈 寃곌낵 �붿껌
	 */
	@Override
	public void run() {
		
		HttpClient _Httpclient = null;
		HttpPost _Httppost = null;
		
		try 
		{
			this.AuthorObject.setCurrentPage(this.AuthorObject.getCurrentPage());
			
			logger.info("###########################");
			logger.info("###########################");
			logger.info("this.AuthorObject.setCurrentPage(this.AuthorObject.getCurrentPage()); >> " + this.AuthorObject.getCurrentPage());
			logger.info("###########################");
			logger.info("###########################");
			
//			if(Base64Coder.decodeString(this.AuthorObject.getUserRequestQuery()))
			
			/**
			 * 1. WAS -> ElasticSearch 검색 요청 전 KEY 필드만 처리하기 위한 대체 작업
			 * 2. ElasticSearch 최종 권한 있는 문서 리스트 및 검색어를 호출
			*/
//			String returnisCachingMemory = _isCachingInMemory(_Httpclient, _Httppost);
//			logger.info("returnisCachingMemory ~~~ " + returnisCachingMemory);
//			_RequestAuthorizedListSearch(_Httpclient, _Httppost, returnisCachingMemory);
			
			_RequestAuthorizedListSearch(_Httpclient, _Httppost, _isCachingInMemory(_Httpclient, _Httppost));
			
			/**
			 *  WAS -> ElasticSearch -> Response -> DociID -> Documentum ACL Check -> ElasticSearch Query -> WAS Return
			 * 최종결과 화면에 리턴
			*/
			new _HttpDocumentumInterface(t, aclresult, this.AuthorObject, true).run();	
			
		} catch (Exception e) {
//			logger.info(e.getMessage());
		}
	}
	
	
	/**
	 * 1. WAS -> Search Log && Caching results
	*/
	private void _UpdateCacheProcessing(ECMAuthorizationCached CachedUserObject, String PermitDocIDList) {
		try 
		{
			// Cahce UserList Insert
			CachedUserObject.InsertUserRequestObject(CachedUserObject, PermitDocIDList);
			
			// Cache User ArrayList 확인
//			CachedUserObject.getShowCacheList();
			
		} catch (Exception e) {
			logger.info(e.getMessage());
		}
	}
	
	
	/**
	 * 사용자별 Caching 문서 리스트가 getMaxAuthorizedThrethold보다 작으면
	 * Documenutum으로부터 문서리스트 채우는 함수
	 * 
	*/
	private void CachingCrawletoDocumentum(ECMAuthorizationCached Cached, HttpClient _Httpclient, HttpPost _Httppost) {
		int isLoop=1;
		int isChecking=1;
		
		if(Cached.getMyCacheSize(this.AuthorObject, this.AuthorObject.getXusername()) < Cached.getMaxAuthorizedThrethold(this.AuthorObject)) {
			while(Cached.getMyCacheSize(this.AuthorObject, this.AuthorObject.getXusername()) < Cached.getMaxAuthorizedThrethold(this.AuthorObject)
					&& isChecking*this.AuthorObject.getCurrentPageSize() <= Cached.getAuthourizedDoucmentList(AuthorObject.getCompanyCode()).get(this.AuthorObject.getXusername()).getHitTotalSearchResults()
					&& isChecking <= HttpConfig.CrawlRetryTimes
					) {
				logger.info("####### Memory Cache Request Again...");
				logger.info("isChecking*this.AuthorObject.getCurrentPageSize() >> " + isChecking*this.AuthorObject.getCurrentPageSize());
				logger.info("this.AuthorObject.getHitTotalSearchResults() >> " + Cached.getAuthourizedDoucmentList(AuthorObject.getCompanyCode()).get(this.AuthorObject.getXusername()).getHitTotalSearchResults());
				// Loop 내에서 from0->1->2로 ElasticSearch로 PageSize만큼 자동쿼리를 만들어줘야함
				this.AuthorObject.setCurrentDocumentumPage(this.AuthorObject.getCurrentDocumentumPage());
				logger.info("### this.AuthorObject.getCurrentDocumentumPage() ### >> " + this.AuthorObject.getCurrentDocumentumPage());
				_RequestNotAuthorizedListSearch(_Httpclient, _Httppost, String.valueOf(this.AuthorObject.getCurrentDocumentumPage()),String.valueOf(isLoop++));
				isChecking++;
			}
		}
	}
	
	
	/**
	 * 1. WAS -> ElasticSearch 검색 요청 전 KEY 필드만 처리하기 위한 대체 작업
	 * 
	*/
	private String _isCachingInMemory(HttpClient _Httpclient, HttpPost _Httppost)  {
		// this.t = t;

		logger.info("### HttpSearchEngineChannel >> " + t.getRequestURI());

		_Httpclient = new DefaultHttpClient();
		_Httppost = new HttpPost(HttpConfig.SearchEngineHostIP+ t.getRequestURI());

		try 
		{
			/**
			 * 최초에 해당 쿼리가 이전에 권한체크한 쿼리와 동일한지 비교 필요
			 * 틀린경우는 초기화, 동일할 경우는 PageNum 참조하여 Cache에서 찾아서 리턴필요
			 * ECM Dctm Search Authz
			*/
			ECMAuthorizationCached Cached = new ECMAuthorizationCached();
			if(!Cached.isAvailableCached(this.AuthorObject)) 
			{
				logger.info("####### isAvailableCached #########");
				logger.info("this.AuthorObject.getCurrentDocumentumPage() >> " + this.AuthorObject.getCurrentDocumentumPage());
				logger.info("Cached.getHitTotalSearchResults() >> " + Cached.getHitTotalSearchResults());
				
				
				_RequestNotAuthorizedListSearch(_Httpclient, _Httppost, String.valueOf(this.AuthorObject.getCurrentDocumentumPage()), String.valueOf(this.AuthorObject.getCurrentDocumentumPage()));
				
				// 1Page에서 15개미만일 경우, Cache를 채우기 위한 로직이 수행되어야 함
				// Cached.getMaxAuthorizedThrethold(this.AuthorObject) 만틈 loop 실행 
				// MaxAuthorizedThrethold = (AuthorObject.CurrentPage*AuthorObject.CurrentPageSize)+AuthorObject.CurrentPageSize;
				/*
				Memory CurrentPage >> 0
				Memory CurrentPageSize >> 15
				Memory Next PageSize >> 15.0
				MyCachedArrayList [0] >> doc0900bf4b8674815d
				MyCachedArrayList [1] >> doc0900bf4b86b235e5
				MyCachedArrayList [2] >> doc0900bf4b86d3d817
				MyCachedArrayList [3] >> doc0900bf4b86da6da7
				4
				Memory this Result >> doc0900bf4b8674815d,doc0900bf4b86b235e5,doc0900bf4b86d3d817,doc0900bf4b86da6da7,
				########## 1Page Cache Check >> 4
				*/
				
				CachingCrawletoDocumentum(Cached,_Httpclient, _Httppost);
				
//				logger.info("########## 1Page Cache Check >> " + Cached.getMyCacheSize(this.AuthorObject, this.AuthorObject.getXusername()));
			}
			else 
			{
				
				
				// 동일 Xusername에 쿼리가 존재함
//				Cached.RemoveUserRequestObject(this.AuthorObject, PermitDocIDList);
				logger.info("####### Memory Cached Processing...");

				logger.info("### MyCacheSize User Info ### >> " + this.AuthorObject.getXusername());
				logger.info("### MyCacheSize getMyCacheSize ### >> " + Cached.getMyCacheSize(this.AuthorObject, this.AuthorObject.getXusername()));
				logger.info("### MyCacheSize UserResponseIDCacheArrayList ### >> " + 
									this.AuthorObject.getAuthourizedDoucmentList(this.AuthorObject.getCompanyCode()).get(this.AuthorObject.getXusername()).getUserResponseIDCacheArrayList().size());
				logger.info("### MyCacheSize getCurrentPage ### >> " + this.AuthorObject.getCurrentPage());
				logger.info("### MyCacheSize PrintResultMapInfo ### >> " + Cached.PrintResultMapInfo(this.AuthorObject, this.AuthorObject.getXusername()));
				logger.info("### getCurrentDocumentumPage ### >> " + Cached.getMaxAuthorizedThrethold(this.AuthorObject));
				logger.info("### getMaxAuthorizedThrethold ### >> " + this.AuthorObject.getCurrentDocumentumPage());
				
				/*
				// Cache Re fulling
				 * // Cached.getMaxAuthorizedThrethold(this.AuthorObject) 만틈 loop 실행 
				   // MaxAuthorizedThrethold = (AuthorObject.CurrentPage*AuthorObject.CurrentPageSize)+AuthorObject.CurrentPageSize;
				 */
				CachingCrawletoDocumentum(Cached,_Httpclient, _Httppost);
				
//				PermitDocIDList = Cached.getMemoryMyCachedList(this.AuthorObject, this.AuthorObject.getXusername(), this.AuthorObject.getCurrentPage(), this.AuthorObject.getCurrentPageSize());
			}
			
			PermitDocIDList = Cached.getMemoryMyCachedList(this.AuthorObject, this.AuthorObject.getXusername(), this.AuthorObject.getCurrentPage(), this.AuthorObject.getCurrentPageSize());
			
			HttpConfig.end = System.currentTimeMillis();
			HttpConfig.total = (HttpConfig.end - HttpConfig.start);
			logger.info("Cahcing Check Delay Time >> " + HttpConfig.total + " ms ..");
			
			return PermitDocIDList;
		
		} catch (Exception e) {
			logger.info(e.getMessage());
		} 
		
		return null;
	}
	
	
	/**
	 * 1. WAS -> ElasticSearch 검색 요청 전 KEY 필드만 처리하기 위한 대체 작업
	 * 
	*/
	private String _RequestNotAuthorizedListSearch(HttpClient _Httpclient, HttpPost _Httppost, String thisFrom, String toFrom)  {
		// this.t = t;

		logger.info("### HttpSearchEngineChannel >> " + t.getRequestURI());

		_Httpclient = new DefaultHttpClient();
		_Httppost = new HttpPost(HttpConfig.SearchEngineHostIP+ t.getRequestURI());

		try 
		{
			/**
			 * 동일사용자인 경우 쿼리가 다를 경우 초기화 후, 아래 재검색 후 권한->사용자 리스트 갱신
			 *  WAS -> ElasticSearch 검색 요청 전 KEY 필드만 처리하기 위한 대체 작업
			*/
			
			logger.info("######Origin Query >>" + this.AuthorObject.getUserRequestQuery());
			
			logger.info("\"from\":" + String.valueOf(Integer.parseInt(thisFrom)) + " >> \"from\":" + String.valueOf(Integer.parseInt(thisFrom)));
			logger.info("this.AuthorObject.getCurrentPageSize() >> " + this.AuthorObject.getCurrentPageSize());
			logger.info("########### this.AuthorObject.getCurrentPage() >> " + this.AuthorObject.getCurrentPage());
			logger.info("########### this.AuthorObject.getCurrentPage()*HttpConfig.MaxPagingNum >> " + this.AuthorObject.getCurrentPage()*HttpConfig.MaxPagingNum);
			String realQuery = this.AuthorObject.getUserRequestQuery()
					.replace(HttpConfig.fields, "[\"id\"]").replace(HttpConfig.highlight, "").replace(HttpConfig.highlight2, "")
					.replace("\"from\":" + String.valueOf(this.AuthorObject.getCurrentPage()*this.AuthorObject.getCurrentPageSize()), "\"from\":" + (Integer.parseInt(toFrom)*HttpConfig.MaxPagingNum))
					.replace(HttpConfig.fielddata_fields, "").replace("\"size\":" + this.AuthorObject.getCurrentPageSize(), "\"size\":" + HttpConfig.MaxPagingNum + "");
			
			
			logger.info("\nRealQuery >> " + realQuery + "\n");
			
			HttpEntity entity = new ByteArrayEntity(realQuery.getBytes("utf-8"));
			_Httppost.setEntity(entity);
			HttpResponse response1 = _Httpclient.execute(_Httppost);
			result = EntityUtils.toString(response1.getEntity());
			
			// WAS -> ElasticSearch -> Response -> DociID 추룰 결과
			StringBuffer DocumentListBuffer = new HttpCommonParsing().HttpElasticResponseACK(this.AuthorObject, result);
//			logger.info("HttpSearchEngineChannel ACK from ElasticSearch >> " + DocumentListBuffer.toString());
			
			
			if(this.AuthorObject.getHitTotalSearchResults() > 0) 
			{
				PermitDocIDList = new HttpAuthorizationFilterECM(new HttpCommonParsing().HttpCreateACLInfo(this.AuthorObject, DocumentListBuffer)).run();
//				logger.info("#### [Documentum Server Before DocIDLIST >> " +  PermitDocIDList);
				logger.info("#### [Documentum Server," + this.AuthorObject.getXusername() + "] ECM PermitList [" + PermitDocIDList.split(",").length + "] >> " +  PermitDocIDList + "\n");
//				logger.info("############# this.AuthorObject.getUserResponseIDList()++ >> " + PermitDocIDList);
			
				_UpdateCacheProcessing(this.AuthorObject, PermitDocIDList);
				return new ECMAuthorizationCached().getMemoryMyCachedList(this.AuthorObject, this.AuthorObject.getXusername(), this.AuthorObject.getCurrentPage(), this.AuthorObject.getCurrentPageSize());
			}
			else {
				InitialNoDataSend();
			}
			
		} catch (MalformedURLException e) {
			logger.info(e.getMessage());
		} catch (IOException e) {
			logger.info(e.getMessage());
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	/**
	 * ElasticSearch No Data일 경우
	 * 결과 없음 Return
	 */
	private void InitialNoDataSend() {
		logger.info("########### No Results ~~~~~");
		StringBuffer noBuffer = new StringBuffer();
		noBuffer.append("{\"took\": 4,\"timed_out\": false,\"_shards\": {\"total\": 5,\"successful\": 5,\"failed\": 0},\"hits\": {\"total\": 0,\"max_score\": null,\"hits\": []}}");
		new _HttpDocumentumInterface(t, noBuffer.toString(), this.AuthorObject, false).run();
	}
	
	
	/**
	 * ElasticSearch 최종 권한 있는 문서 리스트 및 검색어를 호출
	 */
	private String _RequestAuthorizedListSearch(HttpClient _Httpclient, HttpPost _Httppost, String PermitDocIDList) 
	{
	
		logger.info("##### _RequestAuthorizedListSearch -> ElasticSearch >> " + PermitDocIDList);
		
		// null 일 경우 강제로 WAS에 전달
		// No Data
		if(PermitDocIDList == null) {
			InitialNoDataSend();
		}
		
		_Httpclient = new DefaultHttpClient();
		_Httppost = new HttpPost(HttpConfig.SearchEngineHostIP+ t.getRequestURI());

		StringBuilder tmpQuery = new StringBuilder();
//		logger.info("@@@@@@@@@@@@@@@@@@ " + PermitDocIDList);
		String[] resultsList = PermitDocIDList.split(",");
		
		try { 
						
			// 키워드 및 권한 있는 문서리스트를 쿼리로 전달하기 위한 작업
//			String querys = "\"query\":{\"filtered\":{\"query\":{\"bool\":{\"must\":[";
			String querys = "\"must\":[";
			StringBuffer idList = new StringBuffer();
			idList.append("\"must\":[{\"bool\":{\"should\":[{\"terms\":{\"KEY\":[");
//			idList.append("\"query\":{\"filtered\":{\"query\":{\"bool\":{\"must\":[{\"bool\":{\"should\":[{\"terms\":{\"KEY\":[");
			
			int loop=0;
			logger.info("##### _RequestAuthorizedListSearch -> this.AuthorObject.getCurrentPageSize() >> " + this.AuthorObject.getCurrentPageSize());
			for(String r : resultsList) {
				if(loop == this.AuthorObject.getCurrentPageSize()) {
					break;
				}	
				if(loop == resultsList.length-1 || loop == HttpConfig.MaxPagingNum-1 || loop == this.AuthorObject.getCurrentPageSize()-1){
					idList.append("\"" + r + "\"");
				}
				else {
					idList.append("\"" + r + "\",");
				}
				
				loop++;
			}
			idList.append("]}}]}},");
			
//			logger.info("##### First Request Query >> " + this.AuthorObject.getUserRequestQuery());
			
//			tmpQuery.append(this.AuthorObject.getUserRequestQuery().replace(" ", "").replace(querys, idList.toString()));
			tmpQuery.append(this.AuthorObject.getUserRequestQuery().replace(querys, idList.toString()));
			
			logger.info("##### Last Request Query >> " + tmpQuery.toString().replace("\"from\":" + this.AuthorObject.getCurrentPage()*this.AuthorObject.getCurrentPageSize() + ",", ""));
//			logger.info("##### Last Request Query >> " + tmpQuery.toString().replace("\"from\":\"[^\\d]*,", "\"from\":0,"));
						
			HttpEntity entity1 = new ByteArrayEntity(tmpQuery.toString().replace("\"from\":" + this.AuthorObject.getCurrentPage()*this.AuthorObject.getCurrentPageSize() + ",", "").getBytes("utf-8"));
//			HttpEntity entity1 = new ByteArrayEntity(tmpQuery.toString().replace("\"from\":\"[^\\d]*,", "\"from\":0,").getBytes("utf-8"));
			
			_Httppost.setEntity(entity1);
			HttpResponse response2 = _Httpclient.execute(_Httppost);
			aclresult = EntityUtils.toString(response2.getEntity());
			
//			logger.info(aclresult);
		}
		catch(Exception e) {
			e.getStackTrace();
		}
		
		return aclresult;
	}
}


/**
 * ECM Authorized Document List Return Class
 */
class _HttpDocumentumInterface {
	Logger logger = Logger.getLogger(this.getClass());
	
	ECMAuthorizationCached Cached;
	String result;
	HttpExchange t;
	boolean isOk = false;

	public _HttpDocumentumInterface(HttpExchange t, String result, ECMAuthorizationCached Cached, boolean isOk) {
		this.t = t;
		this.result = result;
		this.Cached = Cached;
		this.isOk = isOk;
	}

	public void run() {
		logger.info("\n##### _HttpDocumentumInterface ####");

		Headers headers = this.t.getResponseHeaders();
		headers.add("Content-length", String.valueOf(result.length()));
		headers.add("Content-Type", "application/json; charset=utf-8");
		headers.add("Access-Control-CORS-ENABLED", "true");
		headers.add("Access-Control-Allow-ORIGIN", "true");
		headers.add("Access-Control-Allow-Credentials", "true");
		try {
			this.t.sendResponseHeaders(200, 0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		}

//		ECMAuthorizationCached Cached = new ECMAuthorizationCached();
		/**
		 * ########################################## Return
		 * ##########################################
		 */
		
//		logger.info("###### Return OFinal >> " + result);
		String strResults="";
		if(this.isOk) {
			logger.info("###### Return Final Total Search Results >> " + String.valueOf(this.Cached.getAuthourizedDoucmentList(this.Cached.getCompanyCode()).get(this.Cached.getXusername()).getHitTotalSearchResults()));
			strResults = result.replace("\"hits\":{\"total\":" + this.Cached.getCurrentPageSize(), "\"hits\":{\"total\":" + this.Cached.getAuthourizedDoucmentList(this.Cached.getCompanyCode()).get(this.Cached.getXusername()).getHitTotalSearchResults());
		}else{
			strResults = result;
		}
		
		OutputStream os = this.t.getResponseBody();
		try {
			os.write(strResults.getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		} finally {
			logger.info("### Return ###");
			try {
				os.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
			}
		}

		HttpConfig.end = System.currentTimeMillis();
		HttpConfig.total = (HttpConfig.end - HttpConfig.start);
		logger.info("Elastic Final Delay Time >> " + HttpConfig.total + " ms ..\n\n\n\n\n");
	}
}
