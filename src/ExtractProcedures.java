import java.sql.*;
import java.io.*;
import com.booktrack.config.DatabaseConfig;

/**
 * Utility to extract stored procedure source code from Oracle database
 * and recreate the user_procedures.sql file
 */
public class ExtractProcedures {
    
    public static void main(String[] args) {
        try {
            // Get database connection
            DatabaseConfig dbConfig = DatabaseConfig.getInstance();
            Connection conn = dbConfig.getConnection();
            
            if (conn == null) {
                System.err.println("Failed to connect to database");
                return;
            }
            
            System.out.println("Connected to database successfully");
            
            // Query to get all procedure source code
            String query = """
                SELECT name, 
                       LISTAGG(text, '') WITHIN GROUP (ORDER BY line) AS procedure_code
                FROM user_source
                WHERE type = 'PROCEDURE'
                GROUP BY name
                ORDER BY name
                """;
            
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            
            // Create output file
            FileWriter writer = new FileWriter("sql/user_procedures_extracted.sql");
            writer.write("-- Extracted User Procedures from Database\n");
            writer.write("-- Generated on: " + new java.util.Date() + "\n\n");
            
            boolean foundProcedures = false;
            
            while (rs.next()) {
                foundProcedures = true;
                String procedureName = rs.getString("name");
                String procedureCode = rs.getString("procedure_code");
                
                System.out.println("Extracting procedure: " + procedureName);
                
                writer.write("-- ================================================\n");
                writer.write("-- Procedure: " + procedureName + "\n");
                writer.write("-- ================================================\n");
                
                // Add CREATE OR REPLACE if not already present
                if (!procedureCode.trim().toUpperCase().startsWith("CREATE")) {
                    writer.write("CREATE OR REPLACE ");
                }
                
                writer.write(procedureCode);
                
                // Ensure procedure ends with /
                if (!procedureCode.trim().endsWith("/")) {
                    writer.write("\n/\n");
                } else {
                    writer.write("\n");
                }
                
                writer.write("\n");
            }
            
            if (!foundProcedures) {
                System.out.println("No procedures found in the database");
                writer.write("-- No procedures found in the database\n");
            } else {
                System.out.println("Procedures extracted successfully to sql/user_procedures_extracted.sql");
            }
            
            writer.close();
            rs.close();
            stmt.close();
            conn.close();
            
            // Also print procedure list to console
            System.out.println("\n=== PROCEDURE LIST ===");
            printProcedureList();
            
        } catch (Exception e) {
            System.err.println("Error extracting procedures: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void printProcedureList() {
        try {
            DatabaseConfig dbConfig = DatabaseConfig.getInstance();
            Connection conn = dbConfig.getConnection();
            
            String query = """
                SELECT object_name, status, created, last_ddl_time
                FROM user_objects 
                WHERE object_type = 'PROCEDURE'
                ORDER BY object_name
                """;
            
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            
            System.out.printf("%-30s %-10s %-20s %-20s%n", 
                            "PROCEDURE_NAME", "STATUS", "CREATED", "LAST_MODIFIED");
            System.out.println("=".repeat(80));
            
            while (rs.next()) {
                System.out.printf("%-30s %-10s %-20s %-20s%n",
                    rs.getString("object_name"),
                    rs.getString("status"),
                    rs.getTimestamp("created"),
                    rs.getTimestamp("last_ddl_time"));
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
        } catch (Exception e) {
            System.err.println("Error listing procedures: " + e.getMessage());
        }
    }
}
