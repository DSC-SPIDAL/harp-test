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

public class Verify {
    private ArrayList<String> topics;
    private HashMap<Integer, ArrayList<String>> qrels;
    private ArrayList<Instance> data;
    private int TERM;
    private HashMap<String, double[]> weight;
    private HashMap<String, Double> results;
    private double F_1;

    public static void main(String[] args) throws Exception {
        Verify V = new Verify();
        System.exit(V.run(args));
    }
    
    public int run(String[] args) throws Exception {
        if (args.length < 6) {
            System.err.println("Usage: Verify <#terms> " +
                               "<topics> <qrels> <test data> " +
                               "<trained parameter> <output file>");
            return -1;
        }

        TERM  = Integer.parseInt(args[0]);
        String topicsPath = args[1];
        String qrelsPath  = args[2];
        String dataPath   = args[3];
        String WPath      = args[4];
        String outputPath = args[5];

        topics = LoadTopicList(topicsPath);
        qrels  = LoadQrels(qrelsPath);
        data   = LoadData(dataPath);

        weight = LoadWeight(WPath);
        results = new HashMap<String, Double>();

        F_1 = 0.0;
        for (int i = 0; i < topics.size(); ++i)
            test(topics.get(i));
        F_1 /= (double)topics.size();

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

    public HashMap<String, double[]> LoadWeight(String filePath) throws IOException {
        HashMap<String, double[]> W = new HashMap<String, double[]>();

        for(String line:Files.readAllLines(Paths.get(filePath))) {
            String[] parts = line.split("\\s+");
            String cat = parts[0];
            double[] w = new double[TERM + 1];

            for(int i = 1; i < parts.length; i++) {
                w[i - 1] = Double.parseDouble(parts[i]);
            }
            
            W.put(cat, w);
        }
        
        return W;
    }

    public void outputData(String outputPath) throws IOException{
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath));
        IntStream.range(0, topics.size())
                 .forEach(i -> { try {
                             String cat = topics.get(i);
                             writer.write(cat + ", " + results.get(cat) + "\n");
                     } catch (IOException e) {
                         throw new RuntimeException(e);
                     }});
        writer.write(F_1 + "\n");
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

    public void test(String cat) {
        double w[] = weight.get(cat);
        double A, B, C, F;
        double p;

        A = B = C = 0.0;
        for (Instance inst : data) {
            p = predict(w, inst.term);

            if (p >= 0.5) {
                if (qrels.get(inst.id).contains(cat))
                    A += 1.0;
                else
                    B += 1.0;
            }
            else if (qrels.get(inst.id).contains(cat))
                C += 1.0;
        }
        //System.out.println(cat + " : " + A + " , " + B + " , " + C);

        if (A == 0.0 && B == 0.0 && C == 0.0)
            F = 0.0;
        else
            F = (A + A) / (A + A + B + C);
        
        results.put(cat, F);
        F_1 += F;
    }
}
