import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Pair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.File;
import expertSystem.DBConfig;
import expertSystem.Defense;
import expertSystem.Facts;
import expertSystem.Trust;
import expertSystem.WashingMachine;

public class Main extends Application
{
    private static String URL;
    private static String USERNAME;
    private static String PASSWORD;

    private static Driver driver;

    private ResultSet result;
    private ArrayList<WashingMachine> arr;
    private ArrayList<Facts> facts;
    private ArrayList<Trust> trusts;
    private ArrayList<Boolean> answers;
    private ArrayList<Spinner<Double>> factTextFields;

    private Text text;

    private Button playButton;
    private Button databaseButton;
    private Button addMachineButton;
    private Button changeMachineButton;
    private Button deleteMachineButton;
    private Button addFactButton;
    private Button updateFactButton;
    private Button deleteFactButton; 
    private Button backButton;

    private Scene scene;

    private int questionsCounter=1;

    public static void main(String[] args) throws Exception 
    {
        launch(args);
    }

    @Override
    public void init() throws Exception
    {
        try
        {
            driver = new com.mysql.cj.jdbc.Driver();
            DriverManager.registerDriver(driver);
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }

        DBConfig config = new DBConfig();
        URL = config.getUrl();
        USERNAME = config.getUsername();
        PASSWORD = config.getPassword();

        arr = new ArrayList<WashingMachine>();
        facts = new ArrayList<Facts>();
        trusts = new ArrayList<Trust>();
        answers = new ArrayList<Boolean>();
        factTextFields = new ArrayList<Spinner<Double>>();

        super.init();
    }

    @Override
    public void start(Stage window) throws Exception 
    {
        VBox root = new VBox(10);
        root.setAlignment(javafx.geometry.Pos.CENTER);
        root.setPadding(new Insets(10));
        GridPane gridPane = new GridPane(10, 10);
        gridPane.setAlignment(javafx.geometry.Pos.CENTER);
        HBox hBox = new HBox(10);
        hBox.setAlignment(javafx.geometry.Pos.CENTER);
        hBox.setPadding(new Insets(50));

        text = new Text();
        text.setText("Выберите действие");
        text.getStyleClass().add("text-widget");

        playButton = new Button("Запустить ЭС");
        playButton.setOnAction(e -> 
        {
            System.out.println("ЭС запущена!");

            try(Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD); Statement statement=connection.createStatement())
            {
                result = statement.executeQuery("SELECT * FROM lifp_lab2.washing_machines;");
                while (result.next())
                {
                    arr.add(new WashingMachine(result.getString("name"), new Image(result.getString("image"))));
                }

                result = statement.executeQuery("SELECT * FROM lifp_lab2.facts;");
                while (result.next())
                {
                    facts.add(new Facts(result.getString("name"), result.getDouble("weight")));
                }

                result = statement.executeQuery("SELECT * FROM lifp_lab2.trust;");
                while (result.next())
                {
                    trusts.add(new Trust(result.getInt("washingMachine_id"), result.getInt("fact_id"), 
                    result.getDouble("trust_cf")));
                }

                connection.close();
                statement.close();
            }
            catch (SQLException ex)
            {
                ex.printStackTrace();
            }

            root.getChildren().clear();

            Collections.sort(trusts, Comparator.comparing(Trust::getWashingMachineId).thenComparing(Trust::getFactId));

            Text showQuestion = new Text();
            showQuestion.getStyleClass().add("text-widget");
            Text showNumberOfQuestion = new Text();
            showNumberOfQuestion.getStyleClass().add("text-widget");

            Button yesButton = new Button("Да");
            yesButton.setOnAction(ev ->
            {
                answers.add(true);

                if (questionsCounter >= facts.size()) 
                {
                    endOfTest(window, root);
                }
                else
                {
                    questionsCounter++;
                    showNumberOfQuestion.setText("Вопрос " + String.valueOf(questionsCounter) + "/" + facts.size());
                    showQuestion.setText(facts.get(questionsCounter - 1).getName());
                }

            });
            Button noButton = new Button("Нет");
            noButton.setOnAction(ev ->
            {
                answers.add(false);

                if (questionsCounter >= facts.size()) 
                {
                    endOfTest(window, root);
                }
                else
                {
                    questionsCounter++;
                    showNumberOfQuestion.setText("Вопрос " + String.valueOf(questionsCounter) + "/" + facts.size());
                    showQuestion.setText(facts.get(questionsCounter - 1).getName());
                }
            });
                
            showNumberOfQuestion.setText("Вопрос " + String.valueOf(questionsCounter) + "/" + facts.size());
            showQuestion.setText(facts.get(questionsCounter - 1).getName());

            root.getChildren().addAll(showNumberOfQuestion, showQuestion, yesButton, noButton, backButton);
        }
        );

