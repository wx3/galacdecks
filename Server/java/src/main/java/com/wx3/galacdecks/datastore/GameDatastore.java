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
package com.wx3.galacdecks.datastore;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;
import org.hibernate.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.wx3.galacdecks.Bootstrap;
import com.wx3.galacdecks.ai.AiManager;
import com.wx3.galacdecks.ai.AiHint;
import com.wx3.galacdecks.game.EntityPrototype;
import com.wx3.galacdecks.game.EntityRule;
import com.wx3.galacdecks.game.GameInstance;
import com.wx3.galacdecks.game.GameState;
import com.wx3.galacdecks.game.GameSystem;
import com.wx3.galacdecks.game.PlayerShip;
import com.wx3.galacdecks.game.PlayerInstance;
import com.wx3.galacdecks.game.ValidatorScript;
import com.wx3.galacdecks.server.AuthenticationException;
import com.wx3.galacdecks.server.Authenticator;
import com.wx3.galacdecks.server.User;

/**
 * @author Kevin
 *
 */
public class GameDatastore implements Authenticator {
	
	final static Logger logger = LoggerFactory.getLogger(GameDatastore.class);
	
	private static SessionFactory sessionFactory;
	
	private Map<String, EntityPrototype> cardsById = new HashMap<String, EntityPrototype>();
	private Map<String, EntityRule> rulesById = new HashMap<String, EntityRule>();
	private Map<String, GameSystem> systemsById = new HashMap<>();
	private Map<String, PlayerShip> playerShips = new HashMap<>();
	
	public GameDatastore() {
		sessionFactory = createSessionFactory();
	}
	
	private SessionFactory createSessionFactory() {
    	Configuration configuration = new Configuration().configure("hibernate.cfg.xml"); 
    	StandardServiceRegistryBuilder serviceRegistryBuilder = new StandardServiceRegistryBuilder();
        serviceRegistryBuilder.applySettings(configuration.getProperties());
        ServiceRegistry serviceRegistry = serviceRegistryBuilder.build();
        return configuration.buildSessionFactory(serviceRegistry);
    }
	
	public void loadCache() {
		Collection<EntityPrototype> cards = loadPrototypes();
		for(EntityPrototype card : cards) {
			cardsById.put(card.getId(), card);
		}
		Collection<EntityRule> rules = loadRules();
		for(EntityRule rule :rules) {
			rulesById.put(rule.getId(), rule);
		}
		Collection<GameSystem> systems = loadSystems();
		for(GameSystem system : systems) {
			systemsById.put(system.id, system);
		}
		Collection<PlayerShip> ships = loadShips();
		for(PlayerShip ship : ships) {
			playerShips.put(ship.getId(), ship);
		}
	}
	
	public void createUser(User user) {
		Session session = sessionFactory.openSession();
    	session.beginTransaction();
    	session.save(user);
    	session.getTransaction().commit();
	}
	
	public User newUser(String username) {
		Session session = sessionFactory.openSession();
    	session.beginTransaction();
    	User user = User.CreateUser(username);
    	session.save(user);
    	session.getTransaction().commit();
    	return user;
	}
	
	public User getUser(String username) {
		Session session = sessionFactory.openSession();
    	session.beginTransaction();
    	Criteria criteria = session.createCriteria(User.class);
    	User user = (User) criteria.add(Restrictions.eq("username",username)).uniqueResult();
    	session.getTransaction().commit();
		return user;
	}
	
	public void createPrototype(com.wx3.galacdecks.game.EntityPrototype prototype) {
		Session session = sessionFactory.openSession();
    	session.beginTransaction();
    	session.save(prototype);
    	session.getTransaction().commit();
    	
    	session = sessionFactory.openSession();
    	session.beginTransaction();
    	EntityPrototype loaded = (EntityPrototype) session.get(EntityPrototype.class, prototype.getId());
    	session.getTransaction().commit();
    	
    	if(loaded.getAiHints().size() != prototype.getAiHints().size()) {
    		logger.info("Houston, we have a problem.");
    	}
	}
	
	public void createValidator(ValidatorScript validator) {
		Session session = sessionFactory.openSession();
    	session.beginTransaction();
    	session.save(validator);
    	session.getTransaction().commit();
	}
	
	public void createRule(EntityRule rule) {
		Session session = sessionFactory.openSession();
    	session.beginTransaction();
    	session.save(rule);
    	session.getTransaction().commit();
	}
	
	public void createAiHint(AiHint hint) {
		Session session = sessionFactory.openSession();
    	session.beginTransaction();
    	session.save(hint);
    	session.getTransaction().commit();
	}
	
	public void createSystem(GameSystem system) {
		Session session = sessionFactory.openSession();
    	session.beginTransaction();
    	session.save(system);
    	session.getTransaction().commit();
	}
	
