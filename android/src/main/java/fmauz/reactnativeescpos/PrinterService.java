package fmauz.reactnativeescpos;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.github.escposjava.print.Printer;
import io.github.escposjava.print.exceptions.BarcodeSizeError;
import io.github.escposjava.print.exceptions.QRCodeException;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import fmauz.reactnativeescpos.helpers.EscPosHelper;
import fmauz.reactnativeescpos.utils.BitMatrixUtils;
import static io.github.escposjava.print.Commands.*;

public class PrinterService {
    public static final int PRINTING_WIDTH_58_MM = 384;
    public static final int PRINTING_WIDTH_80_MM = 576;
    private static final String CARRIAGE_RETURN = System.getProperty("line.separator");
    private LayoutBuilder layoutBuilder = new LayoutBuilder();
    private final int DEFAULT_QR_CODE_SIZE = 200;
    private int printingWidth = PRINTING_WIDTH_58_MM;
    private io.github.escposjava.PrinterService basePrinterService;

    public PrinterService(Printer printer) throws IOException {
        basePrinterService = new io.github.escposjava.PrinterService(printer);
    }

    public PrinterService(Printer printer, int printingWidth) throws IOException {
        basePrinterService = new io.github.escposjava.PrinterService(printer);
        this.printingWidth = printingWidth;
    }

    public void cutPart() {
        basePrinterService.cutPart();
    }

    public void cutFull() {
        basePrinterService.cutFull();
    }

    public void print(String text) throws UnsupportedEncodingException {
        write(text.getBytes("GBK"));
    }

    public void printLn(String text) throws UnsupportedEncodingException {
        print(text + CARRIAGE_RETURN);
    }

    public void lineBreak() {
        basePrinterService.lineBreak();
    }

    public void lineBreak(int nbLine) {
        basePrinterService.lineBreak(nbLine);
    }

    // TODO: This isn't working correctly
    public void printBarcode(String code, String bc, int width, int height, String pos, String font)
            throws BarcodeSizeError {
        basePrinterService.printBarcode(code, bc, width, height, pos, font);
    }

    public void printSample() throws IOException {
        String design =
            "               ABC Inc. {C}               " + "\n" +
            "           1234 Main Street {C}           " + "\n" +
            "        Anytown, US 12345-6789 {C}        " + "\n" +
            "            (555) 123-4567 {C}            " + "\n" +
            "                                          " + "\n" +
            "          D0004 | Table #: A1 {C}         " + "\n" +
            "------------------------------------------" + "\n" +
            "Item            {<>}    Qty  Price  Amount" + "\n" +
            "Chicken Rice    {<>}      2  12.50   25.00" + "\n" +
            "Coke Zero       {<>}      5   3.00   15.00" + "\n" +
            "Fries           {<>}      3   3.00    9.00" + "\n" +
            "Fresh Oyster    {<>}      1   8.00    8.00" + "\n" +
            "Lobster Roll    {<>}      1  16.50   16.50" + "\n" +
            "------------------------------------------" + "\n" +
            "       {QR[Where are the aliens?]}        " + "\n";

        printDesign(design);
    }

    public static void printReport(String ip, int port, String value) throws IOException, Throwable {
        Socket sock = new Socket(ip, port);
        OutputStreamWriter dout = new OutputStreamWriter( sock.getOutputStream(), "LATIN1" );

        JSONObject report = new JSONObject(value);
        char[] h1 = { 0x1B, 0x46 };
        dout.write(h1);
        dout.write("                 Tomball" + "\n");
        dout.write("------------------------------------------" + "\n");
        dout.write("Terminal: " + report.getString("profileName") + "\n");
        dout.write("Sala:     " + report.getString("roomName") + "\n");
        dout.write("Inicio:   " + report.getString("startDate") + "\n");
        dout.write("Termino:  " + report.getString("endDate") + "\n");
        dout.write("------------------------------------------" + "\n\n");
        dout.write( "Qtd. Vendas diretas: " + report.getString("totalDirectTickets") + "\n");
        dout.write( "Vendas diretas:      " + report.getString("amountDirectTickets") + "\n");
        dout.write( "Premio:              " + report.getString("amountAwardTickets") + "\n");
        dout.write( "Comissao:            " + report.getString("commissionTickets") + "\n");
        dout.write( "Subtotal:            " + report.getString("subtotalDirect") + "\n\n");
        dout.write( "Qtd. Recargas:       " + report.getString("totalCharge") + "\n");
        dout.write( "Vendas recargas:     " + report.getString("amountCharge") + "\n");
        dout.write( "Comissao:            " + report.getString("commissionCharge") + "\n");
        dout.write( "Subtotal:            " + report.getString("subtotalCharge") + "\n\n");
        dout.write( "Total:               " + report.getString("total") + "\n\n" );
        dout.write("------------------------------------------" + "\n");
        dout.write("               tomballapp.com" + "\n");
        dout.write("            " + report.getString("printerDate") + "\n");
        dout.write("\n\n\n");
        dout.flush();
        dout.close();
        sock.close();
    }

