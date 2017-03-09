/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.ugandaemrreports.reporting.metadata;

import org.openmrs.Concept;
import org.openmrs.ConceptNumeric;
import org.openmrs.api.context.Context;
import org.openmrs.module.metadatadeploy.MissingMetadataException;
import org.openmrs.module.reporting.common.ObjectUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Dictionary for concepts used by KenyaEMR
 */
public class Dictionary extends Metadata.Concept {

	/**
	 * Gets a concept by an identifier (mapping or UUID)
	 * @param identifier the identifier
	 * @return the concept
	 * @throws MissingMetadataException if the concept could not be found
	 */
	public static Concept getConcept(String identifier) {
		Concept concept;

		if (identifier.contains(":")) {
			String[] tokens = identifier.split(":");
			concept = Context.getConceptService().getConceptByMapping(tokens[1].trim(), tokens[0].trim());
		}
		else {
			// Assume it's a UUID
			concept = Context.getConceptService().getConceptByUuid(identifier);
		}

		if (concept == null) {
			throw new MissingMetadataException(Concept.class, identifier);
		}

		// getConcept doesn't always return ConceptNumeric for numeric concepts
		if (concept.getDatatype().isNumeric() && !(concept instanceof ConceptNumeric)) {
			concept = Context.getConceptService().getConceptNumeric(concept.getId());

			if (concept == null) {
				throw new MissingMetadataException(ConceptNumeric.class, identifier);
			}
		}

		return concept;
	}

	/**
	 * Convenience method to fetch a list of concepts
	 * @param identifiers the concept identifiers
	 * @return the concepts
	 * @throws MissingMetadataException if a concept could not be found
	 */
	public static List<Concept> getConcepts(String... identifiers) {
		List<Concept> concepts = new ArrayList<Concept>();
		for (String identifier : identifiers) {
			concepts.add(getConcept(identifier));
		}
		return concepts;
	}
	
	/**
	 * @return the List of Concepts that matches the passed comma-separated list of concept lookups
	 */
	public static List<Concept> getConceptList(String lookup) {
		List<Concept> l = new ArrayList<Concept>();
		if (ObjectUtil.notNull(lookup)) {
			String[] split = lookup.split(",");
			for (String s : split) {
				l.add(getConcept(s));
			}
		}
		return l;
	}
	
	/**
	 * @return the List of Concepts that matches the passed any separated list of concept lookups
	 */
	public static List<Concept> getConceptList(String lookup, String separator) {
		List<Concept> l = new ArrayList<Concept>();
		if (ObjectUtil.notNull(lookup)) {
			if (ObjectUtil.notNull(separator)) {
				String[] split = lookup.split(separator);
				for (String s : split) {
					l.add(getConcept(s));
				}
			} else {
				l.add(getConcept(lookup));
			}
		}
		return l;
	}
	
	/**
	 * @return the List of Concepts that matches the passed comma-separated list of concept lookups
	 */
	public static List<Concept> getConceptsInSet(String lookup) {
		List<Concept> ret = new ArrayList<Concept>();
		Concept set = getConcept(lookup);
		for (Concept c : set.getSetMembers()) {
			ret.add(c);
		}
		return ret;
	}
}