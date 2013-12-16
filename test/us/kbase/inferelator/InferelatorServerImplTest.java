package us.kbase.inferelator;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import us.kbase.auth.AuthService;
import us.kbase.auth.AuthToken;
import us.kbase.common.service.UObject;
import us.kbase.workspaceservice.GetObjectOutput;
import us.kbase.workspaceservice.GetObjectParams;


public class InferelatorServerImplTest {

	private static final String USER_NAME = "aktest";
	private static final String PASSWORD = "1475rokegi";
	private static final String workspaceName = "AKtest";

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
		String testFile = "test/outfile";
		InferelatorRunResult result = InferelatorServerImpl.parseInferelatorOutput(testFile, Long.parseLong("43"));
		//System.out.println(result.getInteractions().size());
		assertNotNull(result);
		assertNotNull(result.getClusters().get(0));
		assertNull(result.getClusters().get(0).getInteractions());
		assertEquals(13, result.getClusters().get(7).getInteractions().size());
		assertEquals("VNG5176C", result.getClusters().get(39).getInteractions().get(0).getRegulatorId());
		assertEquals(Double.valueOf("0.2599"), result.getClusters().get(39).getInteractions().get(0).getCoeff());
	}

	@Test
	public final void testCmonkeyJsonExport() throws Exception {
		AuthToken token = AuthService.login(USER_NAME, new String(PASSWORD)).getToken();
		String id = "TestCmonkeyRunResult";
		GetObjectParams objectParams = new GetObjectParams().withType("CmonkeyRunResult").withId(id).withWorkspace(workspaceName).withAuth(token.toString());
		GetObjectOutput output = InferelatorServerImpl.wsClient(token.toString()).getObject(objectParams);
		CmonkeyRunResult result = UObject.transformObjectToObject(output.getData(), CmonkeyRunResult.class);

		String resultJson = "[";
		
		for (CmonkeyCluster cluster: result.getNetwork().getClusters()){
			resultJson += "{";
			resultJson += "\"nrows\": "+cluster.getGeneIds().size()+",";
			resultJson += "\"ncols\": "+cluster.getDatasetIds().size()+",";
			resultJson += "\"rows\": [";
			for (String geneId : cluster.getGeneIds()){
				resultJson += "\""+geneId+"\",";
			}
			resultJson = resultJson.substring(0, resultJson.length() - 1);
			resultJson += "], ";
			resultJson += "\"cols\": [";
			for (String condition : cluster.getDatasetIds()){
				resultJson += "\""+condition+"\",";
			}
			resultJson = resultJson.substring(0, resultJson.length() - 1);
			resultJson += "], ";
			resultJson += "\"k\": "+cluster.getId()+",";
			resultJson += "\"resid\": "+cluster.getResidual()+"},";
		}
		resultJson = resultJson.substring(0, resultJson.length() - 1);
		resultJson += "]";
		
		System.out.println(resultJson);
		assertNotNull(result);
		
	}


}
