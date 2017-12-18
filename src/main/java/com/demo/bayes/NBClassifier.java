package com.demo.bayes;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class NBClassifier {

    private static final int COUNT = 4; //总类别数
    // 指定count结果文件
    private static final String COUNT_RESULT_PATH="output"+File.separator+"part-r-00000";
    // 指定保存模型参数的文件
    private static final String MODEL_FILE_PATH="output"+File.separator+"model.txt";

    // 这里存储的是各个类别的先验概率的对数似然
    private static HashMap<String, Double> prior;
    // 这里存储的是类条件概率的对数似然(已做平滑操作)
    private static HashMap<String, Double> likelihood;
    // 存储每一类的词总数： eg:喜悦 4488781.0
    private static HashMap<String, Double> likelihoodNorm;
    // 存储所有的词
    private static HashSet<String> V;

    // 计算出先验概率以及类条件概率的对数似然并存储
    static {
        prior = new HashMap<>(8);
        likelihood = new HashMap<>(1 << 18); //预估有200000+种搭配
        likelihoodNorm = new HashMap<>(8);
        V = new HashSet<>(1 << 17); //预估一共有100000+种词

        BufferedReader br = null;
        BufferedWriter bw = null;
        String line;
        try {
            br = new BufferedReader(new FileReader(COUNT_RESULT_PATH));

            // 先算出先验概率的对数似然
            double sumRow = 0;
            String[] keys = new String[COUNT];
            for (int i=0; i<COUNT; i++){
                String[] ss = br.readLine().substring(1).split("\t");
                double row = Double.parseDouble(ss[1]);
                prior.put(ss[0], row); //暂且先存频数
                sumRow += row;
                keys[i] = ss[0];
            }
            for (int i=0; i<COUNT; i++){
                // 略有bug,如果训练集有一个类别数量为0.不可能的哎呀
                prior.replace(keys[i], Math.log(prior.get(keys[i]) / sumRow));
            }

            // 再算出类条件概率的对数似然, 额外还要算出总类别数
            for (int i=0; i<COUNT; i++){
                String[] ss = br.readLine().substring(1).split("\t");
                likelihoodNorm.put(ss[0], Double.parseDouble(ss[1]));
            }
            while ((line = br.readLine()) != null){
                String[] pseudoWordCount = line.split("\t");
                String[] labelWord = pseudoWordCount[0].split("-");
                //暂且先存下
                likelihood.put(pseudoWordCount[0], Double.parseDouble(pseudoWordCount[1]));
                V.add(labelWord[1]); //添加新词到集合中
            }
            // 遍历并修改算出类条件概率的对数似然的值并做平滑
            for (Map.Entry<String, Double> entry : likelihood.entrySet()) {
                String[] labelWord = entry.getKey().split("-");
                likelihood.replace(entry.getKey(),
                        Math.log((1 + entry.getValue()) /
                                likelihoodNorm.get(labelWord[0]) + V.size()));

            }

            // 把计算得出的模型参数进行输出
            bw = new BufferedWriter(new FileWriter(MODEL_FILE_PATH));
            StringBuffer sb = new StringBuffer();
            // 先输出先验概率的对数似然(注意这样做会产生空行)
            for (Map.Entry<String, Double> entry : prior.entrySet()){
                sb.append("1" + entry.getKey() +"\t" + entry.getValue() + "\n");
            }
            // 再输出所有词的种数
            sb.append(V.size() + "\n");
            bw.write(sb.toString());
            // 再输出所有类条件概率似然(已做平滑)
            StringBuffer sb2 = new StringBuffer();
            for (Map.Entry<String, Double> entry : likelihood.entrySet()) {
                sb2.append(entry.getKey() + "\t" + entry.getValue() + "\n");
            }
            bw.write(sb2.toString());

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try{
                br.close();
                bw.flush();
                bw.close();
            } catch (NullPointerException | IOException e){
                e.printStackTrace();
            }

        }
    }


    private static void train(String trainingData, String modelFile) throws Exception{
        BufferedReader br = new BufferedReader(new FileReader(trainingData));
        String line = null;

        HashMap<String, Integer> model = new HashMap<>();

        int id = 1;

        while ((line = br.readLine()) != null){
            if (id % 10000 == 0){
                System.out.println("line = " + id);
            }

            String label = line.substring(0, line.indexOf("\t"));
            String[] words = line.substring(line.indexOf("\t") + 1).split(" ");

            // label count
            if (model.containsKey(label)) {
                model.put(label, model.get(label) + 1);
            }else{
                model.put(label, 1);
            }

            //label-word count
            for (String word : words){
                if (Pattern.matches("[\u4E00-\u9FA5]+", word)){
                    String pseudoW = label + "-" +word;
                }
            }
        }
    }

    private static String predict(String sentence){

        String predLabel = null;
        double maxValue = Double.NEGATIVE_INFINITY;

        String[] words = sentence.split(" ");
        Set<String> labelSet = prior.keySet();

        for (String label : labelSet){
            double tempValue = prior.get(label); //先获得先验概率的对数似然，再不断累加

            for (String word : words) {
                // 如果是中文词才进行计算
                if (BayesCount.check(word)){
                    String pseudoW = label + "-" + word;
                    if (likelihood.containsKey(pseudoW)) {
                        tempValue += likelihood.get(pseudoW);
                    }else{
                        tempValue += Math.log(1 / (likelihoodNorm.get(label) + V.size()));
                    }
                }
            }

            if (tempValue > maxValue) {
                maxValue = tempValue;
                predLabel = label;
            }
        }

        return predLabel;
    }

    private static void validate(){
        BufferedReader testBr = null;
        BufferedReader testResultBr = null;
        String line1;
        int right=0, wrong=0;

        try{
            testBr = new BufferedReader(new FileReader(
                    "output"+ File.separator + "test.txt"));
            testResultBr = new BufferedReader(new FileReader(
                    "output"+ File.separator + "testResult.txt"));
            while ((line1 = testBr.readLine()) != null){
                if (testResultBr.readLine().equals(predict(line1))) right++;
                else wrong++;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try{
                testBr.close();
                testResultBr.close();
            } catch (NullPointerException | IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("测试完毕, 正确:"+right+", 错误:"+wrong);
        System.out.println("正确率:"+right / (double)(right + wrong));

    }

    public static void main(String[] args) throws Exception{
//        try{
//
//            // 一开始用"\\"一点用都没有，后来发现是分隔符原因：linux和windows下的分隔符不同
//            // linux: /  windows: \\
//
//            BufferedWriter bw = new BufferedWriter(new FileWriter(
//                    new File("output" + File.separator + "model.txt")));
//            bw.write("na");
//            bw.flush();
//            bw.close();
//        } catch (IOException e){
//            e.printStackTrace();
//        }
//
        validate();
    }

}
