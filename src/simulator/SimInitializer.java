package simulator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import highway.HWCoordinator;
import highway.Segment;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Class responsible of initialize the simulation according to the json config files,
 * utilizing the SimController interface
 */
public class SimInitializer {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String filepath_coord = "resources/simconfig-coordinator.json";
    private static final String filepath_hwnodes = "resources/simconfig-hwnodes.json";
    private static final String filepath_cars = "resources/simconfig-cars.json";
    private SimController controller;

    public SimInitializer(SimController controller) {
        this.controller = controller;
    }

    private CoordInstantiator readCoord() throws IOException {
        File file = new File(filepath_coord);
        return mapper.readValue(file, CoordInstantiator.class);
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
            CoordInstantiator coord_inst = readCoord();
            List<HWNodeInstantiator> nodes_inst = readHWnodes();
            List<CarInstantiator> cars_inst = readCars();

            HWCoordinator coord = null;
            if (coord_inst.isCreate_Coordinator()) {
                coord = new HWCoordinator(coord_inst.getSegments(), coord_inst.getPort());
                coord.listenForMsgs();
            }
            for (HWNodeInstantiator node : nodes_inst) {
                controller.addHWNode(node.getName(), node.getDelay());
            }
            for (CarInstantiator car : cars_inst) {
                controller.addCar(car.getName(), car.getStartPosition(), car.getVelocity(), car.getDelay());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private static class CoordInstantiator {
        private boolean create_Coordinator;
        private int port;
        private List<Segment> segments;

        public List<Segment> getSegments() {
            return segments;
        }

        public void setSegments(List<Segment> segments) {
            this.segments = segments;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public boolean isCreate_Coordinator() {
            return create_Coordinator;
        }

        public void setCreate_Coordinator(boolean create_Coordinator) {
            this.create_Coordinator = create_Coordinator;
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
