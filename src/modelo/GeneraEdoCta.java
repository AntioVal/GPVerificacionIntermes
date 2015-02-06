/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo;

import controller.ConexionSQL;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

/**
 *
 * @author Luis-Valerio
 */
public class GeneraEdoCta {

    static public Vector errores = new Vector();

    static public boolean init() {
        boolean correcto = false;

        //Lo primero que se realiza es la inserción en la BD de la hora y 
        //fecha en que inicia la genración de estado de cuenta para el termino del mes en curso
//    correcto=true;
            String cuerpo_correo = "";
            //se prosigue con la generacion del LayOut de estado de cuenta
            correcto = generaLayOut();
            if (!errores.isEmpty()) {
                cuerpo_correo = ModeloEstadoCuenta.getEmailBody(errores);
                if (errores.size() > 1) {
                    correcto = false;
                }
            }
            if (correcto) {
                System.out.println("El LayOut de estado de cuenta fue generado correctamente");
                SimpleDateFormat formatNameFile = new SimpleDateFormat("yyyyMM");
                String urlArchivo = "source\\";
                String nombre_reportes = "EDO_CTA_" + formatNameFile.format(new Date()) + ".txt";
                correcto = modelo.EnvioMail.enviaCorreo("liquidaciones@solucionesmultiples.com.mx", "soporte@solucionesmultiples.com.mx", "PRUEBA INTER-MES GENERACION EDO_CTA", cuerpo_correo, urlArchivo, nombre_reportes);
                System.out.println("La generación de LayOut finalizó correctamente.");
                return true;
            } else {
                System.out.println(" ----------------- FATAL ERROR ----------- ");
                System.out.println("Error generando el archivo de estado de cuenta, verificar Excepciones.");
                String urlArchivo = "source\\";
                SimpleDateFormat formatNameFile = new SimpleDateFormat("yyyyMM");
                String nombre_reportes = "EDO_CTA_" + formatNameFile.format(new Date()) + ".txt";
                System.out.println("Nombre reporte:" + nombre_reportes);
                boolean exp = false;
                try {
                    File fichero = new File(urlArchivo + nombre_reportes);
                    if (fichero.exists()) {
                        correcto = modelo.EnvioMail.enviaCorreo("liquidaciones@solucionesmultiples.com.mx", "soporte@solucionesmultiples.com.mx", "ERRORES PRUEBA INTER-MES GENERACION EDO_CTA", cuerpo_correo, urlArchivo, nombre_reportes);
                    } else {
                        correcto = modelo.EnvioMail.enviaCorreo("liquidaciones@solucionesmultiples.com.mx", "soporte@solucionesmultiples.com.mx", "ERRORES PRUEBA INTER-MES GENERACION EDO_CTA", cuerpo_correo);
                    }
                } catch (Exception e) {
                    exp = true;
                    System.out.println("Error:el archivo no pudo ser leído");
                } finally {
                    if (exp) {
                        correcto = modelo.EnvioMail.enviaCorreo("liquidaciones@solucionesmultiples.com.mx", "soporte@solucionesmultiples.com.mx", "ERRORES PRUEBA INTER-MES GENERACION EDO_CTA", cuerpo_correo);
                    }
                }
                if (correcto) {
                        System.out.println("La generación de LayOut finalizó CON_ERRORES, favor de verificar información.");
                        return false;                    
                } else {
                    System.out.println("Error al enviar correo de notificación CON_ERRORES.");
                }
                correcto = false;
            }
        errores = new Vector();
        return correcto;
    }

