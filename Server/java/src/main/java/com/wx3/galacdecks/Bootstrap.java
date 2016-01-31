/**
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015 Kevin Lin
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * 
 */
/**
 * 
 */
package com.wx3.galacdecks;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wx3.galacdecks.ai.AiHint;
import com.wx3.galacdecks.datastore.GameDatastore;
import com.wx3.galacdecks.game.EntityCoordinates;
import com.wx3.galacdecks.game.EntityPrototype;
import com.wx3.galacdecks.game.EntityRule;
import com.wx3.galacdecks.game.EntityStat;
import com.wx3.galacdecks.game.EntityTag;
import com.wx3.galacdecks.game.GameRules;
import com.wx3.galacdecks.game.GameSystem;
import com.wx3.galacdecks.game.PlayerShip;
import com.wx3.galacdecks.game.ValidatorScript;
import com.wx3.galacdecks.server.User;


/**
 * Initializes the Datastore with default data from CSV files.
 * 
 * @author Kevin
 *
 */
public class Bootstrap {

	final Logger logger = LoggerFactory.getLogger(Bootstrap.class);
	// Used to check whether a particular trigger event exists:
	final String eventPackage = "com.wx3.galacdecks.gameevents";
	
	private Map<String, EntityRule> ruleCache = new HashMap<>();
	private Map<String, ValidatorScript> playValidatorCache = new HashMap<>();
	private Map<String, ValidatorScript> discardValidatorCache = new HashMap<>();
	private Map<String, AiHint> aiHintCache = new HashMap<>();
	private Map<String, EntityPrototype> cardCache = new HashMap<>();
	
	static int parseIntOrZero(String i) {
		try {
			return Integer.parseInt(i);
		} catch (NumberFormatException e) {
			return 0;
		}
	}
	
	static double parseDobuleOrZero(String i) {
		try {
			return Double.parseDouble(i);
		} catch (NumberFormatException e) {
			return 0;
		}
	}
	
	/**
	 * Until the players have a way to manage their own decks, this will supply a default deck
	 * @return
	 */
	public static List<String> GetDefaultDeck() {
		List<String> deck = new ArrayList<String>();
		for(int i = 0; i < 4; i++) {
			deck.add("FIGHTER");
			deck.add("DESTROYER");
			deck.add("GATEWAY");
			deck.add("BATTLECRUISER");
			deck.add("BATTLESTATION");
			deck.add("MISSILE_FRIGATE");
			deck.add("MISSILE");
			deck.add("ANNIHILATOR");
			deck.add("UPRISING");
			deck.add("PROTON_SPREAD");
			deck.add("SABOTAGE");
			deck.add("DRAW_2");
		}
		return deck;
	}
	
	public void init(GameDatastore datastore, String csvFolder) {
		try {
			importPlayValidators(datastore, csvFolder + "/validators/play");
			importDiscardValidators(datastore, csvFolder + "/validators/discard");
			importRules(datastore, csvFolder + "/rules");
			importAiHints(datastore, csvFolder + "/aihints");
			importCards(datastore, csvFolder + "/csv/cards.csv"); 
			importSystems(datastore, csvFolder + "/csv/systems.csv");
			addDefaultShips(datastore);
		}
		catch (IOException ex) {
			logger.error("Failed to import data: " + ex.getMessage());
			throw new RuntimeException("Failed to import bootstrap data.", ex);
		}
		addTestUsers(datastore);
		datastore.loadCache();
	}
	
	private void importPlayValidators(GameDatastore datastore, String path) throws IOException {
		Files.walk(Paths.get(path)).forEach(filePath -> {
			if(Files.isRegularFile(filePath)) {
				try {
					if(FilenameUtils.getExtension(filePath.getFileName().toString()).toLowerCase().equals("js")) {
						String id = FilenameUtils.removeExtension(filePath.getFileName().toString());
						List<String> lines = Files.readAllLines(filePath);
						if(lines.size() < 3) {
							throw new RuntimeException("Script file should have at least 2 lines: description and code.");
						}
						String description = lines.get(0).substring(2).trim();
						String script = String.join("\n", lines);
						ValidatorScript validator = ValidatorScript.createValidator(id, script, description);
						//datastore.createValidator(validator);
						playValidatorCache.put(id, validator);
						logger.info("Imported play validator " + id);
					}
				} catch (Exception e) {
					throw new RuntimeException("Failed to parse " + filePath + ": " + e.getMessage());
				}
			}
		});
	}
	
