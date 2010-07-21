package org.iucn.sis.server.extensions.reports;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InvalidClassException;

import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Taxon;

public class ReportGenerator {

	public static class ByteReport {
		private ByteArrayOutputStream outputStream;
		static final long serialVersionUID = 1;

		public ByteReport() {
			outputStream = new ByteArrayOutputStream();
		}

		public byte[] getByteArray() {
			return outputStream.toByteArray();
		}

		public ByteArrayOutputStream getOutputStream() {
			return outputStream;
		}

		public String getString() {
			return outputStream.toString();
		}

		public void writeToFile(String filename) throws Exception {
			System.out.println("writing file");
			File newFile = new File(filename);
			boolean created = newFile.createNewFile();
			if (created)
				System.out.println("New file created");
			FileOutputStream fout = new FileOutputStream(newFile);
			outputStream.writeTo(fout);
			fout.close();
		}
	}

	private Document document;
	private ByteReport report;
	private ReportTemplate template;

	public ReportGenerator(Class templateClass) throws InvalidClassException {
		setTemplate(templateClass);
	}

	public ReportGenerator(ReportTemplate template) throws InvalidClassException {
		setTemplate(template);
	}

	public ByteReport getPDFReport(Assessment assessment) {
		return getPDFReport(assessment, null);
	}

	public ByteReport getPDFReport(Assessment assessment, Taxon taxon) {
		document = template.getDocument();
		report = new ByteReport();
		PdfWriter writer = null;
		try {
			writer = PdfWriter.getInstance(document, report.getOutputStream());
		} catch (Exception e) {
			e.printStackTrace();
		}
		template.setWriter(writer);
		template.parse(assessment, taxon);

		return report;
	}

	public ByteReport getRTFReport(Assessment assessment) {
		document = template.getDocument();
		report = new ByteReport();
		RtfWriter2 writer = null;
		try {
			writer = RtfWriter2.getInstance(document, report.getOutputStream());
		} catch (Exception e) {
			e.printStackTrace();
		}
		template.setWriter(writer);
		template.parse(assessment);

		return report;
	}

	public void setTemplate(Class templateClass) throws InvalidClassException {
		if (!(ReportTemplate.class.isAssignableFrom(templateClass))) {
			throw new InvalidClassException("Template not instance of ReportTemplate.");
		}
		try {
			this.template = (ReportTemplate) templateClass.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setTemplate(ReportTemplate template) throws InvalidClassException {
		this.template = template;
	}

}
