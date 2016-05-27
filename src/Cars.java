package kmcluster;

public class Cars {
    int id;
    String vendor, cluster;
    double carAge;
    double carPrice;
    double driverAge;
    double experience;
    public Cars(){}
    public Cars(int id, String vendor, double carAge, double carPrice,
                double driverAge, double experience, String cluster)
    {
        this.id = id;       
        this.vendor = vendor;       
        this.carAge = carAge;       
        this.carPrice = carPrice;       
        this.driverAge = driverAge;       
        this.experience = experience;       
        this.cluster = cluster;       
    }
    public void set(Cars a){
        this.carAge = a.carAge;
        this.carPrice = a.carPrice;
        this.cluster = a.cluster;
        this.driverAge = a.driverAge;
        this.experience = a.experience;
    }
}