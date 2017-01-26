package phonepron;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author baal
 */
public class Phonepron {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        
        Pronouncer pron = new Pronouncer();
        
        List<String> phones = new ArrayList<String>();
        phones.add("79294456434");
        phones.add("73294545685");
        
        for(String s: phones) {
            List<String> slist = pron.getPhoneNames(s);
            writeFile("c:\\temp\\"+s+".txt", slist);
        }
    }
    
    private static void writeFile(String filename, List<String> text) {
        try {
            File logFile=new File(filename);
            BufferedWriter writer;
            writer = new BufferedWriter(new FileWriter(logFile));
           
            for(String s: text) {
                writer.write(s);
                writer.write("\r\n");
            }
            
            writer.close();
        } catch (IOException ex) {
        }
    }
    
    
}
