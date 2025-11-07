package com.queuectl.cli;

import picocli.CommandLine.Command;
import java.sql.*;

@Command(name="status", description="Show job status summary")
public class StatusCommand implements Runnable {
    @Override
    public void run() {
        String sql="SELECT state, COUNT(*) AS cnt FROM jobs GROUP BY state";
        String dlqSql="SELECT COUNT(*) AS cnt FROM dlq";

        try(Connection con=com.queuectl.db.repository.DBConnection.getConnection();
            Statement st=con.createStatement()){
            System.out.println("Job Status Overview:");
            System.out.println("-----------------------------------");

            try(ResultSet rs=st.executeQuery(sql)){
                while(rs.next()){
                    System.out.printf("%-12s : %d%n",rs.getString("state"),rs.getInt("cnt"));
                }
            }

            try(ResultSet rs=st.executeQuery(dlqSql)){
                if(rs.next()){
                    System.out.printf("%-12s : %d%n","DLQ",rs.getInt("cnt"));
                }
            }
        }catch(SQLException e){
            System.err.println("Error fetching status: "+e.getMessage());
        }
    }
}
