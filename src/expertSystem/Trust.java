package expertSystem;

public class Trust 
{
    private int washingMachineId;
    private int factId;
    private double trusCf;

    public Trust()
    {
        washingMachineId=factId=0;
        trusCf=0;
    }

    public Trust(int washingMachineId, int factId, double trusCf)
    {
        this.washingMachineId=washingMachineId;
        this.factId=factId;
        this.trusCf=trusCf;
    }

    public int getWashingMachineId() { return washingMachineId; }
    public int getFactId() { return factId; }
    public double getTrusCf() { return trusCf; }
}
