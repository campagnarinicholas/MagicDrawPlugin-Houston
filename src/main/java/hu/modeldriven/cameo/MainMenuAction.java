package hu.modeldriven.cameo;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;

import java.awt.event.ActionEvent;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class MainMenuAction extends MDAction {
    private static String rootPath = "C:\\Program Files\\MagicDraw Demo\\";
    public MainMenuAction(String id, String name) {
        super(id, name, null, null);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        String message = getMessage();
        String response = null;
        try {
            response = getFromAPI(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Application.getInstance().getGUILog().showMessage(response);
    }

    public String getMessage() {
        try {
            String xmlFilepath = rootPath + "ForTesting.xml";
            BufferedReader reader = new BufferedReader(new FileReader(xmlFilepath));
            String currLine;
            String buffer = "";
            while((currLine = reader.readLine()) != null) {
                buffer += currLine;
            }
            return buffer;
        } catch(FileNotFoundException f) {
            return "Please save file as xml first";
        } catch(IOException i) {
            return "File is empty";
        }
    }

    public String getFromAPI(String param) throws IOException {
        URL url = new URL("http://127.0.0.1:5000/result?xml=./ForTesting.xml");
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

}