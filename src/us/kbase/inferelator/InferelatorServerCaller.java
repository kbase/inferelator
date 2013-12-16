package us.kbase.inferelator;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import us.kbase.auth.AuthToken;
import us.kbase.auth.TokenFormatException;
import us.kbase.common.service.JsonClientException;
import us.kbase.common.service.UnauthorizedException;
import us.kbase.userandjobstate.UserAndJobStateClient;

public class InferelatorServerCaller {

	private static final String JOB_SERVICE = "http://140.221.84.180:7083";
	private static UserAndJobStateClient _jobClient = null;
	private static boolean deployCluster = true;

	public static String findInteractionsWithInferelator(String wsId, InferelatorRunParameters params, AuthToken authPart) throws TokenFormatException, JsonClientException, IOException{
		String jobId = jobClient(authPart).createJob();
		
		if (deployCluster != true) {
			InferelatorServerThread thread = new InferelatorServerThread (jobId, wsId, params, authPart);
			thread.start();
		} else {
			//TODO call inferelator implementation on cluster
			
		}
		return jobId;
	}

	protected static UserAndJobStateClient jobClient(AuthToken token)
			throws TokenFormatException {
		if (_jobClient == null) {
			try {
				URL jobServiceUrl;
				jobServiceUrl = new URL(JOB_SERVICE);
				_jobClient = new UserAndJobStateClient(jobServiceUrl, token);
			} catch (MalformedURLException e) {
				System.err.println("Wrong URL" + JOB_SERVICE);
				e.printStackTrace();
			} catch (UnauthorizedException e) {
				System.err.println("Unable to authenticate in job service" + JOB_SERVICE);
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			_jobClient.setAuthAllowedForHttp(true);
		}
		return _jobClient;
	}
	
}
