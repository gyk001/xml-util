package cn.guoyukun.xsd;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.log4j.Logger;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLValidator {
	
	//日志对象
	private static final Logger LOG = Logger.getLogger(XMLValidator.class);
	private static SAXParserFactory SAX_PARSER_FACTORY = SAXParserFactory.newInstance();
	private static SchemaFactory SCHEMA_FACTORY = SchemaFactory
			.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

	public static void main(String[] args) {
		String xxx = "xxx";
		InputStream is = new ByteArrayInputStream(xxx.getBytes());
		boolean res = isValidated(is, "/Users/Guo/Downloads/schema/xml.xsd");
		System.out.println(res);
	}
	

	
	public static boolean isValidated(String xmlPath,
			String schemaFilePathList) throws FileNotFoundException {
		return isValidated(xmlPath, schemaFilePathList, 3);
	}
	
	public static boolean isValidated(String xmlPath,
			String schemaFilePathList, final int maxErrorLineNum) throws FileNotFoundException {
		InputStream is = new FileInputStream(xmlPath);
		return isValidated(is, schemaFilePathList, maxErrorLineNum, LOG);
	}

	public static boolean isValidated(InputStream xml,
			String schemaFilePathList) {
		return isValidated(xml, schemaFilePathList, 3, LOG);
	}
	
	public static boolean isValidated(InputStream xml,
			String schemaFilePathList, Logger logger) {
		return isValidated(xml, schemaFilePathList, 3, logger);
	}
	
	/**
	 * 
	 * @param xmlFilePath
	 * @param schemaFilePathList
	 *            , it is comma split
	 * @param maxErrorLineNum
	 * @return boolean
	 */
	public static boolean isValidated(InputStream xml,
			String schemaFilePathList, final int maxErrorLineNum, final Logger logger) {
		final List<String> errorLines = new ArrayList<String>();
		boolean isValid = true;
		String[] schemaDirNames = schemaFilePathList.split(",");
		if (schemaDirNames == null || schemaDirNames.length == 0) {
			return false;
		}

		Source schemas[] = new Source[schemaDirNames.length];
		for (int i = 0; i < schemaDirNames.length; i++) {
			schemas[i] = new StreamSource(schemaDirNames[i]);
		}
		try {
			xml.reset();
			Schema schema = SCHEMA_FACTORY.newSchema(schemas);
			Validator validator = schema.newValidator();
			validator.setErrorHandler(new ErrorHandler() {
				public void error(SAXParseException e) throws SAXException {
					if (errorLines.size() < maxErrorLineNum) {
						errorLines.add("Line[" + e.getLineNumber() + "]" + " "
								+ "ERROR: " + e.getMessage());
					} else {
						logger.error(errorLines);
						throw new SAXException("[ERROR]: too many errors", e);
					}
				}

				public void fatalError(SAXParseException e) throws SAXException {
					logger.error("[FATAL]" , e);
					throw new SAXException("[FATAL]: Line[" + e.getLineNumber()
							+ "] " + e);
				}

				public void warning(SAXParseException e) {
					logger.warn("[WARN]: " , e);
				}
			});
			validator.validate(new StreamSource(xml));
			if (errorLines.size() != 0) {
				isValid = false;
			}
		} catch (SAXException e) {
			errorLines.add("SAXException: " + e.getMessage());
			isValid = false;
		} catch (FileNotFoundException e) {
			errorLines.add("FileNotFoundException: " + e.getMessage());
			isValid = false;
		} catch (IOException e) {
			errorLines.add("IOException: " + e.getMessage());
			isValid = false;
		}
		logger.error(errorLines);
		return isValid;

	}

	
	 /**
     * @param xmlFilePath
     * @return boolean
     */
    static public boolean isWellformed(InputStream is) {
    	try {
    		is.reset();
			SAXParser saxParser = SAX_PARSER_FACTORY.newSAXParser();
			DefaultHandler handler = new DefaultHandler();
			saxParser.parse(is, handler);
		} catch (Exception e) {
			return false;
		}
    	return true;
    }
}
