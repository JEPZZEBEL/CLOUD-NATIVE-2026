package cl.duoc.tickets.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PdfGenerator {

    public static void generateTicketPdf(Path outputPath,
                                         String ticketId,
                                         Long eventoId,
                                         String usuario,
                                         Integer cantidad) throws IOException {

        Files.createDirectories(outputPath.getParent());

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 18);
                cs.newLineAtOffset(50, 750);
                cs.showText("TICKET DE EVENTO");
                cs.endText();

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 12);
                cs.newLineAtOffset(50, 700);
                cs.showText("Ticket ID: " + ticketId);
                cs.newLineAtOffset(0, -20);
                cs.showText("Evento ID: " + eventoId);
                cs.newLineAtOffset(0, -20);
                cs.showText("Usuario: " + usuario);
                cs.newLineAtOffset(0, -20);
                cs.showText("Cantidad: " + cantidad);
                cs.endText();
            }

            doc.save(outputPath.toFile());
        }
    }
}
