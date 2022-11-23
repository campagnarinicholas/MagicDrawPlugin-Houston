package hu.modeldriven.cameo;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class MainMenuActionTest {

    @org.junit.jupiter.api.Test
    void testRecsGetFromAPI() throws IOException {
        MainMenuAction mmu = new MainMenuAction("1", "name");
        String file = "C:\\Users\\timmo\\Houston-1\\data.xml";
        file = file.replace(" ", "%20");
        //String response = mmu.getFromAPI(file, "recommendations", "TODO");
        //System.out.println(response);

    }

    @org.junit.jupiter.api.Test
    void testRulesGetFromAPI() throws IOException {
        MainMenuAction mmu = new MainMenuAction("1", "name");
        String file = "C:\\Users\\timmo\\Houston-1\\data.xml";
        file = file.replace(" ", "%20");
        //String response = mmu.getFromAPI(file, "rules", "TODO");
        //System.out.println(response);

    }

    @org.junit.jupiter.api.Test
    void testAddAttributesAndBehaviors(){
        ArrayList<String> attributes = new ArrayList<String>(){
            {
                add("+Resolution< = 0.1m");
                add("+Value = f1(Input 1)");
                add("+frequency = 1+/-0.1Hz");
            }
        };
        ArrayList<String> behaviors = new ArrayList<String>(){
            {
                add("+range_behavior = stop_program");
            }
        };
        MainMenuAction mmu = new MainMenuAction("1", "name");
        String request = "";
        //mmu.addAttributesAndBehaviors(attributes, behaviors);
        System.out.println(request);

    }
}