package fr.labri.demo.analysis;

import java.io.File;

import moduleDatabase.DatabaseApiSql;

import org.eclipse.core.runtime.Path;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.tasks.core.TaskRepository;

import com.atlassian.connector.eclipse.internal.jira.core.JiraClientFactory;
import com.atlassian.connector.eclipse.internal.jira.core.JiraCorePlugin;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraClient;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraException;
import com.atlassian.connector.eclipse.internal.jira.core.wsdl.beans.RemoteIssue;
import com.atlassian.connector.eclipse.internal.jira.core.wsdl.soap.JiraSoapService;
import com.atlassian.connector.eclipse.internal.jira.core.wsdl.soap.RemoteAuthenticationException;
import com.atlassian.connector.eclipse.internal.jira.core.wsdl.soap.RemoteException;


//TODO Rendre accessible la manipulation de la database
//TODO Dans les module database rendre possible la manipulation d'une table par projet
//TODO Creer deux interfaces pour bdd : une pour remplir, une pour extraire
//TODO Résoudre le probleme des 200 bugs maxi 

@SuppressWarnings("restriction")
public class JiraDataExtractor {

static RemoteIssue[] issues;

	public static void main(String args []) {

		String repositoryUrl = "https://issues.apache.org/jira/";
		String login = "gmeral";
		String password = "harmony";
		String projectKey = "AMQ";


		//Initialisation manuelle du plugin 
		JiraClientFactory clientFactory;
		File serverCache = new Path("serverCache").toFile(); //$NON-NLS-1$
		JiraCorePlugin.initialize(serverCache);


		TaskRepository repository = new TaskRepository(JiraCorePlugin.CONNECTOR_KIND, repositoryUrl); 
		repository.setCredentials(AuthenticationType.REPOSITORY, new AuthenticationCredentials(login, password), false);
		clientFactory = JiraClientFactory.getDefault();	
		JiraClient client = clientFactory.getJiraClient(repository);
		com.atlassian.connector.eclipse.internal.jira.core.service.soap.JiraSoapClient soapClient = client.getSoapClient();
		JiraSoapService soapService;
		String authentificationToken = new String();
		try {
			soapService = soapClient.getSoapService();
			authentificationToken = soapService.login(login, password);
		} catch (JiraException e1) {
			e1.printStackTrace();
		} catch (RemoteAuthenticationException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (java.rmi.RemoteException e) {
			e.printStackTrace();
		}

		DatabaseApiSql issueDatabase = new DatabaseApiSql();

		try {
			String[] projects = {projectKey};

			issues = soapClient.getSoapService().getIssuesFromTextSearchWithProject(authentificationToken, projects, "", 200);
			for(RemoteIssue i : issues) {
				System.out.println(i.getKey());
				issueDatabase.setNewIssue(i.getKey(), i.getStatus());
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (java.rmi.RemoteException e) {
			e.printStackTrace();
		} catch (JiraException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}

	} 
}