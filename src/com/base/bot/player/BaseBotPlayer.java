package com.base.bot.player;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.base.bot.model.PlayerResearch;

public class BaseBotPlayer {
	private static String username = "rycharlind", password = "Bigballr#03",
			gameListUrl = "http://games.espn.go.com/frontpage",
			currGameUrl = "http://games.espn.go.com/flb/clubhouse";
	private static Integer leagueId = 105151, teamId = 16, seasonId = 2014;

	public static void main(String[] args) {
		EspnViewer espn = new EspnViewer(username, password);
		List<NameValuePair> researchParams = new ArrayList<NameValuePair>();
		researchParams.add(new BasicNameValuePair("leagueId", "105151"));
		researchParams.add(new BasicNameValuePair("teamId", "16"));
		researchParams.add(new BasicNameValuePair("seasonId", "2014"));
		researchParams.add(new BasicNameValuePair("view", "research"));
		researchParams.add(new BasicNameValuePair("context", "clubhouse"));
		researchParams.add(new BasicNameValuePair("ajaxPath",
				"playertable/prebuilt/manageroster"));
		researchParams.add(new BasicNameValuePair("managingIr", "false"));
		researchParams.add(new BasicNameValuePair("droppingPlayers", "false"));
		researchParams.add(new BasicNameValuePair("asLM", "false"));
		researchParams.add(new BasicNameValuePair("r", "44812920"));

		String link = espn
				.makePostWithMatchingGetParams(
						"http://games.espn.go.com/flb/playertable/prebuilt/manageroster",
						researchParams);
		
		Document doc = Jsoup.parse(link);
		Elements playerRows = doc.getElementsByClass("pncPlayerRow");
		
		List<PlayerResearch> pResearch = new ArrayList<PlayerResearch>();
		for (Element pr : playerRows) {
			Elements ps = pr.getElementsByTag("td");
			
			PlayerResearch p = new PlayerResearch();
			p.slot = ps.get(0).text();
			if (ps.get(1).text().split(",").length > 2){
				p.name = ps.get(1).text().split(",")[0];
				p.team = ps.get(1).text().split(",")[1];
				p.position = ps.get(1).text().split(",")[2];
			}
			
			p.opponent = ps.get(4).text();
			p.status = ps.get(5).text();
			p.pr7 = ps.get(7).text();
			p.pr15 = ps.get(8).text();
			p.pr30 = ps.get(9).text();
			p.prYear = ps.get(10).text();
			p.positionRank = ps.get(12).text();
			p.avgDraftPosition = ps.get(13).text();
			p.owned = ps.get(14).text();
			p.ownedChange = ps.get(15).text();
			
			pResearch.add(p);
		}

		espn.closeConnection();
	}
}
