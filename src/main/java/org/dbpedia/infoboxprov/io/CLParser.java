package org.dbpedia.infoboxprov.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeSet;


import org.apache.jena.atlas.logging.Log;
import org.dbpedia.infoboxprov.dump.SingleArticle;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;


public class CLParser extends JCommander {
	
	 private String newDate =new SimpleDateFormat("yyyy-MM-dd").format(new Date());
	 
	 @Parameter(names={"-help", "-h"} , description = "Print help information and exit", required = false)
	 private boolean showHelp = false;
	 @Parameter(names={"-name", "-a"} , description = "Name of the Article", required = false)
	 private String singleArticle = null;
	 @Parameter(names={"-later", "-l"} , description = "Last timestamp(Date in yyyy-MM-dd) to extract", required = false)
	 private String later = newDate;
	 @Parameter(names={"-earlier", "-e"} , description = "Earliest timestamp (Date in yyyy-MM-dd) to extract", required = false)
	 private String earlier = "2001-01-02";
	 @Parameter(names={"-rerun", "-r"} , description = "Rerun program after a crash", required = false)
	 private boolean rerun = false;
	 @Parameter(names="-path" , description = "Path to the dump containing directory", required = false)
	 private String path = null;
	 @Parameter(names={"-language", "-lang"}, description = "Dump Language", required = false)
	 private String language = "en";
	 @Parameter(names={"-threads", "-t"} , description = "Number of threads to run", required = false)
	 private int threads = 1;
	 @Parameter(names={"-threadsF", "-tf"} , description = "Number of parallel processed Files", required = false)
	 private int threadsF = 1;
	 @Parameter(names={"-lastchange", "-last"} , description = "Only last change to an existing triple will be saved", required = false)
	 private boolean lastChange = false;
	 @Parameter(names={"-debug", "-d"} , description = "x", required = false)
	 private boolean debug = false;
	 
	 private READVARIANT readvariant = READVARIANT.ReadDefault;
	 private TimeFrame timeFrame = null;
	 private TreeSet<Integer> finishedArticles = null;
	 private JCommander jCommander = null;
	 
	 public CLParser(String[] args) {
		 
		jCommander = new JCommander(this);
		try {
			
			jCommander.parse(args);
			
			if (this.showHelp) {
                help();
                System.exit(0);
            }

		}catch(ParameterException e) {
			
			if(e.getMessage().contains("Was passed main")) {
				
			  System.out.println("Wrong parameter name");
			}
			else {
	
			  System.out.println(e.getMessage());
			}
			
			help();
			System.exit(1);
		}
	 }
	 
	 public CLParser getCLParser(){
		 return this;
	 }
	 
	 public String getPath(){
		 return path;
	 }
	 
	 public boolean getDebug(){
		 return debug;
	 }
	 
	 public String getLanguage(){
		 return language;
	 }
	 
	 public int getThreads(){
		 return threads;
	 }
	 
	 public int getThreadsF(){
		 return threadsF;
	 }
	 
	 public READVARIANT getReadvarian(){
		 return readvariant;
	 }
	 
	 public TreeSet<Integer> getFinishedArticles(){
		 return finishedArticles;
	 }
	 
	 public boolean getVariant(){
		 return lastChange;
	 }
	 
	 public TimeFrame getTimeFrame() {
		 return timeFrame;
	 }
	 
	 public String getSingleArticle() {
		 return singleArticle;
	 }
	 
