package hu.modeldriven.cameo;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.project.ProjectDescriptor;
import com.nomagic.magicdraw.core.project.ProjectDescriptorsFactory;
import com.nomagic.magicdraw.core.project.ProjectsManager;

import java.awt.event.ActionEvent;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainMenuAction extends MDAction {
    public MainMenuAction(String id, String name) {
        super(id, name, null, null);
    }

    // To be called onclick of MenuItem
    // ActionEvent actionEvent - event that happened to call this fn
    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        Project project = saveProject();
        String response = null;
        String fileName = project.getFileName();

        if (fileName != null) {fileName = fileName.replace(" ", "%20");} // Make usable as API param
        try {
            response = getFromAPI(fileName);
            Application.getInstance().getGUILog().showMessage(response);
        } catch (IOException e) {
            Application.getInstance().getGUILog().showMessage(e.getMessage());
        }
    }

    // Send path to XML file to HoustonAPI
    // (Assuming that HoustonAPI is running locally)
    // String fileName = path to XML file
    private String getFromAPI(String fileName) throws IOException {
        URL url = new URL("http://127.0.0.1:5000/result?xml=" + fileName);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        con.disconnect();

        return content.toString();
    }

    // Saves project to ensure we have current version
    private Project saveProject(){
        ProjectsManager projectsManager = Application.getInstance().getProjectsManager();
        Project project = projectsManager.getActiveProject();
        ProjectDescriptor projectDescriptor = ProjectDescriptorsFactory.getDescriptorForProject(project);
        projectsManager.saveProject(projectDescriptor, true);

        return project;
    }

}

/*
sending to python
{
    xml: String(all_the_xml_data)
}
receiving from python
{
    suggestion_1: String(suggestion)
    suggestion_2: String(suggestion)
}

step 1 - figure out jar files -> blocked until we talk to alejandro or someone else who knows magicdraw???
step 2 - figure out better way to send xml
        current- must save as .xml BEFORE hitting button to activate plugin
        future- hit button, send .xml immediately
step 3 - figure out suggestions from python
        How do we send suggestions back through api?
step 4 - update CAMEO ui to show suggestions


 */