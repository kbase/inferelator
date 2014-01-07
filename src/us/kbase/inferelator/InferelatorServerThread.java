package us.kbase.inferelator;

import us.kbase.auth.AuthToken;

public class InferelatorServerThread extends Thread {
	
	String wsName;
	InferelatorRunParameters params;
	String jobId;
	String token;
	AuthToken authPart;
	
	InferelatorServerThread (String jobId, String wsName, InferelatorRunParameters params, AuthToken authPart){
		this.wsName = wsName;
		this.params = params;
		this.jobId = jobId;
		this.authPart = authPart;
	}
	
	public void run (){
		try {
			InferelatorServerImpl.findInteractionsWithInferelator(jobId, wsName, params, authPart, null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
