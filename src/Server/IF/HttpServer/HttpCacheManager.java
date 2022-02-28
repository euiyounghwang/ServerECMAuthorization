package Server.IF.HttpServer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;

import Server.Cached.ECMAuthorizationCached;

public class HttpCacheManager extends Thread{
	
	private long curentNow = 0;
	private long sleepTime = 1 * 60 * 1000;
	 
    // 질의 메서드
    public HttpCacheManager() {
    	
    }
    
    /**
	 * ##############################################
	 *  AuthourizedCacheDocList's Each User Check (Each User TimeStamp 필요)
	 * ##############################################
	 */
    @Override
 	public void run()
    {
 		try {
    			while(true) 
    			{
    				curentNow = System.currentTimeMillis();
					HashMap<String, Object> CachedObject = ECMAuthorizationCached.getAuthourizedCacheDocListGroup();
					for( String key : CachedObject.keySet() ){
						if(!Thread.currentThread().getName().equals(key)) {
							Thread ServerThread = new Thread(new CachingThread(CachedObject, key), key);
	    					ServerThread.start();
	    				}
				    }
					
    				Thread.sleep(sleepTime);
    			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}


class CachingThread implements Runnable {
	Logger logger = Logger.getLogger(this.getClass());
	private String userAccessTime = null;
	private long cacheCleanupDurationTime = 1 * 1200 * 1000;
	private HashMap<String, ECMAuthorizationCached> ECMDocumentsThreadObject;
	
	@SuppressWarnings("unchecked")
	public CachingThread(HashMap<String, Object> object, String Key) {
		ECMDocumentsThreadObject = (HashMap<String, ECMAuthorizationCached>) object.get(Key);
	}
	
	/**
	 * ##############################################
	 *  AuthourizedCacheDocList's Each User Check (Each User TimeStamp 필요)
	 * ##############################################
	 */
    @Override
 	public void run()
    {
 		try {
				getShowCacheList(ECMDocumentsThreadObject);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
		
	/**
	 * Cache User Search List Show
	 */
	private void getShowCacheList(HashMap<String, ECMAuthorizationCached> CachedObject) {
		Iterator<String> keys = CachedObject.keySet().iterator();
		while( keys.hasNext() )	
		{ 
			 String key = keys.next(); 
//			 SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//			 userAccessTime = sdfNow.format(new Date(CachedObject.get(key).getUserAccessTime()));
			 
			 if(!CacheCleanupSleep(CachedObject, key)) {
//				 logger.info("[Caching Thread:" + Thread.currentThread().getName() + "] Total Size >> " + CachedObject.size());
//				 logger.info("[Caching Thread:" + Thread.currentThread().getName() + "] User >> " + key);
//				 logger.info("[Caching Thread:" + Thread.currentThread().getName() + "] User Access Time >> " + CachedObject.get(key).getUserAccessTime() + " [" + userAccessTime + "]");
//				 logger.info("[Caching Thread:" + Thread.currentThread().getName() + "] User CompanyCode >> " + CachedObject.get(key).getCompanyCode());
//				 System.out.print("[Caching Thread:" + Thread.currentThread().getName() + "] User Cache >> " + CachedObject.get(key).getXusername());
//				 System.out.print(" [" +  CachedObject.get(key).getUserResponseIDCacheArrayList().size() + "/" );
//				 System.out.print(" " + CachedObject.get(key).getHitTotalSearchResults() + "] >> \n\n");				 
			 }
	    } 
	}
	
	/**
	 * ##############################################
	 *  AuthourizedCacheDocList's Each User TimeStamp 체크
	 * ##############################################
	 */
	private boolean CacheCleanupSleep(HashMap<String, ECMAuthorizationCached> CachedObject, String key) {
    	boolean isTimeout = false;
        final long now = System.currentTimeMillis();
        final long currentCacheTime = now - CachedObject.get(key).getUserAccessTime();
        
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		if (currentCacheTime < 0 || currentCacheTime > cacheCleanupDurationTime) {
        	synchronized(ECMAuthorizationCached.AuthourizedCacheDocListGroup) {
        		userAccessTime = sdfNow.format(new Date(CachedObject.get(key).getUserAccessTime()));
        		logger.info("[Caching Thread:" + Thread.currentThread().getName() + "] Remove Object >> " + key + " [" + userAccessTime + "]");
        		ECMAuthorizationCached.AuthourizedCacheDocListGroup.remove(CachedObject.get(key).getCompanyCode());
        	}
        	
            isTimeout = true;
        }
        
        return isTimeout;
  }
}
