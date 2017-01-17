import java.util.*;
import java.util.stream.*;
import java.io.*;
import java.nio.file.*;

class Instance {
    public int id;
    public HashMap<Integer,Double> term;

    public Instance(int id, HashMap<Integer,Double> term) {
        this.id = id;
        this.term = term;
    }
}

public class SequentialMLR {
    private ArrayList<String> topics;
    private HashMap<Integer, ArrayList<String>> qrels;
    private ArrayList<Instance> data;
    private int ITER;
    private int TERM;
    private double alpha;
    private double[] weight[];

    public static void main(String[] args) throws Exception {
        SequentialMLR MLR = new SequentialMLR();
        System.exit(MLR.run(args));
    }
    
    public int run(String[] args) throws Exception {
        if (args.length < 7) {
            System.err.println("Usage: SequentialMLR <alpha> <#iter> <#terms> " +
                               "<topics> <qrels> <training data> <output file>");
            return -1;
        }

        alpha = Double.parseDouble(args[0]);
        ITER  = Integer.parseInt(args[1]);
        TERM  = Integer.parseInt(args[2]);
        String topicsPath = args[3];
        String qrelsPath  = args[4];
        String trainPath  = args[5];
        String outputPath = args[6];

        topics = LoadTopicList(topicsPath);
        qrels  = LoadQrels(qrelsPath);
        data   = LoadData(trainPath);

        // topics.stream().forEach(p -> System.out.print(p + " "));
        // qrels.entrySet().stream().forEach(p -> System.out.println(p.getKey() + " : " + p.getValue()));
        // data.entrySet().stream().forEach(p -> System.out.println(p.getKey() + " : " + p.getValue()));

        // alpha /= (double)data.size();
        weight = new double[topics.size()][];
        for (int i = 0; i < topics.size(); ++i)
            weight[i] = new double[TERM + 1];

        for (int i = 0; i < topics.size(); ++i)
            GD(topics.get(i), weight[i]);

        outputData(outputPath);

        return 0;
    }

    public static ArrayList<String> LoadTopicList(String filepath) throws IOException {
        ArrayList<String> topics = new ArrayList<String>();
        
        for(String line : Files.readAllLines(Paths.get(filepath)))
            topics.add(line);
            
        return topics;
    }

    public static HashMap<Integer,ArrayList<String>> LoadQrels(String filepath) throws IOException {
        HashMap<Integer, ArrayList<String>> qrels = new HashMap<Integer, ArrayList<String>>();
        
        for(String line : Files.readAllLines(Paths.get(filepath))) {
            String[] parts = line.split(" ");
            int id = Integer.parseInt(parts[1]);
            
            if(qrels.containsKey(id))
                qrels.get(id).add(parts[0]);
            else {
                ArrayList<String> lst = new ArrayList<String>();
                lst.add(parts[0]);
                qrels.put(id, lst);
            }
        }
        
        return qrels;
    }

    public static ArrayList<Instance> LoadData(String filepath) throws IOException{
        ArrayList<Instance> data = new ArrayList<Instance>();

        for(String line:Files.readAllLines(Paths.get(filepath))) {
            String[] parts = line.split("\\s+");
            int id = Integer.parseInt(parts[0]);
            HashMap<Integer, Double> pair = new HashMap<Integer, Double>();
            
            for(int i = 1; i < parts.length; i++) {
                String[] sub_parts = parts[i].split(":");
                pair.put(Integer.parseInt(sub_parts[0]),
                         Double.parseDouble(sub_parts[1]));
            }
            
            data.add(new Instance(id, pair));
        }
        
        return data;
    }

    public void outputData(String outputPath) throws IOException{
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath));
        IntStream.range(0, topics.size())
                 .forEach(i -> { try {
                         writer.write(topics.get(i));
                         for (int j = 0; j < TERM + 1; ++j)
                             writer.write(" " + weight[i][j]);
                         writer.write("\n");
                     } catch (IOException e) {
                         throw new RuntimeException(e);
                     }});
        writer.close();
    }

    private static double sigmoid(double z) {
        return 1.0 / (1.0 + Math.exp(-z));
    }

    private static double predict(double W[], HashMap<Integer, Double> x) {
        double res = W[0]; // constant

        for(Map.Entry<Integer, Double> entry : x.entrySet()) {
            int key = entry.getKey();
            double value = entry.getValue();

            res += W[key] * value;
        }

        return sigmoid(res);
    }

    private void GD(String cat, double W[]) {
        double p, label;
        Instance x;

        for (int iter = 0; iter < ITER; ++iter) {
            for (int r = 0; r < data.size(); ++r) {
                x = data.get(r);
                p = predict(W, x.term);
            
                if (qrels.get(x.id).contains(cat))
                    label = 1.0;
                else
                    label = 0.0;

                W[0] += alpha * (label - p); // constant
                for(Map.Entry<Integer, Double> entry : x.term.entrySet()) {
                    int key = entry.getKey();
                    double value = entry.getValue();

                    W[key] += alpha * (label - p) * value;
                }
            }
        }
    }
}
