package pl.edu.agh.nlp.spark.algorithms.classification;

import java.io.Serializable;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.classification.NaiveBayes;
import org.apache.spark.mllib.classification.NaiveBayesModel;
import org.apache.spark.mllib.feature.HashingTF;
import org.apache.spark.mllib.feature.IDF;
import org.apache.spark.mllib.feature.IDFModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.regression.LabeledPoint;

import pl.edu.agh.nlp.exceptions.AbsentModelException;
import pl.edu.agh.nlp.model.entities.Article;
import pl.edu.agh.nlp.model.entities.Article.Category;
import pl.edu.agh.nlp.spark.jdbc.ArticlesReader;
import pl.edu.agh.nlp.utils.DataCleaner;
import pl.edu.agh.nlp.utils.Tokenizer;
import scala.Tuple2;

import com.google.common.collect.Lists;

public class SparkClassification implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 92592979711690198L;

	private static final Logger logger = Logger.getLogger(SparkClassification.class);

	private final static double[] splitTable = { 0.6, 0.4 };

	private static final Tokenizer tokenizer = new Tokenizer();
	private static final HashingTF hashingTF = new HashingTF(2000000);

	private NaiveBayesModel model;
	private IDFModel idfModel;

	public void builidModel() throws AbsentModelException {
		// TODO
		System.setProperty("hadoop.home.dir", "C:\\Programs\\hadoop-common-2.2.0-bin-master");

		// Wczytujemy artukuly z bazy danych
		JavaRDD<Article> data = ArticlesReader.readArticlesToRDD();

		// Filtrujemy tylko te z tekstem i kategoria
		data = data.filter(a -> a.getText() != null && !a.getText().isEmpty()).filter(a -> a.getCategory() != null);

		// Obliczamy dzial o najmniejszej liczbie reprezentantow
		final Long classSize = data.keyBy(p -> p.getCategory()).countByKey().values().stream().mapToLong(p -> (long) p).min().getAsLong();

		// Wybieramy z artykulow po rowno z kazdej grupy
		data = data.groupBy(p -> p.getCategory()).map(t -> Lists.newArrayList(t._2).subList(0, classSize.intValue())).flatMap(f -> f);

		System.out.println(data.count());
		// Budowa modelu idf
		idfModel = builidIDFModel(data);

		// Zrzutowanie publikacji na wektory
		JavaRDD<LabeledPoint> labeledPoints = data.map(a -> new LabeledPoint(a.getCategory().getValue(), idfModel.transform(hashingTF
				.transform(tokenizer.tokenize(DataCleaner.clean(a.getText()))))));

		// Dzielimy dane na zbior treningowy oraz testowy
		JavaRDD<LabeledPoint>[] splits = labeledPoints.randomSplit(splitTable);

		JavaRDD<LabeledPoint> training = splits[0];
		JavaRDD<LabeledPoint> test = splits[1];

		// Budowa modelu
		model = NaiveBayes.train(training.rdd());
		evaluateModel(test);
	}

	public IDFModel builidIDFModel(JavaRDD<Article> data) {
		JavaRDD<List<String>> javaRdd = data.map(r -> tokenizer.tokenize(r.getText())).filter(a -> !a.isEmpty());
		JavaRDD<Vector> tfData = hashingTF.transform(javaRdd);
		return new IDF().fit(tfData);
	}

	public double evaluateModel(JavaRDD<LabeledPoint> test) throws AbsentModelException {
		if (model != null) {
			// Ewaluacja modelu
			JavaPairRDD<Double, Double> predictionAndLabel = test.mapToPair(p -> new Tuple2<Double, Double>(model.predict(p.features()), p
					.label()));
			long accuracy = predictionAndLabel.filter(pl -> {
				return pl._1().equals(pl._2());
			}).count();
			double effectiveness = accuracy / (double) test.count();
			logger.info("Skutecznosc: " + effectiveness);
			return effectiveness;
		} else
			throw new AbsentModelException();
	}

	public Category predictCategory(String text) throws AbsentModelException {
		if (model != null && idfModel != null)
			return Category.fromInt((int) model.predict(idfModel.transform(hashingTF.transform(tokenizer.tokenize(text)))));
		else
			throw new AbsentModelException();
	}

	public static void test() throws AbsentModelException {
		SparkClassification sparkClassification = new SparkClassification();
		sparkClassification.builidModel();

		String zdanie1 = "Rafał Sonik wygrał Rajd Kataru. Quadowiec ORLEN Team bezpiecznie dowiózł do mety przewagę nad Mohamedem Abu-Issą i odniósł swoje pierwsze zwycięstwo w Pucharze Świata w tym sezonie. Katarski klasyk na wysokich miejscach ukończyły również dwie samochodowe załogi polskiego zespołu. Jacek Czachor i Marek Dąbrowski zajęli piąte miejsce, a oczko niżej znaleźli się Adam Małysz z pilotem Rafałem Martonem. Obie załogi zdobyły punkty do klasyfikacji Pucharu Świata FIA w rajdach terenowych.";

		System.out.println(sparkClassification.predictCategory(zdanie1));
	}

	public static void main(String[] args) throws AbsentModelException {
		test();

	}
}