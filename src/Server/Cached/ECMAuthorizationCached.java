package Server.Cached;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import Server.Utils.Base64Coder;

public class ECMAuthorizationCached {
	
	private int MaxAuthorizedThrethold=0;
	private int RequestDocumentumSize=50;
	private int CurrentPage=0;
	private int CurrentPageSize=0;
	private int CurrentDocumentumPage=0;
	private long userAccessTime = 0;
	
	private String Xusername = null;
	private String CompanyCode = null;
	private StringBuffer UserRequestQuery = null;
//	private StringBuffer UserRequestModifyQuery = null;

	
	/**
	 * 초기 사용자 요청 시 Object에 담징낳고
	 * 결과에 따라서 Set 처리됨 (PublicSearch HitTotal / 권한 문서에 대한 최종 문서리스트
 	*/
//	private StringBuffer UserResponseIDCacheList = new StringBuffer();;
//	private String[] UserResponseIDCacheListArray = new String[100000];;

	/**
	 * 사용자별 권한 문서에 대한 최종 문서리스트
 	*/
	private Map<String,String> UserResponseIDCacheArrayList, isUserDuplicatedCacheArrayList;
	
	/**
	 * 사용자별 Cache 문서 결과건수
 	*/
	private int HitTotalSearchResults=0;
	
	
	// User History Cached
	// Xusername, Authorized DocumentList
	// Memory Cached List >> Size >> 2
	// My Memory key >> pd292816
	// My Memory Cache >> pd292816 [14/18301] >> 
	// doc0900bf4b89a1221a,doc0900bf4b89a12d65,doc0900bf4b89a15957,doc0900bf4b89a0667b,doc0900bf4b89a148ad,doc0900bf4b89a11a42,doc0900bf4b89a152d9,doc0900bf4b89a0f3ea,doc0900bf4b89a152e8,doc0900bf4b89a0f3dd,doc0900bf4b89a1498a,doc0900bf4b899c1fd9,doc0900bf4b899c1fdb,doc0900bf4b89892c8b,
	// My Memory key >> bigben
	// My Memory Cache >> bigben [56/18301] >> 
	// doc0900bf4b89a148ad,doc0900bf4b89a0f3dd,doc0900bf4b89a0667b,doc0900bf4b89a1498a,doc0900bf4b89a11a42,doc0900bf4b899c1fd9,doc0900bf4b89a0f3ea,doc0900bf4b89a15957,doc0900bf4b899c1fdb,doc0900bf4b89a12d65,doc0900bf4b89a152d9,doc0900bf4b89a1221a,doc0900bf4b89a152e8,doc0900bf4b89892c8b,doc0900bf4b89a0f3dd,doc0900bf4b899c1fdb,doc0900bf4b899c1fd9,doc0900bf4b89a0667b,doc0900bf4b89a1221a,doc0900bf4b89a12d65,doc0900bf4b89a0eed7,doc0900bf4b89a152d9,doc0900bf4b89a0f3ea,doc0900bf4b89a15957,doc0900bf4b89a148ad,doc0900bf4b89a1498a,doc0900bf4b89a152e8,doc0900bf4b89a11a42,doc0900bf4b899c1fd9,doc0900bf4b89a15957,doc0900bf4b89a148ad,doc0900bf4b89a0f3dd,doc0900bf4b89a1498a,doc0900bf4b89a0667b,doc0900bf4b89a152d9,doc0900bf4b89a0f3ea,doc0900bf4b89a0eed7,doc0900bf4b89a1221a,doc0900bf4b89a11a42,doc0900bf4b899c1fdb,doc0900bf4b89a152e8,doc0900bf4b89a12d65,doc0900bf4b89a12d65,doc0900bf4b89a0667b,doc0900bf4b89a1221a,doc0900bf4b89a11a42,doc0900bf4b89a152e8,doc0900bf4b89a148ad,doc0900bf4b899c1fdb,doc0900bf4b89a0f3dd,doc0900bf4b89a0f3ea,doc0900bf4b89a0eed7,doc0900bf4b89a152d9,doc0900bf4b899c1fd9,doc0900bf4b89a1498a,doc0900bf4b89a15957,
	// getAuthourizedDoucmentList(AuthorObject.getCompanyCode()) Key<String> : SABUN,  ECMAuthorizationCached : Number, AuthorizedCacheList <> UserResponseIDCacheArrayList, isUserDuplicatedCacheArrayList:
	// => [30] UserResponseIDCacheArrayList, isUserDuplicatedCacheArrayList <pd292816, doc0900bf4b89a1498a,doc0900bf4b89a11a42,>
	// => [01] UserResponseIDCacheArrayList, isUserDuplicatedCacheArrayList <euiyoung, doc0900bf4b89a1498a,doc0900bf4b89a11a42,>
	//	public static HashMap<String, ECMAuthorizationCached> AuthourizedCacheDocList_30 = new HashMap<String, ECMAuthorizationCached>();	
	//	public static HashMap<String, ECMAuthorizationCached> AuthourizedCacheDocList_01 = new HashMap<String, ECMAuthorizationCached>();	

