package webserver.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import static com.sun.org.apache.bcel.internal.util.SecuritySupport.getResourceAsStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author AidenFox
 */
public class ConfigOptions {
    
    private static ConfigUtils configInstance;
    
    public ConfigOptions(ConfigUtils configInstance) throws IOException{
       ConfigOptions.configInstance = configInstance;
       setDefaults();
    }

    private static final Logger LOG = LoggerFactory.getLogger(ConfigOptions.class);
    
    private static void setDefaults() throws IOException {
            if(configInstance.getLineCount() <= 0) {
                LOG.info("  - Filling " + ConfigUtils.classFullPath + " file, with " + ConfigUtils.cfgTemplate + " contents");
            }
            ObjectMapper mapper = new ObjectMapper(); 
            InputStream is = getResourceAsStream(ConfigUtils.cfgTemplate);
            TypeReference<HashMap<String,Object>> typeRef = new TypeReference<HashMap<String,Object>>() {};
            HashMap<String,Object> map = mapper.readValue(is, typeRef); 
 
            for(Map.Entry<String, Object> entry : map.entrySet()){
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    setProperty(key, value);
            }
            setProperty("created", new Date());
    }  
    
    protected static void setProperty(String key, Object value) {
		if (configInstance.checkProperty(key)) {
			//config.changeProperty(key, value);
                } else {
			configInstance.put(key, value);
                        LOG.info("  - Recording a missing key `" + key + "` with value `"+ value + "`");
                }
    }
        
    public static Object getProperty(String key, String type){
        Object property = "";
        
            switch(type){
                case "Int":
                    if (configInstance.checkProperty(key)) {
                            property = configInstance.getPropertyInteger(key);
                    }
                break;
                
                case "String":
                    if (configInstance.checkProperty(key)) {
                            property = configInstance.getPropertyString(key);
                    }
                break;
                
                case "Bool":
                    if (configInstance.checkProperty(key)) {
                            property = configInstance.getPropertyBoolean(key);
                    }
                break;
                
            }
        
        return property;
    }
        
    public static String getWorkdir(Integer index){
        String path;
        switch(index){
            case 1:
                //In user's HOMEDIR
                path = System.getProperty("user.home", "") + File.separator + File.separator;
            break;
            
            case 2:
                //On user's SYSTEMDRIVE
                path = System.getenv("SYSTEMDRIVE") + File.separator + File.separator;
            break;
            
            default:
                //In a folder launched from
                path = "";
            break;
        
        }
        return path;
    }
        
}
