## SVM on Harp    

### Usage

Migrate the source code
```bash
cp -r edu/iu/svm /harp3-project/harp3-app/src/
cp -r libsvm /harp3-project/harp3-app/src/
```

Open `build.xml` and add
```xml
<include name=“edu/iu/svm/**” />
<include name=“libsvm/**” />
```

Compile Harp SVM
```bash
ant
```

Run Harp SVM
```bash
hadoop jar build/harp3-app-hadoop-2.6.0.jar edu.iu.svm.IterativeSVM <number of mappers> <number of iteration> <output path in HDFS> <dataset path>
```

Fetch the result
```bash
hdfs dfs -get <output path in HDFS> <path you want to store the output>
```