	private HashMap<String, ECMAuthorizationCached> AuthourizedCacheDocList = new HashMap<String, ECMAuthorizationCached>();	
	public static HashMap<String, Object> AuthourizedCacheDocListGroup = new HashMap<String, Object>();	
	
	Logger logger = Logger.getLogger(this.getClass());
	
	/**
	 *  ECMAuthorizationCached Constructor
 	*/	
	public ECMAuthorizationCached() {

	}
	
	
	/**
	 *  ECMAuthorizationCached Memory Object Constructor
 	*/	
	public ECMAuthorizationCached(String XUsername, String CompanyCode, StringBuffer UserRequestQuery, Map<String,String> UserResponseIDCacheArrayList, int CurrentPage, int CurrentPageSize ) {
		this.Xusername = XUsername;
		this.CompanyCode = CompanyCode;
		this.UserRequestQuery = UserRequestQuery;
		this.CurrentPage = CurrentPage;
		this.CurrentPageSize = CurrentPageSize;
		this.MaxAuthorizedThrethold = (CurrentPage*CurrentPageSize)+CurrentPageSize;
	}

	
	/**
	 * User access time
	 */
	public long getUserAccessTime() {
		return userAccessTime;
	}


	/**
	 * User access time
	 */
	public void setUserAccessTime(long userAccessTime) {
		this.userAccessTime = userAccessTime;
	}
	
	
	/**
	 * Page
	 * @return int
	 */
	public void setCurrentPage(int currentPage) {
		CurrentPage = currentPage;
	}


	/**
	 * Documentum Cache Size 자동 채움을 위해 ElatsicSearch로 from size 조절을 위한 함수 및 변수
	 * @return int
	 */
	public int getCurrentDocumentumPage() {
		return CurrentDocumentumPage;
	}

	

	/**
	 * Documentum Cache Size 자동 채움을 위해 ElatsicSearch로 from size 조절을 위한 함수 및 변수
	 * @return int
	 */
	public void setCurrentDocumentumPage(int currentDocumentumPage) {
		CurrentDocumentumPage = currentDocumentumPage;
	}


	/**
	 * 결과에 따라서 Set 처리됨 (PublicSearch HitTotal)
	 * 향후 권한 체크시 Public Search 전체 카운트 기준으로 loop 처리되어야 함
 	 * @return int
	 */
	public int getHitTotalSearchResults() {
		return HitTotalSearchResults;
	}


	/**
	 * 결과에 따라서 Set 처리됨 (PublicSearch HitTotal)
	 * 향후 권한 체크시 Public Search 전체 카운트 기준으로 loop 처리되어야 함
 	 * @return int
	 */
	public void setHitTotalSearchResults(int hitTotalSearchResults) {
		HitTotalSearchResults = hitTotalSearchResults;
	}


