package pl.edu.agh.nlp.spark;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaSparkContext;

public class SparkContextFactory {
	private static SparkContext sc;

	public static SparkContext getSparkContext() {
		if (sc == null) {
			Logger.getLogger("org").setLevel(Level.OFF);
			Logger.getLogger("akka").setLevel(Level.OFF);
			SparkConf conf = new SparkConf().setAppName("NLP").setMaster("local[2]");
			sc = new SparkContext(conf);
		}
		return sc;
	}

	public static JavaSparkContext getJavaSparkContext() {
		return new JavaSparkContext(getSparkContext());
	}
}
