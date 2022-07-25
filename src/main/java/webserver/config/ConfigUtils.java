package webserver.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author AidenFox
 */

public abstract class ConfigUtils {
	/*INPUT*/
        public final File classFullPath;
        public final String cfgTemplate;
        
        
	private Boolean cached = false;
	private HashMap<String, String> cache;
	private InputStream input = null;
        private static final Logger LOG = LoggerFactory.getLogger(ConfigUtils.class);

	public ConfigUtils(File fullPath, String cfgTemplate) {
		this.classFullPath = fullPath;
                this.cfgTemplate = cfgTemplate;
	}

	public Boolean isCached() {
		return cached;
	}

	public void setCached(Boolean cached) {
		this.cached = cached;
		if (cached.equals(false)) {
			cache = null;
                }
	}
        
        public void load() {
            LOG.info("Loading config "+classFullPath);
		if (!classFullPath.exists()){
                    create();
                    LOG.info("  - Creating " + classFullPath);
                } else {
                    LOG.info("  - Config file exists "+getLineCount()+" lines");
                }

		if (cached){
			cache = this.loadHashMap();
                }
                try {
                    ConfigOptions.setDefaults(classFullPath, cfgTemplate);
                } catch (IOException ex) {}
	}

	private void create() {
			FileOutputStream output = null;
			try {
				classFullPath.getParentFile().mkdirs();
				output = new FileOutputStream(classFullPath);
				byte[] buf = new byte[8192];
				int length;
				while ((length = input.read(buf)) > 0) {
					output.write(buf, 0, length);
				}
			} catch (Exception e) {
			} finally {
				try {
					//input.close();
				} catch (Exception ignored) {
				}
				try {
					if (output != null)
						output.close();
				} catch (Exception ignored) {
				}
			}
	}

	private HashMap<String, String> loadHashMap() {
		HashMap<String, String> result = new HashMap<String, String>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(classFullPath));
			String line;
			while ((line = br.readLine()) != null) {
				if ((line.isEmpty()) || (line.startsWith("#")) || (!line.contains(": ")))
					continue;
				String[] args = line.split(": ");
				if (args.length < 2) {
					result.put(args[0], null);
					continue;
				}
				result.put(args[0], args[1]);
			}
		} catch (IOException ex) {
		} finally {
			try {
				br.close();
			} catch (Exception e) {
			}
		}
		return result;
	}

	public String getPropertyString(String property) {
		try {
			if (cached)
				return cache.get(property);
			else {
				HashMap<String, String> contents = loadHashMap();
				return contents.get(property);
			}
		} catch (Exception e) {}
		return null;
	}

	public Integer getPropertyInteger(String property) {
		try {
			if (this.cached)
				return Integer.parseInt(cache.get(property));
			else {
				HashMap<String, String> contents = loadHashMap();
				return Integer.parseInt(contents.get(property));
			}
		} catch (Exception e) {
		}
		return null;
	}

	public Boolean getPropertyBoolean(String property) {
		try {
			String result;
			if (this.cached)
				result = this.cache.get(property);
			else {
				HashMap<String, String> contents = this.loadHashMap();
				result = contents.get(property);
			}
			if (result != null && result.equalsIgnoreCase("true"))
				return true;
			else
				return false;
		} catch (Exception e) {
		}
		return null;
	}

	public Double getPropertyDouble(String property) {
		try {
			String result;
			if (cached)
				result = cache.get(property);
			else {
				HashMap<String, String> contents = this.loadHashMap();
				result = contents.get(property);
			}
			if (!result.contains(""))
				result += ".0";
			return Double.parseDouble(result);
		} catch (Exception e) {
		}
		return null;
	}

	public Boolean checkProperty(String key) {
		String check;
		try {
                    if (cached){
        		check = cache.get(key);
                    } else {
                        HashMap<String, String> contents = this.loadHashMap();
			check = contents.get(key);
			}
			if (check != null)
				return true;
		} catch (Exception e) {
			return false;
		}

		return false;
	}

	private void flush(HashMap<Integer, String> newContents) {
		try {
			this.delFile(classFullPath);
			classFullPath.createNewFile();
			BufferedWriter writer = new BufferedWriter(new FileWriter(classFullPath));
			for (int i = 1; i <= newContents.size(); i++) {
				String line = newContents.get(i);
				if (line == null || line.split(": ").length == 1) {
					writer.append("");
					continue;
				}
				writer.append(line);
				writer.append("\n");
			}
			writer.flush();
			writer.close();
			if (cached)
				load();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void delFile(File file) {
		if (file.exists())
			file.delete();
	}

	private HashMap<Integer, String> getAllFileContents() {
		HashMap<Integer, String> result = new HashMap<Integer, String>();
		BufferedReader br = null;
		Integer i = 1;
		try {
			br = new BufferedReader(new FileReader(classFullPath));
			String line;
			while ((line = br.readLine()) != null) {
				if (line.isEmpty()) {
					result.put(i, null);
					i++;
					continue;
				}

				result.put(i, line);
				i++;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (Exception e) {
			}
		}

		return result;
	}

	public void insertComment(String comment) {
		HashMap<Integer, String> contents = this.getAllFileContents();
		contents.put(contents.size() + 1, "#" + comment);
		flush(contents);
	}

	public void insertComment(String comment, Integer line) {
            LOG.info("Inserting a comment - #" + comment);
		HashMap<Integer, String> contents = getAllFileContents();
		if (line >= contents.size() + 1) {
			return;
                }
		HashMap<Integer, String> newContents = new HashMap<>();
		for (int i = 1; i < line; i++){
			newContents.put(i, contents.get(i));
                }
		newContents.put(line, "#" + comment);
		for (int i = line; i <= contents.size(); i++){
			newContents.put(i + 1, contents.get(i));
                }
		flush(newContents);
	}

	public void put(String property, Object obj) {
		HashMap<Integer, String> contents = this.getAllFileContents();
		contents.put(contents.size() + 1, property + ": " + obj.toString());
		flush(contents);
	}

	public void put(String property, Object obj, Integer line) {
		HashMap<Integer, String> contents = this.getAllFileContents();
		if (line >= contents.size() + 1)
			return;
		HashMap<Integer, String> newContents = new HashMap<Integer, String>();
		for (int i = 1; i < line; i++)
			newContents.put(i, contents.get(i));
		newContents.put(line, property + ": " + obj.toString());
		for (int i = line; i <= contents.size(); i++)
			newContents.put(i + 1, contents.get(i));
		flush(newContents);
	}

	public void changeProperty(String property, Object obj) {
		HashMap<Integer, String> contents = this.getAllFileContents();
		if ((contents == null))
			return;
		for (int i = 1; i <= contents.size(); i++) {
			if (contents.get(i) == null)
				continue;
			String check = contents.get(i);
			if (check.startsWith(property)) {
				check = check.replace(property, "");
				if (!(check.startsWith(": ")))
					continue;
				contents.remove(i);
				contents.put(i, property + ": " + obj.toString());
			}
		}
		this.flush(contents);
	}

	public Integer getLineCount() {
		HashMap<Integer, String> contents = getAllFileContents();
		return contents.size();
	}
}