	/**
	 * 현재 캐시에 남아 있는 값 비교
	 * 비교 시 사용자 캐시가 모자랄 경우 Documentum에 가야될지 결정 변수
	 * 
	 * 29개 
	 * Case1) Sequential User Paging
	 * 1Page >> 0*15 + 15 = (MaxAuthorizedThrethold) = 15
	 * 2Page >> 1*15 + 15 = (MaxAuthorizedThrethold) = 30 => MaxAuthorizedThrethold <= My MemoryCache Size -> OK, else -> Documentum Server Request (How Many Size?)
	 * 
	 * Case2) Random User Paging
	 * 1Page >> 0*15 + 15 = (MaxAuthorizedThrethold) = 15
	 * ...
	 * ...
	 * 7Page >> 6*15 + 15 = (MaxAuthorizedThrethold) = 105 => MaxAuthorizedThrethold <= My MemoryCache Size -> OK, else -> Documentum Server Request (How Many Size?)
	 * 
	 * => How Many Size : RequestDocumentumSize Caching Loop (Documentum) Until MaxAuthorizedThrethold+15
	 * 
 	 * @return int
	 */	
	public int getMaxAuthorizedThrethold(ECMAuthorizationCached AuthorObject) {
		logger.info("getMaxAuthorizedThrethold1 >> " + AuthorObject.CurrentPage + "," + AuthorObject.CurrentPageSize);
		MaxAuthorizedThrethold = (AuthorObject.CurrentPage*AuthorObject.CurrentPageSize)+AuthorObject.CurrentPageSize;
//		MaxAuthorizedThrethold = ((AuthorObject.CurrentPage+1)*AuthorObject.CurrentPageSize)+1;
//		MaxAuthorizedThrethold = (AuthorObject.CurrentPage*AuthorObject.CurrentPageSize);
		logger.info("getMaxAuthorizedThrethold2 >> " + MaxAuthorizedThrethold);
		return MaxAuthorizedThrethold;
	}

	
	/**
	 * 현재 요청된 Page Num
 	 * @return int
	 */
	public int getCurrentPage() {
		return CurrentPage;
	}


	/**
	 * 현재 요청된 PageSize
 	 * @return int
	 */
	public int getCurrentPageSize() {
		return CurrentPageSize;
	}

	
	/**
	 * Documentum 요청 시 문서 요청수 Threthold
 	 * @return Boolean
	 */
	public int getRequestDocumentumSize() {
		return RequestDocumentumSize;
	}

	
	/**
	 * Documentum 요청 시 문서 요청수 설정
     * @param Xusername
	 * @return Boolean
	 */
	public void setRequestDocumentumSize(int requestDocumentumSize) {
		RequestDocumentumSize = requestDocumentumSize;
	}
	
	
	/**
	 * Cache String Authorized Result String
	 */
	public Map<String, String> getUserResponseIDCacheArrayList() {
		return UserResponseIDCacheArrayList;
	}


	/**
	 * Cache String Authorized Result String
	 */
	public void setUserResponseIDCacheArrayList(
			Map<String, String> userResponseIDCacheArrayList) {
		UserResponseIDCacheArrayList = userResponseIDCacheArrayList;
	}

	

	public Map<String, String> getIsUserDuplicatedCacheArrayList() {
		return isUserDuplicatedCacheArrayList;
	}



	/**
	 * Cache String Authorized Result String
	 */
	public void setIsUserDuplicatedCacheArrayList(
			Map<String, String> isUserDuplicatedCacheArrayList) {
		this.isUserDuplicatedCacheArrayList = isUserDuplicatedCacheArrayList;
	}


	/**
	 * Cache에 해당 사용자 권한 문서리스트가 존재하는지 비교
     * @param Xusername
	 * @return Boolean
	 */
	public Boolean getisUserCached(ECMAuthorizationCached AuthorObject, String Xusername) {
		boolean isExists = false;
		
		if(getAuthourizedDoucmentList(AuthorObject.getCompanyCode()).get(Xusername) != null)
			isExists = true;
		
		return isExists;
	}
	
	
	/**
	 * Cache에 해당 사용자 권한 문서리스트가 존재하는지 비교하여
	 * 해당 쿼리를 Return
	 * @param Xusername
	 * @return String
	 */
	public String getUserPreviousRequestQuery(ECMAuthorizationCached AuthorObject, String Xusername) {
		
		if(getAuthourizedDoucmentList(AuthorObject.getCompanyCode()).get(Xusername) != null) {
				return getAuthourizedDoucmentList(AuthorObject.getCompanyCode()).get(Xusername).getUserRequestQuery();
		}
		
		return null;
	}
	

