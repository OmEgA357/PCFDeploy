import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.cloudfoundry.client.lib.CloudCredentials;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.cloudfoundry.client.lib.domain.CloudService;
import org.cloudfoundry.client.lib.domain.CloudSpace;
import org.cloudfoundry.client.lib.domain.Staging;

public class PCFRun {

	private static final String SPACE_NAME = "development";
	private static final String ORG_NAME = "vaibhav_sawant-org";
	private static final String JAVA_BUILDPACK = "https://github.com/cloudfoundry/java-buildpack";
	private static final String APP_NAME = "PushStudioAppTest";
	
	public static void main(String[] args) throws IOException {
		Properties prop = new Properties();
		InputStream input = null;
		input = new FileInputStream("config.properties");

		prop.load(input);

		String target = prop.getProperty("target");
		String user = prop.getProperty("user");
		String password = prop.getProperty("password");
		
	  /* **************** Set proxy **************** */
		
		System.getProperties().put( "proxySet", "true" );
		System.getProperties().put( "proxyHost", "172.25.74.10" );
		System.getProperties().put( "proxyPort", "2006" );
	
		
		/*System.setProperty("java.net.useSystemProxies", "true");*/
		CloudCredentials credentials = new CloudCredentials(user, password);
		CloudFoundryClient client = new CloudFoundryClient(credentials, getTargetURL(target),ORG_NAME,SPACE_NAME);
		client.login();

		System.out.println("%nSpaces:%n");
		for (CloudSpace space : client.getSpaces()) {
			System.out.printf("  %s/t(%s)%n", space.getName(), space.getOrganization().getName());
		}

		System.out.printf("%nApplications:%n");
		for (CloudApplication application : client.getApplications()) {
			System.out.printf("  %s%n", application.getName());
		}

		System.out.printf("%nServices%n");
		for (CloudService service : client.getServices()) {
			System.out.printf("  %s/t(%s)%n", service.getName(), service.getLabel());
		}
		
		
		String baseUrl = "http://"+APP_NAME+".cfapps.io";
		List<String> uris = Arrays.asList(baseUrl);
		
		client.createApplication(APP_NAME,new Staging(null,JAVA_BUILDPACK),1024,1024,uris,null);
		File appFile = new File("D:/10636747/AppJWorkspaceMicroService/ProjectOne-microservice-module/target/Studio-0.0.1-SNAPSHOT.jar");
		try {
			client.uploadApplication(APP_NAME, appFile);
			System.out.println("URLs: "+client.getApplication(APP_NAME).getUris());
			System.out.println("State: "+client.getApplication(APP_NAME).getState());
		} catch (IOException e) {
			e.printStackTrace();
		}
		client.startApplication(APP_NAME);
		System.out.println("Done");
	}

	private static URL getTargetURL(String target) {
		try {
			return  URI.create("https://api.run.pivotal.io").toURL();
		} catch (MalformedURLException e) {
			throw new RuntimeException("The target URL is not valid: " + e.getMessage());
		}
	}

}