	private void importDiscardValidators(GameDatastore datastore, String path) throws IOException {
		Files.walk(Paths.get(path)).forEach(filePath -> {
			if(Files.isRegularFile(filePath)) {
				try {
					if(FilenameUtils.getExtension(filePath.getFileName().toString()).toLowerCase().equals("js")) {
						String id = FilenameUtils.removeExtension(filePath.getFileName().toString());
						List<String> lines = Files.readAllLines(filePath);
						if(lines.size() < 3) {
							throw new RuntimeException("Script file should have at least 2 lines: description and code.");
						}
						String description = lines.get(0).substring(2).trim();
						String script = String.join("\n", lines);
						ValidatorScript validator = ValidatorScript.createValidator(id, script, description);
						discardValidatorCache.put(id, validator);
						logger.info("Imported discard validator " + id);
					}
				} catch (Exception e) {
					throw new RuntimeException("Failed to parse " + filePath + ": " + e.getMessage());
				}
			}
		});
	}
	
	private void importRules(GameDatastore datastore, String path) throws IOException {
		Files.walk(Paths.get(path)).forEach(filePath -> {
			if(Files.isRegularFile(filePath)) {
				try {
					if(FilenameUtils.getExtension(filePath.getFileName().toString()).toLowerCase().equals("js")) {
						String id = FilenameUtils.removeExtension(filePath.getFileName().toString());
						List<String> lines = Files.readAllLines(filePath);
						if(lines.size() < 3) {
							throw new RuntimeException("Script file should have at least 3 lines: description, trigger, and code.");
						}
						String description = lines.get(0).substring(2).trim();
						
						String trigger = lines.get(1).substring(11).trim();
						// Check that this actually is a valid trigger event:
						try {
							if(!trigger.equals(GameRules.BUFF_PHASE)) {
								Class.forName(eventPackage + "." + trigger);	
							}
							
						} catch(ClassNotFoundException e) {
							throw new RuntimeException("No such GameEvent: " + trigger);
						}
						String script = String.join("\n", lines);
						EntityRule rule = EntityRule.createRule(trigger, script, id, description);
						datastore.createRule(rule);
						ruleCache.put(id, rule);
						logger.info("Imported rule " + id);
					}
				} catch (Exception e) {
					throw new RuntimeException("Failed to parse " + filePath + ": " + e.getMessage());
				}
			}
		});
	}
	