	/**
	 * Cache에 해당 사용자 및 동일한 쿼리가 존재하는지 비교
	 * @param ECMAuthorizationCached AuthorObject
	 * @return Boolean
	 */
	public boolean isAvailableCached(ECMAuthorizationCached AuthorObject) {
		
		logger.info("isAvailableCached AuthorObject.getCurrentPage() >> " +  AuthorObject.getCurrentPage());
		
		if(getAuthourizedDoucmentList(AuthorObject.getCompanyCode()).get(AuthorObject.getXusername()) != null) {
			
//			logger.info("getAuthourizedDoucmentList(AuthorObject.getCompanyCode()).get(AuthorObject.getXusername()).getUserRequestQuery() >> " + getAuthourizedDoucmentList(AuthorObject.getCompanyCode()).get(AuthorObject.getXusername()).getUserRequestQuery());
//			logger.info("AuthorObject.getUserRequestQuery() >> " + AuthorObject.getUserRequestQuery());
			
			if(Base64Coder.encodeString(getAuthourizedDoucmentList(AuthorObject.getCompanyCode()).get(AuthorObject.getXusername()).getUserRequestQuery().replace(" ", "").replace("\"from\":0", ""))
					 .equals(Base64Coder.encodeString(AuthorObject.getUserRequestQuery().replace(" ", "").replace("\"from\":" + AuthorObject.getCurrentPage()*AuthorObject.getCurrentPageSize(), "")))) {
				return true;
			}
			else {
				// 기존 사용자 권한 검색 리스트가 있고 이전 쿼리와 현재 쿼리가 같은 경우
				// 사용자 리스트를 초기화
				RemoveUserRequestObject(AuthorObject);
			}
		}
				
		return false;
	}
	
	
	/**
	 * Cache User Search ArrayList Synchronized
	 * 키워드가 달라지만 캐시 삭제
	 * @param ECMAuthorizationCached AuthorObject, String PermitDocIDList
	 * @return synchronized void
	 */
	public synchronized void RemoveUserRequestObject(ECMAuthorizationCached AuthorObject) {
		logger.info(" #### Before RemoveUserRequestObject >> " + AuthorObject.getXusername());
		
		if(getAuthourizedDoucmentList(AuthorObject.getCompanyCode()).get(AuthorObject.getXusername()) != null 
				&&	(!Base64Coder.encodeString(getAuthourizedDoucmentList(AuthorObject.getCompanyCode()).get(AuthorObject.getXusername()).getUserRequestQuery().replace(" ", "").replace("\"from\":" + AuthorObject.getCurrentPage(), ""))
					 .equals(Base64Coder.encodeString(AuthorObject.getUserRequestQuery().replace(" ", "").replace("\"from\":" + AuthorObject.getCurrentPage(), ""))))) {
			
			logger.info(" #### After RemoveUserRequestObject >> " + AuthorObject.getXusername());
			synchronized(getAuthourizedDoucmentList(AuthorObject.getCompanyCode())) {
				getAuthourizedDoucmentList(AuthorObject.getCompanyCode()).remove(AuthorObject.Xusername);
			}
		}
	}
	
	/**
	 * Cache User Search ArrayList Synchronized
	 * 향후, 캐시 timeout으로 일정 시간이 지나서 삭제가 될 경우에 동기화 Lock이 필요
	 * @param ECMAuthorizationCached AuthorObject, String PermitDocIDList
	 * @return synchronized void
	 */
	public synchronized void InsertUserRequestObject(ECMAuthorizationCached AuthorObject, String PermitDocIDList) {
		
		logger.info("InsertUserRequestObject [" + AuthorObject.Xusername + "] >> " + PermitDocIDList);
		
		if(PermitDocIDList != null || PermitDocIDList != "") {
			if(AuthorObject.getXusername() != null) {
				if(PermitDocIDList != null && PermitDocIDList.contains("doc")) {
					synchronized(AuthorObject) {
						synchronized(getAuthourizedDoucmentList(AuthorObject.getCompanyCode())) {
							AuthorObject.setUserResponseIDList(AuthorObject,PermitDocIDList);
							logger.info("####### InsertUserRequestObject:getAuthourizedDoucmentList().put >> " + AuthorObject.Xusername + " >> AuthorObject.Size : " + AuthorObject.getUserResponseIDCacheArrayList().size());							
							getAuthourizedDoucmentList(AuthorObject.getCompanyCode()).put(AuthorObject.Xusername, AuthorObject);
//							AuthourizedCacheDocList.put(AuthorObject.Xusername, AuthorObject);
//							AuthourizedCacheDocListGroup.put(AuthorObject.getCompanyCode(), AuthourizedCacheDocList);
						}
					}	
				}
			}
		}
	}
	
