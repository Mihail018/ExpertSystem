package expertSystem;

public class Facts 
{
    private String name;
    private double weight;

    public Facts()
    {
        name = "Вопрос";
        weight = 0;
    }

    public Facts(String name, double weight)
    {
        this.name = name;
        this.weight = weight;
    }

    public String getName() { return name; }
    public double getWeight() { return weight; }

    public void setWeight(double weight) { this.weight=weight; }
}
