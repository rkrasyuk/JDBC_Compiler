package com.company;

import java.sql.*;
import java.util.logging.*;
import java.io.*;

public class Recompiler implements Runnable {
    private String classId, shortName, status;

    public Recompiler(String classId, String shortName, String status) {
        this.classId = classId;
        this.shortName = shortName;
        this.status = status;
    }

    public String getClassId(){
        return this.classId;
    }

    public String getShortName(){
        return this.shortName;
    }

    @Override
    public void run() {
        try {
            System.out.println("Starting recompile thread for [" + this.classId + "].[" + this.shortName + "]...");
            Class.forName("oracle.jdbc.OracleDriver");
            Connection con = DriverManager.getConnection(Compiler.url, Compiler.USER, Compiler.pswrd);
            Statement st = con.createStatement();
            DbmsOutput dbmsOutput = new DbmsOutput(con, this.classId, this.shortName);
            dbmsOutput.enable(32700);
            st.executeUpdate("declare " +
                    "aft_status varchar2(255); " +
                            "CURSOR get_meth IS " +
                            "select id, class_id, short_name, status from methods m " +
                            "where m.short_name = '" + this.shortName + "' and m.class_id = '" + this.classId + "'; " +
                            "begin " +
                            "dbms_output.enable(32700); " +
                            "FOR v_get_meth IN get_meth LOOP " +
                            "method.recompile(v_get_meth.id); " +
                            "select status into aft_status from methods m where m.id = v_get_meth.id; " +
                            "DBMS_OUTPUT.put_line('Compile ['||v_get_meth.class_id||'].' || '['||v_get_meth.short_name||'] '||' ID:'||v_get_meth.id||' Status:'||aft_status); " +
                            "END LOOP; " +
                            "end;"
            );
            dbmsOutput.show();

            String newStatus = "unknown";
            ResultSet ress = con.createStatement().executeQuery("SELECT status FROM methods WHERE class_id='" + this.classId + "' AND short_name='" + this.shortName + "'");
            while(ress.next()) {
                newStatus = ress.getString("STATUS");
            }
            if (!status.equals(newStatus)) {
                File file = new File("\\recomp_logs\\" + "recompile_" + Timestamp.getTimeStamp() + "_[" + this.classId + "].[" + this.shortName + "]_" + Compiler.shema + ".log");
                BufferedWriter wrt = new BufferedWriter(new FileWriter(file));
                wrt.write("Now is " + Timestamp.getTimeStamp() + ".\n");
                wrt.write("[" + this.classId + "].[" + this.shortName + "] at " + Compiler.shema + " was recompiled.\n");
                wrt.write("Method status modified from " + status + " to " + newStatus + ".\n");
                wrt.flush();
                wrt.close();
            }
            con.close();
        } catch (Exception ex) {
            Logger.getLogger(Recompiler.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}