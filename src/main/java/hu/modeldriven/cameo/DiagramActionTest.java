package hu.modeldriven.cameo;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class DiagramActionTest {

    @Test
    void getFromAPI() throws IOException {
        DiagramAction da = new DiagramAction("1", "test");
        String file = "C:\\Users\\timmo\\Houston-1\\data.xml";
        file = file.replace(" ", "%20");
        //String response = da.getFromAPI(file, "I-1");
        //System.out.println(response);

    }
}