/*******************************************************************************
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
 *******************************************************************************/
package com.wx3.galacdecks.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import com.wx3.galacdecks.ai.AiHint;

/**
 * An entity prototype is what it sounds like-- a template for
 * entities created in the game. For example, most entities in
 * the game will be created from a card, sharing the same
 * base stats, default tags, rules, etc.
 * 
 * @author Kevin
 *
 */
@Entity
@Table(name="entity_prototypes")
public class EntityPrototype {

	@Id
	private String id;
	private String name;
	private String description;
	private String flavor;
	private String unitPrefab;
	private String summonEffect;
	private String projectile;
	private String portrait;
	
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "prototype_tags")
	private Set<EntityTag> tags = new HashSet<EntityTag>();
	
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "prototype_stats")
	@Column(name="stat_value")
	@MapKeyColumn(name="stat_name")
	private Map<EntityStat,Integer> stats = new HashMap<EntityStat,Integer>();
	
	@ManyToMany(cascade={CascadeType.ALL}, fetch = FetchType.EAGER)
	@JoinTable(name="prototype_rules", 
		joinColumns = @JoinColumn(name="prototypeId"),
		inverseJoinColumns = @JoinColumn(name="ruleId"))
	@OrderColumn(name="ruleOrder")
	private List<EntityRule> rules = new ArrayList<EntityRule>();
	
	@ManyToOne(cascade=CascadeType.ALL)
	private ValidatorScript playValidator;
	
	@ManyToOne(cascade=CascadeType.ALL)
	private ValidatorScript discardValidator;
	
	@ManyToMany(cascade={CascadeType.ALL}, fetch = FetchType.EAGER)
	@JoinTable(name="prototype_ai_hints", 
		joinColumns = @JoinColumn(name="prototypeIdHint"),
		inverseJoinColumns = @JoinColumn(name="aiHintId"))
	private Set<AiHint> aiHints = new HashSet<AiHint>();
	
	// yeah, maybe we need a more compact static constructor...
	public static EntityPrototype createPrototype(String id, 
			String name, 
			String description, 
			String flavor,
			String unitPrefab,
			String summonEffect,
			String projectile,
			String portrait,
			Collection<EntityTag> tags, 
			List<EntityRule> rules,
			ValidatorScript playValidator,
			ValidatorScript discardValidator,
			Map<EntityStat,Integer> stats, 
			Set<AiHint> aiHints) {
		EntityPrototype prototype = new EntityPrototype();
		prototype.id = id;
		prototype.name = name;
		prototype.description = description;
		prototype.flavor = flavor;
		prototype.unitPrefab = unitPrefab;
		prototype.summonEffect = summonEffect;
		prototype.projectile = projectile;
		prototype.portrait = portrait;
		prototype.tags = new HashSet<EntityTag>(tags);
		prototype.playValidator = playValidator;
		prototype.discardValidator = discardValidator;
		prototype.rules = new ArrayList<EntityRule>(rules);
		prototype.stats = new HashMap<EntityStat,Integer>(stats);
		prototype.aiHints = new HashSet<AiHint>(aiHints);
		return prototype;
	}
	
	public EntityPrototype() {}
	
	public EntityPrototype(String name, String description) {
		this.name = name;
		this.description = description;
	}
	
	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getFlavor() {
		return flavor;
	}
	
	public String getUnitPrefab() {
		return unitPrefab;
	}
	
	public String getSummonEffect() {
		return summonEffect;
	}
	
	public String getProjectile() {
		return projectile;
	}
	
	public String getPortrait() {
		return portrait;
	}
	
	public Set<EntityTag> getTags() {
		return tags;
	}
	
	public Map<EntityStat,Integer> getStats() {
		return stats;
	}
	
	public List<EntityRule> getRules() {
		return rules;
	}
	
	public ValidatorScript getPlayValidator() {
		return playValidator;
	}
	
	public ValidatorScript getDiscardValidator() {
		return discardValidator;
	}
	
	public Collection<AiHint> getAiHints() {
		return aiHints;
	}	
	
}
