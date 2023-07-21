import java.util.LinkedHashMap;
import java.util.Map;

public class ParseXML {
    public static Map<Integer, Computer> computers;
    private String xml, trimmedXML;
    private int compSectionBegin, compSectionEnd;

    public ParseXML(String xml) {
        this.xml = xml;
        computers = new LinkedHashMap<>();
        trimXML();
    }

    private void trimXML() {
        compSectionBegin = xml.indexOf("<computers>") + 11;
        compSectionEnd = xml.indexOf("</computers>");
        trimmedXML = xml.substring(compSectionBegin, compSectionEnd);

        evaluateXML();
    }

    private void evaluateXML() {
        while (trimmedXML.contains("<computer>")) {
            int IDleftBound = trimmedXML.indexOf("<id>") + 4;
            int IDrightBound = trimmedXML.indexOf("</id>");
            int nameLeftBound = trimmedXML.indexOf("<name>") + 6;
            int nameRightBound = trimmedXML.indexOf("</name>");
            int snLeftBound = trimmedXML.indexOf("<serial_number>") + 15;
            int snRightBound = trimmedXML.indexOf("</serial_number>");

            int jamfCompID = Integer.parseInt(trimmedXML.substring(IDleftBound, IDrightBound));
            String name = trimmedXML.substring(nameLeftBound, nameRightBound);
            String serial = trimmedXML.substring(snLeftBound, snRightBound);

            computers.put(jamfCompID, new Computer(serial, name));

            trimmedXML = trimmedXML.substring(trimmedXML.indexOf("</computer>") + 11);
        }
    }
}
