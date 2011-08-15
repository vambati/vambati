package options; 
 
import java.io.BufferedReader; 
import java.io.IOException; 
 
import java.io.FileReader; 
import java.util.Hashtable; 
import java.util.Enumeration; 
 
public class Options { 
 
    private Hashtable<String,String> h; 
 
    public String configPath = ""; 
     
    public String requiredOptions[] = { 
            "QUERY_TYPE", 
            "SOURCE_LABEL",
            "TARGET_LABEL",
            "BATCH_SIZE"
            }; 
 
    public Options (String filepath)  
    { 
        configPath = filepath;  
        // Initialize the hash table 
        h = new Hashtable<String,String>(requiredOptions.length); 
        for (int i = 0; i < requiredOptions.length; i++) 
            h.put(requiredOptions[i], ""); 
 
        // Check if there is a global variable, that contains the path to the 
        // options. 
        boolean error = false; 
 
        // Open the options file. 
        try {  
            BufferedReader in = new BufferedReader(new FileReader(configPath)); 
            
            while (in.ready()) { 
                String line = in.readLine().trim(); 
                if (line.length() == 0) 
                    continue; 
                if (line.charAt(0) == '#') 
                    continue; 
                line = line.replaceFirst("=", " "); 
                String parts[] = line.split(" "); 
                h.put(parts[0], parts[parts.length - 1]); 
            } 
 
            in.close(); 
        } catch (IOException e) { 
            error = true; 
            System.err.println("Error reading from file " + configPath); 
            System.err.println(e); 
        } 
 
        // Check we don't have any values that aren't set. 
        Enumeration<String> e = h.keys(); 
        while (e.hasMoreElements()) { 
            String key = (String) e.nextElement(); 
            String value = (String) h.get(key); 
            if (value == null || value.equals("")) { 
                System.err.println("Error: Option " + key + " is not set."); 
                error = true; 
            } 
        } 
 
        // All errors in loading the options are fatal! 
        if (error) { 
            System.err.println("CONFIG EXITING!"); 
            System.exit(1); 
        } 
    } 
 
    public boolean defined(String name){ 
        if(h.containsKey(name)){ 
            return true; 
        } 
        return false; 
    } 
     
    public void put(String name,String value){ 
        h.put(name,value); 
    } 
     
    public String get(String name) { 
        if (h.get(name) == null) { 
            System.err.println("Error: Option " + name + " is not set."); 
            //Logger.log("Error: Option " + name + " is not set."); 
            System.exit(1); 
        } 
        return (String) h.get(name); 
    } 
 
    public int getInt(String name) { 
        String s = get(name); 
        return Integer.parseInt(s); 
    } 
 
    public String[] getStrArr(String name) { 
        String s = get(name); 
        return s.split(","); 
    } 
 
    public double getDouble(String name) { 
        String s = get(name); 
        return Double.parseDouble(s); 
    } 
     
    public boolean getBool(String name) { 
        String s = get(name); 
        if (s.toLowerCase().equals("true")) 
            return true; 
        return false; 
    } 
} 
