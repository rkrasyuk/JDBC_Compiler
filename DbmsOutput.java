package com.company;

import java.sql.*;
import java.io.*;

public class DbmsOutput {
    private CallableStatement enable_stmt;
    private CallableStatement disable_stmt;
    private CallableStatement show_stmt;
    private String cId;
    private String sName;

    public DbmsOutput(Connection conn, String classId, String shortName) throws SQLException {
        this.cId = classId;
        this.sName = shortName;
        enable_stmt = conn.prepareCall("begin dbms_output.enable(:1); end;");
        disable_stmt = conn.prepareCall("begin dbms_output.disable; end;");
        show_stmt = conn.prepareCall("declare "
        + "l_line varchar2(32700); "
        + "l_done number; "
        + "l_buffer long; "
        + "begin "
        + " loop "
        + " exit when l_done = 1; "
        + " dbms_output.get_line(l_line, l_done); "
        + " l_buffer := l_buffer || l_line || chr(10); "
        + " end loop; "
        + " :done := l_done; "
        + " :buffer := l_buffer; "
        + "end;");
    }

    public void enable(int size) throws SQLException {
        enable_stmt.setInt(1, size);
        enable_stmt.executeUpdate();
    }

    public void disable() throws SQLException {
        disable_stmt.executeUpdate();
    }

    public void show() throws Exception {
        int done = 0;
        show_stmt.registerOutParameter(1, Types.INTEGER);
        show_stmt.registerOutParameter(2, Types.VARCHAR);
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File("\\dbms_output\\dbms_output_[" + this.cId + "].[" + this.sName + "]_" + Timestamp.getTimeStamp() + "_.log" )));

        for(;;){
            show_stmt.executeUpdate();
            writer.write(show_stmt.getString(2));
            writer.flush();
            if ((done = show_stmt.getInt(1)) == 1) break;
        }
    }

    public void close() throws SQLException {
        enable_stmt.close();
        disable_stmt.close();
        show_stmt.close();
    }
}