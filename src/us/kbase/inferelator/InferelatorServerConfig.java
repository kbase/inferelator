package us.kbase.inferelator;

public class InferelatorServerConfig {
	
	
	//Deployment options
	protected static boolean DEPLOY_AWE = true;//false for running Inferelator locally;
	
	//Service credentials
	protected static final String SERVICE_LOGIN = "inferelator";
	protected static final String SERVICE_PASSWORD = "TridcatVosemPopugaev1976";
	
	//Service URLs
	protected static String JOB_SERVICE_URL; //prod: "https://kbase.us/services/userandjobstate";//dev:"http://140.221.84.180:7083";
	protected static String AWE_SERVICE_URL; //prod: "http://140.221.85.171:7080/job";
	protected static String ID_SERVICE_URL; //prod: "https://kbase.us/services/idserver";
	protected static String WS_SERVICE_URL; //prod: "https://kbase.us/services/ws";//dev: "http://140.221.84.209:7058";
	
	//Paths
	protected static final String JOB_DIRECTORY = "/var/tmp/inferelator/";
	protected static String INFERELATOR_DIRECTORY; // "/kb/runtime/cmonkey-python/inferelator/";
	protected static String INFERELATOR_RUN_PATH = INFERELATOR_DIRECTORY + "run_inf.R";
	protected static String AWF_CONFIG_FILE; // "/kb/deployment/services/inferelator/inferelator.awf";

	//Logging options
	
	//Writes all JSON calls to AWE client and all AWE responses to /var/tmp/inferelator/inferelator-awe.log
	//This is a serious security threat because log will contain all auth tokens
	//SET IT TO FALSE ON PRODUCTION  
	public static final boolean LOG_AWE_CALLS = false;
	
	//object type to save in workspace 
	protected static final String INFERELATOR_RUN_RESULT_TYPE = "Inferelator.InferelatorRunResult";
	
	public static String getWsUrl (){
		return WS_SERVICE_URL;
	}

	public static String getIdUrl (){
		return ID_SERVICE_URL;
	}

}
