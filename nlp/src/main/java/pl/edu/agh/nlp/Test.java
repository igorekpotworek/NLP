package pl.edu.agh.nlp;

import java.util.regex.Pattern;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Test {
	static ApplicationContext context = new ClassPathXmlApplicationContext("root-context.xml");

	public static void main(String[] args) {

		System.out.println(Pattern.compile("\\� poprzednia .* nast�pna \\�", Pattern.DOTALL).matcher("� poprzednia 1 2 3 nast�pna �")
				.replaceAll(""));
	}
}