    public static void printRecharge(String ip, int port, String value) throws IOException, Throwable {
        Socket sock = new Socket(ip, port);
        OutputStreamWriter dout = new OutputStreamWriter( sock.getOutputStream(), "LATIN1" );

        JSONObject report = new JSONObject(value);
        char[] h = { 0x1B, 0x45 };
        char[] dupla = { 0x1B,0x56 };
        char[] h1 = { 0x1B, 0x46 };
        char[] fontA = { 0x1b, 0x4d, 0x00 };
        char[] fontB = { 0x1b, 0x4d, 0x01 };
        char[] doubleWidth = { 0x1b, 0x21, 0x20 };
        char[] doubleHeight = { 0x1b, 0x21, 0x10 };
        char[] normal = { 0x1b, 0x21, 0x00};
        char[] alignCenter = { 0x1b, 0x61, 0x01 };
        char[] alignLeft = { 0x1b, 0x61, 0x00 };
        char[] alignRight = { 0x1b, 0x61, 0x02 };
        
        dout.write(alignCenter);
        dout.write("Tomball" + "\n");
        dout.write(alignLeft);
        dout.write("--------------------------------------------------" + "\n");
        dout.write("Sala:     " + report.getString("roomName") + "\n");
        dout.write("Terminal: " + report.getString("profileName") + "\n");
        dout.write("Valor:    " + report.getString("price") + "\n");
        dout.write("Data:     " + report.getString("createdAtFormatted") + "\n");
        dout.write("--------------------------------------------------" + "\n");
        dout.write("Para usar a recarga acesse tomballapp.com, clique" + "\n");
        dout.write("no menu Recargas e entre o codigo abaixo." + "\n");
        dout.write("--------------------------------------------------" + "\n\n");
        dout.write(alignCenter);
        dout.write(doubleWidth);
        dout.write(report.getString("code"));
        dout.write(normal);
        dout.write("\n\n--------------------------------------------------" + "\n");
        dout.write("tomballapp.com" + "\n");
        dout.write(report.getString("printerDate") + "\n");
        dout.write(alignLeft);
        dout.write("\n\n\n");
        dout.flush();
        dout.close();
        sock.close();
    }

    public void printRecharge(String value) throws IOException, Throwable {
        JSONObject report = new JSONObject(value);
        String design = "";
        design += "{C}Tomball" + "\n";
        design += "--------------------------------" + "\n";
        design += "Sala:     " + report.getString("roomName") + "\n";
        design += "Terminal: " + report.getString("profileName") + "\n";
        design += "Valor:    " + report.getString("price") + "\n";
        design += "Data:     " + report.getString("createdAtFormatted") + "\n";
        design += "--------------------------------" + "\n";
        design += "Para usar a recarga acesse" + "\n";
        design += "tomballapp.com, clique no menu" + "\n";
        design += "Recargas e entre o codigo abaixo." + "\n";
        design += "--------------------------------" + "\n";
        design += "{C}{H1}" + report.getString("code") ;
        design += "{LS:M}\n--------------------------------" + "\n";
        design += "{C}tomballapp.com" + "\n";
        design += "{C}" + report.getString("printerDate") + "\n";
        design += "{LS:M}\n\n\n";
        printDesign(design);
    }

