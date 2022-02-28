package Server.Common.Parsing;

import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
 




import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import Server.Cached.ECMAuthorizationCached;
import Server.Config.HttpConfig;

public class HttpCommonParsing {

	/**
	 * ElasticSearch 응답 XML 권한 체크 XML 생성 처리
	 * 
	 */
	public StringBuffer HttpCreateACLInfo(ECMAuthorizationCached AuthorObject, StringBuffer result) {
		
		StringBuffer sb = new StringBuffer();
		try {
			 String[] userinfo = result.toString().split(",");
			 
			 if(userinfo != null) {
				 sb.append("<AuthorizationQuery>");
				 sb.append("<ConnectorQuery>");
				 sb.append("<Identity source=\"connector\">" + AuthorObject.getXusername() + "</Identity>");
				 for(String DocIDList : userinfo) {
					 sb.append("<Resource>googleconnector://" + new HttpConfig().getDocumentumServiceURL().get(AuthorObject.getCompanyCode()) 
							 	+ ".localhost/doc?docid=" + DocIDList.replace("doc","") + "</Resource>");
				 }
				 sb.append("</ConnectorQuery>");
				 sb.append("</AuthorizationQuery>");
			 }	 
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return sb;
	}
	

	/**
	 * ElasticSearch 응답 XML 요청 후 결과
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 * 
	 */
	public String HttpResponseACLInfo(String result) throws SAXException, IOException, ParserConfigurationException {
		
		InputSource is = new InputSource(new StringReader(result));
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
        StringBuffer sb = new StringBuffer();
		try {
			
			XPath xpath = XPathFactory.newInstance().newXPath();
	         
			NodeList ispermit = (NodeList)xpath.evaluate("//AuthorizationResponse/Answer/Decision", document, XPathConstants.NODESET);
			Boolean[] isPermitArray = new Boolean[ispermit.getLength()];
		    for( int idx=0; idx<ispermit.getLength(); idx++ ){
		            if(ispermit.item(idx).getTextContent().equals("Permit")) {
		            	isPermitArray[idx] = true;
		            }
		            else {
		            	isPermitArray[idx] = false;
		            }
		    }
		    
		    NodeList cols = (NodeList)xpath.evaluate("//AuthorizationResponse/Answer/Resource", document, XPathConstants.NODESET);
	        for( int idx=0; idx<cols.getLength(); idx++ ){
	        	if(isPermitArray[idx]) {
	        		sb.append("doc" + cols.item(idx).getTextContent().split("=")[1] +",");
	        	}
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}

//		System.out.println("HttpResponseACLInfo !~!" + sb.toString());
		
		return sb.toString();
	}
	
	
	public StringBuffer HttpElasticResponseACK(ECMAuthorizationCached AuthorizedCached, String result) {
		JSONParser parser = new JSONParser();
		StringBuffer _response = new StringBuffer();
		String _id = null;

		try {
//			System.out.println(result);
			 Object obj = parser.parse(result);
			 
	           JSONObject jsonObject = (JSONObject) obj;

	           if(jsonObject.get("hits") != null)
	           {
	        	   JSONObject jsonSubObject = (JSONObject)jsonObject.get("hits");
	        	   
	        	   if(jsonSubObject.get("total") != null) {
		        	   AuthorizedCached.setHitTotalSearchResults(Integer.parseInt(String.valueOf(jsonSubObject.get("total"))));
		        	   System.out.println("#### PublicSearch TotalHitCount >> " + String.valueOf(jsonSubObject.get("total")));
		        	   
		        	   AuthorizedCached.setHitTotalSearchResults(Integer.parseInt(String.valueOf(jsonSubObject.get("total"))));
	        	   }
	        	   
	        	   JSONArray items = (JSONArray) jsonSubObject.get("hits");
		          
//		           @SuppressWarnings("unchecked")
		           Iterator<String> iterator = items.iterator();
		   
		           while (iterator.hasNext()) 
		           {
		        	   Object obj1 = parser.parse(String.valueOf(iterator.next()));
		          	   JSONObject jsonArrayObject = (JSONObject) obj1;
		          	   
		          	   if(jsonArrayObject.get("_id") != null) {
		          		 _id = String.valueOf(jsonArrayObject.get("_id"));
		          		_response.append(_id +",");
		          	   }
		           }
	           }
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return _response;
	}
	
}
