package convertToUtf8;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainClass {
	
	public static final String DEFAULT_ENCODING = "UTF-8";
	public static final Logger logger = LogManager.getLogger("convertToUtf8Logger");

	public static void convertToUtf8(String dirPath, String encoding) {
		logger.info("Starting converting subtitles files!");
		logger.info("...");
		encoding = (encoding == null) ? DEFAULT_ENCODING : encoding;
		
		List<File> srtsFileToConvert = getSrtsFile(dirPath);
		
		if(srtsFileToConvert.size() > 0) {
			for (File currentSrtFile : srtsFileToConvert) {
				// Create the File object of the target File
				File targetFile = new File(currentSrtFile.getParent() + "/converted-"+currentSrtFile.getName());
				String pathSourceFile = currentSrtFile.getAbsolutePath();
				
				// Convert the file, delete the old one and leave only the new one
				convert(currentSrtFile, targetFile, encoding);
				currentSrtFile.delete();
				targetFile.renameTo(new File(pathSourceFile));
			}
		} else {
			logger.info("No subtitles files found");
		}
		
		logger.info("done convertToUtf8 program on dirPath {}", dirPath);
	}
	
	public static void convert(File sourceFile, File targetFile, String encoding) {
	    BufferedReader br = null;
	    StringBuilder builder = new StringBuilder();
	    BufferedWriter bw = null;
	    boolean successReading = true;

    	try {
			logger.info("Reading the file {} to BufferedReader", sourceFile);
			br = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile)));
			String line;
			
			logger.info("writing the srt file to the StringBuilder...");			
			while((line = br.readLine()) != null) {
				builder.append(line);
				builder.append(System.lineSeparator());
			}
		} catch (FileNotFoundException e) {
			logger.error("there is no file: {}  in directory: {}", sourceFile.getName(), sourceFile.getParent());
			successReading = false;
		} catch (IOException e) {
			logger.error("there was a problem reading file: {}", sourceFile.getAbsolutePath());
			successReading = false;
		} finally {
			if(br != null) {
				try {
					br.close();
				} catch (IOException e) {
					logger.fatal("error with closing BufferedReader");
				}
			}
		}
        
    	// If the reading part failed, there is no point in the writing part
    	if (!successReading) {
    		return ;
    	}
    	
        try {
			logger.info("Write all the StringBuilder to the target file: {}", targetFile);
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile), encoding));
			bw.write(builder.toString());
		} catch (UnsupportedEncodingException e) {
			logger.error("unable to write to file: {} with encoding {}", targetFile.getAbsolutePath(), encoding);
		} catch (FileNotFoundException e) {
			logger.error("file: {} not found in directory: {}", targetFile.getName(), targetFile.getParent());
		} catch (IOException e) {
			logger.error("there was a problem writing to file: {}", targetFile.getAbsolutePath());
		} finally {
			if(bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					logger.fatal("error with closing BufferedWriter");
				}
			}
		}
	}
	
	public static List<File> getSrtsFile(String directoryPath) {
		List<File> allSrtFiles = new ArrayList<File>();
		logger.info("Try to watch \"{}\" directory", directoryPath);
		File directory = new File(directoryPath);
		
		if(!directory.isDirectory()) {
			logger.error("The path \"{}\" is not a directory!", directoryPath);
			return new ArrayList<File>();
		}
		
		logger.info("Getting all the files in the directory");
		File[] listFiles = directory.listFiles();
		
		logger.info("Searching for all subtitles files recursivly");
		for (File currentFile : listFiles) {
			
			if(currentFile.getAbsolutePath().endsWith(".srt")) {
				logger.info("Found file: {}", currentFile.getName());
				allSrtFiles.add(currentFile);
			} else if(currentFile.isDirectory()) {
				logger.info("Searching in directory: {}", currentFile);
				allSrtFiles.addAll(getSrtsFile(currentFile.getAbsolutePath()));
			}
		}

		return allSrtFiles;
	}

}
