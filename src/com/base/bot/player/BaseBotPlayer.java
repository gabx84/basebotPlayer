package com.base.bot.player;

import java.lang.invoke.SwitchPoint;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.base.bot.model.LeagueData;
import com.base.bot.model.PlayerResearch;
import com.base.bot.model.PlayerSwitch;

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

		Elements sc = doc.getElementsByAttributeValue("type", "hidden");
		LeagueData leagueData = new LeagueData();
		for (Element el : sc) {
			switch (el.attr("name")) {
			case "incoming":
				leagueData.incoming = el.val();
				break;
			case "leagueId":
				leagueData.leagueId = el.val();
				break;
			case "teamId":
				leagueData.teamId = el.val();
				break;
			case "scoringPeriodId":
				leagueData.scoringPeriodId = el.val();
				break;
			}
		}

		List<PlayerResearch> pResearch = new ArrayList<PlayerResearch>();
		for (Element pr : playerRows) {
			Elements ps = pr.getElementsByTag("td");

			PlayerResearch p = new PlayerResearch();

			for (String className : ps.get(0).classNames()) {
				if (className.split("_").length > 1) {
					p.slotId = className.split("_")[1];
				}
			}

			p.slot = ps.get(0).text();

			String[] nameColumn = ps.get(1).text().split(",");
			if (nameColumn.length >= 2) {
				p.playerId = splitIfPossible(ps.get(1).id(),
						"_", 1);
				p.name = nameColumn[0];
				p.team = nameColumn[1].split(" ")[0];
				p.position = nameColumn[1].split(" ")[1];
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

		List<PlayerSwitch> playerSwitch = new ArrayList<PlayerSwitch>();

		playerSwitch.addAll(switchPlayersByName(pResearch, "Matt Lindstrom",
				"Steve Cishek"));

		movePlayer(espn, leagueData, playerSwitch);

		System.out.println(pResearch.get(11).slotId + "_"
				+ pResearch.get(1).playerId);

		espn.closeConnection();
	}

	private static List<PlayerSwitch> switchPlayersByName(
			List<PlayerResearch> pResearch, String name1, String name2) {
		List<PlayerResearch> pResearchSwitch = new ArrayList<PlayerResearch>();

		for (PlayerResearch pr : pResearch) {
			if (pr.name != null) {
				if (pr.name.equals(name1)) {
					pResearchSwitch.add(pr);
				}
				if (pr.name.equals(name2)) {
					pResearchSwitch.add(pr);
				}
			}
		}

		return switchTwoPlayers(pResearchSwitch.get(0), pResearchSwitch.get(1));
	}

	private static List<PlayerSwitch> switchTwoPlayers(PlayerResearch player1,
			PlayerResearch player2) {
		List<PlayerSwitch> playerSwitch = new ArrayList<PlayerSwitch>();
		playerSwitch.add(new PlayerSwitch(player1.playerId, player1.slotId,
				player2.slotId));
		playerSwitch.add(new PlayerSwitch(player2.playerId, player2.slotId,
				player1.slotId));
		return playerSwitch;
	}

	private static String splitIfPossible(String textToSplit, String regex,
			Integer location) {
		String[] splitText = textToSplit.split(regex);
		if (splitText.length > location) {
			return splitText[location];
		}
		return null;
	}

	private static void movePlayer(EspnViewer espn, LeagueData leagueData,
			List<PlayerSwitch> playerSwitch) {

		String playerSwitchString = "";
		for (PlayerSwitch ps : playerSwitch) {
			playerSwitchString += ps.toString() + "|";
		}
		if (playerSwitchString.length() > 1) {
			playerSwitchString = playerSwitchString.substring(0,
					playerSwitchString.length() - 1);
		}

		List<NameValuePair> switchParams = new ArrayList<NameValuePair>();
		switchParams.add(new BasicNameValuePair("leagueId", leagueData.leagueId));
		switchParams.add(new BasicNameValuePair("teamId", leagueData.teamId));
		switchParams.add(new BasicNameValuePair("scoringPeriodId", leagueData.scoringPeriodId));
		switchParams.add(new BasicNameValuePair("returnSm", "true"));
		switchParams.add(new BasicNameValuePair("trans", playerSwitchString));

		System.out.println(espn.makePostWithMatchingGetParams(
				"http://games.espn.go.com/flb/pnc/saveRoster", switchParams));
	}
}
