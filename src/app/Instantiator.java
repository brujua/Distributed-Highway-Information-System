package app;

import com.fasterxml.jackson.databind.ObjectMapper;
import highway.HWCoordinator;
import highway.Segment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

public final class Instantiator {
    private static final Logger logger = LoggerFactory.getLogger(Instantiator.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String filepath_coord = "resources/hw-config.json";

    private static Config readConfig() throws IOException {
        File file = new File(filepath_coord);
        return mapper.readValue(file, Config.class);
    }

    public static HWCoordinator getCoordinator() {
        try {
            Config config = readConfig();
            return new HWCoordinator(config.getSegments());
        } catch (IOException e) {
            logger.error("Hw config file is corrupt: " + e.getMessage());
        }
        return null;
    }

    private static class Config {
        private List<Segment> segments;

        public List<Segment> getSegments() {
            return segments;
        }

        public void setSegments(List<Segment> segments) {
            this.segments = segments;
        }

    }
}
