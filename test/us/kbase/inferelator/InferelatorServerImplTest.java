package us.kbase.inferelator;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import us.kbase.auth.AuthException;
import us.kbase.auth.AuthService;
import us.kbase.auth.AuthToken;
import us.kbase.auth.TokenFormatException;
import us.kbase.cmonkey.CmonkeyCluster;
import us.kbase.cmonkey.CmonkeyRunResult;
import us.kbase.common.service.JsonClientException;
import us.kbase.common.service.Tuple11;
import us.kbase.common.service.Tuple7;
import us.kbase.common.service.UObject;
import us.kbase.common.service.UnauthorizedException;
import us.kbase.userandjobstate.Results;
import us.kbase.userandjobstate.UserAndJobStateClient;
import us.kbase.util.DataImporter;
import us.kbase.util.WsDeluxeUtil;
import us.kbase.workspace.GetModuleInfoParams;
import us.kbase.workspace.ListModulesParams;
import us.kbase.workspace.ListObjectsParams;
import us.kbase.workspace.ModuleInfo;
import us.kbase.workspace.ObjectData;
import us.kbase.workspace.ObjectIdentity;
import us.kbase.workspace.RegisterTypespecParams;
import us.kbase.workspace.WorkspaceClient;


public class InferelatorServerImplTest {

	private static final String JOB_SERVICE = InferelatorServerConfig.JOB_SERVICE_URL;

	private static final String USER_NAME = "";
	private static final String PASSWORD = "";
	private static final String workspaceName = "ENIGMA_KBASE";
	private String genomeRef = "ENIGMA_KBASE/Desulfovibrio_vulgaris_Hildenborough";//"ENIGMA_KBASE/Halobacterium_sp_NRC-1";//
	private String testSeriesRef = "AKtest/Halobacterium_sp_expression_series";
	private String testCmonkeyRunResultRef = "AKtest/kb|cmonkeyrunresult.132";
	private String testTfListRef = "AKtest/kb|genelist.5";
	