    public void printReport(String value) throws IOException, Throwable {
        JSONObject report = new JSONObject(value);

        print("            Tomball" + "\n");
        print("--------------------------------" + "\n");
        print("Terminal: " + report.getString("profileName") + "\n");
        print("Sala:     " + report.getString("roomName") + "\n");
        print("Inicio:   " + report.getString("startDate") + "\n");
        print("Termino:  " + report.getString("endDate") + "\n");
        print("--------------------------------" + "\n\n");
        print( "Qt. Vendas diretas: " + report.getString("totalDirectTickets") + "\n");
        print( "Vendas diretas:     " + report.getString("amountDirectTickets") + "\n");
        print( "Premio:             " + report.getString("amountAwardTickets") + "\n");
        print( "Comissao:           " + report.getString("commissionTickets") + "\n");
        print( "Subtotal:           " + report.getString("subtotalDirect") + "\n\n");
        print( "Qtd. Recargas:      " + report.getString("totalCharge") + "\n");
        print( "Vendas recargas:    " + report.getString("amountCharge") + "\n");
        print( "Comissao:           " + report.getString("commissionCharge") + "\n");
        print( "Subtotal:           " + report.getString("subtotalCharge") + "\n\n");
        print( "Total:              " + report.getString("total") + "\n\n" );
        print("--------------------------------" + "\n");
        print("        tomballapp.com" + "\n");
        print("     " + report.getString("printerDate") + "\n");
        print("\n\n\n");
    }

    public static void printCard(String ip, int port, String cards) throws IOException, Throwable {
        Socket sock = new Socket(ip, port);
        OutputStreamWriter dout = new OutputStreamWriter( sock.getOutputStream(), "LATIN1" );

        JSONArray matchCards = new JSONArray(cards);
        for (int i=0; i < matchCards.length(); i++) {
            JSONObject matchCard = matchCards.getJSONObject(i);
            printCard(dout, matchCard);
        }
        dout.write("\n");
        dout.flush();
        dout.close();
        sock.close();
    }

    public void printCard(String cards) throws IOException, Throwable {
        JSONArray matchCards = new JSONArray(cards);
        JSONObject matchCardFirst = matchCards.getJSONObject(0);

        String roomName = matchCardFirst.getString("roomName");
        String profileName = matchCardFirst.getString("profileName");
        String createdAt = matchCardFirst.getString("createdAtFormatted");
        String matchDate = matchCardFirst.getString("matchDateFormatted");
        String matchName = matchCardFirst.getString("matchName");
        String ticketId = matchCardFirst.getString("ticketId");
        String matchCardId = matchCardFirst.getString("id");
        Double price = matchCardFirst.getDouble("price");

        String cardsDesign = "";
        cardsDesign += ("            Tomball" + "\n");
        cardsDesign += ("--------------------------------" + "\n");
        cardsDesign += ("Codigo:    " + matchCardId + "\n");
        cardsDesign += ("Terminal:  " + profileName + "\n");
        cardsDesign += ("Sala:      " + roomName + "\n");
        cardsDesign += ("Partida:   " + matchName + "\n");
        cardsDesign += ("Horario:   " + matchDate + "\n");
        cardsDesign += ("Valor:     " + price + "\n");
        cardsDesign += ("Data:      " + createdAt + "\n");
        cardsDesign += ("--------------------------------" + "\n");

        for (int i=0; i < matchCards.length(); i++) {
            JSONObject matchCard = matchCards.getJSONObject(i);
            cardsDesign += printCardDesign(matchCard) + "\n\n\n";
        }

        printDesign(cardsDesign);
        print("--------------------------------" + "\n");
        print("tomballapp.com\n");
        print("\n\n\n\n");
    }

    public static JSONObject getCardNumber( JSONObject matchCard, int positionX, int positionY ) throws JSONException {
        JSONArray cardNumbers = matchCard.getJSONArray("CardNumbers");
        JSONObject cardNumber = null;

        for(int i = 0; i <= cardNumbers.length()-1; i++){
            JSONObject ref = cardNumbers.getJSONObject(i);
            if( ref.getInt("positionX") == positionX && ref.getInt("positionY") == positionY ){
                cardNumber = ref;
                break;
            }
        }

        return cardNumber;
    }

