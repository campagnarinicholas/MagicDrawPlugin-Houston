package hu.modeldriven.cameo;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class MainMenuActionTest {

    @org.junit.jupiter.api.Test
    void testGetFromAPI() throws IOException {
        MainMenuAction mmu = new MainMenuAction("1", "name");
        String file = "C:\\Users\\timmo\\Houston-1\\data.xml";
        file = file.replace(" ", "%20");
        String response = mmu.getFromAPI(file);
        System.out.println(response);

    }
}