	private static WorkspaceClient _wsClient = null;
	private static final String WS_SERVICE_URL = InferelatorServerConfig.WS_SERVICE_URL;
	protected static WorkspaceClient wsClient(String token) throws TokenFormatException, UnauthorizedException, IOException{
		if(_wsClient == null)
		{
			URL workspaceClientUrl = new URL (WS_SERVICE_URL);
			AuthToken authToken = new AuthToken(token);
			_wsClient = new WorkspaceClient(workspaceClientUrl, authToken);
			_wsClient.setAuthAllowedForHttp(true);
		}
		return _wsClient;
	} 

	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		fail("Not yet implemented");
	}
	
	@Test
	public final void testCurrentDir() throws Exception {
		String currentDir = System.getProperty("user.dir");
		System.out.println(currentDir);
		assertNotNull(currentDir);
	}

	@Test
	public final void testParseInferelatorOutput() throws Exception {
		AuthToken token = AuthService.login(USER_NAME, new String(PASSWORD)).getToken();
		String testFile = "test/outfile";//"/home/kbase/Documents/dvh/dvh-inferelator.json";//
		CmonkeyRunResult cmonkeyRunResult = WsDeluxeUtil.getObjectFromWsByRef(testCmonkeyRunResultRef, token.toString()).getData().asClassInstance(CmonkeyRunResult.class);
		HashMap<String, String> clusterIds = new HashMap<String, String>();
		Integer clusterNumber = 1;
		for (int i = 0; i < cmonkeyRunResult.getNetwork().getClusters().size(); i++){
			CmonkeyCluster cluster =  cmonkeyRunResult.getNetwork().getClusters().get(i);
			clusterIds.put(clusterNumber.toString(), cluster.getId());
			clusterNumber++;
		}
		
		InferelatorRunResult result = InferelatorServerImpl.parseInferelatorOutput(testFile, clusterIds);
/*		result.setParams(new InferelatorRunParameters().withCmonkeyRunResultWsRef(testCmonkeyRunResultRef).withExpressionSeriesWsRef(testSeriesRef).withTfListWsRef(testTfListRef));
		result.setOrganism("Desulfovibrio vulgaris str. Hildenborough");
		WsDeluxeUtil.saveObjectToWorkspace(UObject.transformObjectToObject(result, UObject.class), "Inferelator.InferelatorRunResult", "AKtest", "D_vulgaris_Hildenborough_inferelator_result", token.toString());
*/
		//System.out.println(result.getInteractions().size());
		assertNotNull(result);
		assertNotNull(result.getHits().get(0));
		assertEquals("kb|cmonkeycluster.3620", result.getHits().get(0).getBiclusterId());
		assertEquals(1629, result.getHits().size());
		assertEquals("kb|feature.4272", result.getHits().get(0).getTfId());
		assertEquals(Double.valueOf("0.12954"), result.getHits().get(0).getCoeff());
	}

	@Test
	public final void testWriteClusterStack() throws Exception {
		AuthToken token = AuthService.login(USER_NAME, new String(PASSWORD)).getToken();
		//System.out.println(token.toString());
		String name = "AKtest/kb|cmonkeyrunresult.122";
		CmonkeyRunResult cmonkeyRunResult = WsDeluxeUtil.getObjectFromWsByRef(name, token.toString()).getData().asClassInstance(CmonkeyRunResult.class);
		InferelatorServerImpl.writeClusterStack("test/", cmonkeyRunResult);
	}

	@Test
	public void testWsRegisterType() throws Exception {
		
		AuthToken token = AuthService.login(USER_NAME, new String(PASSWORD)).getToken();
		RegisterTypespecParams params = new RegisterTypespecParams();
		String specFileName = "/home/kbase/dev_container/modules/inferelator/kbase_inferelator.spec";
		String spec = "";
		BufferedReader br = null;
		try {
			String line = null;
			br = new BufferedReader(new FileReader(specFileName));
			while ((line = br.readLine()) != null) {
				spec += line + "\n";
			}
		} catch (IOException e) {
			System.out.println(specFileName + "read error\n" + e.getLocalizedMessage());
		} finally {
			if (br != null) {
				br.close();
			}
		}
		params.setSpec(spec);

		params.setMod("Inferelator");
		List<String> types = new ArrayList<String>();
		types.add("GeneList");
		types.add("InferelatorRunResult");

		params.setNewTypes(types);
		Map<String,String> result = WsDeluxeUtil.wsClient(token.toString()).registerTypespec(params);
		System.out.println(result.toString());
		assertNotNull(result);
		
	}

	@Test
	public void testWsListType() throws Exception {
		
		AuthToken token = AuthService.login(USER_NAME, new String(PASSWORD)).getToken();
		ListModulesParams params = new ListModulesParams(); 
		params.setOwner("");
		List<String> modulenames = WsDeluxeUtil.wsClient(token.toString()).listModules(params);
		for(String modulename: modulenames){
			System.out.println(modulename);
		}
		assertNotNull(modulenames);
		assertEquals(modulenames.get(0), "Cmonkey");
		
	}

	@Test
	public void testWsModuleInfo() throws Exception {
		
		AuthToken token = AuthService.login("", new String("")).getToken();
		GetModuleInfoParams params = new GetModuleInfoParams(); 
		params.setMod("Sequences");
		ModuleInfo moduleInfo = WsDeluxeUtil.wsClient(token.toString()).getModuleInfo(params);
		System.out.println("Description " + moduleInfo.getDescription());
		System.out.println("Owners " + moduleInfo.getOwners().toString());
		System.out.println("Spec " + moduleInfo.getSpec());
		System.out.println("Functions " + moduleInfo.getFunctions().toString());
		System.out.println("Versions " + moduleInfo.getVer());
		System.out.println("Types " + moduleInfo.getTypes().toString());
		
		assertNotNull(moduleInfo);
	}

	@Test
	public void testImportGeneList() throws Exception {
		
		AuthToken token = AuthService.login(USER_NAME, new String(PASSWORD)).getToken();
		String fileName = "/home/kbase/Documents/dvh/dvh-tflist.txt";//"test/tfsfile.txt";//
		GeneList geneList = DataImporter.importGeneList(fileName, genomeRef, token.toString());
		for (String gene: geneList.getGenes()){
			System.out.println(gene);
			
		}
		WsDeluxeUtil.saveObjectToWorkspace(UObject.transformObjectToObject(
				geneList, UObject.class), "Inferelator.GeneList", workspaceName, "D_vulgaris_Hildenborough_TFs", token.toString());
		assertNotNull(geneList);
	}

