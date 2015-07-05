package pl.edu.agh.nlp.spark.jdbc;

import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.recommendation.Rating;
import org.apache.spark.rdd.JdbcRDD;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pl.edu.agh.nlp.model.ArticleMapper;
import pl.edu.agh.nlp.model.RatingMapper;
import pl.edu.agh.nlp.model.entities.Article;
import pl.edu.agh.nlp.spark.SparkContextFactory;

@Service
public class ArticlesReader {

	@Autowired
	private NlpServiceConnectionFactory nlpServiceConnectionFactory;

	public JavaRDD<Article> readArticlesToRDD() {
		SparkContext sc = SparkContextFactory.getSparkContext();
		JavaSparkContext jsc = new JavaSparkContext(sc);
		return JdbcRDD.create(jsc, nlpServiceConnectionFactory, "select * from articles where  ? <= id AND id <= ?", 0, 600000, 4,
				new ArticleMapper());
	}

	public JavaRDD<Rating> readArticlesHistoryToRDD() {
		SparkContext sc = SparkContextFactory.getSparkContext();
		JavaSparkContext jsc = new JavaSparkContext(sc);
		return JdbcRDD.create(jsc, nlpServiceConnectionFactory, "select * from users_articles where  ? <= userId AND userId <= ?", 1, 1000,
				2, new RatingMapper());
	}
}
