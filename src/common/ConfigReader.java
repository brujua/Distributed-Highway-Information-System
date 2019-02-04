package common;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class ConfigReader {
    private static final String DEFAULT_ID_FOR_NODE = "unknown";
    private ResourceBundle resourceBundle;

    public ConfigReader(String configFileName) throws MissingResourceException {
        resourceBundle = ResourceBundle.getBundle(configFileName);
    }

    public String getStringProperty(String propertyName) {
        return resourceBundle.getString(propertyName);
    }

    public int getIntProperty(String propertyName) {
        return Integer.valueOf(resourceBundle.getString(propertyName));
    }

    public List<StNode> getNodes() {
        List<StNode> nodes = new ArrayList<>();
        int numberOfPossibleNodes = Integer.valueOf(resourceBundle.getString("nodenumber"));
        for (int i = 0; i < numberOfPossibleNodes; i++) {
            String ip = resourceBundle.getString("node" + i);
            int port_start = Integer.valueOf(resourceBundle.getString("port_range_start" + i));
            int port_end = Integer.valueOf(resourceBundle.getString("port_range_end" + i));
            for (int port = port_start; port <= port_end; port++) {
                nodes.add(new StNode(DEFAULT_ID_FOR_NODE, ip, port));
            }
        }
        return nodes;
    }
}