        databaseButton = new Button("Управление базой знаний");
        databaseButton.setOnAction(event ->
        {
            root.getChildren().clear();
            gridPane.getChildren().clear();
            gridPane.addColumn(0, addMachineButton, changeMachineButton, deleteMachineButton);
            gridPane.addColumn(1, addFactButton, updateFactButton, deleteFactButton);
            root.getChildren().addAll(gridPane, backButton);
        });

        backButton = new Button("На главную");
        backButton.setOnAction(ev ->
        {
            if (!answers.isEmpty())
            {
                answers.clear();
                facts.clear();
                arr.clear();
                trusts.clear();
                questionsCounter=1;
            }
            root.getChildren().clear();
            gridPane.getChildren().clear();
            hBox.getChildren().clear();
            root.getChildren().addAll(playButton, databaseButton);
        });

        addMachineButton = new Button("Добавить стиральную машину");
        addMachineButton.setOnAction(e -> 
        {
            root.getChildren().clear();
            hBox.getChildren().clear();

            Text txt = new Text("Введите модель стиральной машины: ");
            txt.getStyleClass().add("text-widget");

            TextField nameText = new TextField();
            nameText.setPromptText("Модель стиральной машины");

            ImageView imageView = new ImageView("resources/washingMachinesLogo/noImage.png");
            imageView.setFitWidth(300);
            imageView.setFitHeight(300);

            Button choosePhotoButton = new Button("Выбрать фото");
            choosePhotoButton.setOnAction(ev ->
            {
                imageView.setImage(getPhoto(window));
            });

            Button enterWeightsButton = new Button("Ввести веса");
            enterWeightsButton.setOnAction(ev ->
            {
                enterWeights(window, -1);
            });

            Button enterAddButton = new Button("Подтвердить");
            enterAddButton.setOnAction(ev -> 
            {
                WashingMachine addWashingMachine = new WashingMachine(nameText.getText(), imageView.getImage());

                try(Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD); Statement statement=connection.createStatement())
                {
                    int count=0;
                    result = statement.executeQuery("SELECT COUNT(*) as count FROM lifp_lab2.washing_machines;");
                    if (result.next()) count=Integer.parseInt(result.getString("count"));

                    String query = "INSERT lifp_lab2.washing_machines(id, name, image) VALUES (?, ?, ?);";
                    PreparedStatement preparedStatement = connection.prepareStatement(query);
                    preparedStatement.setInt(1, count+1);
                    preparedStatement.setString(2, addWashingMachine.getName());
                    preparedStatement.setString(3, addWashingMachine.getImage().getUrl());
                    preparedStatement.executeUpdate();

                int countMachines=0;
                result = statement.executeQuery("SELECT COUNT(*) as count FROM lifp_lab2.washing_machines");
                if (result.next()) countMachines=result.getInt("count");

                int countId=0;
                result = statement.executeQuery("SELECT COUNT(*) as count FROM lifp_lab2.trust");
                if (result.next()) countId=result.getInt("count");
                countId++;

                factTextFields=Defense.checkEmptyWeights(factTextFields, "facts", URL, USERNAME, PASSWORD);

                for (int i=0; i<factTextFields.size(); i++)
                {
                    query = "INSERT INTO lifp_lab2.trust (id, washingMachine_id, fact_id, trust_cf) VALUES (?, ?, ?, ?)";
                    preparedStatement = connection.prepareStatement(query);                    
                    preparedStatement.setInt(1, countId++);
                    preparedStatement.setInt(2, countMachines);
                    preparedStatement.setInt(3, i+1);
                    preparedStatement.setDouble(4, factTextFields.get(i).getValue());
                    preparedStatement.executeUpdate();
                }

                    connection.close();
                    statement.close();
                }
                catch (SQLException ex)
                {
                    ex.printStackTrace();
                }

                factTextFields.clear();

                System.out.println("Элемент добавлен!");

                onMain(root, "Стиральная машина успешно добавлена!");
            });

            hBox.getChildren().addAll(txt, nameText);

            HBox.setHgrow(nameText, Priority.ALWAYS);
            VBox.setMargin(hBox, new Insets(0, 0, -50, 0));
            root.getChildren().addAll(hBox, imageView, choosePhotoButton, enterWeightsButton, enterAddButton, backButton);
        }
        );

        changeMachineButton = new Button("Изменить стиральную машину");
        changeMachineButton.setOnAction(e -> 
        {
            root.getChildren().clear();
            hBox.getChildren().clear();
            Text txt1 = new Text("Введите id элемента, который хотите изменить");
            txt1.getStyleClass().add("text-widget");
            int maxValue = Defense.checkId("washing_machines", URL, USERNAME, PASSWORD);
            Spinner<Integer> requestTextField = new Spinner<Integer>();
            requestTextField.setPromptText("Введите id элемента, который хотите изменить");
            if (maxValue!=0)
            requestTextField.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, maxValue, 1, 1));
            else onMain(root, "Ошибка! В базе данных отсутствуют элементы");
            requestTextField.setEditable(true);

            Button enterButton = new Button("Подтвердить");

            hBox.getChildren().addAll(txt1, requestTextField);
            HBox.setHgrow(requestTextField, Priority.ALWAYS);
            VBox.setMargin(hBox, new Insets(0, 0, -50, 0));
            root.getChildren().addAll(hBox, enterButton, databaseButton);

            enterButton.setOnAction(event -> 
            {
                root.getChildren().clear();

                Text txt = new Text("Введите модель стиральной машины: ");
                txt.getStyleClass().add("text-widget");

                TextField nameText = new TextField();
                nameText.setPromptText("Модель стиральной машины");

                ImageView imageView = new ImageView("resources/logo.png");
                imageView.setFitWidth(300);
                imageView.setFitHeight(300);
                

                try(Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD); Statement statement=connection.createStatement())
                {
                    result = statement.executeQuery("SELECT * FROM lifp_lab2.washing_machines WHERE id = " + requestTextField.getValue());

                    if (result.next())
                    {
                        nameText.setText(result.getString("name"));
                        imageView.setImage(new Image(result.getString("image")));
                    }

                    connection.close();
                    statement.close();
                }
                catch (SQLException ex)
                {
                    ex.printStackTrace();
                }

                Button enterWeightsButton = new Button("Изменить веса");
                enterWeightsButton.setOnAction(ev ->
                {
                    enterWeights(window, requestTextField.getValue());
                });

                Button enterUpdateButton = new Button("Подтвердить");
                enterUpdateButton.setOnAction(ev ->
                {
                    try(Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD); Statement statement=connection.createStatement())
                    {
                        String query = "UPDATE lifp_lab2.washing_machines SET name = ?, image = ? where id = ?;";
                        PreparedStatement preparedStatement = connection.prepareStatement(query);                    
                        preparedStatement.setString(1, nameText.getText());
                        preparedStatement.setString(2, imageView.getImage().getUrl());
                        preparedStatement.setInt(3, requestTextField.getValue());
                        preparedStatement.executeUpdate();

                        for (int i=0; i<factTextFields.size(); i++)
                        {
                            query = "UPDATE lifp_lab2.trust SET trust_cf = ? where washingMachine_id = ? AND fact_id = ?;";
                            preparedStatement = connection.prepareStatement(query);                    
                            preparedStatement.setDouble(1, factTextFields.get(i).getValue());
                            preparedStatement.setInt(2, requestTextField.getValue());
                            preparedStatement.setInt(3, i+1);
                            preparedStatement.executeUpdate();
                        }
                    }
                    catch (SQLException ex)
                    {
                        ex.printStackTrace();
                    }

                    factTextFields.clear();
                    System.out.println("Элемент изменен!");

                    onMain(root, "Элемент успешно изменен!");
                });

                Button choosePhotoButton = new Button("Выбрать фото");
                choosePhotoButton.setOnAction(ev ->
                {
                        imageView.setImage(getPhoto(window));
                });

                root.getChildren().addAll(txt, nameText, imageView, choosePhotoButton, enterWeightsButton, enterUpdateButton, backButton);
            });
        }
        );

        deleteMachineButton = new Button("Удалить стиральную машину");
        deleteMachineButton.setOnAction(e -> 
        {
            root.getChildren().clear();
            hBox.getChildren().clear();
            
            Spinner<Integer> requestTextField = new Spinner<Integer>();
            int maxValue=Defense.checkId("washing_machines", URL, USERNAME, PASSWORD);
            requestTextField.setPromptText("Введите id элемента, который хотите изменить");
            if (maxValue!=0)
            requestTextField.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, maxValue, 1, 1));
            else onMain(root, "Ошибка! В базе данных отсутствуют элементы");
            requestTextField.setEditable(true);

            Button enterButton = new Button("Подтвердить");
            enterButton.setOnAction(event ->
            {
                int id = requestTextField.getValue();
                try(Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD); Statement statement=connection.createStatement())
                {
                    int count=0;
                    result = statement.executeQuery("SELECT COUNT(*) as count FROM lifp_lab2.washing_machines;");
                    if (result.next()) count=Integer.parseInt(result.getString("count"));
                    statement.executeUpdate("DELETE FROM lifp_lab2.washing_machines WHERE id=" + id + " LIMIT 1;");

                    for (int i=id + 1; i<=count; i++)
                    {
                        statement.executeUpdate("update lifp_lab2.washing_machines set id = " + String.valueOf(i - 1) + " where id = " + String.valueOf(i) +";");
                    }

                    result = statement.executeQuery("SELECT id from lifp_lab2.trust WHERE washingMachine_id=" + id);
                    int counter=0;
                    while (result.next()) {count=result.getInt("id"); counter++; }
                    statement.executeUpdate("DELETE FROM lifp_lab2.trust WHERE washingMachine_id=" + id);
                    statement.executeUpdate("UPDATE lifp_lab2.trust SET id = id - " + counter + " WHERE id > " + count);

                    onMain(root, "Элемент успешно удален!");

                    System.out.println("Элемент удален!");

                    connection.close();
                    statement.close();
                }
                catch (SQLException ex)
                {
                    ex.printStackTrace();
                }
            }
            );

            hBox.getChildren().addAll(new Text("Введите id элемента, который хотите удалить"), requestTextField);
            hBox.getChildren().get(0).getStyleClass().add("text-widget");
            HBox.setHgrow(requestTextField, Priority.ALWAYS);
            VBox.setMargin(hBox, new Insets(0, 0, -50, 0));
            root.getChildren().addAll(hBox, enterButton, backButton);
        }
        );

        addFactButton = new Button("Добавить факт");
        addFactButton.setOnAction(e ->
        {
            root.getChildren().clear();
            hBox.getChildren().clear();

            Text requestFactNameText = new Text("Введите название факта");
            requestFactNameText.getStyleClass().add("text-widget");
            TextField factNameTextField = new TextField();
            factNameTextField.setPromptText("Введите название факта");
            factNameTextField.getStyleClass().add("text-field");
            Text requestFactWeightText = new Text("Введите вес факта");
            requestFactWeightText.getStyleClass().add("text-widget");
            Spinner<Double> factWeightTextField = new Spinner<Double>();
            factWeightTextField.setPromptText("Введите вес факта");
            factWeightTextField.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 1, 0, 0.01));
            factWeightTextField.setEditable(true);

            Button enterWeightsButton = new Button("Введите коэффициенты");
            enterWeightsButton.setOnAction(ev -> 
            {
                enterFacts(window, -1);
            });

            Button enterButton = new Button("Подтвердить");
            enterButton.setOnAction(ev ->
            {
                try(Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD); Statement statement=connection.createStatement())
                {
                    int factId = 0;
                    result = statement.executeQuery("SELECT MAX(id) as id from lifp_lab2.facts;");
                    if (result.next()) factId = result.getInt("id");
                    factId++;

                    String query = "INSERT INTO lifp_lab2.facts (id, name, weight) VALUE (?, ?, ?);";
                    PreparedStatement preparedStatement = connection.prepareStatement(query);
                    preparedStatement.setInt(1, factId);
                    preparedStatement.setString(2, factNameTextField.getText());
                    preparedStatement.setDouble(3, factWeightTextField.getValue());
                    preparedStatement.executeUpdate();

                    int id = 0;
                    result = statement.executeQuery("SELECT max(id) as id from lifp_lab2.trust;");
                    if (result.next()) id = result.getInt("id");
                    id++;

                    ArrayList<Integer> machinesId = new ArrayList<Integer>();
                    result = statement.executeQuery("SELECT id from lifp_lab2.washing_machines;");
                    while (result.next()) 
                    {
                        machinesId.add(result.getInt("id"));
                    }

                    factTextFields=Defense.checkEmptyWeights(factTextFields, "washing_machines", URL, USERNAME, PASSWORD);

                    for (int i=0; i<machinesId.size(); i++)
                    {
                        query = "INSERT INTO lifp_lab2.trust (id, washingMachine_id, fact_id, trust_cf) VALUES (?, ?, ?, ?);";
                        preparedStatement = connection.prepareStatement(query);
                        preparedStatement.setInt(1, id++);
                        preparedStatement.setInt(2, machinesId.get(i));
                        preparedStatement.setInt(3, factId);
                        preparedStatement.setDouble(4, factTextFields.get(i).getValue());
                        preparedStatement.executeUpdate();
                    }

                    factTextFields.clear();

                    connection.close();
                    statement.close();
                }
                catch (SQLException ex)
                {
                    ex.printStackTrace();
                }

                onMain(root, "Факт добавлен!");
            });

            HBox hBox2 = new HBox(10);
            hBox2.setAlignment(javafx.geometry.Pos.CENTER);
            hBox2.setPadding(new Insets(50));
            hBox.getChildren().addAll(requestFactNameText, factNameTextField);
            hBox2.getChildren().addAll(requestFactWeightText, factWeightTextField);
            HBox.setHgrow(factNameTextField, Priority.ALWAYS);
            HBox.setHgrow(factWeightTextField, Priority.ALWAYS);
            VBox.setMargin(hBox, new Insets(0, 0, -70, 0));
            VBox.setMargin(hBox2, new Insets(0, 0, -50, 0));
            root.getChildren().addAll(hBox, hBox2, enterWeightsButton, enterButton, databaseButton);
        }); 

        updateFactButton = new Button("Изменить факт");
        updateFactButton.setOnAction(e -> 
        {
            root.getChildren().clear();
            hBox.getChildren().clear();
            
            Text factIdText = new Text("Введите id факта, который хотите изменить");
            factIdText.getStyleClass().add("text-widget");
            Spinner<Integer> factIdTextField = new Spinner<Integer>();
            int maxValue=Defense.checkId("facts", URL, USERNAME, PASSWORD);
            if (maxValue!=0)
            factIdTextField.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, maxValue, 1, 1));
            else onMain(root, "Ошибка! В базе данных отсутствуют элементы");
            factIdTextField.setPromptText("Введите id факта, который хотите изменить");
            factIdTextField.setEditable(true);
            
            Button enterButton = new Button("Подтвердить");
            enterButton.setOnAction(ev ->
            {
                root.getChildren().clear();

                Text factNameText = new Text("Введите название факта");
                factNameText.getStyleClass().add("text-widget");
                TextField factNameTextField = new TextField();
                factNameTextField.setPromptText("Введите факт...");

                Text factWeightText = new Text("Введите вес факта");
                factWeightText.getStyleClass().add("text-widget");
                Spinner<Double> factWeightTextField = new Spinner<Double>();
                factWeightTextField.setPromptText("Вес факта...");
                factWeightTextField.setEditable(true);

                try(Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD); Statement statement=connection.createStatement())
                {
                    result = statement.executeQuery("SELECT * from lifp_lab2.facts where id = " + factIdTextField.getValue());

                    if (result.next())
                    {
                        factNameTextField.setText(result.getString("name"));
                        factWeightTextField.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 1, result.getDouble("weight"), 0.01));
                    }

                    connection.close();
                    statement.close();
                }
                catch (SQLException ex)
                {
                    ex.printStackTrace();
                }

                Button updateCf = new Button("Изменить веса");
                updateCf.setOnAction(event -> 
                {
                    enterFacts(window, factIdTextField.getValue());
                });

                Button enterResButton = new Button("Подтвердить");
                enterResButton.setOnAction(ev1 ->
                {
                    try(Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD); Statement statement=connection.createStatement())
                    {
                        String query = "UPDATE lifp_lab2.facts set name = ?, weight = ? where id = ?;";
                        PreparedStatement preparedStatement = connection.prepareStatement(query);
                        preparedStatement.setString(1, factNameTextField.getText());
                        preparedStatement.setDouble(2, factWeightTextField.getValue());
                        preparedStatement.setInt(3, factIdTextField.getValue());
                        preparedStatement.executeUpdate();

                        ArrayList<Integer> idList = new ArrayList<Integer>();
                        result = statement.executeQuery("SELECT id FROM lifp_lab2.trust where fact_id = " + factIdTextField.getValue());
                        while (result.next()) idList.add(result.getInt("id"));

                        for (int i=0; i<idList.size(); i++)
                        {
                            query = "UPDATE lifp_lab2.trust set trust_cf = ? where id = ?";
                            preparedStatement = connection.prepareStatement(query);
                            preparedStatement.setDouble(1, factTextFields.get(i).getValue());
                            preparedStatement.setInt(2, idList.get(i));
                            preparedStatement.executeUpdate();
                        }

                        connection.close();
                        statement.close();
                    }
                    catch (SQLException ex)
                    {
                        ex.printStackTrace();
                    }

                    factTextFields.clear();
                    onMain(root, "Факт успешно изменен!");
                });

                hBox.getChildren().clear();
                HBox hBox2 = new HBox(10);
                hBox2.setAlignment(javafx.geometry.Pos.CENTER);
                hBox2.setPadding(new Insets(50));
                hBox.getChildren().addAll(factNameText, factNameTextField);
                hBox2.getChildren().addAll(factWeightText, factWeightTextField);
                HBox.setHgrow(factNameTextField, Priority.ALWAYS);
                HBox.setHgrow(factWeightTextField, Priority.ALWAYS);
                VBox.setMargin(hBox, new Insets(0, 0, -100, 0));
                VBox.setMargin(hBox2, new Insets(0, 0, -50, 0));
                root.getChildren().addAll(hBox, hBox2, updateCf, enterResButton, databaseButton);
            });

            hBox.getChildren().addAll(factIdText, factIdTextField);
            HBox.setHgrow(factIdTextField, Priority.ALWAYS);
            VBox.setMargin(hBox, new Insets(0, 0, -50, 0));
            root.getChildren().addAll(hBox, enterButton, databaseButton);
        });

        deleteFactButton = new Button("Удалить факт");
        deleteFactButton.setOnAction(e ->
        {
            root.getChildren().clear();
            hBox.getChildren().clear();
            
            Text factIdText = new Text("Введите id факта, который хотите удалить");
            factIdText.getStyleClass().add("text-widget");
            Spinner<Integer> factIdTextField = new Spinner<Integer>();
            int maxValue=Defense.checkId("facts", URL, USERNAME, PASSWORD);
            if (maxValue!=0)
            factIdTextField.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, maxValue, 1, 1));
            else onMain(root, "Ошибка! В базе данных отсутствуют элементы");
            
            factIdTextField.setPromptText("Введите id факта, который хотите удалить");
            factIdTextField.setEditable(true);

            Button enterButton = new Button("Подтвердить");
            enterButton.setOnAction(ev ->
            {
                try(Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD); Statement statement=connection.createStatement())
                    {
                        statement.executeUpdate("DELETE FROM lifp_lab2.facts where id = " + factIdTextField.getValue());
                        statement.executeUpdate("UPDATE lifp_lab2.facts set id = id - 1 where id > " + factIdTextField.getValue());

                        int id = 0;
                        int count = 0;
                        result = statement.executeQuery("SELECT COUNT(*) as count FROM lifp_lab2.trust where fact_id = " + factIdTextField.getValue());
                        if (result.next()) count=result.getInt("count");
                        
                        for (int i=0; i<count; i++)
                        {
                            result = statement.executeQuery("SELECT id FROM lifp_lab2.trust where fact_id = " + factIdTextField.getValue() + " limit 1;");
                            if (result.next()) id = result.getInt("id");
                            statement.executeUpdate("DELETE from lifp_lab2.trust where fact_id = " + factIdTextField.getValue() + " limit 1;");
                            statement.executeUpdate("UPDATE lifp_lab2.trust set id = id - 1 where id > " + id);
                        }

                        connection.close();
                        statement.close();
                    }
                    catch (SQLException ex)
                    {
                        ex.printStackTrace();
                    }

                    onMain(root, "Факт успешно удален!");
            });

            hBox.getChildren().addAll(factIdText, factIdTextField);
            HBox.setHgrow(factIdTextField, Priority.ALWAYS);
            VBox.setMargin(hBox, new Insets(0, 0, -50, 0));
            root.getChildren().addAll(hBox, enterButton, databaseButton);
        });

        root.getChildren().addAll(text, playButton, databaseButton);

        BackgroundImage background = new BackgroundImage(new Image("resources/background.jpg"), BackgroundRepeat.NO_REPEAT, 
        BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, new BackgroundSize(100, 100, true, true, true, true));
        root.setBackground(new Background(background));

        scene = new Scene(root, Screen.getPrimary().getVisualBounds().getWidth(), Screen.getPrimary().getVisualBounds().getHeight()-30);
        Image icon = new Image("resources/logo.png");
        window.getIcons().add(icon);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        window.setScene(scene);
        window.setTitle("Стиральные машины");
        window.show();
    }

    @Override
    public void stop() throws Exception 
    {
        try { DriverManager.deregisterDriver(driver); }
        catch (SQLException ex) { ex.printStackTrace(); }

        super.stop();
    }

    private void onMain(VBox root, String message)
    {
        root.getChildren().clear();

        Text textMessage = new Text(message);
        textMessage.getStyleClass().add("text-widget");
        
        Button onMainButton = new Button("На главную");
        onMainButton.setOnAction(e ->
        {
            root.getChildren().clear();
            root.getChildren().addAll(text, playButton, databaseButton);
        });

        root.getChildren().addAll(textMessage, onMainButton);
    }

    private Image getPhoto(Stage window)
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg"));
        File file = fileChooser.showOpenDialog(window);
        if (file != null)
        {
            Image image = new Image(file.toURI().toString());
            System.out.println(file.toURI().toString());
            return image;
        }

        return new Image("resorces/logo.png");
    }

    private void endOfTest(Stage parent, VBox root)
    {
        root.getChildren().clear();

        boolean checkResults = false;
        for (Boolean answer : answers)
        {
            if (answer==true)
            {
                checkResults=true;
                break;
            }
        }
        
        questionsCounter=1;

        ObservableList<Pair<String, Double>> result;

        if (!checkResults)
        {
            result = FXCollections.observableArrayList();
            for (int i=0; i<arr.size(); i++)
            {
                result.add(i, new Pair<String, Double>(arr.get(i).getName(), 1.0));
            }
        }
        else
        {
            result = calculateResults(arr, facts, trusts, answers);
        }

        ImageView imageView = new ImageView();
        imageView.setFitWidth(300);
        imageView.setFitHeight(300);
        
        for (int i=0; i<arr.size(); i++)
        {
            if (arr.get(i).getName().equals(result.get(0).getKey()))
            {
                imageView.setImage(arr.get(i).getImage());
                break;
            }
        }

        if (checkResults)
        {
            root.getChildren().addAll(imageView, new Text("Вам подходит стиральная машина " + result.get(0).getKey()));
        }
        else 
        {
            root.getChildren().add(new Text("Вам подойдет любая стиральная машина из подобранных"));
            root.getChildren().get(0).getStyleClass().add("text-widget");
        }

        Button showResultsButton = new Button("Показать результаты");
        showResultsButton.setOnAction(ev ->
        {
            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();

            BarChart<Number, String> barChart = new BarChart<>(yAxis, xAxis);

            XYChart.Series<Number, String> series = new XYChart.Series<>();
            series.setName("Стиральные машины");

            for (int i = 0; i < result.size(); i++) 
            {
                series.getData().add(new XYChart.Data<>(result.get(result.size() - i - 1).getValue(), result.get(result.size() - i - 1).getKey()));
            }

            barChart.getData().add(series);
            barChart.setStyle("-fx-background-color: white;");

            Scene resultsScene = new Scene(barChart, scene.getWidth(), scene.getHeight());

            Stage stage = new Stage();
            stage.setScene(resultsScene);
            stage.setTitle("Результат");
            stage.getIcons().add(new Image("resources/logo.png"));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(parent);
            stage.setOnHiding(e -> parent.show());
            stage.show();
        });

        facts.clear();
        trusts.clear();
        arr.clear();
        answers.clear();

        root.getChildren().addAll(showResultsButton, backButton);
    }

    private ObservableList<Pair<String, Double>> calculateResults(ArrayList<WashingMachine> arr, ArrayList<Facts> facts, ArrayList<Trust> trusts, ArrayList<Boolean> answers)
    {
        ArrayList<Double> resultCfs = new ArrayList<Double>();

        for (int i = 0; i < arr.size(); i++)
        {
            double coifficientsSum = 0;
            int positivefacts = 0;

            for (int j = 0; j < facts.size(); j++)
            {
                Trust trust = trusts.get((j+1)*(i+1)-1);

                if (answers.get(j))
                {
                    coifficientsSum += trust.getTrusCf() * facts.get(j).getWeight();
                    positivefacts++;
                }
            }

            if (positivefacts>0)
            {
                resultCfs.add(coifficientsSum/(positivefacts));
            }
        }

        double minValue = Collections.min(resultCfs);
        double maxValue = Collections.max(resultCfs);

        for (int i=0; i<resultCfs.size(); i++)
        {
            resultCfs.set(i, (resultCfs.get(i) - minValue) / (maxValue - minValue));
        }

        ObservableList<Pair<String, Double>> result = FXCollections.observableArrayList();
        for (int i=0; i<resultCfs.size(); i++)
        {
            result.add(new Pair<String,Double>(arr.get(i).getName(), resultCfs.get(i)));
        }

        result.sort(Comparator.comparing(Pair::getValue));
        FXCollections.reverse(result);

        return result;
    }

    private void enterWeights(Stage parent, int checkButton)
    {
        Stage stage = new Stage();
        stage.setTitle("Введите веса");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(parent);
        stage.setOnHiding(e -> parent.show());

        ObservableList<Facts> names = FXCollections.observableArrayList();
        if (checkButton==-1) { names = enterRequest(); }
        else { names=enterRequest(checkButton); }

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setAlignment(javafx.geometry.Pos.CENTER);
        gridPane.setStyle("-fx-background-color: white;");

        for (int i=0; i<names.size(); i++)
        {
            Spinner<Double> spin = new Spinner<Double>(0, 1.0, 0);
            spin.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 1, names.get(i).getWeight(), 0.01));
            spin.setEditable(true);
            factTextFields.add(spin);
            Text machine = new Text(names.get(i).getName());
            machine.getStyleClass().add("text-widget");
            gridPane.add(machine, 0, i);
            gridPane.add(factTextFields.get(i), 1, i);
        }

        Button enterButton = new Button("Подтвердить");
        Button cancellationButton = new Button("Отмена");

        gridPane.add(enterButton, 0, names.size());
        gridPane.add(cancellationButton, 1, names.size());

        enterButton.setOnAction(e->
        {
            stage.close();
        });

        cancellationButton.setOnAction(ev->
        {
            factTextFields.clear();
            stage.close();
        });

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(gridPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        Scene scene = new Scene(scrollPane, 1200, 600);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        Image icon = new Image("resources/logo.png");
        stage.getIcons().add(icon);
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> { factTextFields.clear(); });
        stage.show();
    }

    private ObservableList<Facts> enterRequest() // Запрос о заполнении фактов
    {
        ObservableList<Facts> names = FXCollections.observableArrayList();

        try(Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD); Statement statement=connection.createStatement())
        {
            result = statement.executeQuery("SELECT * FROM lifp_lab2.facts");

            while (result.next())
            {
                names.add(new Facts(result.getString("name"), 0));
            }

            connection.close();
            statement.close();
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }

        return names;
    }

    private ObservableList<Facts> enterRequest(int idCount) // Запрос об изменении фактов
    {
        ObservableList<Facts> names = FXCollections.observableArrayList();

        try(Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD); Statement statement=connection.createStatement())
        {
            result = statement.executeQuery("SELECT * FROM lifp_lab2.facts");

            while (result.next())
            {
                names.add(new Facts(result.getString("name"), 0));
            }

            result = statement.executeQuery("SELECT * FROM lifp_lab2.trust where washingMachine_id=" + idCount);
            int index = 0;
            while (result.next()) 
            {
                Facts name = names.get(index);
                name.setWeight(result.getDouble("trust_cf"));
                names.set(index++, name);
            }

            connection.close();
            statement.close();
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }

        return names;
    }

    private void enterFacts(Stage parent, int checkButton)
    {
        Stage stage = new Stage();
        stage.setTitle("Введите коэффициенты");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(parent);
        stage.setOnHiding(e -> parent.show());

        ObservableList<String> names = getWashingNames();
        ObservableList<String> cfs = FXCollections.observableArrayList();
        if (checkButton==-1)
        {
            for (int i=0; i<names.size(); i++)
            {
                cfs.add("0");
            }
        }
        else cfs=getFactsCf(checkButton);

        names.clear();
        names = getWashingNames();

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setAlignment(javafx.geometry.Pos.CENTER);
        gridPane.setStyle("-fx-background-color: white;");

        for (int i=0; i<names.size(); i++)
        {
            Spinner<Double> spin = new Spinner<Double>(0.0, 1.0, Double.parseDouble(cfs.get(i)));
            spin.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 1, Double.parseDouble(cfs.get(i)), 0.01));
            spin.setEditable(true);
            factTextFields.add(spin);
            Text fact = new Text(names.get(i));
            fact.getStyleClass().add("text-widget");
            gridPane.add(fact, 0, i);
            gridPane.add(factTextFields.get(i), 1, i);
        }

        Button enterButton = new Button("Подтвердить");
        enterButton.setOnAction(e->
        {
            stage.close();
        });

        Button cancellationButton = new Button("Отмена");
        cancellationButton.setOnAction(ev->
        {
            factTextFields.clear();
            stage.close();
        });

        gridPane.add(enterButton, 0, names.size());
        gridPane.add(cancellationButton, 1, names.size());

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(gridPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        Scene scene = new Scene(scrollPane, 1200, 600);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        Image icon = new Image("resources/logo.png");
        stage.getIcons().add(icon);
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> { factTextFields.clear(); });
        stage.show();
    }

    private ObservableList<String> getWashingNames()
    {
        ObservableList<String> names = FXCollections.observableArrayList();

        try(Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD); Statement statement=connection.createStatement())
        {
            result = statement.executeQuery("SELECT name FROM lifp_lab2.washing_machines;");
            while (result.next()) names.add(result.getString("name"));

            connection.close();
            statement.close();
        } 
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }

        return names;
    }

    private ObservableList<String> getFactsCf(int factId)
    {
        ObservableList<String> cfs = FXCollections.observableArrayList();

        try(Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD); Statement statement=connection.createStatement())
        {
            result = statement.executeQuery("SELECT trust_cf FROM lifp_lab2.trust where fact_id = " + factId);
            while (result.next()) cfs.add(result.getString("trust_cf"));

            connection.close();
            statement.close();
        } 
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }

        return cfs;
    }
}