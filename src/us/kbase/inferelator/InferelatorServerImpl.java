package us.kbase.inferelator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import us.kbase.auth.AuthException;
import us.kbase.auth.AuthToken;
import us.kbase.auth.TokenFormatException;
import us.kbase.common.service.JacksonTupleModule;
import us.kbase.common.service.JsonClientException;
import us.kbase.common.service.Tuple11;
import us.kbase.common.service.UObject;
import us.kbase.common.service.UnauthorizedException;
import us.kbase.idserverapi.IDServerAPIClient;
import us.kbase.userandjobstate.InitProgress;
import us.kbase.userandjobstate.Results;
import us.kbase.userandjobstate.UserAndJobStateClient;
import us.kbase.workspaceservice.GetObjectOutput;
import us.kbase.workspaceservice.GetObjectParams;
import us.kbase.workspaceservice.ObjectData;
import us.kbase.workspaceservice.SaveObjectParams;
import us.kbase.workspaceservice.WorkspaceServiceClient;

public class InferelatorServerImpl {

	private static final String JOB_SERVICE = "http://140.221.84.180:7083";
	private static final String WS_SERVICE_URL = "http://kbase.us/services/workspace";
	private static final String ID_SERVICE_URL = "http://kbase.us/services/idserver";
	private static final String JOB_PATH = "/var/tmp/inferelator/";
	private static final String INFERELATOR_DIR = "/kb/runtime/cmonkey-python/inferelator/";
	private static final String inputNetworkFileName = "dataset.json";
	private static final String inputExpressionFileName = "ratios.tsv";
	private static final String inputTflistFileName = "tflist.txt";
	private static final String outputFileName = "outfile.json";

	private static IDServerAPIClient _idClient = null;
	private static UserAndJobStateClient _jobClient = null;
	private static WorkspaceServiceClient _wsClient = null;
	private static Date date = new Date();
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	private ObjectMapper mapper;