    public static String printCardDesign(JSONObject matchCard) throws JSONException {
        String roomName = matchCard.getString("roomName");
        String profileName = matchCard.getString("profileName");
        String createdAt = matchCard.getString("createdAtFormatted");
        String matchDate = matchCard.getString("matchDateFormatted");
        String matchName = matchCard.getString("matchName");
        String ticketId = matchCard.getString("ticketId");
        String matchCardId = matchCard.getString("id");
        Double price = matchCard.getDouble("price");

        JSONArray cardNumbers = matchCard.getJSONArray("CardNumbers");
        String line = matchCardId + "\n";
        line += "{H1} ──┬──┬──┬──┬──{LS:M}\n";
        for( int y = 0; y < 3; y++) {
            line += "{H1}│";
            for(int i = 0; i <= cardNumbers.length()-1; i++){
                JSONObject ref = cardNumbers.getJSONObject(i);
                if( ref.getInt("positionY") == y ){
                    line += String.format("%02d", ref.getInt("value")) + "│";
                }
            }
            if(y < 2){
                line += "{H1}├──┼──┼──┼──┼──┤{LS:M}\n";
            }else{
                line += "{H1}└──┴──┴──┴──┴──┘{LS:M}\n";
            }
        }

        return line;

    }

    public static void printCard(OutputStreamWriter dout, JSONObject matchCard) throws IOException, JSONException {
        char[] c = { 0x1B, 0x12, 0x1B, 0x33, 0x12  };
        char[] h1 = { 0x1B, 0x46 };
        char[] dupla = { 0x1B,0x56 };
        char[] th = { 0xDA, 0xC4, 0xC4, 0xC4, 0xC4, 0xC2, 0xC4, 0xC4, 0xC4, 0xC4, 0xC2, 0xC4, 0xC4, 0xC4, 0xC4, 0xC2, 0xC4, 0xC4, 0xC4, 0xC4, 0xC2, 0xC4, 0xC4, 0xC4, 0xC4, 0xC2, 0xC4, 0xC4, 0xC4, 0xC4, 0xC2, 0xC4, 0xC4, 0xC4, 0xC4, 0xC2, 0xC4, 0xC4, 0xC4, 0xC4, 0xC2, 0xC4, 0xC4, 0xC4, 0xC4, 0xBF };
        char[] h = { 0x1B, 0x45 };
        // HEADER DA CARTELA
        dout.write(c);
        dout.write(h);

        String roomName = matchCard.getString("roomName");
        String profileName = matchCard.getString("profileName");
        String createdAt = matchCard.getString("createdAtFormatted");
        String matchDate = matchCard.getString("matchDateFormatted");
        String matchName = matchCard.getString("matchName");
        String ticketId = matchCard.getString("ticketId");
        String matchCardId = matchCard.getString("id");
        Double price = matchCard.getDouble("price");

        dout.write(ticketId + " - " + matchCardId + " - " + matchDate + "\n" );
        dout.write(h1);
        dout.write(th);
        dout.write(c);
        dout.write("\n");
        for( int y = 0; y < 3; y++) {
            dout.write(dupla);
            for (int x = 0; x < 9; x++) {
                dout.write(0xB3);

                JSONObject cardNumber = getCardNumber(matchCard, x, y);
                if ( cardNumber != null ) {
                    char[] boldSt = {0x1B, 0x45};
                    dout.write(boldSt);
                    dout.write(" " + String.format("%02d", cardNumber.getInt("value")) + " ");
                    char[] boldEn = {0x1B, 0x46};
                    dout.write(boldEn);
                } else {
                    dout.write("    ");
                }
            }
            dout.write(0xB3);
            dout.write("\n");

            if (y == 2) {
                char[] tBt = {0xC0, 0xC4, 0xC4, 0xC4, 0xC4, 0xC1, 0xC4, 0xC4, 0xC4, 0xC4, 0xC1, 0xC4, 0xC4, 0xC4, 0xC4, 0xC1, 0xC4, 0xC4, 0xC4, 0xC4, 0xC1, 0xC4, 0xC4, 0xC4, 0xC4, 0xC1, 0xC4, 0xC4, 0xC4, 0xC4, 0xC1, 0xC4, 0xC4, 0xC4, 0xC4, 0xC1, 0xC4, 0xC4, 0xC4, 0xC4, 0xC1, 0xC4, 0xC4, 0xC4, 0xC4, 0xD9};
                dout.write(tBt);
            } else {
                char[] tBt = {0xC3, 0xC4, 0xC4, 0xC4, 0xC4, 0xC5, 0xC4, 0xC4, 0xC4, 0xC4, 0xC5, 0xC4, 0xC4, 0xC4, 0xC4, 0xC5, 0xC4, 0xC4, 0xC4, 0xC4, 0xC5, 0xC4, 0xC4, 0xC4, 0xC4, 0xC5, 0xC4, 0xC4, 0xC4, 0xC4, 0xC5, 0xC4, 0xC4, 0xC4, 0xC4, 0xC5, 0xC4, 0xC4, 0xC4, 0xC4, 0xC5, 0xC4, 0xC4, 0xC4, 0xC4, 0xB4};
                dout.write(tBt);
            }
            dout.write("\n");
        }
        dout.write(h);
        dout.write(createdAt + " - " + profileName + " - " + price + " tomballapp.com\n\n " );

    }

