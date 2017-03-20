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

import java.lang.reflect.Field;
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
		Concept cpt = null;
		
		if (identifier != null) {
			
			identifier = identifier.trim();
			
			// see if this is a parseable int; if so, try looking up concept by id
			try { //handle integer: id
				int conceptId = Integer.parseInt(identifier);
				cpt = Context.getConceptService().getConcept(conceptId);
				
				if (cpt != null) {
					return cpt;
				}
			}
			catch (Exception ex) {
				//do nothing
			}
			
			// handle  mapping id: xyz:ht
			int index = identifier.indexOf(":");
			if (index != -1) {
				String mappingCode = identifier.substring(0, index).trim();
				String conceptCode = identifier.substring(index + 1, identifier.length()).trim();
				cpt = Context.getConceptService().getConceptByMapping(conceptCode, mappingCode);
				
				if (cpt != null) {
					return cpt;
				}
			}
			
			// handle uuid id: "a3e1302b-74bf-11df-9768-17cfc9833272", if the id matches a uuid format
			if (isValidUuidFormat(identifier)) {
				cpt = Context.getConceptService().getConceptByUuid(identifier);
			}
			// finally, if input contains at least one period handle recursively as a code constant
			else if (identifier.contains(".")) {
				return getConcept(evaluateStaticConstant(identifier));
			}
		}
		
		return cpt;
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
	
	/***
	 * Determines if the passed string is in valid uuid format By OpenMRS standards, a uuid must be
	 * 36 characters in length and not contain whitespace, but we do not enforce that a uuid be in
	 * the "canonical" form, with alphanumerics seperated by dashes, since the MVP dictionary does
	 * not use this format (We also are being slightly lenient and accepting uuids that are 37 or 38
	 * characters in length, since the uuid data field is 38 characters long)
	 */
	public static boolean isValidUuidFormat(String uuid) {
		if (uuid.length() < 36 || uuid.length() > 38 || uuid.contains(" ") || uuid.contains(".")) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Evaluates the specified Java constant using reflection
	 * @param fqn the fully qualified name of the constant
	 * @return the constant value
	 */
	protected static String evaluateStaticConstant(String fqn) {
		int lastPeriod = fqn.lastIndexOf(".");
		String clazzName = fqn.substring(0, lastPeriod);
		String constantName = fqn.substring(lastPeriod + 1);
		
		try {
			Class<?> clazz = Context.loadClass(clazzName);
			Field constantField = clazz.getField(constantName);
			Object val = constantField.get(null);
			return val != null ? String.valueOf(val) : null;
		}
		catch (Exception ex) {
			throw new IllegalArgumentException("Unable to evaluate " + fqn, ex);
		}
	}
}