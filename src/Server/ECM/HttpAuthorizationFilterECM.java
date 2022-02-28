package Server.ECM;

import java.io.IOException;
import java.net.MalformedURLException;

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

import Server.Common.Parsing.HttpCommonParsing;
import Server.Config.HttpConfig;


public class HttpAuthorizationFilterECM {
	Logger logger = Logger.getLogger(this.getClass());
	StringBuffer buff = new StringBuffer();

	public HttpAuthorizationFilterECM(StringBuffer buff) {
		this.buff = buff;
	}

	/**
	 * ElasticSearch POST 방식으로 결과 요청
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	 */
	public String run() throws SAXException, ParserConfigurationException {
		HttpClient _Httpclient = new DefaultHttpClient();
		HttpPost _Httppost = new HttpPost(HttpConfig.ConnectorLateHostIP);

		logger.info(HttpConfig.ConnectorLateHostIP);
		
		String result = "";
		try {
			
//			System.out.println(this.buff.toString());
			
			HttpEntity entity = new ByteArrayEntity(this.buff.toString()
					.getBytes("UTF-8"));
			_Httppost.setEntity(entity);
			HttpResponse response = _Httpclient.execute(_Httppost);
			result = EntityUtils.toString(response.getEntity());

			if (response.getStatusLine().getStatusCode() != 200) 
			{
				throw new RuntimeException("Failed : HTTP error code : "
						+ response.getStatusLine().getStatusCode() + "\t"
						+ response.getStatusLine());
			} else {
//				System.out.println("\n\n#### From Documentum Server >> \n" + result.replaceAll("\r\n", "").replaceAll(" ", ""));
				
				return new HttpCommonParsing().HttpResponseACLInfo(result);
			}

		} catch (MalformedURLException e) {
			logger.info(e.getMessage());
		} catch (IOException e) {
			logger.info(e.getMessage());
		}

		return null;
	}
}