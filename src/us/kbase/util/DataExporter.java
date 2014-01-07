package us.kbase.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import us.kbase.auth.TokenFormatException;
import us.kbase.common.service.JsonClientException;
import us.kbase.inferelator.GeneList;
import us.kbase.inferelator.InferelatorServerImpl;

public class DataExporter {

	private static final String GENE_LIST_NAME = InferelatorServerImpl.inputTflistFileName;

	public static void exportGeneList(String setRef,
			String workDir, String token) throws TokenFormatException,
			IOException, JsonClientException {
		GeneList list = WsDeluxeUtil.getObjectFromWsByRef(setRef, token)
				.getData().asClassInstance(GeneList.class);
		if (list != null) {
			BufferedWriter writer = new BufferedWriter(new FileWriter(workDir
					+ GENE_LIST_NAME));

			for (String gene : list.getGenes()) {
				writer.write(gene + "\n");
			}
			list = null;
			if (writer != null)
				writer.close();
		}
	}

}