/*	@Test
	public void testWsTypeInfo() throws Exception {
		
		AuthToken token = AuthService.login(ADMIN_USER_NAME, new String(ADMIN_PASSWORD)).getToken();
		String type = "InferelatorRunResult"; 
		TypeInfo typeInfo = InferelatorServerImpl.wsClient(token.toString()).getTypeInfo(type);
		System.out.println("Description " + typeInfo.getDescription());
		System.out.println("Type definition from spec " + typeInfo.getSpecDef());
		System.out.println("Type definition " + typeInfo.getTypeDef());
		System.out.println("Module versions " + typeInfo.getModuleVers().toString());
		System.out.println("Type versions " + typeInfo.getTypeVers().toString());
		System.out.println("Used types " + typeInfo.getUsedTypeDefs().toString());
		System.out.println("Using functions " + typeInfo.getUsingFuncDefs().toString());
		System.out.println("Using types " + typeInfo.getUsingTypeDefs().toString());
		
		assertNotNull(typeInfo);
	}	
*/	
	/*@Test
	public void testWsCreate() throws Exception {
		
		AuthToken token = AuthService.login(USER_NAME, new String(PASSWORD)).getToken();
		CreateWorkspaceParams params = new CreateWorkspaceParams(); 
		params.setWorkspace("AKtest");
		Tuple8<Long, String, String, String, Long, String, String, String> result = InferelatorServerImpl.wsClient(token.toString()).createWorkspace(params);
		System.out.println(result.getE1());
		System.out.println(result.getE2());
		System.out.println(result.getE3());
		System.out.println(result.getE4());
		System.out.println(result.getE5());
		System.out.println(result.getE6());
		System.out.println(result.getE7());
		System.out.println(result.getE8());
		assertNotNull(result);
		
	}*/


	@Test
	public final void testWriteInputTable() throws Exception {
		AuthToken token = AuthService.login(USER_NAME, new String(PASSWORD)).getToken();
		InferelatorRunParameters params = new InferelatorRunParameters().withExpressionSeriesWsRef(testSeriesRef);
		InferelatorServerImpl.writeExpressionTable("test/1/", params, genomeRef, token.toString());
		assertTrue(new File("test/1/ratios.tsv").exists());
	}

	@Test
	public final void testWriteTfList() throws AuthException, IOException, JsonClientException {
		AuthToken token = AuthService.login(USER_NAME, new String(PASSWORD)).getToken();
		InferelatorRunParameters params = new InferelatorRunParameters().withTfListWsRef(testTfListRef);
		String organism = InferelatorServerImpl.writeTfList("test/1/", params, token.toString());
		System.out.println(organism);
		assertTrue(new File("test/1/tflist.txt").exists());
	}

	@Test
	public void testWsListObjects() throws Exception {
		AuthToken authToken = AuthService.login(USER_NAME, new String(PASSWORD)).getToken();
		ListObjectsParams params = new ListObjectsParams();
		//String type = "ExpressionServices.ExpressionSeries-1.0";
		List<String> workspaces = new ArrayList<String>();
		workspaces.add(workspaceName);
		//workspaces.add("networks_typed_objects_examples");
		//params.setType(type);
		params.setWorkspaces(workspaces);
		List<Tuple11<Long, String, String, String, Long, String, Long, String, String, Long, Map<String, String>>> typeInfo = WsDeluxeUtil.wsClient(authToken.toString()).listObjects(params);
		for (Tuple11<Long, String, String, String, Long, String, Long, String, String, Long, Map<String, String>> object: typeInfo){
			System.out.println(object.getE3() + " : " + object.getE2());
		}
		/*System.out.println(typeInfo.get(0).getE1());
		System.out.println(typeInfo.get(0).getE2());
		System.out.println(typeInfo.get(0).getE3());
		System.out.println(typeInfo.get(0).getE4());
		System.out.println(typeInfo.get(0).getE5());
		System.out.println(typeInfo.get(0).getE6());
		System.out.println(typeInfo.get(0).getE7());
		System.out.println(typeInfo.get(0).getE8());
		System.out.println(typeInfo.get(0).getE9());
		System.out.println(typeInfo.get(0).getE10());
		System.out.println(typeInfo.get(0).getE11());*/
		
		assertNotNull(typeInfo);
	}	

	
	@Test
	public void testWsReadObject() throws Exception {
		AuthToken authToken = AuthService.login(USER_NAME, new String(PASSWORD)).getToken();
		String name = "kb|genelist.5";
		//String exampleWs = "networks_typed_objects_examples";
		
		ObjectData output = WsDeluxeUtil.getObjectFromWorkspace(workspaceName, name, authToken.toString());
		System.out.println(output.getData().toString());
		assertNotNull(output);

	}	
	

	@Test
	public void testWsDeleteObject() throws Exception {
		AuthToken authToken = AuthService.login(USER_NAME, new String(PASSWORD)).getToken();
		String name = "kb|cmonkeyrunresult.136";
		String ref = workspaceName + "/" + name;
		
		List<ObjectIdentity> objectsIdentity = new ArrayList<ObjectIdentity>();
		ObjectIdentity objectIdentity = new ObjectIdentity().withRef(ref);
		objectsIdentity.add(objectIdentity);
		WsDeluxeUtil.wsClient(authToken.toString()).deleteObjects(objectsIdentity);

	}	

	
