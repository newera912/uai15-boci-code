/*
 * This file is part of the PSL software.
 * Copyright 2011-2013 University of Maryland
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.umd.cs.psl.model.atom;

import java.util.HashSet;
import java.util.Set;

/**
 * An event related to a {@link RandomVariableAtom}.
 * <p>
 * An AtomEvent provides two pieces of information in addition to the type of
 * event: the {@link RandomVariableAtom} that caused the event and the
 * {@link AtomEventFramework} that created the event.
 */
public class AtomEvent {
	
	/** Types of AtomEvents */
	public enum Type {
		/** A {@link RandomVariableAtom} was instantiated in memory */
		ConsideredRVAtom,
		
		/** A {@link RandomVariableAtom} was activated */
		ActivatedRVAtom, 
		
		/** A {@link ObservedAtom} was activated */
		ActivatedObsAtom,

		/** A {@link RandomVariableAtom} was activated for online reasoning*/
		ActivatedRVAtomOnline;
		
	}

	/** Set containing {@link Type#ConsideredRVAtom}. */
	public static final Set<Type> ConsideredEventTypeSet = new HashSet<Type>(1);
	/** Set containing {@link Type#ActivatedRVAtom}. */
	public static final Set<Type> ActivatedEventTypeSet = new HashSet<Type>(2);
	/** Set containing {@link Type#ConsideredRVAtom} and {@link Type#ActivatedRVAtom}. */
	public static final Set<Type> AllEventTypesSet = new HashSet<Type>(3);
	
	static {
		ConsideredEventTypeSet.add(Type.ConsideredRVAtom);
		ActivatedEventTypeSet.add(Type.ActivatedRVAtom);
		ActivatedEventTypeSet.add(Type.ActivatedObsAtom);
		ActivatedEventTypeSet.add(Type.ActivatedRVAtomOnline);
		AllEventTypesSet.add(Type.ConsideredRVAtom);
		AllEventTypesSet.add(Type.ActivatedRVAtom);
		AllEventTypesSet.add(Type.ActivatedObsAtom);
		AllEventTypesSet.add(Type.ActivatedRVAtomOnline);
	}
	
	/** A listener for AtomEvents. */
	public interface Listener {
		/**
		 * Notifies this object of an AtomEvent.
		 * 
		 * @param event  event information
		 */
		public void notifyAtomEvent(AtomEvent event);
	}
	
	private final Type type;
	private final GroundAtom atom;
	private final AtomEventFramework eventFramework;
	
	/**
	 * Constructs a new AtomEvent with associated properties
	 * 
	 * @param type  the Type of the new event
	 * @param atom  the RandomVariableAtom for which the event occurred
	 * @param eventFramework  the AtomEventFramework managing this AtomEvent
	 */
	public AtomEvent(Type type, GroundAtom atom, AtomEventFramework eventFramework) {
		this.type = type;
		this.atom = atom;
		this.eventFramework = eventFramework;
	}
	
	/**
	 * @return the associated AtomEvent.Type
	 */
	public Type getType() {
		return type;
	}
	
	/**
	 * @return the associated RandomVariableAtom
	 */
	public GroundAtom getAtom() {
		return atom;
	}
	
	/**
	 * @return the associated AtomEventFramework
	 */
	public AtomEventFramework getEventFramework() {
		return eventFramework;
	}
	
}
