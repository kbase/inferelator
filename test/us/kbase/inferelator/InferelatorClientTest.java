package us.kbase.inferelator;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import us.kbase.auth.AuthException;
import us.kbase.auth.AuthService;
import us.kbase.auth.AuthToken;
import us.kbase.common.service.JsonClientException;
import us.kbase.common.service.Tuple7;
import us.kbase.userandjobstate.Results;
import us.kbase.userandjobstate.UserAndJobStateClient;
import us.kbase.util.WsDeluxeUtil;
import us.kbase.workspace.ObjectData;
import us.kbase.workspace.ObjectIdentity;
import us.kbase.workspace.WorkspaceClient;

public class InferelatorClientTest {

	private static final String JOB_SERVICE = "https://kbase.us/services/userandjobstate";
	
	private static final String USER_NAME = "";
	private static final String PASSWORD = "";
	private static final String workspaceName = "AKtest";
	
//	private String serverUrl = "http://kbase.us/services/inferelator/";
//	private String serverUrl = "http://localhost:7081";
	private String serverUrl = "http://140.221.67.196:7113/";//this is a new enigma instance, old was "http://140.221.85.173:7079/";
	//private String genomeRef = "AKtest/Halobacterium_sp_NRC-1";

	private String testSeriesRef = "AKtest/Halobacterium_sp_expression_series";
	private String testCmonkeyRunResultRef = "AKtest/kb|cmonkeyrunresult.157";
	private String testTfListRef = "AKtest/Halobacterium_sp_TFs";

	@Test
	public void testFindInteractionsWithInferelator() throws AuthException, IOException, JsonClientException {
		AuthToken token = AuthService.login(USER_NAME, new String(PASSWORD)).getToken();
		InferelatorRunParameters params = new InferelatorRunParameters().withCmonkeyRunResultWsRef(testCmonkeyRunResultRef).withExpressionSeriesWsRef(testSeriesRef).withTfListWsRef(testTfListRef);
		URL url = new URL(serverUrl);
		InferelatorClient client = new InferelatorClient(url, token);
		client.setAuthAllowedForHttp(true);
		String jobId = client.runInferelator(workspaceName, params);
		System.out.println(jobId);
		assertNotNull(jobId);
		
		
		URL jobServiceUrl = new URL(JOB_SERVICE);
		UserAndJobStateClient jobClient = new UserAndJobStateClient(
				jobServiceUrl, token);

		String status = "";
		Integer waitingTime = 2;
		String resultId = null;
		
		while (!status.equalsIgnoreCase("finished")){
			
			try {
			    Thread.sleep(120000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		
			try {
				Tuple7<String,String,String,Long,String,Long,Long> t = jobClient.getJobStatus(jobId); 
				//System.out.println(t.getE1());
				//System.out.println(t.getE2());
				status = t.getE3();
				//System.out.println(t.getE3());//Status
				//System.out.println(t.getE4());
				//System.out.println(t.getE5());
				//System.out.println(t.getE6());
				//System.out.println(t.getE7());
				System.out.println("Waiting time: "+ waitingTime.toString() + " minutes");
				waitingTime += 2;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonClientException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			Results res = jobClient.getResults(jobId);			
			resultId = res.getWorkspaceids().get(0);
			System.out.println("Result ID = " + resultId);
			assertNotNull(resultId);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String[] resultIdParts = resultId.split("/");
		resultId = resultIdParts[1];

		WorkspaceClient wsClient = new WorkspaceClient(new URL ("https://kbase.us/services/ws"), new AuthToken(token.toString()));
		wsClient.setAuthAllowedForHttp(true);
		
		List<ObjectIdentity> objectsIdentity = new ArrayList<ObjectIdentity>();
		ObjectIdentity objectIdentity = new ObjectIdentity().withName(resultId).withWorkspace(workspaceName);
		objectsIdentity.add(objectIdentity);
		InferelatorRunResult result = wsClient.getObjects(objectsIdentity).get(0).getData().asClassInstance(InferelatorRunResult.class);

		
		assertNotNull(result.getHits().get(0));
		assertEquals(1659, result.getHits().size());
//		assertEquals("VNG1786", result.getHits().get(0).getTfId());
//		assertEquals(Double.valueOf("0.09119"), result.getHits().get(0).getCoeff());

		
	}


}
