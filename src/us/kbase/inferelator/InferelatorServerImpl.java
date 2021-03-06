package us.kbase.inferelator;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
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
import java.util.Properties;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import us.kbase.auth.AuthException;
import us.kbase.auth.AuthService;
import us.kbase.auth.AuthToken;
import us.kbase.auth.TokenFormatException;
import us.kbase.cmonkey.CmonkeyCluster;
import us.kbase.cmonkey.CmonkeyRunResult;
import us.kbase.common.service.JacksonTupleModule;
import us.kbase.common.service.JsonClientException;
import us.kbase.common.service.UObject;
import us.kbase.common.service.UnauthorizedException;
import us.kbase.kbaseexpression.ExpressionSample;
import us.kbase.kbaseexpression.ExpressionSeries;
import us.kbase.idserverapi.IDServerAPIClient;
import us.kbase.kbasegenomes.Genome;
import us.kbase.userandjobstate.InitProgress;
import us.kbase.userandjobstate.Results;
import us.kbase.userandjobstate.UserAndJobStateClient;
import us.kbase.util.WsDeluxeUtil;
import us.kbase.workspace.ObjectData;

public class InferelatorServerImpl {


	public static final String inputNetworkFileName = "dataset.json";
	public static final String inputExpressionFileName = "ratios.tsv.gz";
	public static final String inputTflistFileName = "tflist.txt";
	public static final String outputFileName = "outfile.json";

	private static IDServerAPIClient _idClient = null;
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	
	protected static IDServerAPIClient idClient() {
		URL idServerUrl = null;

		try {
			idServerUrl = new URL(InferelatorServerConfig.ID_SERVICE_URL);
		} catch (MalformedURLException e1) {
			System.err.println("Unable to reach ID service at "+InferelatorServerConfig.ID_SERVICE_URL);
			e1.printStackTrace();
		}
		if(_idClient == null)
		{
			_idClient = new IDServerAPIClient(idServerUrl);
		}
		return _idClient;
	} 

