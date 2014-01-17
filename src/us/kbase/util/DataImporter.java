package us.kbase.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import us.kbase.auth.TokenFormatException;
import us.kbase.common.service.JsonClientException;
import us.kbase.common.service.UnauthorizedException;
import us.kbase.idserverapi.IDServerAPIClient;
import us.kbase.inferelator.GeneList;
import us.kbase.inferelator.InferelatorServerConfig;
import us.kbase.kbasegenomes.Feature;
import us.kbase.kbasegenomes.Genome;

public class DataImporter {

	private static final String ID_SERVICE_URL = InferelatorServerConfig.ID_SERVICE_URL;
	private static IDServerAPIClient _idClient = null;

	
	protected static IDServerAPIClient idClient() {
		if (_idClient == null) {
			URL idServerUrl = null;
			try {
				idServerUrl = new URL(ID_SERVICE_URL);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			_idClient = new IDServerAPIClient(idServerUrl);
		}
		return _idClient;
	}

	public static GeneList importGeneList (String fileName, String genomeRef, String token) throws TokenFormatException, IOException, JsonClientException{
		GeneList returnVal = new GeneList();
		
		//get genome
		Genome genome = WsDeluxeUtil.getObjectFromWsByRef(genomeRef, token).getData().asClassInstance(Genome.class);
		//get feature names list
		HashMap<String, String> aliases = readFeatures(genome);
		//read file
		BufferedReader br = null;
		List<String> listData = new ArrayList<String>();
		try {
			
			br = new BufferedReader(new FileReader(fileName));
			String line = null;
			while ((line = br.readLine()) != null) {
				//System.out.println(line);
				if (line.equals("")) {
					// do nothing
				} else {
					String featureId = aliases.get(line);
					if (featureId != null){
						listData.add(featureId);
					} else {
						System.out.println("Feature not found : " + line);
					}
				}
			}
		} catch (IOException e) {
			System.out.println(e.getLocalizedMessage());
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					System.out.println(e.getLocalizedMessage());
					e.printStackTrace();
				}
			}
		}
		//make list
		returnVal.setGenes(listData);
		returnVal.setSourceId(genomeRef);
		returnVal.setId(getKbaseId("GeneList"));
		return returnVal;
	}
	
	private static HashMap<String,String> readFeatures (Genome genome) {
		HashMap<String,String> aliases = new HashMap<String, String>(); 
		for (Feature f : genome.getFeatures()){
			String id = f.getId();
			for (String a : f.getAliases()){
				//System.out.println("alias = " + a + " : id = " + id);
				aliases.put(a, id);
			}
		}
		return aliases;
	}
	
	
	protected static String getKbaseId(String entityType) {
		String returnVal = null;

		try {
			if (entityType.equals("GeneList")) {
				returnVal = "kb|genelist."
						+ idClient().allocateIdRange("kb|genelist", 1L)
								.toString();
			} else if (entityType.equals("InteractionSet")) {
				returnVal = "kb|interactionset."
						+ idClient().allocateIdRange("kb|interactionset", 1L)
								.toString();
			} else {
				System.err.println("ID requested for unknown type "
						+ entityType);
				return null;
			}
		} catch (UnauthorizedException e) {
			System.err.println("Unable to get KBase ID for " + entityType + " from " + ID_SERVICE_URL + ": Unauthorized Exception");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Unable to get KBase ID for " + entityType + " from " + ID_SERVICE_URL + ": IO Exception");
			e.printStackTrace();
		} catch (JsonClientException e) {
			System.err.println("Unable to get KBase ID for " + entityType + " from " + ID_SERVICE_URL + ": Json error");
			e.printStackTrace();
		}
		return returnVal;
	}


}