	private void importAiHints(GameDatastore datastore, String path) throws IOException {
		Files.walk(Paths.get(path)).forEach(filePath -> {
			if(Files.isRegularFile(filePath)) {
				try {
					if(FilenameUtils.getExtension(filePath.getFileName().toString()).toLowerCase().equals("js")) {
						String id = FilenameUtils.removeExtension(filePath.getFileName().toString());
						List<String> lines = Files.readAllLines(filePath);
						String script = String.join("\n", lines);
						AiHint hint = new AiHint(id, script);
						//datastore.createAiHint(hint);
						aiHintCache.put(id, hint);
						logger.info("Imported hint " + id);
					}
				} catch (Exception e) {
					throw new RuntimeException("Failed to parse " + filePath + ": " + e.getMessage());
				}
			}
		});
	}
	
	
	private void importCards(GameDatastore datastore, String path) throws IOException {
		Reader reader = new FileReader(path);
		CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL.withHeader());
		int count = 0;
		for(CSVRecord record : parser) {
			String id = record.get("id");
			String name = record.get("name");
			String description = record.get("description");
			String flavor = record.get("flavor");
			String unitPrefab = record.get("unitPrefab");
			String summonEffect = record.get("summonEffect");
			String projectile = record.get("projectile");
			String portrait = record.get("portrait");
			Set<EntityTag> tags = new HashSet<EntityTag>();
			for(EntityTag tag : EntityTag.values()) {
				if(record.isSet(tag.toString())) {
					if(record.get(tag).equals("Y")) {
						tags.add(tag);
					}
				}
			}
			
			String[] types = record.get("types").split(",");
			for(String type : types) {
				if(type.length() > 0) {
					EntityTag tag = EntityTag.valueOf(type);	
					tags.add(tag);	
				}
			}
	
			Map<EntityStat, Integer> stats = new HashMap<EntityStat,Integer>();
			for(EntityStat stat : EntityStat.values()) {
				String statName = stat.toString();
				if(record.isSet(statName)) {
					stats.put(stat, parseIntOrZero(record.get(statName)));
				}
			}
			
			List<EntityRule> rules = new ArrayList<EntityRule>();
			String ruleField = record.get("rules");
			String[] ruleIds = ruleField.split(",");
			for(String ruleId : ruleIds) {
				if(ruleId.length() > 0) {
					if(!ruleCache.containsKey(ruleId)) {
						throw new RuntimeException("Unable to find rule '" + ruleId + "'");
					}
					EntityRule rule = ruleCache.get(ruleId);
					rules.add(rule);
				}
			}
			
			ValidatorScript playValidator = null;
			String playValidatorId = record.get("playValidator");
			if(playValidatorId != null && !playValidatorId.isEmpty()) {
				if(!playValidatorCache.containsKey(playValidatorId)) {
					throw new RuntimeException("Unknown validator '" + playValidatorId + "'");
				}
				playValidator = playValidatorCache.get(playValidatorId);
			}
			
			ValidatorScript discardValidator = null;
			String discardValidatorId = record.get("discardValidator");
			if(discardValidatorId != null && !discardValidatorId.isEmpty()) {
				if(!discardValidatorCache.containsKey(discardValidatorId)) {
					throw new RuntimeException("Unknown validator '" + discardValidatorId + "'");
				}
				discardValidator = discardValidatorCache.get(discardValidatorId);
			}
			
			Set<AiHint> aiHints = new HashSet<>();
			String hintField = record.get("aiHints");
			String[] hintIds = hintField.split(",");
			for(String hintId : hintIds) {
				if(hintId.length() > 0) {
					if(!aiHintCache.containsKey(hintId)) {
						throw new RuntimeException("Unable to find AI Hint '" + hintId + "'");
					}
					AiHint hint = aiHintCache.get(hintId);
					aiHints.add(hint);
				}
			}
			
			EntityPrototype card = EntityPrototype.createPrototype(id, 
					name, 
					description, 
					flavor, 
					unitPrefab, 
					summonEffect,
					projectile,
					portrait,
					tags, 
					rules, 
					playValidator,
					discardValidator,
					stats, 
					aiHints);
			datastore.createPrototype(card);
			if(cardCache.containsKey(card.getId())) {
				throw new RuntimeException("Duplicate card id: " + card.getId());
			}
			cardCache.put(card.getId(), card);
			++count;
		}
		logger.info("Imported " + count + " cards");
		parser.close();
	}
	
	private void importSystems(GameDatastore datastore, String path) throws IOException {
		Reader reader = new FileReader(path);
		CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL.withHeader());
		int count = 0;
		for(CSVRecord record : parser) {
			String id = record.get("id");
			String name = record.get("name");
			String description = record.get("description");
			String pvp = record.get("pvp");
			boolean usePlayerDecks = true;
			if(record.get("usePlayerDecks").toLowerCase().equals("n")) {
				usePlayerDecks = false;
			}
			
			String ruleField = record.get("rootRules");
			String[] ruleIds = ruleField.split(",");
			GameSystem system = new GameSystem();
			system.id = id;
			system.name = name;
			system.description = description;
			system.usePlayerDecks = usePlayerDecks;
			system.rootRules = new ArrayList<>(Arrays.asList(ruleIds));
			if(pvp.toUpperCase().equals("Y")) {
				system.pvp = true;
			} else {
				system.pvp = false;
			}
			datastore.createSystem(system);
			
			++count;
		}
		logger.info("Imported " + count + " systems");
		parser.close();
	}
	
	private void addDefaultShips(GameDatastore datastore) {
		PlayerShip explorer = new PlayerShip("EXPLORER");
		explorer.setShipPrefab("Explorer");
		datastore.createPlayerShip(explorer);
		
		PlayerShip hexStation = new PlayerShip("HEX_STATION");
		hexStation.setShipPrefab("HexStation");
		datastore.createPlayerShip(hexStation);
		
		PlayerShip scout = new PlayerShip("SCOUT");
		scout.setShipPrefab("Scout");
		datastore.createPlayerShip(scout);
	}
	
	private void addTestUsers(GameDatastore datastore) {
		User user1 = datastore.getUser("user1");
    	if(user1 == null) {
        	datastore.newUser("goodguy");
    	}
    	
    	User user2 = datastore.getUser("user2");
    	if(user2 == null) {
        	datastore.newUser("badguy");
    	}
	}
}
