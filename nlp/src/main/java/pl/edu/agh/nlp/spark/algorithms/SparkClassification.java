package pl.edu.agh.nlp.spark.algorithms;

import java.io.Serializable;
import java.util.Arrays;

import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.classification.NaiveBayes;
import org.apache.spark.mllib.classification.NaiveBayesModel;
import org.apache.spark.mllib.feature.HashingTF;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.rdd.JdbcRDD;
import org.languagetool.tokenizers.pl.PolishWordTokenizer;

import pl.edu.agh.nlp.model.ArticleMapper;
import pl.edu.agh.nlp.model.entities.Article;
import pl.edu.agh.nlp.spark.SparkContextFactory;
import pl.edu.agh.nlp.spark.jdbc.PostgresConnection;
import scala.Tuple2;

public class SparkClassification implements Serializable {

	private static final long serialVersionUID = -2451802483479490942L;
	private final static double[] splitTable = { 0.6, 0.4 };
	private final static PolishWordTokenizer tokenizer = new PolishWordTokenizer();

	public NaiveBayesModel builidModel() {
		SparkContext sc = SparkContextFactory.getSparkContext();
		JavaSparkContext jsc = new JavaSparkContext(sc);
		HashingTF htf = new HashingTF(10000);

		JavaRDD<Article> data = JdbcRDD.create(jsc, new PostgresConnection(), "select * from articles where  ? <= id AND id <= ?", 1,
				100000, 10, new ArticleMapper());

		data = data.filter(a -> a.getText() != null).filter(a -> a.getCategory() != null);

		JavaRDD<LabeledPoint> parsedData = data.map(a -> new LabeledPoint(a.getCategory().getValue(), htf.transform(tokenizer.tokenize(a
				.getText()))));

		JavaRDD<LabeledPoint>[] splits = parsedData.randomSplit(splitTable);

		JavaRDD<LabeledPoint> training = splits[0];
		JavaRDD<LabeledPoint> test = splits[1];

		final NaiveBayesModel model = NaiveBayes.train(training.rdd());

		JavaPairRDD<Double, Double> predictionAndLabel = test.mapToPair(p -> new Tuple2<Double, Double>(model.predict(p.features()), p
				.label()));
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
		test(model);

	}
}