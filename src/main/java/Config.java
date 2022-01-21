import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

    public class Config {

        public final HashMap<String, String> config = new HashMap<>();

        public Config(String path){
            try {
                File myObj = new File(path);
                Scanner myReader = new Scanner(myObj);
                while (myReader.hasNextLine()) {
                    String entry = myReader.nextLine();
                    if(!entry.startsWith("#")) {
                        String key = entry.substring(0, entry.indexOf(':')).trim();
                        String value = entry.substring(entry.indexOf(':') + 1).trim();
                        config.put(key, value);
                    }
                }
                myReader.close();
            } catch (FileNotFoundException e) {
                System.out.println("The config file could not be loaded. Did your remember to create it? It should be in the same Directory as the .jar file.");
                e.printStackTrace();
            }
        }
        public String get(String key){
            return config.get(key);
        }


    }

