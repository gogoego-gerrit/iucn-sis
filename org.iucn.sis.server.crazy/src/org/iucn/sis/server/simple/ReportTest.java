package org.iucn.sis.server.simple;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.iucn.sis.server.utils.ReportGenerator;
import org.iucn.sis.server.utils.ReportGenerator.ByteReport;
import org.iucn.sis.server.utils.templates.AssessmentTemplate;
import org.iucn.sis.shared.DisplayData;
import org.iucn.sis.shared.data.assessments.AssessmentParser;
import org.iucn.sis.shared.data.assessments.FieldParser;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;
import org.iucn.sis.shared.taxonomyTree.TaxonNodeFactory;

import com.solertium.lwxml.java.JavaNativeDocument;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.vfs.VFS;

public class ReportTest {

	private static void build(final String pathRoot) {
		try {
			String path = pathRoot + "2142.xml";
			StringBuilder xml = readFileAsString(path);
			NativeDocument ndoc = new JavaNativeDocument();
			ndoc.parse(xml.toString());
			AssessmentParser assParser = new AssessmentParser();
			assParser.parse(ndoc);

			path = pathRoot + "15951.xml";
			xml = readFileAsString(path);
			NativeDocument taxaDoc = new JavaNativeDocument();
			taxaDoc.parse(xml.toString());

			TaxonNode taxon = TaxonNodeFactory.createNode(taxaDoc);

			ReportGenerator rGen = new ReportGenerator(new AssessmentTemplate() {
				@Override
				protected DisplayData fetchDisplayData(String canonicalName, VFS vfs) {
					try {
						String path = pathRoot + canonicalName + ".xml";
						StringBuilder xml = readFileAsString(path);
						NativeDocument jnd = new JavaNativeDocument();
						jnd.parse(xml.toString());

						FieldParser parser = new FieldParser();
						DisplayData currentDisplayData = parser.parseFieldData(jnd);
						return currentDisplayData;
					} catch (IOException e) {
						e.printStackTrace();
						return null;
					}
				}
			});
			ByteReport report = rGen.getPDFReport(assParser.getAssessment(), taxon);

			FileOutputStream out = new FileOutputStream(new File(pathRoot + "2142.pdf"));
			out.write(report.getByteArray());
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		build("/home/adam.schwartz/blat/");
	}

	private static StringBuilder readFileAsString(String absolutePath) throws IOException {
		InputStream in = new FileInputStream(new File(absolutePath));
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuilder xml = new StringBuilder();
		String line = "";

		while ((line = reader.readLine()) != null) {
			xml.append(line);
			xml.append("\n");
		}
		return xml;
	}
}