	public void createPlayerShip(PlayerShip playerShip) {
		Session session = sessionFactory.openSession();
    	session.beginTransaction();
    	session.save(playerShip);
    	session.getTransaction().commit();
	}
	
	/**
	 * Returns an unmodifiable view of the card lookup. The card lookup is shared by
	 * all games so we don't want to accidentally modify it.
	 * @return
	 */
	public Map<String, EntityPrototype> getPrototypes() {
		return Collections.unmodifiableMap(cardsById);
	}
	
	public EntityPrototype getPrototype(String id) {
		if(!cardsById.containsKey(id)) {
			throw new RuntimeException("Could not find card named '" + id + "' in datastore cache.");
		}
		return cardsById.get(id);
	}
	
	public GameSystem getSystem(String id) {
		GameSystem system = systemsById.get(id);
		if(system == null) return null;
		system.setPrototypes(getPrototypes());
		system.setRules(getEntityRules());
		system.setPlayerShips(Collections.unmodifiableMap(playerShips));
		return system;
	}
	
	/**
	 * Return an unmodifiable map of the rule lookup, so games can add rules to entities
	 * dynamically.
	 * 
	 * @return
	 */
	public Map<String, EntityRule> getEntityRules() {
		if(rulesById == null) {
			throw new RuntimeException("Rule cache not initialized");
		}
		return Collections.unmodifiableMap(rulesById);
	}
	
	public PlayerShip getShip(String id) {
		PlayerShip ship = null;
		if(playerShips.containsKey(id)) {
			ship = playerShips.get(id);
			ship.setStartingDeck(Bootstrap.GetDefaultDeck());
		}
		return ship;
	}

	public PlayerInstance authenticate(String authtoken)
			throws AuthenticationException {
		Session session = sessionFactory.openSession();
    	session.beginTransaction();
    	Criteria criteria = session.createCriteria(PlayerInstance.class);
    	PlayerInstance player = (PlayerInstance) criteria.add(Restrictions.eq("authtoken", authtoken)).uniqueResult();
    	session.getTransaction().commit();
		return player;
	}
	
	public void newGame(GameInstance game) {
		Session session = sessionFactory.openSession();
    	session.beginTransaction();
    	session.save(game);
    	session.getTransaction().commit();
	}
	
	public EntityPrototype loadPrototype(String id) {
		Session session = sessionFactory.openSession();
    	session.beginTransaction();
    	EntityPrototype proto = (EntityPrototype) session.get(EntityPrototype.class, id);
    	session.getTransaction().commit();
    	return proto;
	}
	
	private Collection<EntityPrototype> loadPrototypes() {
		Session session = sessionFactory.openSession();
    	session.beginTransaction();
    	// If we're using some kind of SQL database, there may be a behind-the-scenes
    	// join creating multiple results, so we need the DISTINCT_ROOT_ENTITY transformer:
    	@SuppressWarnings("unchecked")
    	List<EntityPrototype> cardList = session.createCriteria(EntityPrototype.class)
			.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)  
    		.list();
    	session.getTransaction().commit();
    	for(EntityPrototype card : cardList) {
    		if(card.getAiHints().size() > 1) {
    			logger.info("Many hints: " + card.getAiHints().size());
    		}
    	}
    	return cardList;
	}
	
	private Collection<EntityRule> loadRules() {
		Session session = sessionFactory.openSession();
    	session.beginTransaction();
    	// If we're using some kind of SQL database, there may be a behind-the-scenes
    	// join creating multiple results, so we need the DISTINCT_ROOT_ENTITY transformer:
    	@SuppressWarnings("unchecked")
    	List<EntityRule> ruleList = session.createCriteria(EntityRule.class)
			.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)  
    		.list();
    	session.getTransaction().commit();
    	return ruleList;
	}
	
	private Collection<GameSystem> loadSystems() {
		Session session = sessionFactory.openSession();
    	session.beginTransaction();
    	// If we're using some kind of SQL database, there may be a behind-the-scenes
    	// join creating multiple results, so we need the DISTINCT_ROOT_ENTITY transformer:
    	@SuppressWarnings("unchecked")
    	List<GameSystem> systemList = session.createCriteria(GameSystem.class)
			.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)  
    		.list();
    	session.getTransaction().commit();
    	return systemList;
	}
	
	private Collection<PlayerShip> loadShips() {
		Session session = sessionFactory.openSession();
    	session.beginTransaction();
    	// If we're using some kind of SQL database, there may be a behind-the-scenes
    	// join creating multiple results, so we need the DISTINCT_ROOT_ENTITY transformer:
    	@SuppressWarnings("unchecked")
    	List<PlayerShip> shipList = session.createCriteria(PlayerShip.class)
			.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)  
    		.list();
    	session.getTransaction().commit();
    	return shipList;
	}
	
}
