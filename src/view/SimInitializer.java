package view;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import highway.HWCoordinator;
import highway.Segment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Class responsible of initialize the simulation according to the json config files,
 * utilizing the HWController interface
 */
public class SimInitializer {

    private static final Logger logger = LoggerFactory.getLogger(SimInitializer.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String filepath_coord = "resources/simconfig.json";
    private static final String filepath_hwnodes = "resources/simconfig-hwnodes.json";
    private static final String filepath_cars = "resources/simconfig-cars.json";
    private HWController controller;
    private boolean simModeOn;

    public SimInitializer(HWController controller) {
        this.controller = controller;
        simModeOn = false;
    }

    private Config readConfig() throws IOException {
        File file = new File(filepath_coord);
        return mapper.readValue(file, Config.class);
    }

    private List<HWNodeInstantiator> readHWnodes() throws IOException {
        File file = new File(filepath_hwnodes);
        return mapper.readValue(file, new TypeReference<List<HWNodeInstantiator>>() {
        });
    }

    private List<CarInstantiator> readCars() throws IOException {
        File file = new File(filepath_cars);
        return mapper.readValue(file, new TypeReference<List<CarInstantiator>>() {
        });
    }

    public void initialize() {
        try {
            Config config = readConfig();
            simModeOn = config.isSimulation_mode_on();

            if (!simModeOn) {
                logger.info("Simulation Mode its OFF");
                return;
            }
            List<CarInstantiator> cars_inst = readCars();
            List<HWNodeInstantiator> nodes_inst = readHWnodes();

            new HWCoordinator(config.getSegments()).listenForMsgs();

            for (HWNodeInstantiator node : nodes_inst) {
                controller.addHWNode(new DrawableHWNode(node.getName()), node.getDelay());
            }
            for (CarInstantiator car : cars_inst) {
                controller.addCar(new DrawableCar(car.getName(), car.getStartPosition(), car.getVelocity()), car.getDelay());
            }

        } catch (IOException e) {
            logger.error("Simulation Config files are corrupt: " + e.getMessage());
        }
    }

    public boolean isSimModeOn() {
        return simModeOn;
    }

    /*
     * INTERNAL REPRESENTATION OF THE NEEDED PARAMETERS FOR INITIALIZATION
     *
     * */

    private static class CarInstantiator {
        private String name;
        private int startPosition;
        private double velocity;
        private int delay;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getStartPosition() {
            return startPosition;
        }

        public void setStartPosition(int startPosition) {
            this.startPosition = startPosition;
        }

        public double getVelocity() {
            return velocity;
        }

        public void setVelocity(double velocity) {
            this.velocity = velocity;
        }

        public int getDelay() {
            return delay;
        }

        public void setDelay(int delay) {
            this.delay = delay;
        }
    }

    private static class Config {
        private boolean simulation_mode_on;
        private List<Segment> segments;

        public List<Segment> getSegments() {
            return segments;
        }

        public void setSegments(List<Segment> segments) {
            this.segments = segments;
        }

        public boolean isSimulation_mode_on() {
            return simulation_mode_on;
        }

        public void setSimulation_mode_on(boolean simulation_mode_on) {
            this.simulation_mode_on = simulation_mode_on;
        }
    }

    private static class HWNodeInstantiator {
        private String name;
        private int delay;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getDelay() {
            return delay;
        }

        public void setDelay(int delay) {
            this.delay = delay;
        }
    }
}