	/**
	 * Cache User Search List Show
	 */
	public void getShowCacheList(ECMAuthorizationCached AuthorObject) {
		logger.info("Memory Cached List >> CompanyCode >> " + AuthorObject.getCompanyCode());
		logger.info("Memory Cached List >> Size >> " + getAuthourizedDoucmentList(AuthorObject.getCompanyCode()).size());
		
		Iterator<String> keys = getAuthourizedDoucmentList(AuthorObject.getCompanyCode()).keySet().iterator();
		while( keys.hasNext() )	
		{ 
			 String key = keys.next(); 
			 logger.info("My Memory key >> " + key);
			 System.out.print("My Memory Cache >> " + getAuthourizedDoucmentList(AuthorObject.getCompanyCode()).get(key).getXusername());
			 System.out.print(" [" +  getAuthourizedDoucmentList(AuthorObject.getCompanyCode()).get(key).getUserResponseIDCacheArrayList().size() + "/" );
			 System.out.print(getAuthourizedDoucmentList(AuthorObject.getCompanyCode()).get(key).getHitTotalSearchResults() + "] >> \n");
			 logger.info(PrintResultMapInfo(AuthorObject, key));
	    } 
	}
	
	/**
	 * Cache에 해당 사용자 권한 문서리스트가 존재하는지 비교하여
	 * 해당 페이지 및 PageSize에 맞는 권한 체크된 문서리스트를 Return
	 * @param Xusername
	 * @return String
	 */
	public String getMemoryMyCachedList(ECMAuthorizationCached AuthorObject, String Xusername, int CurrentPage, int PageSize) {
		
		// getUserResponseIDList() : doc0900bf4b89a0a3ca,doc0900bf4b89a0d048,doc0900bf4b89a08bd0,doc0900bf4b89a0bbe4,doc0900bf4b89a0be6d,doc0900bf4b89a11eba,doc0900bf4b8bbb5d30,doc0900bf4b8bc42b58,doc0900bf4b89a0be77,doc0900bf4b8af6c3b3,doc0900bf4b89a11a42,doc0900bf4b89a0de1d,doc0900bf4b89a15ed6,doc0900bf4b89a0ea4a,doc0900bf4b89a0e822,doc0900bf4b89a06f55,doc0900bf4b89a0a3cf,
//		Memory getMemoryMyCachedList >> 1
//		Memory CurrentPage >> 1
//		Memory CurrentPageSize >> 15
//		Memory Next PageSize >> 30.0
//		MyCachedArrayList [15] >> doc0900bf4b89a08bd0
//		16
//		null
		
//       최졍결과에서 from 파라티터 제거 필요
//		 {"fields":["KEY","TITLE","TARGET_URL","MAIL_ID","SECURITY_LEVEL","USER_NO","USER_NAME","INPUTDATE","UPDATED_USER_NO","UPDATED_DATE","COMPANY_CODE","COMPANY_NAME","DEPT_CODE","DEPT_NAME","SYSTEM_SUB_CODE","SYSTEM_SUB_NAME","VERSION","USER_INFO","SYSTEM_TAG","EXPT_RCMD_CNT","GNR_RCMD_CNT","SUM_RCMD_CNT","SORT_RCMD","EXPT_YN","FILE_FORMAT","FILE_SIZE","DATE_CREATE","DATE_MODIFY","COWORKER_ID","ECM_CONTENT_SOURCE","ECM_IS_PRIDE","ECM_FOLDER_ID","ECM_DOC_TYPE","ECM_OPEN_FLAG","DEPT_CODE_PATH","DEPT_NAME_PATH"],"query":{"filtered":{"query":{"bool":{"must":[{"bool":{"should":[{"terms":{"KEY":["doc0900bf4b89a11eba"]}}]}},{"bool":{"should":[{"query_string":{"query":"제선","default_operator":"OR","fields":["TITLE^3.0","CONTENT"]}}]}}]}}}},"sort":[{"INPUTDATE":{"order":"desc"}}],"from":1,"highlight":{"require_field_match":true,"pre_tags":["<b>"],"post_tags":["</b>"],"fields":{"TITLE":{},"CONTENT":{"number_of_fragments":3,"fragment_size":120}}},"fielddata_fields":["CONTENT.truncated"],"size":15}
		
//		StringBuffer buffers = new StringBuffer();

		logger.info("User >> " + Xusername);
		logger.info("Memory getMemoryMyCachedList >> " + getAuthourizedDoucmentList(AuthorObject.getCompanyCode()).size());
		
		logger.info("Memory CurrentPage >> " + CurrentPage);
		logger.info("Memory CurrentPageSize >> " + PageSize);
		logger.info("Memory Next PageSize >> " + Double.parseDouble(String.valueOf((CurrentPage/PageSize)*(PageSize) + PageSize)));
		logger.info("getMemoryMyCachedList >> ");
	 	
		String PageDocList="";
	
		try {
			if(getAuthourizedDoucmentList(AuthorObject.getCompanyCode()).get(Xusername) != null) {
//				logger.info("String[] MyCachedArrayList = PrintResultMapInfo(Xusername).split(\",\"); >> " + PrintResultMapInfo(AuthorObject, Xusername));
				String[] MyCachedArrayList = PrintResultMapInfo(AuthorObject, Xusername).split(",");
				if(MyCachedArrayList != null) {
					// CurrentPage  0*(15-1) => 0~14, 1*(15-1) 1*(15-1)+15.. 
					for(int j=CurrentPage*PageSize; j < (CurrentPage*PageSize) + PageSize; j++) {
						if(MyCachedArrayList[j] != null) {
							logger.info("MyCachedArrayList [" + j + "] >> " + MyCachedArrayList[j]);
							PageDocList += MyCachedArrayList[j] + ",";	
						}
						else {
							break;
						}
					}
				}
			}
		}
		catch(Exception ex) {
			logger.info(ex.getMessage());
		}
		
		logger.info("################ getMemoryMyCachedList Array...~~~ " + PageDocList);
		
		return PageDocList;
	}
	
	
		
	
	/**
	 * Cache User Search List Show
	 */
	public Integer getMyCacheSize(ECMAuthorizationCached AuthorObject, String Xusername) {
		return getAuthourizedDoucmentList(AuthorObject.getCompanyCode()).get(Xusername).getUserResponseIDCacheArrayList().size();
//		return getAuthourizedDoucmentList(AuthorObject.getCompanyCode()).get(Xusername).getUserResponseIDList().split(",").length;
	}
	
	
	

