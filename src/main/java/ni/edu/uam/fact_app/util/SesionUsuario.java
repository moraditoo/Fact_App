package ni.edu.uam.fact_app.util;

import ni.edu.uam.fact_app.model.Usuario;

public final class SesionUsuario {
    private static Usuario usuarioActual;

    private SesionUsuario() { }

    public static void setUsuarioActual(Usuario usuario) {
        usuarioActual = usuario;
    }

    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public static void cerrarSesion() {
        usuarioActual = null;
    }

    public static boolean esAdministrador() {
        if (usuarioActual == null || usuarioActual.getEmpleado() == null || usuarioActual.getEmpleado().getCargo() == null) {
            return false;
        }
        return usuarioActual.getEmpleado().getCargo().getNombre().equalsIgnoreCase("Administrador");
    }

    public static boolean esCajero() {
        if (usuarioActual == null || usuarioActual.getEmpleado() == null || usuarioActual.getEmpleado().getCargo() == null) {
            return false;
        }
        return usuarioActual.getEmpleado().getCargo().getNombre().equalsIgnoreCase("Cajero");
    }

    public static boolean esBodeguero() {
        if (usuarioActual == null || usuarioActual.getEmpleado() == null || usuarioActual.getEmpleado().getCargo() == null) {
            return false;
        }
        return usuarioActual.getEmpleado().getCargo().getNombre().equalsIgnoreCase("Bodeguero");
    }
}