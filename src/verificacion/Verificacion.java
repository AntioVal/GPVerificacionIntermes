/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package verificacion;

import modelo.GeneraEdoCta;

/**
 *
 * @author Luis-Valerio
 */
public class Verificacion {

    public static boolean corteLast = false;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        if (args != null && args.length == 1) {
            //Aplicacion ejecutada con un solo argumento(todavía es día de corte o antes de que lo sea)
            if (args[0].equals("last")) {
                System.out.println("Ejecutando el proceso para estados de cuenta del mes anterior");
                corteLast = true;
                GeneraEdoCta.init();
            }
        } else {
            GeneraEdoCta.init();
        }
    }
}
