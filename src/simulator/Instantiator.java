package simulator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.StNode;
import highway.HWCoordinator;
import highway.HWNode;
import highway.Segment;
import network.Messageable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Instantiator {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String filepath_coord = "resources/simconfig-coordinator.json";
    private static final String filepath_hwnodes = "resources/simconfig-hwnodes.json";
    private static final String filepath_cars = "resources/simconfig-cars.json";
    private SimMainHandler handler;



    /*private Instantiator(SimMainHandler handler){
        this.handler = handler;
    }*/

    public static void main(String[] args) {
        new Instantiator().instantiate();
    }

    private CoordInstantiator readCoord() throws IOException {
        File file = new File(filepath_coord);
        return mapper.readValue(file, CoordInstantiator.class);
    }

    private List<HWNodeInstantiator> readHWnodes() throws IOException {
        File file = new File(filepath_hwnodes);
        return mapper.readValue(file, new TypeReference<HWNodeInstantiator>() {
        });
    }

    private List<CarInstantiator> readCars() throws IOException {
        File file = new File(filepath_cars);
        return mapper.readValue(file, new TypeReference<CarInstantiator>() {
        });
    }

    public void instantiate() {
        try {
            CoordInstantiator coord_inst = readCoord();
            List<HWNodeInstantiator> nodes_inst = readHWnodes();
            List<CarInstantiator> cars_inst = readCars();

            HWCoordinator coord = null;
            //List<HWNodeSim> hwnodes;
            List<HWNode> hwnodes = new ArrayList<>();
            List<CarSim> car;

            if (coord_inst.isCreate_Coordinator()) {
                coord = new HWCoordinator(coord_inst.getSegments(), coord_inst.getPort());
            }
            ArrayList<Messageable> posiblesCoord = new ArrayList<>();
            posiblesCoord.add(coord);
            ArrayList<StNode> posibleshwnodes = new ArrayList<>();
            for (HWNodeInstantiator n : nodes_inst) {
                HWNode node = new HWNode(posiblesCoord);
                hwnodes.add(node);
                posibleshwnodes.add(node.getStNode());
            }
            //TODO not finished, better to do the config files for the nodes first, so that their initialisation its easier


        } catch (IOException e) {
            e.printStackTrace();
        }
       /* List<Segment> segments;
        CoordInstantiator coor = new CoordInstantiator();
        coor.setCreate_Coordinator(true);
        coor.setPort(9000);
        segments = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            double begin = i * 50;
            double end = begin + 50;
            segments.add(new Segment(begin, end, 0, 100, i));
        }
        coor.setSegments(segments);
        try{
            File file = new File(filepath);
            //Ident output so it is human readable and editable
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(file,coor);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
       /*try{
           File file = new File(filepath);
           segments = mapper.readValue(file,new TypeReference<List<Segment>>(){});
           System.out.println(segments);

       } catch (IOException e) {
           e.printStackTrace();
       }*/
    }

    private class CarInstantiator {
        private String name;
        private int x_position;
        private double velocity;
        private int delay;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getX_position() {
            return x_position;
        }

        public void setX_position(int x_position) {
            this.x_position = x_position;
        }

        public double getVelocity() {
            return velocity;
        }

        public void setVelocity(double velocity) {
            this.velocity = velocity;
        }
    }

    private class CoordInstantiator {
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

    private class HWNodeInstantiator {
        private String ip;
        private int port;

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }
}
