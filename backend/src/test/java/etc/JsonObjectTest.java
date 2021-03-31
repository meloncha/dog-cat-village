package etc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

public class JsonObjectTest {

    private static final Gson gson = new GsonBuilder().create();

    @Test
    public void test (){

        String json = "{" +
                "\"test\":\"a\"}";

        System.out.println(json);
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

        System.out.println(jsonObject.get("test").getAsString());
        jsonObject.get("test2").getAsString();
    }
}
