package pl.edu.agh.nlp;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import pl.edu.agh.nlp.model.dao.ArticlesDao;
import pl.edu.agh.nlp.model.entities.Article;

public class DataCleaner {
	private static ApplicationContext context = new ClassPathXmlApplicationContext("root-context.xml");

	public static String clean(String phrase) {
		return phrase.replaceAll("Tagi:.*", "").replaceAll("Pozosta�o znak�w: 4000 Zaloguj si� Tw�j podpis.*", "")
				.replaceAll("(fot.*)", "");
	}

	public static void main(String[] args) {
		ArticlesDao articlesDao = context.getBean(ArticlesDao.class);
		List<Article> articles = articlesDao.findAll();
		System.out.println("Data loaded");

		articles = articles
				.stream()
				.filter(a -> a.getText().contains("Pozosta�o znak�w: 4000 Zaloguj si� Tw�j podpis")
						|| a.getText().contains("(fot.") || a.getText().contains("Tagi:")
						|| a.getIntro().contains("Pozosta�o znak�w: 4000 Zaloguj si� Tw�j podpis")
						|| a.getIntro().contains("(fot.") || a.getIntro().contains("Tagi:"))
				.collect(Collectors.toList());
		System.out.println(articles.size());
		articles.forEach(a -> {
			a.setIntro(clean(a.getIntro()));
			a.setText(clean(a.getText()));
		});

		System.out.println("Cleaning complete");

		// articlesDao.updateBatch(articles);
		System.out.println("Data saved");
	}
}
