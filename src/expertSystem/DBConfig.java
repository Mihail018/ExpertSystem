package expertSystem;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class DBConfig 
{
    private String url;
    private String username;
    private String password;

    public DBConfig() 
    {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream("src/resources/config.properties")) 
        {
            properties.load(fis);
            url = properties.getProperty("db.url");
            username = properties.getProperty("db.username");
            password = properties.getProperty("db.password");
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }

    public String getUrl() { return url; }

    public String getUsername() { return username; }

    public String getPassword() { return password; }
}