	 public void validate(){
		 
		 
		 this.timeFrame = new TimeFrame(earlier, later);
		 
		 
		 if(timeFrame.getTimeFrame() != null){
			 
			 readvariant = READVARIANT.ReadTimeFiltered;
			
		 } else if (rerun){
			
			 readvariant = READVARIANT.ReadRerun;
			 readLogs("log");
			 
		 } else if (rerun && timeFrame.getTimeFrame() != null){
			 
			 readvariant = READVARIANT.ReadTimeFilteredRerun;
			 readLogs("log");
		 }
		 
		 try{
			 
			 
			 if(singleArticle == null && path == null) 
			 throw new ParameterException("Article name or dump path needed");  	 
			 
			 if(singleArticle != null && path != null)
			 throw new ParameterException("Parameter singleArticle doesn't need a path"); 
			 
			 if(threadsF <= 0) {
				 threadsF = 1;
			 }	 
			 
			 if(singleArticle != null && threads != 1) {
			 threads = 1;
			 threadsF = 1;
			 System.out.println("Set maxthread to 1 in case of single Article");
			 System.out.println("Set maxthreadF to 1 in case of single Article");
			 }
			 
			 if(singleArticle != null) {
				
				 SingleArticle singelArticel;
				 String timestamp;
				 if(new File("ArticleDumps/"+singleArticle+".xml").exists()) {
					 
					 new File("ArticleDumps/"+singleArticle+".xml").delete();
				 }
				 
				 if(timeFrame.getTimeFrame() != null){
			
				 timestamp = later + "T00:00:00Z";
				
					 
				 }else {
					 
					 timestamp = "";
				 }
				 
				  
				  while(true) {
				 
		
					  singelArticel = new SingleArticle(this);
					  if(!singelArticel.setPathForArticle(timestamp)) {
			
						  break;
					  }
					  
					  singelArticel.readPageDefault();
					
					  timestamp = singelArticel.getTimestampt();
					 		  
					  if(timestamp ==null) {
						
						PrintWriter wr;
						try {
							
						  wr = new PrintWriter(new FileWriter(new File("ArticleDumps/"+singleArticle+".xml"),true));
			        	  wr.println("</page>");
			        	  wr.println("</mediawiki>");
			        	  wr.close();
			        	  
						 } catch (IOException e) {
							// TODO Auto-generated catch block
							System.out.println("CLParser: Writer");
						 }
					
						 new File("ArticleDumps/tmp.xml").delete();
						 break;
					   }
					  
					   try {
						  
					   Date timestampToDate = new SimpleDateFormat("yyyy-MM-dd")
										.parse(timestamp);
						
					   Date earlierToDate = new SimpleDateFormat("yyyy-MM-dd")
									.parse(earlier);
							
					   if(timestampToDate.before(earlierToDate)) {
						  
						 PrintWriter wr;
						 try {
								
						   wr = new PrintWriter(new FileWriter(new File("ArticleDumps/"+singleArticle+".xml"),true));
				           wr.println("</page>");
				           wr.println("</mediawiki>");
				           wr.close();
				        	  
						 } catch (IOException e) {
							// TODO Auto-generated catch block
							System.out.println("CLParser: Writer");
						 }
						
						 new File("ArticleDumps/tmp.xml").delete();
						 break;
					  }
					  
					  } catch (ParseException e1) {
							System.out.println("CLParser: Date parse Exception");
						}
					  
					  
				  }// end while
				  
				  path = "ArticleDumps/"+singleArticle+".xml";
		
			}
			
			 
		 }catch(ParameterException e){
			 System.out.println(e.getMessage());
			 help();
			 System.exit(1);
		 }
		 
		
	    
		 
	 }
	 
	 public void help() {
		 
		 System.out.println();
		 jCommander.usage();
	 }
	 
	 /**
	 * method to extract the article ids out of log files in /log/
	 *
	 * @param pathToLogs path to the logs created by an earlier run
	 */
	public void readLogs(String pathToLogs) {
		File logDirectory = new File(pathToLogs);
		finishedArticles = new TreeSet<>();
		String temporaryArticleID;
		for (File logFile : logDirectory.listFiles()) {
			System.out.println(logFile.toString());
			try (BufferedReader br = new BufferedReader(
					new FileReader(logFile))) {
				br.readLine();
				while ((temporaryArticleID = br.readLine()) != null) {
					System.out.println(temporaryArticleID);
					finishedArticles.add(Integer.parseInt(temporaryArticleID));
				}
			} catch (FileNotFoundException e) {
				Log.error(e, "File not found!");
				continue;
			} catch (IOException e) {
				Log.error(e, "COULD_NOT_READ_OR_WRITE_CONTEXT");
			}
		}
	}// end readLog
	 
	 
	 
}