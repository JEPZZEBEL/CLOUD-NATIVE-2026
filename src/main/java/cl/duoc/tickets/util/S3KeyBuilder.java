package cl.duoc.tickets.util;

public class S3KeyBuilder {
    public static String build(Long eventoId, String usuario, String ticketId) {
        return "evento" + eventoId + "/" + usuario + "/ticket-" + ticketId + ".pdf";
    }
}
