package expertSystem;

import javafx.scene.image.Image;

public class WashingMachine
{
    private String name; // Название модели
    private Image image; // Изображение

    public WashingMachine()
    {
        name = "Стиральная машина";
        image = new Image("resources/washingMachinesLogo/noImage.png");
    }

    public WashingMachine(String name, Image image)
    {
        this.name = name;
        this.image = image;
    }

    public String getName() { return name; }
    public Image getImage() { return image; }
}