	/**
	 * Cache String Authorized Result Set/Insert Cache
	 * 2016.05.19
	 */
	public void setUserResponseIDList(ECMAuthorizationCached AuthorObject, String userResponseIDList) 
	{
		// Each User's Authorized Document Set/Insert -> 각 사용자 Object가 없다면 Object 생성 후 사용자별 권한 문서 Cache
		// Set/Insert
		if(getAuthourizedDoucmentList(AuthorObject.getCompanyCode()).get(AuthorObject.getXusername()) == null) {
			
			// 사용자별 문서ID 중복 체크 리스트
			isUserDuplicatedCacheArrayList = new LinkedHashMap<String,String>();
			AuthorObject.setIsUserDuplicatedCacheArrayList(isUserDuplicatedCacheArrayList);
			
			// 사용자별 권한문서 리스트
			UserResponseIDCacheArrayList = new LinkedHashMap<String,String>();
			AuthorObject.setUserResponseIDCacheArrayList(UserResponseIDCacheArrayList);
			
			AuthorObject.setUserAccessTime(System.currentTimeMillis());
			
			logger.info("AuthorObject CacheList" + AuthorObject.getUserResponseIDCacheArrayList().size());
			getAuthourizedDoucmentList(AuthorObject.getCompanyCode()).put(AuthorObject.getXusername(), AuthorObject);
		}
		
		logger.info("setUserResponseIDList2");
		/**
		 * Cache String Authorized Result Insert
		 */
		int InitLoop = 0;
		if(getAuthourizedDoucmentList(AuthorObject.getCompanyCode()).get(AuthorObject.getXusername()).getUserResponseIDCacheArrayList() != null)
			InitLoop = getAuthourizedDoucmentList(AuthorObject.getCompanyCode()).get(AuthorObject.getXusername()).getUserResponseIDCacheArrayList().size(); 
		
		logger.info("setUserResponseIDList >> InitLoop >> " + InitLoop);
		
		// isUserDuplicatedCacheArrayList
		for(int i=0; i < userResponseIDList.split(",").length; i++) {
			if(getAuthourizedDoucmentList(AuthorObject.getCompanyCode()).get(AuthorObject.getXusername()).getIsUserDuplicatedCacheArrayList().get(userResponseIDList.split(",")[i]) == null) {
				getAuthourizedDoucmentList(AuthorObject.getCompanyCode()).get(AuthorObject.getXusername()).getIsUserDuplicatedCacheArrayList().put(userResponseIDList.split(",")[i], userResponseIDList.split(",")[i] + ",");
				getAuthourizedDoucmentList(AuthorObject.getCompanyCode()).get(AuthorObject.getXusername()).getUserResponseIDCacheArrayList().put(String.valueOf(InitLoop++), userResponseIDList.split(",")[i] + ",");	
			}
		}
		
		logger.info("\n\n");
		logger.info("#############################################");
		logger.info("#############################################");
		logger.info("All User's Cached AuthorObject.getShowCacheList()");
		// All User Cached List
		AuthorObject.getShowCacheList(AuthorObject);
		logger.info("#############################################");
		logger.info("#############################################");
		logger.info("\n\n");
	}
	
	
	/**
	 * Cache String Authorized Result String Print
	 * 2016.05.18
	 */
	public String PrintResultMapInfo(ECMAuthorizationCached AuthorObject, String Xusername) {
		StringBuffer ResultMapInfo = new StringBuffer();
		
		Iterator<String> keys = getAuthourizedDoucmentList(AuthorObject.getCompanyCode()).get(Xusername).getUserResponseIDCacheArrayList().keySet().iterator();
		while( keys.hasNext() )
		{ 
			 String key = (String) keys.next();
//		        System.out.print(Xusername + " ["+ key + "] >> ");
	         ResultMapInfo.append(getAuthourizedDoucmentList(AuthorObject.getCompanyCode()).get(Xusername).getUserResponseIDCacheArrayList().get(key));
		}
		
		return ResultMapInfo.toString();
		
	}

	
	
