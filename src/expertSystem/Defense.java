package expertSystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import java.util.ArrayList;

public class Defense 
{
    public static int checkId(String table, String URL, String USERNAME, String PASSWORD)
    {
        int id = 0;

        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD); Statement statement=connection.createStatement())
        {
            ResultSet result = statement.executeQuery("SELECT max(id) as id from lifp_lab2." + table);
            if (result.next()) id = result.getInt("id");

            statement.close();
            connection.close();
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }

        return id;
    }

    public static ArrayList<Spinner<Double>> checkEmptyWeights(ArrayList<Spinner<Double>> arr, String table, String URL, String USERNAME, String PASSWORD)
    {
        if (arr.isEmpty())
        {
            int count=0;
            try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD); Statement statement=connection.createStatement())
            {
                ResultSet result = statement.executeQuery("SELECT max(id) as id from lifp_lab2." + table);
                if (result.next()) count = result.getInt("id");

                statement.close();
                connection.close();
            }
            catch (SQLException ex)
            {
                ex.printStackTrace();
            }

            for (int i=0; i<count; i++) 
            {
                Spinner<Double> spin = new Spinner<Double>(0.0, 1.0, 0);
                spin.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 1, 0, 0.01));
                spin.setEditable(true);
                arr.add(spin);
            }
        }

        return arr;
    }
}