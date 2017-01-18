package pl.edu.agh.nlp.spark;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.classification.NaiveBayes;
import org.apache.spark.mllib.classification.NaiveBayesModel;
import org.apache.spark.mllib.feature.HashingTF;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.rdd.EmptyRDD;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;

import scala.Tuple2;

public class SparkClassification implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2451802483479490942L;
	private HashingTF htf;
	private final static double[] splitTable = { 0.6, 0.4 };

	public NaiveBayesModel bulidAndTestModel(List<String> tableNames) {
		SparkConf conf = new SparkConf().setAppName("Sentiment").setMaster("local[2]");
		SparkContext sc = new SparkContext(conf);
		SQLContext sqlContext = new SQLContext(sc);
		JavaRDD<LabeledPoint> training = new EmptyRDD<LabeledPoint>(sc, scala.reflect.ClassTag$.MODULE$.apply(LabeledPoint.class))
				.toJavaRDD();
		JavaRDD<LabeledPoint> test = new EmptyRDD<LabeledPoint>(sc, scala.reflect.ClassTag$.MODULE$.apply(LabeledPoint.class)).toJavaRDD();

		int i = 0;
		for (String tableName : tableNames) {
			DataFrame data = new JdbcLoader().getTableFromJdbc(sqlContext, tableName);
			final int j = i;
			JavaRDD<LabeledPoint> parsedData = data.javaRDD().map(
					row -> new LabeledPoint(j, htf.transform(Arrays.asList(row.getString(0).split(" ")))));
			JavaRDD<LabeledPoint>[] splits = parsedData.randomSplit(splitTable);
			training = training.union(splits[0]);
			test = test.union(splits[1]);

			i++;
		}

		final NaiveBayesModel model = NaiveBayes.train(training.rdd());
		JavaPairRDD<Double, Double> predictionAndLabel = test.mapToPair(p -> new Tuple2<Double, Double>(model.predict(p.features()), p
				.label()));
		long accuracy = predictionAndLabel.filter(pl -> {
			return pl._1().equals(pl._2());
		}).count();

		System.out.println("Skutecznosc: " + accuracy / (double) test.count());
		return model;
	}

	public SparkClassification() {
		htf = new HashingTF(10000);

	}

	public static void test1(NaiveBayesModel model) {
		HashingTF htf = new HashingTF(10000);
		String zdanie1 = "W miniony weekend Milik i Lewandowski zdobyli po dwie bramki dla swoich klub�w. Ten pierwszy przyczyni� si� do wyjazdowego zwyci�stwa Ajaksu Amsterdam z Heerenveen 4:1. Z kolei kr�l strzelc�w Bundesligi zdoby� dwa gole dla Bayernu, kt�ry w Bremie pokona� Werder 4:0. ";
		String zdanie2 = "Dwa dni po referendum, w kt�rym przy��czenie Krymu do Federacji Rosyjskiej popar�o 97 proc. g�osuj�cych, prezydent Rosji W�adimir Putin podpisa� stosowny traktat, przez co nale��cy do Ukrainy p�wysep i Sewastopol (jako miasto o znaczeniu federalnym) sta�y si� oficjalnie cz�ciami pa�stwa rosyjskiego. ";
		System.out.println(model.predict(htf.transform(Arrays.asList(zdanie1.split(" ")))));
		System.out.println(model.predict(htf.transform(Arrays.asList(zdanie2.split(" ")))));
	}

	public static void main(String[] args) {
		String[] s = { "SPORT_NEW", "WIADOMOSCI_NEW" };
		List<String> names = Arrays.asList(s);
		SparkClassification sparkClassification = new SparkClassification();
		sparkClassification.bulidAndTestModel(names);
	}
}