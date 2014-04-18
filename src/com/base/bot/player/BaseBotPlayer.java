package com.base.bot.player;

import java.lang.invoke.SwitchPoint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.base.bot.model.LeagueData;
import com.base.bot.model.PlayerRoster;
import com.base.bot.model.PlayerSwitch;
import com.base.bot.model.Slot;

public class BaseBotPlayer {
	private static String username = "rycharlind", password = "Bigballr#03",
			baseUrl = "http://games.espn.go.com/",
			rosterUrl = "flb/playertable/prebuilt/manageroster",
			saveRosterUrl = "flb/pnc/saveRoster";
	private final static String BENCHID = "16", UTILID = "12";

	public static void main(String[] args) {
		EspnViewer espn = new EspnViewer(username, password);
		Document doc = Jsoup.parse(getRoster(espn));

		List<PlayerRoster> playerRoster = new ArrayList<PlayerRoster>();
		List<Slot> slots = new ArrayList<Slot>();
		
		LeagueData leagueData = getLeagueData(doc);
		getSlotsAndRows(playerRoster, slots, doc);

		//clearPlayers(espn, playerRoster, leagueData);
		movePlayers(espn, leagueData, pickBestPlayers(playerRoster, slots));

		espn.closeConnection();
	}

	private static void getSlotsAndRows(final List<PlayerRoster> playerRoster,
			final List<Slot> slots, Document doc) {

		Elements playerRows = doc.getElementsByClass("pncPlayerRow");
		for (Element pr : playerRows) {
			Elements ps = pr.getElementsByTag("td");

			PlayerRoster p = new PlayerRoster();

			for (String className : ps.get(0).classNames()) {
				if (p.slot.id == null){
					p.slot.id = splitIfPossible(className, "_", 1);
				}
			}
			p.slot.name = ps.get(0).text();

			if (!p.slot.id.equals(BENCHID)) {
				slots.add(p.slot);
			}

			String[] nameColumn = ps.get(1).text().split(",");
			if (nameColumn.length >= 2) {
				p.id = splitIfPossible(ps.get(1).id(), "_", 1);
				p.name = nameColumn[0];
				String[] additionalInfo = nameColumn[1].split(" ");
				p.team = additionalInfo[0];
				p.position = additionalInfo[1];
				if (additionalInfo.length > 3) {
					p.disabled = additionalInfo[3].equals("DL15");
				}
			}

			p.opponent = ps.get(4).text();
			p.status = ps.get(5).text();
			p.pr7 = ps.get(7).text();
			p.pr15 = ps.get(8).text();
			p.pr30 = ps.get(9).text();
			p.prYear = ps.get(10).text();
			p.positionRank = tryIntParse(ps.get(12).text());
			if (p.positionRank == null) {
				p.positionRank = 500;
			}
			p.avgDraftPosition = ps.get(13).text();
			p.owned = ps.get(14).text();
			p.ownedChange = ps.get(15).text();

			if (p.name != null && !p.name.isEmpty()) {
				playerRoster.add(p);
			}
		}
	}

	private static String getRoster(EspnViewer espn) {
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

		return espn.makePostWithMatchingGetParams(baseUrl + rosterUrl,
				researchParams);
	}

	private static LeagueData getLeagueData(Document doc) {
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
		return leagueData;
	}

	private static void clearPlayers(EspnViewer espn,
			List<PlayerRoster> pResearch, LeagueData leagueData) {
		List<PlayerSwitch> playerSwitch = new ArrayList<PlayerSwitch>();
		for (PlayerRoster pr : pResearch) {
			if (!pr.slot.id.equals(BENCHID) && !pr.disabled) {
				playerSwitch.add(new PlayerSwitch(pr.id, pr.slot.id, BENCHID));
			}
		}
		movePlayers(espn, leagueData, playerSwitch);
	}

	private static Integer tryIntParse(String number) {
		Integer returnInt = null;
		try {
			returnInt = Integer.parseInt(number);
		} catch (NumberFormatException e) {
		}
		return returnInt;
	}

	private static List<PlayerSwitch> pickBestPlayers(List<PlayerRoster> pResearch,
			List<Slot> slots) {
		List<PlayerSwitch> ps = new ArrayList<PlayerSwitch>();
		for (Slot s : slots) {
			PlayerRoster p = null;
			for (PlayerRoster pr : pResearch) {
				if (pr.position.equals(s.name) && !pr.disabled) {
					if (p == null) {
						p = pr;
					} else if (pr.positionRank < p.positionRank) {
						p = pr;
					}
				}
			}
			if (p != null) {
				ps.add(new PlayerSwitch(p.id, p.slot.id, s.id));
				pResearch.remove(p);
			}
		}
		
		//util
		PlayerRoster p = null;
		for (PlayerRoster pr : pResearch) {
			if (!pr.position.equals("SP") && !pr.position.equals("RP") && !pr.disabled) {
				if (p == null) {
					p = pr;
				} else if (pr.positionRank < p.positionRank) {
					p = pr;
				}
			}
		}
		if (p != null) {
			ps.add(new PlayerSwitch(p.id, p.slot.id, UTILID));
			pResearch.remove(p);
		}
		
		return ps;
	}

	private static List<PlayerSwitch> switchPlayersByName(
			List<PlayerRoster> playerRoster, String name1, String name2) {
		List<PlayerRoster> pResearchSwitch = new ArrayList<PlayerRoster>();

		for (PlayerRoster pr : playerRoster) {
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

	private static List<PlayerSwitch> switchTwoPlayers(PlayerRoster player1,
			PlayerRoster player2) {
		List<PlayerSwitch> playerSwitch = new ArrayList<PlayerSwitch>();
		playerSwitch.add(new PlayerSwitch(player1.id, player1.slot.id,
				player2.slot.id));
		playerSwitch.add(new PlayerSwitch(player2.id, player2.slot.id,
				player1.slot.id));
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

	private static void movePlayers(EspnViewer espn, LeagueData leagueData,
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
		switchParams
				.add(new BasicNameValuePair("leagueId", leagueData.leagueId));
		switchParams.add(new BasicNameValuePair("teamId", leagueData.teamId));
		switchParams.add(new BasicNameValuePair("scoringPeriodId",
				leagueData.scoringPeriodId));
		switchParams.add(new BasicNameValuePair("returnSm", "true"));
		switchParams.add(new BasicNameValuePair("trans", playerSwitchString));

		System.out.println("MovePlayers: "
				+ espn.makePostWithMatchingGetParams(baseUrl + saveRosterUrl,
						switchParams));
	}

}
