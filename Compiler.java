package com.company;

import java.sql.*;
import java.util.HashMap;
import java.util.logging.*;
import java.io.*;

public class Compiler {
    static String url = null;
    static String shema = "";
    static final String USER = "ibs";
    static String pswrd = "";

    public static void main(String[] args) {
        HashMap<String, String> map = new HashMap<>();
        map.put("EKSSILVER", "jdbc:oracle:thin:@//silver03.sbrf.ru:1521/EKSSILVER");
        map.put("EKSGOLD", "jdbc:oracle:thin:@//gold03.sbrf.ru:1521/EKSGOLD");
        map.put("EKSPLATINUM", "jdbc:oracle:thin:@//platinum03.sbrf.ru:1521/EKSPLATINUM");

        try {
            System.out.println("");
            System.out.println("======= Compiler BETA v.0.2.0 =======");
            System.out.println("");

	    //Get credentials
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Enter database service_name: ");
            Compiler.shema = reader.readLine().toUpperCase();
            Console con = System.console();
            if (con == null) {
                System.err.println("No console");
                System.exit(1);
            }
            char[] pass = con.readPassword("Enter IBS password: ");
            Compiler.pswrd = new String(pass);
            reader.close();
            System.out.println("");
        }
        catch (Exception ex) {
            Logger.getLogger(Compiler.class.getName()).log(Level.SEVERE, null, ex);
        }

	//load jdbc-driver and set DB connection
        Connection connect1;
        try {
            Compiler.url = map.get(Compiler.shema);
            Class.forName("oracle.jdbc.OracleDriver");
            System.out.println("Driver plugged in.");
            connect1 = DriverManager.getConnection(Compiler.url, Compiler.USER, Compiler.pswrd);
            System.out.println("Connected to " + Compiler.shema + ".");

	    //execute sql-query, read and use result set, create list of invalid/processed methods
            Statement stmnt = connect1.createStatement();
            ResultSet res1 = stmnt.executeQuery("SELECT * FROM methods WHERE (status='PROCESSED' OR status='INVALID') AND id <> 'STDLIB'");

            File outlog = new File("\\inv_list\\list_of_invalids_" + Timestamp.getTimeStamp() + "_" + Compiler.shema + ".txt");
            BufferedWriter wrt1 = new BufferedWriter(new FileWriter(outlog));
            int counter = 0;
            System.out.println("Creating list of invalids...");
            while (res1.next()) {
                counter++;
                rdr1.write(res1.getRow() + " id:" + res1.getLong("ID") + " [" + res1.getString("CLASS_ID") + "].[" + res1.getString("SHORT_NAME") + "] is " + res1.getString("STATUS") + "\n");
            }
            wrt1.write("Total of invalid/processed methods: " + counter);
            wrt1.flush();
            System.out.print("Done. ");

            //create recompile threads
            System.out.println("Starting recompile...");
            ResultSet res2 = stmnt.executeQuery("SELECT * FROM methods WHERE (status='PROCESSED' OR status='INVALID') AND id <> 'STDLIB'");

            while (res2.next()) {
                new Thread(new Recompiler(res2.getString("CLASS_ID"), res2.getString("SHORT_NAME"), res2.getString("STATUS"))).start();
                Thread.sleep(400);
            }

            wrt1.close();
            System.out.println("All recompile threads are created. Please wait until the program finishes working...");
            Compiler.pswrd = "0000000000";
            connect1.close();
        }
        catch (Exception ex) {
            Logger.getLogger(Compiler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