    public void printDesign(String text) throws IOException {
        ByteArrayOutputStream baos = generateDesignByteArrayOutputStream(text);
        write(baos.toByteArray());
    }

    public void printImage(String filePath) throws IOException {
        Uri fileUri = Uri.parse(filePath);
        Bitmap image = BitmapFactory.decodeFile(fileUri.getPath());
        printImage(image);
    }

    public void printImage(Bitmap image) throws IOException {
        image = EscPosHelper.resizeImage(image, printingWidth);
        ByteArrayOutputStream baos = generateImageByteArrayOutputStream(image);
        write(baos.toByteArray());
    }

    public void printQRCode(String value, int size) throws QRCodeException {
        ByteArrayOutputStream baos = generateQRCodeByteArrayOutputStream(value, size);
        write(baos.toByteArray());
    }

    public void write(byte[] command) {
        basePrinterService.write(command);
    }

    public void setCharCode(String code) {
        basePrinterService.setCharCode(code);
    }

    public void setCharsOnLine(int charsOnLine) {
        layoutBuilder.setCharsOnLine(charsOnLine);
    }

    public void setPrintingWidth(int printingWidth) {
        this.printingWidth = printingWidth;
    }

    public void setTextDensity(int density) {
        basePrinterService.setTextDensity(density);
    }

    public void beep() {
        basePrinterService.beep();
    }

    public void open() throws IOException {
        basePrinterService.open();
    }

    public void close() throws IOException {
        basePrinterService.close();
    }

    public void kickCashDrawerPin2(){
        basePrinterService.write(CD_KICK_2);
    }

    public void kickCashDrawerPin5(){
        basePrinterService.write(CD_KICK_5);
    }

