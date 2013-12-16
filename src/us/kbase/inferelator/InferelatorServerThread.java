package us.kbase.inferelator;

import us.kbase.auth.AuthToken;

public class InferelatorServerThread extends Thread {
	
	String wsId;
	InferelatorRunParameters params;
	String jobId;
	String token;
	AuthToken authPart;
	
	InferelatorServerThread (String jobId, String wsId, InferelatorRunParameters params, AuthToken authPart){
		this.wsId = wsId;
		this.params = params;
		this.jobId = jobId;
		this.authPart = authPart;
	}
	
	public void run (){
		try {
			InferelatorServerImpl.findInteractionsWithInferelator(jobId, wsId, params, authPart);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