	public static void startup() {
		File propertiesFile;
		String kbTop = System.getenv("KB_TOP");
		if (!kbTop.substring(kbTop.length() - 1).equals("/")) {
			kbTop = kbTop + "/";
		}
		propertiesFile = new File (kbTop + "/services/inferelator/inferelator.properties");
		Properties prop = new Properties();
		InputStream input = null;
		 
		try {
	 
			input = new FileInputStream(propertiesFile);
			// load a properties file
			prop.load(input);
			// set service configs
			InferelatorServerConfig.INFERELATOR_DIRECTORY = prop.getProperty("inferelator");
			InferelatorServerConfig.INFERELATOR_RUN_PATH = InferelatorServerConfig.INFERELATOR_DIRECTORY + "run_inf.R";
			InferelatorServerConfig.JOB_SERVICE_URL = prop.getProperty("ujs_url");
			InferelatorServerConfig.AWE_SERVICE_URL = prop.getProperty("awe_url");
			InferelatorServerConfig.ID_SERVICE_URL = prop.getProperty("id_url");
			InferelatorServerConfig.WS_SERVICE_URL = prop.getProperty("ws_url");
			InferelatorServerConfig.AWF_CONFIG_FILE = prop.getProperty("awf_config");
	 
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void findInteractionsWithInferelator(String jobId, String wsName, InferelatorRunParameters params, AuthToken authPart, String currentDir) throws Exception  {
		//start job
		if (jobId != null) 
			updateJobProgress(jobId,
					"AWE task started. Preparing input...", authPart);
		//make job directory
		String jobPath = "";
		if (InferelatorServerConfig.DEPLOY_AWE){
			jobPath = currentDir + "/" + jobId + "/";
			new File(jobPath).mkdir();
		} else {
			jobPath = InferelatorServerConfig.JOB_DIRECTORY + jobId + "/";
			new File(jobPath).mkdir();
		}
		System.setErr(new PrintStream(new File(jobPath + "servererror.txt")));
		FileWriter writer = new FileWriter(jobPath + "serveroutput.txt");
		Date date = new Date();
		writer.write("log file created " + dateFormat.format(date) + "\n");
		writer.flush();

		//get input data and write input files
		if (jobId != null)
			updateJobProgress(jobId, "Preparing input...", authPart);
		CmonkeyRunResult cmonkeyRunResult;
		try {
			cmonkeyRunResult = WsDeluxeUtil.getObjectFromWsByRef(params.getCmonkeyRunResultWsRef(), authPart.toString()).getData().asClassInstance(CmonkeyRunResult.class);
		} catch (TokenFormatException e) {
			finishJobWithError(jobId, e.getMessage(), "Cmonkey run result download error", authPart);
			e.printStackTrace();
			throw new Exception ("Cmonkey run result download error");
		} catch (UnauthorizedException e) {
			finishJobWithError(jobId, e.getMessage(), "Cmonkey run result download error", authPart);
			e.printStackTrace();
			throw new Exception ("Cmonkey run result download error");
		} catch (IOException e) {
			finishJobWithError(jobId, e.getMessage(), "Cmonkey run result download error", authPart);
			e.printStackTrace();
			throw new Exception ("Cmonkey run result download error");
		} catch (JsonClientException e) {
			finishJobWithError(jobId, e.getMessage(), "Cmonkey run result download error", authPart);
			e.printStackTrace();
			throw new Exception ("Cmonkey run result download error");
		}
		try {
			writeExpressionTable(jobPath, params, cmonkeyRunResult.getParameters().getGenomeRef(), authPart.toString());
		} catch (Exception e) {
			finishJobWithError(jobId, e.getMessage(), "Expression data file creation error", authPart);
			e.printStackTrace();
			throw new Exception ("Expression data file creation error");
		}
		String organism;
		try {
			organism = writeTfList(jobPath, params, authPart.toString());
		} catch (TokenFormatException e) {
			finishJobWithError(jobId, e.getMessage(), "TF list file creation error", authPart);
			e.printStackTrace();
			throw new Exception ("TF list file creation error");
		} catch (IOException e) {
			finishJobWithError(jobId, e.getMessage(), "TF list file creation error", authPart);
			e.printStackTrace();
			throw new Exception ("TF list file creation error");
		} catch (JsonClientException e) {
			finishJobWithError(jobId, e.getMessage(), "TF list file creation error", authPart);
			e.printStackTrace();
			throw new Exception ("TF list file creation error");
		}
		HashMap<String, String> clusterIds = writeClusterStack(jobPath, cmonkeyRunResult);
		writer.write("Input files created\n");
		writer.flush();
		
		//run inferelator
		if (jobId != null)
			updateJobProgress(jobId, "Input prepared. Starting Inferelator...",authPart);
		String inferelatorCommandLine = InferelatorServerConfig.INFERELATOR_RUN_PATH + " --tfsfile " + jobPath + inputTflistFileName +
				" --json " + jobPath + inputNetworkFileName + " --ratios " +
				jobPath + inputExpressionFileName + " --outfile "+ jobPath + outputFileName;
		writer.write("Run Inferelator : " + inferelatorCommandLine + "\n");
		writer.flush();
		Integer exitVal;
		try {
			exitVal = executeCommand (inferelatorCommandLine, jobPath, jobId, authPart);
		} catch (InterruptedException e) {
			finishJobWithError(jobId, e.getMessage(), "Inferelator execution error", authPart);
			e.printStackTrace();
			throw new Exception ("Inferelator execution error");
		} catch (IOException e) {
			finishJobWithError(jobId, e.getMessage(), "Inferelator execution error", authPart);
			e.printStackTrace();
			throw new Exception ("Inferelator execution error");
		}
		//Integer exitVal = mockExecuteCommand (inferelatorCommandLine, jobPath, jobId, authPart);
		writer.write("Exit value : " + exitVal.toString() + "\n");
		writer.flush();
		
		//parse output file  
		if (jobId != null) updateJobProgress (jobId, "Inferelator finished. Processing output...", authPart);
		InferelatorRunResult runResult;
		try {
			runResult = parseInferelatorOutput (jobPath+outputFileName, clusterIds);
		} catch (JsonProcessingException e) {
			finishJobWithError(jobId, e.getMessage(), "Inferelator output parsing error", authPart);
			e.printStackTrace();
			throw new Exception ("Inferelator output parsing error");
		} catch (IOException e) {
			finishJobWithError(jobId, e.getMessage(), "Inferelator output parsing error", authPart);
			e.printStackTrace();
			throw new Exception ("Inferelator output parsing error");
		} catch (JsonClientException e) {
			finishJobWithError(jobId, e.getMessage(), "Inferelator output parsing error", authPart);
			e.printStackTrace();
			throw new Exception ("Inferelator output parsing error");
		}
		runResult.setParams(params);
		runResult.setOrganism(organism);
		writer.write("Result organism : " + runResult.getOrganism() + "\n");
		writer.write("Result ID : " + runResult.getId() + "\n");
		writer.flush();

		//save run result
		if (jobId != null) updateJobProgress (jobId, "Output created. Saving to workspace...", authPart);
		try {
			WsDeluxeUtil.saveObjectToWorkspace(UObject.transformObjectToObject(runResult, UObject.class), InferelatorServerConfig.INFERELATOR_RUN_RESULT_TYPE, wsName, runResult.getId(), authPart.toString());
		} catch (TokenFormatException e) {
			finishJobWithError(jobId, e.getMessage(), "Inferelator result upload error", authPart);
			e.printStackTrace();
			throw new Exception ("Inferelator result upload error");
		} catch (UnauthorizedException e) {
			finishJobWithError(jobId, e.getMessage(), "Inferelator result upload error", authPart);
			e.printStackTrace();
			throw new Exception ("Inferelator result upload error");
		} catch (IOException e) {
			finishJobWithError(jobId, e.getMessage(), "Inferelator result upload error", authPart);
			e.printStackTrace();
			throw new Exception ("Inferelator result upload error");
		} catch (JsonClientException e) {
			finishJobWithError(jobId, e.getMessage(), "Inferelator result upload error", authPart);
			e.printStackTrace();
			throw new Exception ("Inferelator result upload error");
		}
		if (jobId != null) finishJob (jobId, wsName, runResult.getId(), authPart);
		writer.write("Job finished\n");
		writer.flush();
		writer.close();
		//clean up
		if (!InferelatorServerConfig.DEPLOY_AWE){
			File fileDelete = new File(jobPath);
			deleteDirectoryRecursively(fileDelete);
		}
	}
	
	protected static String writeTfList(String jobPath,
			InferelatorRunParameters params, String token) throws TokenFormatException, IOException, JsonClientException {
		
		GeneList inputTfList = WsDeluxeUtil.getObjectFromWsByRef(params.getTfListWsRef(), token).getData().asClassInstance(GeneList.class);
		Genome genome = WsDeluxeUtil.getObjectFromWsByRef(inputTfList.getSourceId(), token).getData().asClassInstance(Genome.class);
		String returnVal = genome.getScientificName();
		genome = null;
		BufferedWriter writer = new BufferedWriter(new FileWriter(jobPath+inputTflistFileName));
		for (String tf: inputTfList.getGenes()) {
			writer.write(tf + "\n");
		}
		writer.close();
		inputTfList = null;
		return returnVal;
	}

	protected static void writeExpressionTable(String jobPath,
			InferelatorRunParameters params, String genomeRef, String token) throws Exception {
		ExpressionSeries series = WsDeluxeUtil.getObjectFromWsByRef(params.getExpressionSeriesWsRef(), token).getData().asClassInstance(ExpressionSeries.class);
		OutputStreamWriter writer = new OutputStreamWriter(new GZIPOutputStream(
				new BufferedOutputStream(new FileOutputStream(jobPath+inputExpressionFileName))));
		//BufferedWriter writer = new BufferedWriter(new FileWriter(jobPath+inputExpressionFileName)); for plain text
		writer.write("GENE");

		//get list of samples
		Set<String> genomeIds = series.getGenomeExpressionSampleIdsMap().keySet();
		List<String> sampleIdsList = null;
		if (genomeIds.size() > 1) {
			throw new Exception ("ExpressionSeries contains more than one genome ID");
		} else {
			String genomeId = genomeIds.iterator().next();
			sampleIdsList = series.getGenomeExpressionSampleIdsMap().get(genomeId);
		}
		List<ObjectData> samples = WsDeluxeUtil.getObjectsFromWsByRef(sampleIdsList, token);

		//write sample IDs
		for (ObjectData data: samples){
			writer.write("\t"+data.getData().asClassInstance(ExpressionSample.class).getId());
		}
		//writer.flush();

		//make list of genes
		List<String> geneNames=new ArrayList<String>();
		for(ObjectData data: samples){
			geneNames.addAll(data.getData().asClassInstance(ExpressionSample.class).getExpressionLevels().keySet());
		};
		List<String> uniqueGeneNames = new ArrayList<String>(new HashSet<String>(geneNames));
		geneNames = null;

		//write data
		for(String geneName:uniqueGeneNames){
			writer.write("\n"+geneName);
			DecimalFormat df = new DecimalFormat("0.000");
			for (ObjectData data: samples){
				if (data.getData().asClassInstance(ExpressionSample.class).getExpressionLevels().containsKey(geneName)){
					if (data.getData().asClassInstance(ExpressionSample.class).getExpressionLevels().get(geneName).toString().matches("-.*")){
						writer.write("\t"+ df.format(data.getData().asClassInstance(ExpressionSample.class).getExpressionLevels().get(geneName)));
					} else {
						writer.write("\t "+ df.format(data.getData().asClassInstance(ExpressionSample.class).getExpressionLevels().get(geneName)));
					}
				} else {
					writer.write("\tNA");
				}
			}
			//writer.flush();			
		}
		writer.write("\n");
		writer.close();
		series = null;
	}

	protected static HashMap<String, String> writeClusterStack(String jobPath, CmonkeyRunResult cmonkeyRunResult) throws IOException{
		HashMap<String, String> returnVal = new HashMap<String, String>();
		BufferedWriter writer = new BufferedWriter(new FileWriter(jobPath+inputNetworkFileName));
			writer.write("[");

			Integer clusterNumber = 1;
			for (int i = 0; i < cmonkeyRunResult.getNetwork().getClusters().size(); i++){
				CmonkeyCluster cluster =  cmonkeyRunResult.getNetwork().getClusters().get(i);
				writer.write(" \n{\n");
				writer.write(" \"nrows\": "+cluster.getGeneIds().size()+",\n");
				writer.write("\"ncols\": "+cluster.getSampleWsIds().size()+",\n");
				writer.write("\"rows\": [");
				for (int j = 0; j < cluster.getGeneIds().size(); j++){
					writer.write(" \""+cluster.getGeneIds().get(j)+"\"");
					if (j < cluster.getGeneIds().size() - 1) {
						writer.write(",");
					} else {
						writer.write(" ],\n");
					}
				}
				writer.write("\"cols\": [");
				for (int j = 0; j < cluster.getSampleWsIds().size(); j++){
					writer.write(" \""+cluster.getSampleWsIds().get(j)+"\"");
					if (j < cluster.getSampleWsIds().size() - 1) {
						writer.write(",");
					} else {
						writer.write(" ],\n");
					}
				}
				writer.write("\"k\": "+clusterNumber+",\n");
				returnVal.put(clusterNumber.toString(), cluster.getId());
				clusterNumber++;
				writer.write("\"resid\": "+cluster.getResidual()+"\n}");
				if (i < cmonkeyRunResult.getNetwork().getClusters().size() - 1) {
					writer.write(",");
				} else {
					writer.write("\n]");
				}
			}
		writer.close();
		cmonkeyRunResult = null;
		return returnVal;
	}

	protected static InferelatorRunResult parseInferelatorOutput(String fileName, HashMap<String, String> clusterIds) throws JsonProcessingException, IOException, JsonClientException {
		InferelatorRunResult result = new InferelatorRunResult();
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		result.setId(getKbaseId(InferelatorRunResult.class.getSimpleName()));
		
		JsonNode rootNode = new ObjectMapper().registerModule(new JacksonTupleModule()).readTree(br);
		
		//System.out.println(rootNode.toString());
		
		Iterator<String> clusterNodes = rootNode.fieldNames();
		List<InferelatorHit> hitList = new ArrayList<InferelatorHit>();
	    while(clusterNodes.hasNext()){
            String clusterName = clusterNodes.next();
            System.out.println("Cluster : " + clusterName);
            JsonNode clusterValue = rootNode.get(clusterName);
            Iterator<String> hits = clusterValue.fieldNames();
            if (hits.hasNext()){
            	while(hits.hasNext()){
            		InferelatorHit hit = new InferelatorHit().withBiclusterId(clusterIds.get(clusterName));
            		String tfName = hits.next();
            		hit.setTfId(tfName);
            		hit.setCoeff(clusterValue.get(tfName).asDouble());
            		hitList.add(hit);
            	}
            } else {
            	System.out.println("No predictions for cluster : " + clusterName);
            }
	    }
	    result.setHits(hitList);
		return result;
	}

	protected static void startJob (String jobId, String desc, Long tasks, AuthToken token) throws IOException, JsonClientException, AuthException {
		
		String status = "cmonkey service job started. Preparing input...";
		InitProgress initProgress = new InitProgress();
		initProgress.setPtype("task");
		initProgress.setMax(tasks);
		Date date = new Date();
		date.setTime(date.getTime()+108000000L);
		
		URL jobServiceUrl = new URL(InferelatorServerConfig.JOB_SERVICE_URL);
		UserAndJobStateClient jobClient = new UserAndJobStateClient(jobServiceUrl, token);
		jobClient.startJob(jobId, AuthService.login(InferelatorServerConfig.SERVICE_LOGIN, new String(InferelatorServerConfig.SERVICE_PASSWORD)).getToken().toString(), status, desc, initProgress, dateFormat.format(date));
		jobClient = null;
	}

	protected static void updateJobProgress (String jobId, String status, AuthToken token) throws IOException, JsonClientException, AuthException{
		Date date = new Date();
		date.setTime(date.getTime()+1000000L);
		URL jobServiceUrl = new URL(InferelatorServerConfig.JOB_SERVICE_URL);
		UserAndJobStateClient jobClient = new UserAndJobStateClient(jobServiceUrl, token);
		jobClient.updateJobProgress(jobId, AuthService.login(InferelatorServerConfig.SERVICE_LOGIN, new String(InferelatorServerConfig.SERVICE_PASSWORD)).getToken().toString(), status, 1L, dateFormat.format(date));
		jobClient = null;
	}
	
	protected static void finishJob (String jobId, String wsId, String objectId, AuthToken token) throws IOException, JsonClientException, AuthException{
		String status = "Finished";
		String error = null;
		Results res = new Results();
		List<String> workspaceIds = new ArrayList<String>();
		workspaceIds.add(wsId + "/" + objectId);
		res.setWorkspaceids(workspaceIds);
		URL jobServiceUrl = new URL(InferelatorServerConfig.JOB_SERVICE_URL);
		UserAndJobStateClient jobClient = new UserAndJobStateClient(jobServiceUrl, token);
		jobClient.completeJob(jobId, AuthService.login(InferelatorServerConfig.SERVICE_LOGIN, new String(InferelatorServerConfig.SERVICE_PASSWORD)).getToken().toString(), status, error, res);
		jobClient = null;
	}

	protected static void finishJobWithError(String jobId, String error, String status, AuthToken token) throws UnauthorizedException,
	IOException, JsonClientException, AuthException {
		Results res = new Results();
		URL jobServiceUrl = new URL(InferelatorServerConfig.JOB_SERVICE_URL);
		UserAndJobStateClient jobClient = new UserAndJobStateClient(jobServiceUrl, token);
		jobClient.completeJob(jobId, AuthService.login(InferelatorServerConfig.SERVICE_LOGIN, new String(InferelatorServerConfig.SERVICE_PASSWORD)).getToken().toString(), status, error, res);
	}

	protected static String getKbaseId(String entityType) throws IOException, JsonClientException {
		String returnVal = null;
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
		return returnVal;
	}
	
	protected static Integer executeCommand(String commandLine, String jobPath, String jobId, AuthToken authPart) throws InterruptedException, IOException {
		Integer exitVal = null;
		Process p = Runtime.getRuntime().exec(commandLine, null, new File(InferelatorServerConfig.INFERELATOR_DIRECTORY));
		StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR", jobId, authPart, jobPath+"errorlog.txt");            
            // any output?
		StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "OUTPUT", jobId, authPart, jobPath+"out.txt");
            // kick them off
		errorGobbler.start();
		outputGobbler.start();
            // any error???
		exitVal = p.waitFor();
		System.out.println("ExitValue: " + exitVal);      
		return exitVal;
	}

	
	public static void deleteDirectoryRecursively(File startFile) {
		if (startFile.isDirectory()) {
			for (File f : startFile.listFiles()) {
				deleteDirectoryRecursively(f);
			}
			startFile.delete();
		} else {
			startFile.delete();
		}
	}

}