    /**
     * DESIGN 1: Order List                       *
     *          D0004 | Table #: A1 {C} {H1}      *
     * ------------------------------------------ *
     * [Dine In] {U} {B}                          *
     * [ ] Espresso {H2}                          *
     *     - No sugar, Regular 9oz, Hot           *
     *                               {H3} {R} x 1 *
     * ------------------------------------------ *
     * [ ] Blueberry Cheesecake {H2}              *
     *     - Slice                                *
     *                               {H3} {R} x 1 *
     *                                            *
     * DESIGN 2: Menu Items                       *
     * ------------------------------------------ *
     * Item         {<>}       Qty  Price  Amount *
     * Pork Rice    {<>}         1  13.80   13.80 *
     *                                            *
     * DESIGN 3: Barcode                          *
     * {QR[Love me, hate me.]} {C}                *
     **/
    private ByteArrayOutputStream generateDesignByteArrayOutputStream(String text) throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(text.trim()));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String line;

        while ((line = reader.readLine()) != null) {
            if (line.matches("\\{QR\\[(.+)\\]\\}")) {
                try {
                    baos.write(generateQRCodeByteArrayOutputStream(line.replaceAll("\\{QR\\[(.+)\\]\\}", "$1"),
                            DEFAULT_QR_CODE_SIZE).toByteArray());
                } catch (QRCodeException e) {
                    throw new IOException(e);
                }
                continue;
            }

            boolean bold = line.contains("{B}");
            boolean underline = line.contains("{U}");
            boolean h1 = line.contains("{H1}");
            boolean h2 = line.contains("{H2}");
            boolean h3 = line.contains("{H3}");
            boolean lsm = line.contains("{LS:M}");
            boolean lsl = line.contains("{LS:L}");
            boolean ct = line.contains("{C}");
            boolean rt = line.contains("{R}");
            int charsOnLine = layoutBuilder.getCharsOnLine();

            // TODO: Shouldn't put it here
            byte[] ESC_t = new byte[] { 0x1b, 't', 0x00 };
            byte[] ESC_M = new byte[] { 0x1b, 'M', 0x00 };
            byte[] FS_and = new byte[] { 0x1c, '&' };
            byte[] TXT_NORMAL_NEW = new byte[] { 0x1d, '!', 0x00 };
            byte[] TXT_4SQUARE_NEW = new byte[] { 0x1d, '!', 0x11 };
            byte[] TXT_2HEIGHT_NEW = new byte[] { 0x1d, '!', 0x01 };
            byte[] TXT_2WIDTH_NEW = new byte[] { 0x1d, '!', 0x10 };
            byte[] LINE_SPACE_68 = new byte[] { 0x1b, 0x33, 68 };
            byte[] LINE_SPACE_88 = new byte[] { 0x1b, 0x33, 120 };
            byte[] DEFAULT_LINE_SPACE = new byte[] { 0x1b, 50 };

            baos.write(ESC_t);
            baos.write(FS_and);
            baos.write(ESC_M);

            // Add tags
            if (bold) {
                baos.write(TXT_BOLD_ON);
                line = line.replace("{B}", "");
            }
            if (underline) {
                baos.write(TXT_UNDERL_ON);
                line = line.replace("{U}", "");
            }
            if (h1) {
                baos.write(TXT_4SQUARE_NEW);
                baos.write(LINE_SPACE_88);
                line = line.replace("{H1}", "");
                charsOnLine = charsOnLine / 2;
            } else if (h2) {
                baos.write(TXT_2HEIGHT_NEW);
                baos.write(LINE_SPACE_88);
                line = line.replace("{H2}", "");
            } else if (h3) {
                baos.write(TXT_2WIDTH_NEW);
                baos.write(LINE_SPACE_68);
                line = line.replace("{H3}", "");
                charsOnLine = charsOnLine / 2;
            }
            if (lsm) {
                baos.write(LINE_SPACE_24);
                line = line.replace("{LS:M}", "");
            } else if (lsl) {
                baos.write(LINE_SPACE_30);
                line = line.replace("{LS:L}", "");
            }
            if (ct) {
                baos.write(TXT_ALIGN_CT);
                line = line.replace("{C}", "");
            }
            if (rt) {
                baos.write(TXT_ALIGN_RT);
                line = line.replace("{R}", "");
            }

            try {
                baos.write(layoutBuilder.createFromDesign(line, charsOnLine).getBytes("GBK"));
            } catch (UnsupportedEncodingException e) {
                // Do nothing?
            }

            // Remove tags
            if (bold) {
                baos.write(TXT_BOLD_OFF);
            }
            if (underline) {
                baos.write(TXT_UNDERL_OFF);
            }
            if (h1 || h2 || h3) {
                baos.write(DEFAULT_LINE_SPACE);
                baos.write(TXT_NORMAL_NEW);
            }
            if (lsm || lsl) {
                baos.write(LINE_SPACE_24);
            }
            if (ct || rt) {
                baos.write(TXT_ALIGN_LT);
            }
        }

        return baos;
    }

    private ByteArrayOutputStream generateImageByteArrayOutputStream(Bitmap image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        baos.write(LINE_SPACE_24);
        for (int y = 0; y < image.getHeight(); y += 24) {
            baos.write(SELECT_BIT_IMAGE_MODE); // bit mode
            // width, low & high
            baos.write(new byte[] { (byte) (0x00ff & image.getWidth()), (byte) ((0xff00 & image.getWidth()) >> 8) });
            for (int x = 0; x < image.getWidth(); x++) {
                // For each vertical line/slice must collect 3 bytes (24 bytes)
                baos.write(EscPosHelper.collectImageSlice(y, x, image));
            }
            baos.write(CTL_LF);
        }

        return baos;
    }

    private ByteArrayOutputStream generateQRCodeByteArrayOutputStream(String value, int size) throws QRCodeException {
        try {
            BitMatrix result = new QRCodeWriter().encode(value, BarcodeFormat.QR_CODE, size, size, null);
            Bitmap qrcode = BitMatrixUtils.convertToBitmap(result);
            return generateImageByteArrayOutputStream(qrcode);
        } catch (IllegalArgumentException | WriterException | IOException e) {
            // Unsupported format
            throw new QRCodeException("QRCode generation error", e);
        }
    }
}
