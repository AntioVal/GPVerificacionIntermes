/*
 *    Creado por:                   Hernández Paredes Denis Alin
 *    Fecha:                        28/07/2011
 *    Descripción:                  Controlador : "EnvioMail.java" Clase para el enviar correo electrónico.
 *    Responsable:                  Carlos Altamirano
 */
package modelo;

import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.BodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.InternetAddress;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

public class EnvioMail {

    /**
     * Método que envia correo electrónico sin archivo adjunto.
     * @param correoOrigen : Correo emisor.
     * @param correoDestino : Correos destino.
     * @param asunto : Asunto del correo electrónico.
     * @param texto : Mensaje del Correo.
     * @return boolean: True si la transacción se realizó
     * satisfactoriamente ese en otro caso.
     */
    public static boolean enviaCorreo(String correoOrigen, String correoDestino, String asunto, String texto) {

        boolean envio = false;
        Transport t = null;
        Properties props = null;

        try {
            props = new Properties();
            // Nombre del host de correo.
            props.setProperty("mail.smtp.host", "mail.abugaber.com");
            // Puerto para envio de correos.
            props.setProperty("mail.smtp.sport", "587");
            // Nombre del usuario que envia el correo.
            props.setProperty("mail.smtp.user", correoOrigen);
            // Si requiere o no usuario y password para conectarse.
            props.setProperty("mail.smtp.auth", "true");
            // Preparamos la sesion
            Session session = Session.getDefaultInstance(props);

            InternetAddress address[] = new InternetAddress[1];
            address[0] = new InternetAddress(correoDestino);

            // Construimos el mensaje a enviar.
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(correoOrigen));
            message.setRecipients(Message.RecipientType.TO, address);
            message.setSubject(asunto);
            message.setText(texto, "ISO-8859-1", "html");

            // Enviamos el mensaje.
            t = session.getTransport("smtp");
            //Autentificamos usuario y password del emisor.
            t.connect(correoOrigen, "riesgo2014");
            t.sendMessage(message, message.getAllRecipients());

            // Cerramos las conexiones.
            if (t != null) {
                t.close();
            }
            envio = true;
        } catch (Exception e) {
            envio = false;
            System.out.println("enviaSoloCorreo:" + e.getMessage());
            // Cerramos las conexiones.
            try {
                if (t != null) {
                    t.close();
                }
            } catch (Exception ex) {
                System.out.println("enviaSoloCorreo:" + ex.getMessage());
            }
        }
        return envio;
    }

    /**
     * Método que envía correo electrónico con uno o más archivos adjuntos.
     * @param correoOrigen : Correo emisor.
     * @param correosDestino : Correo destinatarios.
     * @param asunto : Asunto del correo electrónico.
     * @param cuerpo: Cuerpo del correo (puede ser código HTML).
     * @param urlArchivo : Ruta de los archivos adjuntos.
     * @param archivosAdjunto : Nombre de archivos a adjuntar (archivo1.jpg;archivo2.pdf;...)
     * @return true si todo va bien, else en otro caso.
     */
    public static boolean enviaCorreo(String correoOrigen, String correosDestino, String asunto, String cuerpo, String urlArchivo, String archivosAdjunto) {
        boolean envio = false;
        Transport t = null;
        Properties props = null;
        String[] correos = null;
        String[] archivos = null;
        try {
            props = new Properties();
            // Nombre del host de correo.
            props.setProperty("mail.smtp.host", "mail.abugaber.com");
            // Puerto para envio de correos.
            props.setProperty("mail.smtp.sport", "587");
            // Nombre del usuario que envia el correo.
            props.setProperty("mail.smtp.user", correoOrigen);
            // Si requiere o no usuario y password para conectarse.
            props.setProperty("mail.smtp.auth", "true");
            // Preparamos la sesion
            Session session = Session.getDefaultInstance(props);
            // Obtenemos mas información del proceso en pantalla.
            //session.setDebug(true);

            //Obtenemos el correo electrónico de los receptores.
            if (correosDestino.indexOf(",") != 1) {
                correos = correosDestino.split(",");
            } else {
                correos = new String[1];
                correos[0] = correosDestino;
            }
            //Generamos un inetAdress para cada dirección electrónica obtenida.
            InternetAddress address[] = new InternetAddress[correos.length];
            for (int i = 0; i < correos.length; i++) {
                address[i] = new InternetAddress(correos[i]);
            }
            // Construimos el mensaje a enviar.
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(correoOrigen));
            message.setRecipients(Message.RecipientType.TO, address);
            message.setSubject(asunto);
            message.setText(cuerpo, "ISO-8859-1", "html");
            //Construimos el cuerpo del mensaje.
            MimeBodyPart text = new MimeBodyPart();
            text.setText(cuerpo, "ISO-8859-1", "html");
            BodyPart xtexto = (BodyPart) text;

            //Obtenemos el nombre de los archivos que seran adjuntados.
            if (archivosAdjunto.indexOf(";") != 1) {
                archivos = archivosAdjunto.split(";");
            } else {
                archivos = new String[1];
                archivos[0] = archivosAdjunto;
            }
            //Construimos el adjunto (adjuntamos los n-archivos)
            List<BodyPart> bp = new LinkedList<BodyPart>();
            for (int i = 0; i <= archivos.length - 1; i++) {
                BodyPart adjunto = new MimeBodyPart();
                adjunto.setDataHandler(new DataHandler(new FileDataSource(urlArchivo + archivos[i])));
                adjunto.setFileName(archivos[i]);
                //Añadimos el elemento a la lista
                bp.add(adjunto);
            }
            // Agrupamos el cuerpo del correo y el adjunto.
            MimeMultipart multiParte = new MimeMultipart();
            multiParte.addBodyPart(xtexto);

            Iterator it = bp.iterator();
            while (it.hasNext()) {
                BodyPart attach = (BodyPart) it.next();
                //Lo añadimos al mensaje
                multiParte.addBodyPart(attach);
            }
            //Especificamos el cuerpo y el adjunto del correo
            message.setContent(multiParte);
            // Enviamos el correo.
            t = session.getTransport("smtp");
            //Autentificamos usuario y password del emisor.
            t.connect(correoOrigen, "riesgo2014");
            t.sendMessage(message, message.getAllRecipients());
            // Cerramos las conexiones.
            if (t != null) {
                t.close();
            }
            envio = true;
        } catch (Exception e) {
            envio = false;
            System.out.println("enviaCorreo:" + e.getMessage());
            // Cerramos las conexiones.
            try {
                if (t != null) {
                    t.close();
                }
            } catch (Exception ex) {
                System.out.println("enviaCorreo:" + ex.getMessage());
            }
        }
        return envio;
    }
}
