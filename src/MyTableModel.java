package kmcluster;

import java.util.*;
import javax.swing.table.*;

public class MyTableModel extends AbstractTableModel {
    private final String[] columnNames = {"ID", "Vendor", "Car age", "Car price",
                                    "Driver age", "Experience", "Cluster"};
    ArrayList<Cars> list = null;
    
    MyTableModel(ArrayList<Cars> list) {
        this.list = list;
    }
    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }
    @Override
    public int getRowCount() {
        return list.size();
    }
    @Override
    public int getColumnCount() {
        return columnNames.length;
    }
    @Override
    public Object getValueAt(int row, int col) {
        Cars car = list.get(row);
        switch (col) {
            case 0:
                return car.id;
            case 1:
                return car.vendor;
            case 2:
                return car.carAge;
            case 3:
                return car.carPrice;
            case 4:
                return car.driverAge;
            case 5:
                return car.experience;
            case 6:
                return car.cluster;
            default:
                return "unknown";
        }
    }    
    @Override
    public Class getColumnClass(int c) {
               return getValueAt(0, c).getClass();
    }
}