    static boolean generaLayOut() {
        ConexionSQL conn = new ConexionSQL();
        ConexionSQL conn_In = new ConexionSQL();
        int movs_por_generar = 0;
        int movs_correctos = 0;
        int movs_erroneos = 0;
        Vector errores = new Vector();
        Vector datos_error = new Vector();
        Writer writer = null;

        Connection connection = null;
        Statement statement = null;
        Connection connection_In = null;
        Statement statement_In = null;
        Vector contratos = new Vector();
        Vector<Double> info_EC = null;
        String MySql = "";

        try {
            connection = conn.ConectaSQLServer();
            statement = connection.createStatement();
            ResultSet rstSQLServer = null;
            int i = 1;
            Date fechaHoy = new Date();
//            SimpleDateFormat formatoFechaCodes = new SimpleDateFormat("dd/MMM/yyyy");
            SimpleDateFormat formatoFechaCodes = new SimpleDateFormat("dd/MMM/yyyy");
            DecimalFormat formato_numero = new DecimalFormat("0.00");
            String correoCC = "";
            String correoCCO = "";
            System.out.println("Comenzamos generacion de archivo");


            statement.execute("SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED");
            MySql = "select clave_contrato,id_codes,correo from contratos where status ='A' and clave_contrato!='FID000PRB0514' and id_codes not in('0') order by clave_contrato asc ";

            rstSQLServer = statement.executeQuery(MySql);
            Calendar calendario = Calendar.getInstance();
//            SimpleDateFormat formatoFechaBD = new SimpleDateFormat("yyyy-dd-MM");
            SimpleDateFormat formatoFechaBD = new SimpleDateFormat("yyyy-MM-dd");
            //Si esta habilitado lanzar la generaciónd de estados de cuenta del mes anterior, se resta un mes a la fecha actual
            if (verificacion.Verificacion.corteLast) {
                calendario.add(Calendar.MONTH, -1);
            }
            calendario.set(Calendar.DAY_OF_MONTH, 1);
            String fecha_inicio_mes = formatoFechaBD.format(calendario.getTime());
            String fecha_hoy = formatoFechaBD.format(new java.util.Date());
            if (verificacion.Verificacion.corteLast) {
                Date maxDiaMes = ModeloEstadoCuenta.getUltimoDiaHabilDeMesAnterior();
                fecha_hoy = formatoFechaBD.format(maxDiaMes);
            }
            System.out.println("Comienza while de contratos");
            while (rstSQLServer.next()) {
                movs_por_generar++;
                String contrato = rstSQLServer.getString("clave_contrato");
//                System.out.println("Obteniendo info de contrato= " + contrato);
                String id_codes = rstSQLServer.getString("id_codes");
                String correo_asesor = rstSQLServer.getString("correo").trim();
                String correos_usuarios = ModeloEstadoCuenta.getCorreoUsuariosFideicomiso(contrato);
                if (correo_asesor != null && !correo_asesor.equals("") && !correo_asesor.isEmpty()) {
                    correoCCO = correo_asesor + ",edocta@solucionesmultiples.com.mx";
                } else {
                    correoCCO = "edocta@solucionesmultiples.com.mx";
                }
                double saldo_inicio_mes = ModeloEstadoCuenta.getSaldo_inicio_mes(contrato, fecha_inicio_mes);
                info_EC = ModeloEstadoCuenta.getDatos_EC(contrato, fecha_inicio_mes, fecha_hoy, saldo_inicio_mes);
                Vector info_contratos = new Vector();
                if (verificacion.Verificacion.corteLast) {
                    fechaHoy = ModeloEstadoCuenta.getUltimoDiaHabilDeMesAnterior();
                }
                info_contratos.add(id_codes + "|" + contrato + "|del 01 al " + DateFormat.getDateInstance(DateFormat.LONG).format(fechaHoy)
                        + "|" + formato_numero.format(saldo_inicio_mes) + "|" + formato_numero.format(info_EC.get(0)));

                info_contratos.add("|" + formato_numero.format(info_EC.get(1)) + "|" + formato_numero.format(info_EC.get(2))
                        + "|" + formato_numero.format(info_EC.get(3)) + "|" + formato_numero.format(info_EC.get(4)));
                info_contratos.add(info_EC.get(5));
                info_contratos.add("|" + correos_usuarios + "|" + correoCC + "|" + correoCCO);
                info_contratos.add(info_EC.get(6));
                info_contratos.add(contrato);
                contratos.add(info_contratos);

            }
            System.out.println("finaliza while de contratos ");
            rstSQLServer.close();
            statement.close();
            if (connection != null) {
                conn.Desconecta(connection);
            }

            DecimalFormat formatoDec = new DecimalFormat("0.00");
            int j = 1;
            int campo = 1;
//                writer = new OutputStreamWriter(new FileOutputStream("source\\EDO_CTA_201407.txt"), "UTF-8");
            SimpleDateFormat formatNameFile = new SimpleDateFormat("yyyyMM");
            writer = new OutputStreamWriter(new FileOutputStream("source\\EDO_CTA_" + formatNameFile.format(new Date()) + ".txt"), "UTF-8");
            writer.write("00|SME060316PB2\r\n");

            for (j = 0; j < contratos.size(); j++) {
//                System.out.println("Comienza ");
                Vector info_contratos = (Vector) contratos.get(j);
                double saldo_final_mes = Double.parseDouble(info_contratos.get(2).toString());
                double saldo_actual = ModeloEstadoCuenta.getSaldo(info_contratos.get(5).toString());
                if(verificacion.Verificacion.corteLast){
                saldo_actual = ModeloEstadoCuenta.getSaldo_ini_sig_mes(info_contratos.get(5).toString(), formatoFechaBD);
                }
                double diferencia = saldo_actual - saldo_final_mes;
                boolean diferenciaGrande = true;
                if (Math.abs(diferencia) > 1) {
                    System.out.println("La diferencia es muy grande: " + diferencia);
                    diferenciaGrande = true;
                } else {
                    // se prosigue a modificar el saldo_final_mes y se inserta en BD
                    saldo_final_mes = saldo_actual;
                    diferenciaGrande = false;
                }

                System.out.println("----------------  " + info_contratos.get(5) + "  ----------------");
                writer.write("01|" + info_contratos.get(0) + info_contratos.get(1) + "|" + formato_numero.format(saldo_final_mes) + info_contratos.get(3) + "\r\n");
                int numero_movimientos = ((Double) info_contratos.get(4)).intValue();
//                System.out.println("Numero de movimientos=" + numero_movimientos);

                if (numero_movimientos > 0) {
                    ////CONSULTA DE ESTADO DE CUENTA     
                    connection_In = conn_In.ConectaSQLServer();
                    statement_In = connection_In.createStatement();
                    ResultSet rstSQLServer_In = null;
//                    System.out.println("Comienza segunda consulta");
                    statement_In.execute("SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED ");
                    MySql = " select * from EC_" + info_contratos.get(5).toString();
                    MySql += " where fecha>='" + fecha_inicio_mes + " 00:00:00.000' ";
                    MySql += " and fecha<='" + fecha_hoy + " 23:59:59.999' ";
                    MySql += " order by fecha asc ";

                    rstSQLServer_In = statement_In.executeQuery(MySql);
                    while (rstSQLServer_In.next()) {
                        java.util.Date fecha = rstSQLServer_In.getDate("fecha");
                        String concepto = rstSQLServer_In.getString("concepto");
                        String observaciones = rstSQLServer_In.getString("observaciones");
                        double cargo = rstSQLServer_In.getDouble("cargo");
                        double abono = rstSQLServer_In.getDouble("abono");
                        double saldo = rstSQLServer_In.getDouble("saldo");
                        writer.write("02|" + formatoFechaCodes.format(fecha) + "|" + concepto + "| " + observaciones + "|" + formatoDec.format(cargo) + "|" + formatoDec.format(abono) + "|" + formatoDec.format(saldo) + "\r\n");
                        campo++;
                    }
                    rstSQLServer_In.close();
                    statement_In.close();
                } else if (numero_movimientos == 0) {
                    writer.write("02|" + formatoFechaCodes.format(new java.util.Date()) + "|" + "NO EXISTEN MOVIMIENTOS" + "| " + " " + "|" + "0.0" + "|" + "0.0" + "|" + "0.0" + "\r\n");
                }
                //se realizará comparación entre saldo_actual y saldo_final_mes
                campo = 1;
//                System.out.println("----------------TERMINA: " + info_contratos.get(5).toString() + "--------------------");

                if (!diferenciaGrande) {
//                    System.out.println("Actualizando saldo_inicio_mes en BD");
////////////////////////********************************************************************                        
//                      ESTO SE QUITA  CUANDO SE PONGA A FUNCIONAR REALMENTE                        
//                    boolean bandera = false;
//                    if (ModeloEstadoCuenta.compruebaNuevoSaldoMes(info_contratos.get(5).toString())) {
////                        bandera = ModeloEstadoCuenta.setSaldo_inicio_mes(info_contratos.get(5).toString(), saldo_final_mes);
//                    } else {
//                        System.out.println("Error verificando si se contaba con este saldo al inicio de mes");
//                        datos_error = new Vector();
//                        datos_error.add(info_contratos.get(5).toString());
//                        datos_error.add("Error al verificar saldo de nuevo inicio de mes.");
//                        errores.add(datos_error);
//                    }
                    boolean bandera = true;
                    if (!bandera) {
                        movs_erroneos++;
                        System.out.println("Error al actualizar el saldo al inicio de mes");
                        datos_error = new Vector();
                        datos_error.add(info_contratos.get(5).toString());
                        datos_error.add("Error al actualizar el saldo a inicio de mes en Base de Datos.");
                        errores.add(datos_error);
                    } else {
                        movs_correctos++;
                    }
                } else {
                    movs_erroneos++;
                    writer.write("ERROR: El saldo actual y el saldo final de mes, ¡NO SON IGUALES!\r\n");
                    datos_error = new Vector();
                    datos_error.add(info_contratos.get(5).toString());
                    datos_error.add("El saldo actual y el saldo final de mes, no coinciden.");
                    errores.add(datos_error);
                }
            }
            System.out.println("Resumen de estado de cuenta:");
            System.out.println("Movimientos encontrados: " + movs_por_generar);
            System.out.println("Movimientos generados correctamente: " + movs_correctos);
            System.out.println("Movimientos con errores: " + movs_erroneos);
            datos_error = new Vector();
            datos_error.add("" + movs_erroneos);
            datos_error.add("" + movs_correctos);
            datos_error.add("" + movs_por_generar);
            errores.add(datos_error);
            System.out.println("{{{{{{{{{ TERMINA GENERACIÓN DE LAYOUT{}}}}}}}}}}");
        } catch (Exception ex) {
            movs_erroneos = -1;
            System.out.println("!!!!!!!!!!!!!!!!!!Error al generar archivo LayOut:" + ex.getMessage());
        } finally {
            try {
                writer.close();
//                writer_inicio.close();
            } catch (Exception e) {
                System.out.println("GneraEdoCta:" + e.getMessage());
            }
            GeneraEdoCta.errores = errores;
        }

        return movs_erroneos == 0;
    }
}
