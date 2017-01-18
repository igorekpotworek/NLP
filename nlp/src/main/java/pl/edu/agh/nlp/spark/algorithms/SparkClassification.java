package pl.edu.agh.nlp.spark.algorithms;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.classification.NaiveBayes;
import org.apache.spark.mllib.classification.NaiveBayesModel;
import org.apache.spark.mllib.feature.HashingTF;
import org.apache.spark.mllib.feature.IDF;
import org.apache.spark.mllib.feature.IDFModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.regression.LabeledPoint;

import pl.edu.agh.nlp.DataCleaner;
import pl.edu.agh.nlp.model.entities.Article;
import pl.edu.agh.nlp.spark.SparkContextFactory;
import pl.edu.agh.nlp.spark.utils.Tokenizer;
import scala.Tuple2;

public class SparkClassification implements Serializable {

	private static final long serialVersionUID = -2451802483479490942L;
	private final static double[] splitTable = { 0.6, 0.4 };
	private final static Tokenizer tokenizer = new Tokenizer();

	private static final HashingTF hashingTF = new HashingTF(2000000);

	public NaiveBayesModel builidModel() {
		System.setProperty("hadoop.home.dir", "C:\\Programs\\hadoop-common-2.2.0-bin-master");
		SparkContext sc = SparkContextFactory.getSparkContext();
		JavaSparkContext jsc = new JavaSparkContext(sc);

		// // Wczytujemy artukuly z bazy danych
		// JavaRDD<Article> data = JdbcRDD.create(jsc, new PostgresConnection(),
		// "select * from articles where  ? <= id AND id <= ?", 1, 1000000, 2, new ArticleMapper());
		//
		// // Filtrujemy tylko te z tekstem i kategoria
		// data = data.filter(a -> a.getText() != null && !a.getText().isEmpty()).filter(a -> a.getCategory() != null);
		//
		// // Obliczamy dzial o najmniejszej liczbie reprezentantow
		// final Long classSize = data.keyBy(p -> p.getCategory()).countByKey().values().stream().mapToLong(p -> (long) p)
		// .min().getAsLong();
		//
		// // Wybieramy z artykulow po rowno z kazdej grupy
		// data = data.groupBy(p -> p.getCategory()).map(t -> Lists.newArrayList(t._2).subList(0, classSize.intValue()))
		// .flatMap(f -> f);
		//
		// data.saveAsObjectFile("file:///D:/models/tmp.o");
		JavaRDD<Article> data = jsc.objectFile("file:///D:/models/tmp.o");
		System.out.println(data.count());

		// System.out.println(uniformData.count());

		// Budowa modelu idf

		JavaRDD<List<String>> javaRdd = data.map(r -> tokenizer.tokenize(r.getText())).filter(a -> !a.isEmpty());
		JavaRDD<Vector> tfData = hashingTF.transform(javaRdd);
		IDFModel idfModel = new IDF().fit(tfData);

		// Zrzutowanie publikacji na wektory
		JavaRDD<LabeledPoint> labeledPoints = data.map(a -> new LabeledPoint(a.getCategory().getValue(), idfModel
				.transform(hashingTF.transform(tokenizer.tokenize(DataCleaner.clean(a.getText()))))));

		// Dzielimy dane na zbior treningowy oraz testowy
		JavaRDD<LabeledPoint>[] splits = labeledPoints.randomSplit(splitTable);

		JavaRDD<LabeledPoint> training = splits[0];
		JavaRDD<LabeledPoint> test = splits[1];

		// Budowa modelu
		final NaiveBayesModel model = NaiveBayes.train(training.rdd());

		// Ewaluacja modelu
		JavaPairRDD<Double, Double> predictionAndLabel = test.mapToPair(p -> new Tuple2<Double, Double>(model.predict(p
				.features()), p.label()));
		long accuracy = predictionAndLabel.filter(pl -> {
			return pl._1().equals(pl._2());
		}).count();

		System.out.println("Skutecznosc: " + accuracy / (double) test.count());
		return model;

	}

	public static void test(NaiveBayesModel model) {
		HashingTF htf = new HashingTF(10000);
		String zdanie1 = "W miniony weekend Milik i Lewandowski zdobyli po dwie bramki dla swoich klub�w. Ten pierwszy przyczyni� si� do wyjazdowego zwyci�stwa Ajaksu Amsterdam z Heerenveen 4:1. Z kolei kr�l strzelc�w Bundesligi zdoby� dwa gole dla Bayernu, kt�ry w Bremie pokona� Werder 4:0. ";
		String zdanie2 = "Dwa dni po referendum, w kt�rym przy��czenie Krymu do Federacji Rosyjskiej popar�o 97 proc. g�osuj�cych, prezydent Rosji W�adimir Putin podpisa� stosowny traktat, przez co nale��cy do Ukrainy p�wysep i Sewastopol (jako miasto o znaczeniu federalnym) sta�y si� oficjalnie cz�ciami pa�stwa rosyjskiego. ";

		System.out.println(model.predict(htf.transform(Arrays.asList(zdanie1.split(" ")))));
		System.out.println(model.predict(htf.transform(Arrays.asList(zdanie2.split(" ")))));
	}

	public static void main(String[] args) {

		SparkClassification sparkClassification = new SparkClassification();
		NaiveBayesModel model = sparkClassification.builidModel();
		// test(model);

	}
}