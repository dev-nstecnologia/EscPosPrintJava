package formatter;

import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FormatterAll {

    /**
     * Convert a date string in the Sefaz format (AAAA-MM-DDThh:mm:ssTZD)
     * to Brazil Timestamp format (dd/MM/yyyy hh:mm:ss).<br/>
     * Ex.: 2014-09-06T10:20:00-03:00 -> 06/09/2014 10:20:00
     * @param dataSefaz
     * @return
     */
    public static String dataSefazToTimestampBRString(String dataSefaz) throws ParseException {
        SimpleDateFormat sefazDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
        SimpleDateFormat brTimeStampDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        Date sefazDate = sefazDateFormat.parse(dataSefaz);
        return brTimeStampDateFormat.format(sefazDate);
    }

    /*
    * Formata todos os dados: CNPJ, Fone e Chave de Acesso
    *
    * */
    public static String formatCnpjString(String cnpj) {
        if (StringUtils.length(cnpj) != 14){
            throw new NumberFormatException("CNPJ must have 14 digits");
        }

        StringBuilder formattedCnpj = new StringBuilder(cnpj);
        formattedCnpj.insert(2, ".");
        formattedCnpj.insert(6, ".");
        formattedCnpj.insert(10, "/");
        formattedCnpj.insert(15, "-");

        return formattedCnpj.toString();
    }

    public static String formatFoneNumber(String fone, boolean silently){
        StringBuilder foneStr = new StringBuilder(fone);

        if (fone.length() == 8) {
            foneStr.insert(4, "-");

        } else if (fone.length() == 9){
            foneStr.insert(5, "-");

        } else if (fone.length() == 10){
            foneStr.insert(0, "(");
            foneStr.insert(3, ")");
            foneStr.insert(8, "-");

        } else if (fone.length() == 11) {
            if (fone.startsWith("0")){
                foneStr.insert(0, "(");
                foneStr.insert(4, ")");
                foneStr.insert(9, "-");

            } else {
                foneStr.insert(0, "(");
                foneStr.insert(3, ")");
                foneStr.insert(9, "-");
            }

        } else if (fone.length() == 12) {
            foneStr.insert(0, "(");
            foneStr.insert(3, ")");
            foneStr.insert(4, "(");
            foneStr.insert(7, ")");
            foneStr.insert(12, "-");

        } else {
            if (silently) return fone;
            throw new NumberFormatException("Invalid phone number length: " + fone.length());
        }

        return foneStr.toString();

    }

    public static String formatFoneNumber(String fone){
        return formatFoneNumber(fone, true);
    }

    public static String formatChaveAcessoWithSpaces(String key){
        StringBuilder formatted = new StringBuilder(key);

        formatted.insert(4, " ");
        formatted.insert(9, " ");
        formatted.insert(14, " ");
        formatted.insert(19, " ");
        formatted.insert(24, " ");
        formatted.insert(29, " ");
        formatted.insert(34, " ");
        formatted.insert(39, " ");
        formatted.insert(44, " ");
        formatted.insert(49, " ");

        return formatted.toString();
    }

    public static String convertToFormat(String value, String format, Locale locale){
        DecimalFormat df = new DecimalFormat(format, new DecimalFormatSymbols(locale));

        return df.format(Double.parseDouble(value));
    }
}
