package fr.labri.demo.analysis;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.lightningbug.api.BugzillaClient;
import de.lightningbug.api.domain.Bug;
import de.lightningbug.api.service.BugService;
import fr.labri.harmony.core.analysis.AbstractAnalysis;
import fr.labri.harmony.core.config.model.AnalysisConfiguration;
import fr.labri.harmony.core.dao.Dao;
import fr.labri.harmony.core.model.Author;
import fr.labri.harmony.core.model.Source;

//TODO Hashmap
//TODO Statistiques
//TODO API JIRA
//TODO Bug trouvés non-linkés alors qu'ils existent ?!!

public class DemoAnalysis extends AbstractAnalysis{
	
	public static int nbFoundBugs = 0;
	public static int nbLinkedBugs = 0;
	

	public DemoAnalysis() {
		super();		
	}

	public DemoAnalysis(AnalysisConfiguration config, Dao dao, Properties properties) {
		super(config, dao, properties);
	}

	@Override
	public void runOn(Source src) throws MalformedURLException {

		Map<String, String> bugReport = bugzillaReportExtractor("https://issues.apache.org/bugzilla/", "jjalageas@yahoo.com", "pepsi718");
		
		
		ArrayList<String> links = new ArrayList<String>();

		for (Author auth : src.getAuthors()) {

			for (int i=0; i<auth.getEvents().size(); i++){

				String commitLog = auth.getEvents().get(i).getMetadata().get("commit_message");

				//link search
				ArrayList<String> link = compareLogToReport(commitLog, bugReport);
				if(link.size() > 0){
					for(String l: link){
						String linkDisplay = "Commit " + auth.getEvents().get(i).getNativeId() + " linked to bug " + l;
						links.add(linkDisplay);
					}			
				}

			}
		}

		for(String s: links)
			System.out.println(s);
		System.out.println();
		System.out.println("Nombre de bugs trouvés : " + nbFoundBugs);
		System.out.println("Nombre de bugs linkés : " + nbLinkedBugs);
		System.out.println("Nombre de bugs dans le Bugzilla : " + bugReport.size());

	}


	public ArrayList<String> compareLogToReport(String commitLog, Map<String, String> bugReportIds){

		ArrayList<String> linkReport = new ArrayList<String>();
		Pattern pattern = Pattern.compile("(\\d){5}");
		Matcher matcher = pattern.matcher(commitLog);

		while (matcher.find()) {
			String foundID = matcher.group();
			System.out.println("Id bug trouve : " + foundID);
			++nbFoundBugs;
				if(bugReportIds.containsKey(foundID)) {
					linkReport.add(foundID);
					++nbLinkedBugs;
				}

		}
		return linkReport;
	}


	public Map<String, String> bugzillaReportExtractor(String bugzillaAddress, String username, String password) throws MalformedURLException{

		Map<String, String> bugReport = new HashMap<>();
		BugzillaClient client = new BugzillaClient(new URL(bugzillaAddress), username, password);
		client.login();
		BugService bugService = new BugService(client);
		Map<String, Object[]> searchParams = new HashMap<String, Object[]>();
		searchParams.put("summary", new Object[]{"ant"});
		List<Bug> bugs = bugService.search(searchParams);
		for(Bug b: bugs) {
			String id = b.getId().toString();
			bugReport.put(id, id);

		}
		return bugReport;
	}


}