	protected static IDServerAPIClient idClient() {
		URL idServerUrl = null;

		try {
			idServerUrl = new URL(ID_SERVICE_URL);
		} catch (MalformedURLException e1) {
			System.err.println("Unable to reach ID service at "+ID_SERVICE_URL);
			e1.printStackTrace();
		}
		if(_idClient == null)
		{
			_idClient = new IDServerAPIClient(idServerUrl);
		}
		return _idClient;
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
				System.err.println("Unable to authenticate in job service " + JOB_SERVICE);
				e.printStackTrace();
			} catch (IOException e) {
				System.err.println("Unable to communicate with job service at" + JOB_SERVICE);
				e.printStackTrace();
			}
			_jobClient.setAuthAllowedForHttp(true);
		}
		return _jobClient;
	}
	
	protected static WorkspaceServiceClient wsClient(String token) {
		if(_wsClient == null)
		{
			URL workspaceClientUrl;
			try {
				workspaceClientUrl = new URL (WS_SERVICE_URL);
				AuthToken authToken = new AuthToken(token);
				_wsClient = new WorkspaceServiceClient(workspaceClientUrl, authToken);
				_wsClient.setAuthAllowedForHttp(true);
			} catch (MalformedURLException e) {
				System.err.println("Bad URL? Unable to communicate with workspace service at" + WS_SERVICE_URL);
				e.printStackTrace();
			} catch (TokenFormatException e) {
				System.err.println("Unable to authenticate");
				e.printStackTrace();
			} catch (UnauthorizedException e) {
				System.err.println("Unable to authenticate in workspace service at" + WS_SERVICE_URL);
				e.printStackTrace();
			} catch (IOException e) {
				System.err.println("Unable to communicate with workspace service at" + WS_SERVICE_URL);
				e.printStackTrace();
			}
		}
		return _wsClient;
	} 

	public static void findInteractionsWithInferelator(String jobId, String wsId, InferelatorRunParameters params, AuthToken authPart){
		//start job
		String desc = "Inferelator service job. Method: findInteractionsWithInferelator. Input: cmonkeyRunResult " + params.getCmonkeyRunResultId() + 
				", expressionDataSeries " + params.getExpressionDataSeriesId() + ", regulators list " + params.getGeneListId() + ". Workspace: " + wsId + ".";
		Long tasks = 23L;
		if (jobId != null) startJob (jobId, desc, tasks, authPart);
		
		//make job directory
		String jobPath = JOB_PATH + jobId + "/";
		try {
			Runtime.getRuntime().exec("mkdir " + jobPath);
		} catch (IOException e) {
			System.err.println("Job directory creation error: " + jobPath);
			e.printStackTrace();
		}
		
		//get input objects
		CmonkeyRunResult inputNetwork = UObject.transformObjectToObject(getObjectFromWorkspace(CmonkeyRunResult.class.getSimpleName(), wsId, params.getCmonkeyRunResultId(), authPart.toString()), CmonkeyRunResult.class);
		String cmonkeyJson = generateCmonkeyJson(inputNetwork);
		ExpressionDataSeries inputSeries = UObject.transformObjectToObject(getObjectFromWorkspace(ExpressionDataSeries.class.getSimpleName(), wsId, params.getExpressionDataSeriesId(), authPart.toString()), ExpressionDataSeries.class);
		String expressionTable = getInputTable(inputSeries);
		List<String> inputTfList = UObject.transformObjectToObject(getObjectFromWorkspace(List.class.getSimpleName(), wsId, params.getGeneListId(), authPart.toString()), List.class);

		String tfList = null;
		for (String tf: inputTfList){
			tfList += tf + "\n";
		}
		
		//write input files
		writeInputFile(jobPath+inputNetworkFileName, cmonkeyJson);
		writeInputFile(jobPath+inputExpressionFileName, expressionTable);
		writeInputFile(jobPath+inputTflistFileName, tfList);
		
		//run inferelator
		String inferelatorCommandLine = "./run_inf.R  --tfsfile " +jobPath+inputTflistFileName+
				" --json "+jobPath+inputNetworkFileName+" --ratios "
				+jobPath+inputExpressionFileName+" --outfile "+jobPath+outputFileName;
		try {
			executeCommand (inferelatorCommandLine, jobPath, jobId, authPart);
		} catch (InterruptedException e) {
			System.err.println("Inferelator running error");
			updateJobProgress (jobId, "Inferelator running error", authPart);
			e.printStackTrace();
		}
		if (jobId != null) updateJobProgress (jobId, "Inferelator finished. Processing output...", authPart);
		
		//parse output file  
		InferelatorRunResult runResult = parseInferelatorOutput (jobPath+outputFileName, inputNetwork.getClustersNumber());

		//save run result
		try {
			saveObjectToWorkspace (UObject.transformObjectToObject(runResult, UObject.class), runResult.getClass().getSimpleName(), wsId, runResult.getId(), authPart.toString());
		} catch (Exception e) {
			System.err.println("Workspace write error: Unable to save "+runResult.getClass().getSimpleName() +" to workspace "+wsId+" at "+WS_SERVICE_URL);
			e.printStackTrace();
		}
		if (jobId != null) finishJob (jobId, wsId, runResult.getId(), authPart);
		
		//clean up
		//Runtime.getRuntime().exec("rm -r " + jobPath);

	}

	
	protected static InferelatorRunResult parseInferelatorOutput(String fileName, Long clusters) {
		InferelatorRunResult result = new InferelatorRunResult();
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			result.setId(getKbaseId(InferelatorRunResult.class.getSimpleName()));
			
			JsonNode rootNode = new ObjectMapper().registerModule(new JacksonTupleModule()).readTree(br);
			
			//System.out.println(rootNode.toString());
			
			Iterator<String> clusterNodes = rootNode.fieldNames();
			List<InferelatorCluster> clusterList = new ArrayList<InferelatorCluster>();
		    while(clusterNodes.hasNext()){
	            String clusterName = clusterNodes.next();
	            System.out.println("Cluster : " + clusterName);
	            InferelatorCluster cluster = new InferelatorCluster();
	            cluster.setId(clusterName);
	            JsonNode clusterValue = rootNode.get(clusterName);
	            Iterator<String> interactions = clusterValue.fieldNames();
	            if (interactions.hasNext()){
	            	List<InferelatorInteraction> interactionList = new ArrayList<InferelatorInteraction>();
	            	while(interactions.hasNext()){
	            		InferelatorInteraction interaction = new InferelatorInteraction();
	            		String tfName = interactions.next();
	            		Double coeff = clusterValue.get(tfName).asDouble();
	            		System.out.println("\tTF : "+tfName+" ; COEFF : "+coeff);
	            		interaction.setRegulatorId(tfName);
	            		interaction.setCoeff(coeff);
	            		interactionList.add(interaction);
	            	}
	            	cluster.setInteractions(interactionList);
	            } else {
	            	System.out.println("No predictions for cluster : " + clusterName);
	            }
	            clusterList.add(cluster);
		    }
		    result.setClusters(clusterList);
			
		} catch (FileNotFoundException e) {
			System.err.println("Output file "+fileName+" not found.");
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return result;
	}

	protected static void startJob (String jobId, String desc, Long tasks, AuthToken token) {
		
		String status = "cmonkey service job started. Preparing input...";
		InitProgress initProgress = new InitProgress();
		initProgress.setPtype("task");
		initProgress.setMax(tasks);
		date.setTime(date.getTime()+108000000L);
	
		try {
			//System.out.println(dateFormat.format(date));
			jobClient(token).startJob(jobId, token.toString(), status, desc, initProgress, dateFormat.format(date));
		} catch (JsonClientException e) {
			System.err.println("Unable to start job \"" + desc + "\". JSON client error at " + JOB_SERVICE);
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Unable to start job \"" + desc + "\" at " + JOB_SERVICE);
			e.printStackTrace();
		} catch (AuthException e) {
			System.err.println("Unable to start job \"" + desc + "\". Authentication error at " + JOB_SERVICE);
			e.printStackTrace();
		}
	}

	protected static void updateJobProgress (String jobId, String status, AuthToken token){
		try {
			date.setTime(date.getTime()+1000000L);
			jobClient(token).updateJobProgress(jobId, token.toString(), status, 1L, dateFormat.format(date));
		} catch (JsonClientException e) {
			System.err.println("Unable to update job \"" + jobId + ":" + status + "\". JSON client error at " + JOB_SERVICE);
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Unable to update job \"" + jobId + ":" + status + "\" at " + JOB_SERVICE);
			e.printStackTrace();
		} catch (AuthException e) {
			System.err.println("Unable to update job \"" + jobId + ":" + status + "\". Authentication error at " + JOB_SERVICE);
			e.printStackTrace();
		}

	}
	
	protected static void finishJob (String jobId, String wsId, String objectId, AuthToken token){
		try {
			String status = "Finished";
			String error = null;
			
			Results res = new Results();
			List<String> workspaceIds = new ArrayList<String>();
			workspaceIds.add(wsId + "/" + objectId);
			res.setWorkspaceids(workspaceIds);
			jobClient(token).completeJob(jobId, token.toString(), status, error, res); 
		} catch (JsonClientException e) {
			System.err.println("Unable to finish job \"" + jobId + ": result object " + objectId + "\". JSON client error at " + JOB_SERVICE);
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Unable to finish job \"" + jobId + ": result object " + objectId + "\" at " + JOB_SERVICE);
			e.printStackTrace();
		} catch (AuthException e) {
			System.err.println("Unable to finish job \"" + jobId + ": result object " + objectId + "\". Authentication error at " + JOB_SERVICE);
			e.printStackTrace();
		}
	}

	protected static String getKbaseId(String entityType) {
		String returnVal = null;
		try {
			if (entityType.equals("InferelatorRunResult")) {
				returnVal = "kb|inferelatorrunresult." + idClient().allocateIdRange("inferelatorrunresult", 1L).toString();
			} else if (entityType.equals("CmonkeyNetwork")) {
				returnVal = "kb|cmonkeynetwork." + idClient().allocateIdRange("cmonkeynetwork", 1L).toString();
			} else if (entityType.equals("CmonkeyCluster")) {
				returnVal = "kb|cmonkeycluster." + idClient().allocateIdRange("cmonkeycluster", 1L).toString();
			} else if (entityType.equals("CmonkeyMotif")) {
				returnVal = "kb|cmonkeymotif." + idClient().allocateIdRange("cmonkeymotif", 1L).toString();
			} else if (entityType.equals("MastHit")) {
				returnVal = "kb|masthit." + idClient().allocateIdRange("masthit", 1L).toString();
			} else if (entityType.equals("ExpressionDataSeries")) {
				returnVal = "kb|expressiondataseries." + idClient().allocateIdRange("expressiondataseries", 1L).toString();
			} else {
				System.out.println("ID requested for unknown type " + entityType);
			}
		} catch (IOException e) {
			System.err.println("IO error: Unable to get KBase ID ");
			e.printStackTrace();
		} catch (JsonClientException e) {
			System.err.println("Json client error: Unable to get KBase ID ");
			e.printStackTrace();
		}

		return returnVal;
	}
	
	protected static UObject getObjectFromWorkspace (String type, String workspaceName, String id, String token) {
		try {
			GetObjectParams objectParams = new GetObjectParams().withType(type).withId(id).withWorkspace(workspaceName).withAuth(token);
			GetObjectOutput output = wsClient(token.toString()).getObject(objectParams);
			UObject returnVal = UObject.transformObjectToObject(output.getData(), UObject.class);
			return returnVal;
		} catch (IOException e) {
			System.err.println("Unable to get object " + id + " from workspace " + workspaceName + ": IO error");
			e.printStackTrace();
		} catch (JsonClientException e) {
			System.err.println("Unable to get object " + id + " from workspace " + workspaceName + ": Json error");
			e.printStackTrace();
		}
		return null;
	}

	protected static void saveObjectToWorkspace (UObject object, String type, String workspaceName, String id, String token) throws Exception {

		ObjectData objectData = UObject.transformObjectToObject(object, ObjectData.class);
		SaveObjectParams params = new SaveObjectParams();
		params.setAuth(token);
		params.setCompressed(0L);
		params.setData(objectData);
		params.setId(id); 
		params.setJson(0L); 
		params.setType(type);
		
		Map<String, String> metadata = new HashMap<String, String>();
		params.setMetadata(metadata);
		
		params.setWorkspace(workspaceName);
		Tuple11<String, String, String, Long, String, String, String, String, String, String, Map<String,String>> ret = wsClient(token).saveObject(params);
		
		System.out.println("Saving object:");
		System.out.println(ret.getE1());
/*		System.out.println(ret.getE2());
		System.out.println(ret.getE3());
		System.out.println(ret.getE4());
		System.out.println(ret.getE5());
		System.out.println(ret.getE6());
		System.out.println(ret.getE7());
		System.out.println(ret.getE8());
		System.out.println(ret.getE9());
		System.out.println(ret.getE10());
		System.out.println(ret.getE11());
*/
	}

	protected static String getInputTable(ExpressionDataSeries series){
		String result = "GENE";
		List<HashMap<String, Double>> dataCollection = new ArrayList<HashMap<String, Double>>();
		//make list of conditions
		for(ExpressionDataSample sample:series.getSamples()){
			result+="\t"+sample.getId();
			HashMap<String, Double> dataSet= new HashMap<String, Double>();
			for (ExpressionDataPoint point:sample.getPoints()){
				dataSet.put(point.getGene(), point.getExpressionValue());
			}
			dataCollection.add(dataSet);
		}
		//make list of genes
		List<String> geneNames=new ArrayList<String>();
		for(ExpressionDataSample sample:series.getSamples()){
			for(ExpressionDataPoint point:sample.getPoints()){
				geneNames.add(point.getGene());
			}
		}
		List<String> uniqueGeneNames = new ArrayList<String>(new HashSet<String>(geneNames));
		for(String geneName:uniqueGeneNames){
			result+="\n"+geneName;
			DecimalFormat df = new DecimalFormat("0.000");
			for (HashMap<String, Double> dataSetHashmap: dataCollection){
				if (dataSetHashmap.containsKey(geneName)){
					if (dataSetHashmap.get(geneName).toString().matches("-.*")){
						result+="\t"+ df.format(dataSetHashmap.get(geneName));
					} else {
						result+="\t "+ df.format(dataSetHashmap.get(geneName));
					}
				} else {
					result+="\tNA";
				}
			}			
		}
		result+="\n";
		return result;
	}

	protected static void writeInputFile (String inputFileName, String input){
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(inputFileName));
			writer.write(input);
		} catch (IOException e) {
			System.out.println(e.getLocalizedMessage());
		} finally {
			try {
				if (writer != null)
					writer.close();
			} catch (IOException e) {
				System.out.println(e.getLocalizedMessage());
			}
		}

	}
	
	protected static String generateCmonkeyJson (CmonkeyRunResult result){
		String resultJson = "[";
		
		for (CmonkeyCluster cluster: result.getNetwork().getClusters()){
			resultJson += "{";
			resultJson += "\"nrows\": "+cluster.getGeneIds().size()+",";
			resultJson += "\"ncols\": "+cluster.getDatasetIds().size()+",";
			resultJson += "\"rows\": [";
			for (String geneId : cluster.getGeneIds()){
				resultJson += "\""+geneId+"\",";
			}
			resultJson += "]'";
			resultJson += "\"cols\": [";
			for (String condition : cluster.getDatasetIds()){
				resultJson += "\""+condition+"\",";
			}
			resultJson += "]'";
			resultJson += "\"k\": "+cluster.getId()+",";
			resultJson += "\"resid\": "+cluster.getResidual()+"},";
		}
		resultJson = resultJson.substring(0, resultJson.length() - 1);
		resultJson += "]";
		
		//System.out.println(resultJson);
		return resultJson;
	}

	protected static Integer executeCommand(String commandLine, String jobPath, String jobId, AuthToken authPart) throws InterruptedException {
		Integer exitVal = null;
		try {
			Process p = Runtime.getRuntime().exec(commandLine, null, new File(INFERELATOR_DIR));
			
			StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR", jobId, authPart, jobPath+"errorlog.txt");            
	            
	            // any output?
			StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "OUTPUT", jobId, authPart, jobPath+"out.txt");
	                
	            // kick them off
			errorGobbler.start();
			outputGobbler.start();
	                                    
	            // any error???
			exitVal = p.waitFor();
			System.out.println("ExitValue: " + exitVal);      
		} catch (IOException e) {
			System.out.println(e.getLocalizedMessage());
		} finally {
		}
		return exitVal;
	}

}