/*	@Test
	public void testWhichR() {
		String testFileName = "test/javaoutput.txt";
		InferelatorServerImpl.executeCommand("which R", testFileName);
		String line = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader(testFileName));
			line = br.readLine();
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			try {
				Runtime.getRuntime().exec("rm " + testFileName);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		assertEquals("/kb/runtime/bin/R", line);
	}
*/
	
	@Test
	public void testInferelatorCaller() throws Exception {
		AuthToken token = AuthService.login(USER_NAME, new String(PASSWORD)).getToken();
		InferelatorRunParameters params = new InferelatorRunParameters().withCmonkeyRunResultWsRef(testCmonkeyRunResultRef).withExpressionSeriesWsRef(testSeriesRef).withTfListWsRef(testTfListRef);
		String jobId = InferelatorServerCaller.findInteractionsWithInferelator(workspaceName, params, token);
		
		System.out.println("Job ID = " + jobId);
		assertNotNull(jobId);
		
		String status = "";
		Integer waitingTime = 2;
		String resultId = null;
		
		URL jobServiceUrl = new URL(JOB_SERVICE);
		UserAndJobStateClient jobClient = new UserAndJobStateClient(jobServiceUrl, token);

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
		
		InferelatorRunResult result = WsDeluxeUtil.getObjectFromWorkspace(workspaceName, resultId, token.toString()).getData().asClassInstance(InferelatorRunResult.class);
		
		assertEquals(39, result.getHits().size());
		assertEquals("1", result.getHits().get(0).getBiclusterId());


	}


	@Test
	public void testInferelatorServerImpl() throws Exception {
		AuthToken token = AuthService.login(USER_NAME, new String(PASSWORD)).getToken();
		URL jobServiceUrl = new URL(JOB_SERVICE);
		UserAndJobStateClient jobClient = new UserAndJobStateClient(
				jobServiceUrl, token);
		//jobClient.setAuthAllowedForHttp(true);
		String jobId = jobClient.createJob();

		
		InferelatorRunParameters params = new InferelatorRunParameters().withCmonkeyRunResultWsRef(testCmonkeyRunResultRef).withExpressionSeriesWsRef(testSeriesRef).withTfListWsRef(testTfListRef);
		InferelatorServerImpl.findInteractionsWithInferelator(jobId, workspaceName, params, token, null);
		System.out.println(jobId);
		assertNotNull(jobId);

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
		
		InferelatorRunResult result = WsDeluxeUtil.getObjectFromWorkspace(workspaceName, resultId, token.toString()).getData().asClassInstance(InferelatorRunResult.class);
		
		assertEquals(39, result.getHits().size());
		assertEquals("1", result.getHits().get(0).getBiclusterId());

	}

	@Test
	public void testDeleteJob() throws AuthException, IOException, UnauthorizedException, JsonClientException {
		String jobId = "52e71254e4b0ef83573320ed";

//		AuthToken token = AuthService.login(JOB_ACCOUNT, new String(JOB_PASSWORD)).getToken();
		AuthToken token = AuthService.login(USER_NAME, new String(PASSWORD)).getToken();
		AuthToken serviceToken = AuthService.login(InferelatorServerConfig.SERVICE_LOGIN, new String(InferelatorServerConfig.SERVICE_PASSWORD)).getToken();

		URL jobServiceUrl = new URL(JOB_SERVICE);
		UserAndJobStateClient jobClient = new UserAndJobStateClient(jobServiceUrl, token);
		jobClient.forceDeleteJob(serviceToken.toString(), jobId);
	}

}
