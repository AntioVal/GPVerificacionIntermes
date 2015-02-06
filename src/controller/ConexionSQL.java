/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author Luis-Valerio
 */
public class ConexionSQL {

    public ConexionSQL() {
    }       
    
    public Connection ConectaSQLServer() throws SQLException {
        Connection lcnnConexion;
//        System.out.println("Creando conexion ...");
        

        // Conexión local
//        String login = "admin";
//        String password = "admin";
//        String classForName = "sun.jdbc.odbc.JdbcOdbcDriver";
//        String url = "jdbc:odbc:prueba";

        // Conexión local en servidor (189.204.71.149)
//        String login = "fideicomiso";
//              String password = "FIDEICOMISO2011X";
//            String classForName = "sun.jdbc.odbc.JdbcOdbcDriver";
//          String url = "jdbc:odbc:ServerPrincipal";
          
//        Conexion en servidor virtual  
//        String login = "fideicomiso";
//        String password = "FIDEICOMISO2011X";
        String classForName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
//        String url = "jdbc:odbc:Atenea";     
                
        
        try {
            Class.forName(classForName);        
            
        String connectionUrl = "jdbc:sqlserver://localhost:1433;" +
        //CONEXION EN SERVIDOR VIRTUAL                
//        "databaseName=sofom;user=fideicomiso;password=FIDEICOMISO2011X;";
        //CONEXION EN LOCALHOST
        "databaseName=sofom;user=Administrador;password=Administrador;";
//        CONEXION EN SERVIDOR DE SOFOM
//        "databaseName=sofom;user=Administrador;password=sistemas2012#GP;";
        lcnnConexion = DriverManager.getConnection(connectionUrl);
        

            // Conexión
//            lcnnConexion = DriverManager.getConnection(url, login, password);
        } catch (Exception e) {
            System.out.println(e.toString());
            lcnnConexion = null;
        }
        return lcnnConexion;
    }
//
//    public Connection ConectaMySQL() {
//        Connection conexion = null;
//
//        try {
//            Class.forName("com.mysql.jdbc.Driver");
//            String servidor = "jdbc:mysql://localhost/inventario";
//            String usuarioDB = "root";
//            String passwordDB = "";
//            conexion = DriverManager.getConnection(servidor, usuarioDB, passwordDB);
//        } catch (ClassNotFoundException ex) {
////            JOptionPane.showMessageDialog(null, ex, "Error1 en la Conexión con la BD "+ex.getMessage(), JOptionPane.ERROR_MESSAGE);
//            conexion = null;
//        } catch (SQLException ex) {
////            JOptionPane.showMessageDialog(null, ex, "Error2 en la Conexión con la BD "+ex.getMessage(), JOptionPane.ERROR_MESSAGE);
//            conexion = null;
//        } catch (Exception ex) {
////            JOptionPane.showMessageDialog(null, ex, "Error3 en la Conexión con la BD "+ex.getMessage(), JOptionPane.ERROR_MESSAGE);
//            conexion = null;
//        } finally {
//            return conexion;
//        }
//    }

    /**
     * Método para desconectar una conexión existente
     */
    public boolean Desconecta(Connection lcnnConexion) {
        try {
            lcnnConexion.close();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }    
    
}
