package kmcluster;

import java.awt.Color;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.*;
import static javax.swing.JFrame.*;
import javax.swing.text.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

public class Analysis {
    JTextArea textArea = new JTextArea();
    JTextArea textAreaRes = new JTextArea();
    JTextArea textAreaIntermed = new JTextArea();
    DefaultCaret caretRes = (DefaultCaret)textAreaRes.getCaret();
    JScrollPane scrollPaneRes = new JScrollPane(textAreaRes);
    int outLine = 0;
    JFrame frame = new JFrame("K - means cluster analysis");
    JTabbedPane tabpane = new JTabbedPane();
    JPanel panel = new JPanel();
    JPanel panelBar = new JPanel();
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    JFreeChart barChart = ChartFactory.createBarChart("Clusters chart", "Clusters", "Count of objects", dataset, PlotOrientation.VERTICAL, true, true, true);
    CategoryPlot barCh = barChart.getCategoryPlot();
    ChartPanel barPanel = new ChartPanel(barChart);

    public Analysis(){
        try {
            Thread t1 = new Thread(){
                @Override
                public void run(){
                    try {
                        insertDB(10000);
                    } catch (SQLException ex) {
                        Logger.getLogger(Analysis.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };
            //t1.start();
            initComponents();
        } catch (SQLException ex) {
            Logger.getLogger(Analysis.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    // component initialization
    private void initComponents() throws SQLException {
        //all of database entries will be contained in ResultSets arraylist
        ArrayList<Cars> ResultSets = new ArrayList<>();
        MyTableModel mtm = new MyTableModel(ResultSets);
        JTable table = new JTable(mtm);
        //clusters count choosing combo
        String[] comboBoxItems = {"", "2", "3", "4", "5", "7", "10"};
        JButton buttonAnalyze = new JButton("Analyze");
        JButton buttonReadDB = new JButton("Read database");
        JButton buttonUpdateDB = new JButton("Update database");
        buttonAnalyze.setEnabled(false);
        buttonUpdateDB.setEnabled(false);
        JScrollPane scrollPane = new JScrollPane(table);
        JScrollPane scrollPane1 = new JScrollPane(textArea);
        JScrollPane scrollPaneIntermed = new JScrollPane(textAreaIntermed);
        JComboBox comboBox = new JComboBox(comboBoxItems);
        DefaultCaret caret = (DefaultCaret)textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);        
        DefaultCaret caretIntermed = (DefaultCaret)textAreaIntermed.getCaret();
        caretIntermed.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);        
        panel.setLayout(null);
        panel.add(scrollPane);
        panel.add(buttonAnalyze);
        panel.add(buttonReadDB);
        panel.add(buttonUpdateDB);
        scrollPane.setBounds(5, 5, 700, 475);
        buttonReadDB.setBounds(730, 7, 140, 20);
        buttonAnalyze.setBounds(910, 7, 140, 20);
        buttonUpdateDB.setBounds(730, 34, 140, 20);
        comboBox.setBounds(910, 34, 140, 20);
        scrollPane1.setBounds(730, 61, 320, 70);
        scrollPaneIntermed.setBounds(730, 135, 320, 345);

        panel.add(comboBox);
        panel.add(scrollPane1);
        panel.add(scrollPaneIntermed);
        tabpane.add("General", panel);
        frame.add(tabpane);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setSize(1100, 550);
        frame.setVisible(true);

        class MyActionListener implements ActionListener {
            JButton buttonAnalyze, buttonReadDB, buttonUpdateDB, button;
            JTextField textField;
            ArrayList<Cars> ResultSets;
            JComboBox comboBox;
            int clusterCount;
            MyTableModel mtm;
            public MyActionListener(JButton buttonAnalyze, JButton buttonReadDB, JButton buttonUpdateDB,
                     ArrayList<Cars> ResultSets, JComboBox comboBox, MyTableModel mtm){
                this.buttonAnalyze = buttonAnalyze;
                this.buttonReadDB = buttonReadDB;
                this.buttonUpdateDB = buttonUpdateDB;
                this.ResultSets = ResultSets;
                this.comboBox = comboBox;
                this.mtm = mtm;
            }
            
            @Override
            public void actionPerformed(ActionEvent e){
                button = (JButton)e.getSource();
                Thread t = new Thread(){
                    @Override
                    public void run(){
                        if(button == buttonAnalyze){
                            buttonAnalyze.setEnabled(false);
                            buttonReadDB.setEnabled(false);
                            buttonUpdateDB.setEnabled(false);
                            if("".equals((String)comboBox.getSelectedItem())){
                                clusterCount = 0;
                            } else {
                                clusterCount = Integer.parseInt((String)comboBox.getSelectedItem());
                            }
                            try {
                                analysis(ResultSets, clusterCount);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(Analysis.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            mtm.fireTableDataChanged();
                            buttonAnalyze.setEnabled(true);
                            buttonReadDB.setEnabled(true);
                            buttonUpdateDB.setEnabled(true);
                        }
                        if(button == buttonReadDB){
                            buttonAnalyze.setEnabled(false);
                            buttonReadDB.setEnabled(false);
                            buttonUpdateDB.setEnabled(false);
                            try {
                                readDB(ResultSets);
                            } catch (SQLException ex) {
                                Logger.getLogger(Analysis.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            mtm.fireTableDataChanged();
                            buttonAnalyze.setEnabled(true);
                            buttonReadDB.setEnabled(true);
                            buttonUpdateDB.setEnabled(true);
                        }
                        if(button == buttonUpdateDB){
                            buttonAnalyze.setEnabled(false);
                            buttonReadDB.setEnabled(false);
                            buttonUpdateDB.setEnabled(false);
                            try {
                                updateDB(ResultSets);
                            } catch (SQLException ex) {
                                Logger.getLogger(Analysis.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            buttonAnalyze.setEnabled(true);
                            buttonReadDB.setEnabled(true);
                            buttonUpdateDB.setEnabled(true);
                        }
                    }
                };
                t.start();
            }
        }
        MyActionListener myAL = new MyActionListener(buttonAnalyze, buttonReadDB, buttonUpdateDB,
                                                     ResultSets, comboBox, mtm);
        buttonAnalyze.addActionListener(myAL);
        buttonReadDB.addActionListener(myAL);
        buttonUpdateDB.addActionListener(myAL);
    }
    //analysis
    void analysis(ArrayList<Cars> ResultSets, int clusterCount) throws InterruptedException{
        if(clusterCount < 1 || clusterCount > ResultSets.size()) {
            outLine++;
            textArea.append("#" + outLine + " Wrong count of clusters;\n");
            return;
        }
        ArrayList<Cars> Centroid = new ArrayList<>();
        ArrayList<String> str = new ArrayList<>();
        ArrayList<String> s = new ArrayList<>();
        boolean continueExecution;
        int i, iterCount = 0;
        int ex = 0, ex1 = 0;
        int iter = 1000000;
        for(i = 0; i < clusterCount; i++) s.add("Cluster " + i);
        for(i = 0; i < ResultSets.size(); i++){
            if(ResultSets.get(i).carPrice > 1){
                normalizeAll(ResultSets);
                break;
            }
        }
        CentroidInit(Centroid, clusterCount, ResultSets);
        for(i = 0; i < ResultSets.size(); i++) str.add("");
        long startTime = System.nanoTime();
        Thread thread_1, thread_2;
        MyThread_1 thr_1, thr_2 = new MyThread_1();
        //analysis execution
        while(ex < iter){
            //Распараллелить(!)
            //Minimal distance definition from object to centroid
/*            
            for(i = 0; i < ResultSets.size(); i++) {
                str.set(i, ResultSets.get(i).cluster);
                ResultSets.get(i).cluster = s.get(minDistance(Centroid, ResultSets.get(i)));
            }
*/
            //ArrayList separates into 2 equal parts
            continueExecution = true;
            thread_1 = new MyThread(ResultSets, Centroid, ResultSets.size() * 0,
                    ResultSets.size() / 2, str, s);
            thread_1.start();
            thread_2 = new MyThread(ResultSets, Centroid, ResultSets.size() / 2,
                    ResultSets.size(), str, s);
            thread_2.start();
            //Waiting while all of threads is alive, then continue
            while(continueExecution){
                if(!thread_1.isAlive() && !thread_2.isAlive()){
                    continueExecution = false;
                }
            }
            //k-means values writing into Centroid<Cars>
            for(i = 0; i < clusterCount; i++){
/*
                Centroid.set(i, centroidDefinition(ResultSets, Centroid.get(i), s.get(i)));
*/
                continueExecution = true;
                thr_1 = new MyThread_1(ResultSets, Centroid, i, s);
                i++;
                thr_1.start();
                if(i < clusterCount) {
                    thr_2 = new MyThread_1(ResultSets, Centroid, i, s);
                    thr_2.start();
                }
                //Waiting while all of threads is alive, then continue
                while(continueExecution){
                    if(!thr_1.isAlive() && !thr_2.isAlive()){
                        for(int ce = 0; ce < clusterCount; ce++){
                            textAreaIntermed.append("Intermediate " + ce + "th centroid coordinates:\n" +
                                "     " + String.valueOf((Centroid.get(ce)).carAge) +
                                "     " + String.valueOf((Centroid.get(ce)).carPrice) +
                                "     " + String.valueOf((Centroid.get(ce)).driverAge) + 
                                "     " + String.valueOf((Centroid.get(ce)).experience) + "\n");
                        }
                        textAreaIntermed.append("\n");
                        continueExecution = false;
                    }
                }
            }        
            
            for(i = 0; i < ResultSets.size(); i++){
                if(str.get(i).equals(ResultSets.get(i).cluster)) ex1++;
            }
            if(ex1 == ResultSets.size()) {
                iterCount = ex;
                ex = iter;
            } else ex1 = 0;        
            ex++;
        }        
        long timeDifference = (System.nanoTime() - startTime) / 1000000000;
        outLine++;
        textArea.append("#" + outLine + " Analysis done: " + iterCount + " iterations operated, "
                + "time is " + timeDifference +  ";\n");
        int [] stat = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        for (Cars ResultSet : ResultSets) {
            for (int sta = 0; sta < clusterCount; sta++) {
                if (ResultSet.cluster == null ? s.get(sta) == null : ResultSet.cluster.equals(s.get(sta))) {
                    stat[sta]++;
                }
            }
        }
        for(int sta = 0; sta < clusterCount; sta++){
            textAreaRes.append(String.valueOf(stat[sta]) + " elements in " + s.get(sta) + "\n");
        }
        textAreaRes.append("/////////////////////////////////////////////////////\n");
        for(int sta = 0; sta < clusterCount; sta++){
            dataset.addValue(stat[sta], s.get(sta), s.get(sta));
        }
        caretRes.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);        
        panelBar.add(scrollPaneRes);
        panelBar.add(barPanel);
        tabpane.add("Chart", panelBar);
        barCh.setBackgroundPaint(Color.LIGHT_GRAY);
        barCh.setRangeGridlinePaint(Color.BLACK);
        panelBar.setLayout(null);
        scrollPaneRes.setBounds(810, 0, 270, 480);
        barPanel.setBounds(3, 0, 800, 480);
    }
    //MyThread inner class, arraylist separate
    class MyThread extends Thread {
        ArrayList<Cars> ResultSubSet;
        ArrayList<Cars> Centroid;
        int start, finish;
        int i, clusterCount;
        ArrayList<String> str = new ArrayList<>();
        ArrayList<String> s = new ArrayList<>();
        MyThread(ArrayList<Cars> ResultSubSet, ArrayList<Cars> Centroid,
                int start, int finish, ArrayList<String> str, ArrayList<String> s){
            this.ResultSubSet = ResultSubSet;
            this.start = start;
            this.finish = finish;
            this.str = str;
            this.s = s;
            this.Centroid = Centroid;
        }
        @Override
        public void run(){
            for(i = start; i < finish; i++) {
                str.set(i, ResultSubSet.get(i).cluster);
                ResultSubSet.get(i).cluster = s.get(minDistance(Centroid, ResultSubSet.get(i)));
            }
        }
    }
    //MyThread_1 inner class, centroid definition distribute
    class MyThread_1 extends Thread {
        ArrayList<Cars> ResultSets;
        ArrayList<Cars> Centroid;
        int i;
        ArrayList<String> s = new ArrayList<>();
        MyThread_1(ArrayList<Cars> ResultSets, ArrayList<Cars> Centroid,
                int i, ArrayList<String> s){
            this.ResultSets = ResultSets;
            this.s = s;
            this.i = i;
            this.Centroid = Centroid;
        }
        MyThread_1(){}
        @Override
        public void run(){
            Centroid.set(i, centroidDefinition(ResultSets, Centroid.get(i), s.get(i)));
        }
    }
    //finding Euclidean distance from centroid to object
    public double EuclideanDist(Cars centroid, Cars a){
        double distance;
        distance = Math.sqrt(pow(centroid.carAge - a.carAge) +
                             pow(centroid.carPrice - a.carPrice) +
                             pow(centroid.driverAge - a.driverAge) +
                             pow(centroid.experience - a.experience));        
        return distance;
    }
    //returns 2 powered number
    public double pow(double number){
        return number * number;
    }
    //Database values normalization
    private void normalizeAll(ArrayList<Cars> rs){
        double maximumCarPrice, minimumCarPrice;
        double maximumCarAge, minimumCarAge;
        double maximumDriverAge, minimumDriverAge;
        double maximumExperience, minimumExperience;
        int tempCarPrice, tempCarAge, tempDriverAge, tempExperience;
        maximumCarPrice = (rs.get(0)).carPrice;
        minimumCarPrice = maximumCarPrice; 
        maximumCarAge = (rs.get(0)).carAge;
        minimumCarAge = maximumCarAge; 
        maximumDriverAge = (rs.get(0)).driverAge;
        minimumDriverAge = maximumDriverAge; 
        maximumExperience = (rs.get(0)).experience;
        minimumExperience = maximumExperience; 
        for (Cars r : rs) {
            if (maximumCarPrice < (r).carPrice) {
                maximumCarPrice = (r).carPrice;
            }
            if (maximumCarAge < (r).carAge) {
                maximumCarAge = (r).carAge;
            }
            if (maximumDriverAge < (r).driverAge) {
                maximumDriverAge = (r).driverAge;
            }
            if (maximumExperience < (r).experience) {
                maximumExperience = (r).experience;
            }
            if (minimumCarPrice > (r).carPrice) {
                minimumCarPrice = (r).carPrice;
            }
            if (minimumCarAge > (r).carAge) {
                minimumCarAge = (r).carAge;
            }
            if (minimumDriverAge > (r).driverAge) {
                minimumDriverAge = (r).driverAge;
            }
            if (minimumExperience > (r).experience) {
                minimumExperience = (r).experience;
            }
        }
        for (Cars r : rs) {
            (r).carPrice = ((r).carPrice - minimumCarPrice) / (maximumCarPrice - minimumCarPrice);
            tempCarPrice = (int)((r).carPrice * 1000000);
            (r).carPrice = (double)tempCarPrice / 1000000;

            (r).carAge = ((r).carAge - minimumCarAge) / (maximumCarAge - minimumCarAge);
            tempCarAge = (int)((r).carAge * 1000000);
            (r).carAge = (double)tempCarAge / 1000000;

            (r).driverAge = ((r).driverAge - minimumDriverAge) / (maximumDriverAge - minimumDriverAge);
            tempDriverAge = (int)((r).driverAge * 1000000);
            (r).driverAge = (double)tempDriverAge / 1000000;

            (r).experience = ((r).experience - minimumExperience) / (maximumExperience - minimumExperience);
            tempExperience = (int)((r).experience * 1000000);
            (r).experience = (double)tempExperience / 1000000;
        }
        outLine++;
        textArea.append("#" + outLine + " All data normalized;\n");
    }
    //carPrice values normalization method
    private void normalize(ArrayList<Cars> rs){
        double maximumCarPrice, minimumCarPrice;
        int tempCarPrice;
        maximumCarPrice = (rs.get(0)).carPrice;
        minimumCarPrice = maximumCarPrice; 
        for (Cars r : rs) {
            if (maximumCarPrice < (r).carPrice) {
                maximumCarPrice = (r).carPrice;
            }
            if (minimumCarPrice > (r).carPrice) {
                minimumCarPrice = (r).carPrice;
            }
        }
        for (Cars r : rs) {
            (r).carPrice = ((r).carPrice - minimumCarPrice) / (maximumCarPrice - minimumCarPrice);
            tempCarPrice = (int)((r).carPrice * 1000);
            (r).carPrice = (double)tempCarPrice / 10;
        }
        outLine++;
        textArea.append("#" + outLine + " CarPrice field normalized;\n");
    }
    //Cluster centroid definition
    private Cars centroidDefinition(ArrayList<Cars> rs, Cars c, String s){
        Cars centroid = new Cars();
        int i, k = 0;
        for(i = 0; i < rs.size(); i++) {
            if((rs.get(i)).cluster.equals(s)){
                centroid.carAge += rs.get(i).carAge;
                centroid.carPrice += rs.get(i).carPrice;
                centroid.driverAge += rs.get(i).driverAge;
                centroid.experience += rs.get(i).experience;
                k++;
            }
        }
        if(k == 0) {
            return c;
        }
        centroid.carAge = centroid.carAge / k;
        centroid.carPrice = centroid.carPrice / k;
        centroid.driverAge = centroid.driverAge / k;
        centroid.experience = centroid.experience / k;
        return centroid;
    }
    //print sets
    private void printSets(ArrayList<Cars> rs){
        for (Cars r : rs) {
            System.out.print((r).id + " ");
            System.out.print((r).vendor + " ");
            System.out.print((r).carAge + " ");
            System.out.print((r).carPrice + " ");
            System.out.print((r).driverAge + " ");
            System.out.print((r).experience + " ");
            System.out.print((r).cluster + " ");
            System.out.print("\n");            
        }
    }
    //minimal distance from object to centroids
    private int minDistance(ArrayList<Cars> centroid, Cars car){
        double mindist = 1000000;
        int min = -1;
        for(int i = 0; i < centroid.size(); i++){
            if(EuclideanDist(centroid.get(i), car) < mindist) {
                mindist = EuclideanDist(centroid.get(i), car);
                min = i;
            } 
        }
        return min;
    }
    //random centroids initialization
    //Optimize(!)
    private void randCentroidInit(ArrayList<Cars> centroid, int clusCount, ArrayList<Cars> rs){
        for (int i = 0; i < clusCount; i++) {
            Cars c = new Cars();
            (c).id = 0;
            (c).vendor = null;
            (c).carAge = rs.get(i).carAge;
            (c).carPrice = rs.get(i).carPrice;
            (c).driverAge = rs.get(i).driverAge;
            (c).experience = rs.get(i).experience;
            (c).cluster = null;
            centroid.add(c);
        }
    }
    //Optimized centroids initialization
    private void CentroidInit(ArrayList<Cars> centroid, int clusCount, ArrayList<Cars> rs){
        for(int i = 0; i < clusCount; i++){
            double step = 1.4142 / (double)clusCount;
            Cars c = new Cars();
            (c).id = 0;
            (c).vendor = null;
            (c).carAge = step * i;
            (c).carPrice = step * i;
            (c).driverAge = step * i;
            (c).experience = step * i;
            (c).cluster = null;
            textAreaIntermed.append(" Initial " + i + "th centroid coordinates:\n" +
                    "     " + String.valueOf((c).carAge) +
                    "     " + String.valueOf((c).carPrice) +
                    "     " + String.valueOf((c).driverAge) + 
                    "     " + String.valueOf((c).experience) + "\n");
            centroid.add(c);
        }
        textAreaIntermed.append("\n");
    }
    //read data from test.cars table
    private void readDB(ArrayList<Cars> ResultSets) throws SQLException{
        DBConnection mdbc = new DBConnection();
        mdbc.init();
        Connection conn = mdbc.getMyConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("Select * from cars");
        //DB tables to objects
        ResultSets.clear();
        try{ 
            while(rs.next()){
                Cars a = new Cars(
                    rs.getInt("ID"), rs.getString("vendor"), rs.getInt("carAge"),
                    rs.getDouble("carPrice"), rs.getInt("driverAge"),
                    rs.getInt("experience"), rs.getString("cluster")
                );
                ResultSets.add(a);
            }   
        }
        catch(Exception e){
            outLine++;
            textArea.append("#" + outLine + " Exception in Car table;");
        }
        outLine++;
        textArea.append("#" + outLine + " Database loaded: " + ResultSets.size()+ " entries;\n");
    }
    
    //insert data into test.cars
    private void insertDB(int n) throws SQLException{
        DBConnection dbc = new DBConnection();
        dbc.init();
        try (Connection conn = dbc.getMyConnection()) {
            Statement st = conn.createStatement();
            String insertStr;
            String [] vendorStr = {"Audi", "Mersedes",
                "Nissan", "Toyota", "Honda", "Mitsubishi",
                "Volkswagen", "Subaru", "KIA", "Hyundai"};
            String vendor;
            int carAge;
            int carPrice;
            int driverAge;
            int experience;
            int done, i;
            for(i = 0; i < n; i++) {
                carAge = (int)(Math.random() * 30);
                carPrice = ((int)(Math.random() * 9 + 1) * 10000) + ((int)(Math.random() * 9 + 1) * 1000);
                driverAge = (int)(Math.random() * 72) + 18;
                experience = (int)(Math.random() * (driverAge - 17));
                vendor = vendorStr[i%10] + "_" + String.valueOf((int)(Math.random() * 10));
                try {
                    insertStr = "INSERT INTO  `test`.`cars`(ID, vendor, carAge, carPrice, driverAge, experience,cluster)" +
                            "VALUES(NULL, '" +
                            vendor + "', '" +
                            carAge + "', '" +
                            carPrice + "', '" +
                            driverAge + "', '" +
                            experience + "', '0');";
                    done = st.executeUpdate(insertStr);
                }
                catch(Exception e){
                    outLine++;
                    textArea.append("#" + outLine + " Error occurred in inserting data;");
                }
            }   
            st.close();
            conn.close();
            dbc.close(st);
        }
    }    
    private void updateDB(ArrayList<Cars> ResultSets) throws SQLException{
        DBConnection dbc = new DBConnection();
        dbc.init();
        Statement st;
        try (Connection conn = dbc.getMyConnection()) {
            st = conn.createStatement();
            String insertStr;
            int done, i, id;
            for(i = 0; i < ResultSets.size(); i++) {
                id = i + 1;
                insertStr = "UPDATE  `test`.`cars` SET"
                        + " `vendor` = '" + ResultSets.get(i).vendor + "',"
                        + " `carAge` = '" + ResultSets.get(i).carAge + "',"
                        + " `carPrice` = '" + ResultSets.get(i).carPrice + "',"
                        + " `driverAge` = '" + ResultSets.get(i).driverAge + "',"
                        + " `experience` = '" + ResultSets.get(i).experience + "',"
                        + " `cluster` = '" + ResultSets.get(i).cluster + "' "
                        + "WHERE `cars`.`ID` = " + id + ";";
                done = st.executeUpdate(insertStr);
            }
            outLine++;
            textArea.append("#" + outLine + " Database updated;\n");
            st.close();
        }
        dbc.close(st);
    } 
}