	/**
	 * POSCO, Fmaily User Group
	 * String : Company Code
	 * Object : HashMap<String, ECMAuthorizationCached> AuthourizedCacheDocList = new HashMap<String, ECMAuthorizationCached>();
	 * 
	 * 2016.07.06
	 */
	public static HashMap<String, Object> getAuthourizedCacheDocListGroup() {
		return AuthourizedCacheDocListGroup;
	}

	/**
	 * POSCO, Fmaily User Group
	 * String : Company Code
	 * Object : HashMap<String, ECMAuthorizationCached> AuthourizedCacheDocList = new HashMap<String, ECMAuthorizationCached>();
	 * 
	 * 2016.07.06
	 */
	public void setAuthourizedCacheDocListGroup(
			HashMap<String, Object> authourizedCacheDocListGroup) {
		AuthourizedCacheDocListGroup = authourizedCacheDocListGroup;
	}


	/**
	 * String Key : pd292816
	 * Object : HashMap<String, ECMAuthorizationCached> AuthourizedCacheDocList = new HashMap<String, ECMAuthorizationCached>();
	 * 2016.05.18
	 */
	@SuppressWarnings("unchecked")
	public HashMap<String, ECMAuthorizationCached> getAuthourizedDoucmentList(String CompanyCode) {
		if(getAuthourizedCacheDocListGroup().get(CompanyCode) == null) {
			getAuthourizedCacheDocListGroup().put(CompanyCode, AuthourizedCacheDocList);
		}
		return (HashMap<String, ECMAuthorizationCached>) getAuthourizedCacheDocListGroup().get(CompanyCode);
	}

	/**
	 * User Query
	 * 2016.05.18
	 */
	public String getUserRequestQuery() {
		return UserRequestQuery.toString();
	}

	/**
	 * User Query
	 * 2016.05.18
	 */
	public void setUserRequestQuery(StringBuffer userRequestQuery) {
		UserRequestQuery = userRequestQuery;
	}

	public String getXusername() {
		return Xusername;
	}

	public void setXusername(String xusername) {
		Xusername = xusername;
	}

	public String getCompanyCode() {
		return CompanyCode;
	}

	public void setCompanyCode(String companyCode) {
		CompanyCode = companyCode;
	}
}
