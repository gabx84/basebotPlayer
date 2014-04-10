package com.base.bot.player;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ExampleClass {

	/**
	 * @param args
	 */
	public static void runPage() {

		EspnViewer espn = new EspnViewer("rycharlind", "Bigballr#03");

		String link = espn
				.getPageContents("http://games.espn.go.com/frontpage");
		//System.out.println(link);
	
		Document doc = Jsoup.parse(link);
		Elements elements = doc.getElementsByClass("main");
		for (Element element : elements){
			System.out.println(element.text());
		}
		
		espn.closeConnection();
	}

}
