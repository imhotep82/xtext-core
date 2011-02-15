/*******************************************************************************
 * Copyright (c) 2011 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.serializer;

import java.util.List;
import java.util.Map;

import org.eclipse.xtext.AbstractElement;
import org.eclipse.xtext.Action;
import org.eclipse.xtext.ParserRule;
import org.eclipse.xtext.RuleCall;
import org.eclipse.xtext.grammaranalysis.IPDAState;
import org.eclipse.xtext.grammaranalysis.IPDAState.PDAStateType;
import org.eclipse.xtext.serializer.impl.RCStack;
import org.eclipse.xtext.serializer.impl.SyntacticSequencerPDAProvider;

import com.google.common.base.Predicate;
import com.google.inject.ImplementedBy;

/**
 * @author Moritz Eysholdt - Initial contribution and API
 */
@ImplementedBy(SyntacticSequencerPDAProvider.class)
public interface ISyntacticSequencerPDAProvider {

	interface ISynAbsorberState extends ISynState {
		List<ISynTransition> getOutTransitions();

		Map<AbstractElement, ISynTransition> getOutTransitionsByElement();

		Map<AbstractElement, ISynTransition> getOutTransitionsByRuleCallEnter();

		Map<AbstractElement, ISynTransition> getOutTransitionsByRuleCallExit();
	}

	interface ISynEmitterState extends ISynState, ISynNavigable {
	}

	interface ISynFollowerOwner {
		List<ISynState> getFollowers();
	}

	interface ISynNavigable extends ISynFollowerOwner {
		ISynAbsorberState getTarget();

		int getDistanceTo(Predicate<ISynState> matches, Predicate<ISynState> bounds, RCStack stack);

		int getDistanceWithStackToAbsorber(RCStack stack);

		List<ISynState> getShortestPathTo(Predicate<ISynState> matches, Predicate<ISynState> bounds, RCStack stack,
				boolean addMatch);

		List<ISynState> getShortestPathTo(AbstractElement ele, RCStack stack, boolean addMatch);

		List<ISynState> getShortestPathToAbsorber(RCStack stack);

		boolean involvesUnassignedTokenRuleCalls();

		boolean isSyntacticallyAmbiguous();

		boolean hasEmitters();
	}

	interface ISynState extends ISynFollowerOwner {

		AbstractElement getGrammarElement();

		SynStateType getType();
	}

	interface ISynTransition extends ISynNavigable {

		ISynAbsorberState getSource();

	}

	class SynPredicates {
		public static Predicate<ISynState> absorber(final AbstractElement ele) {
			return new Predicate<ISynState>() {
				public boolean apply(ISynState input) {
					return input.getGrammarElement() == ele && input instanceof ISynAbsorberState;
				}
			};
		}

		public static Predicate<ISynState> element(final AbstractElement ele) {
			return new Predicate<ISynState>() {
				public boolean apply(ISynState input) {
					return input.getGrammarElement() == ele;
				}
			};
		}

		public static Predicate<ISynState> emitter(final AbstractElement ele) {
			return new Predicate<ISynState>() {
				public boolean apply(ISynState input) {
					return input.getGrammarElement() == ele && input instanceof ISynEmitterState;
				}
			};
		}

		public static Predicate<ISynState> ruleCallEnter(final RuleCall ele) {
			return new Predicate<ISynState>() {
				public boolean apply(ISynState input) {
					return input.getGrammarElement() == ele && input.getType().isRuleCallEnter();
				}
			};
		}

		public static Predicate<ISynState> ruleCallExit(final RuleCall ele) {
			return new Predicate<ISynState>() {
				public boolean apply(ISynState input) {
					return input.getGrammarElement() == ele && input.getType().isRuleCallExit();
				}
			};
		}

		public static Predicate<ISynState> ruleCallExits() {
			return new Predicate<ISynState>() {
				public boolean apply(ISynState input) {
					return input.getType().isRuleCallExit();
				}
			};
		}

		public static Predicate<ISynState> ruleCallExitsOrAbsorber() {
			return new Predicate<ISynState>() {
				public boolean apply(ISynState input) {
					return input.getType().isRuleCallExit() || input instanceof ISynAbsorberState;
				}
			};
		}

		public static Predicate<ISynState> absorber() {
			return new Predicate<ISynState>() {
				public boolean apply(ISynState input) {
					return input instanceof ISynAbsorberState;
				}
			};
		}
	}

	enum SynStateType {
		ASSIGNED_ACTION_CALL(PDAStateType.ELEMENT), //
		ASSIGNED_BOOLEAN_KEYWORD(PDAStateType.ELEMENT), //
		ASSIGNED_CROSSREF_DATATYPE_RULE_CALL(PDAStateType.ELEMENT), //
		ASSIGNED_CROSSREF_ENUM_RULE_CALL(PDAStateType.ELEMENT), //  
		ASSIGNED_CROSSREF_KEYWORD(PDAStateType.ELEMENT), // 
		ASSIGNED_CROSSREF_TERMINAL_RULE_CALL(PDAStateType.ELEMENT), //
		ASSIGNED_DATATYPE_RULE_CALL(PDAStateType.ELEMENT), //
		ASSIGNED_ENUM_RULE_CALL(PDAStateType.ELEMENT), //
		ASSIGNED_KEYWORD(PDAStateType.ELEMENT), //
		ASSIGNED_PARSER_RULE_CALL(PDAStateType.ELEMENT), //
		ASSIGNED_TERMINAL_RULE_CALL(PDAStateType.ELEMENT), //
		START(PDAStateType.START), //
		STOP(PDAStateType.STOP), //
		TRANSITION(null), //
		UNASSIGEND_ACTION_CALL(PDAStateType.ELEMENT), //
		UNASSIGEND_KEYWORD(PDAStateType.ELEMENT), //
		UNASSIGNED_DATATYPE_RULE_CALL(PDAStateType.ELEMENT), //
		UNASSIGNED_PARSER_RULE_ENTER(PDAStateType.RULECALL_ENTER), //
		UNASSIGNED_PARSER_RULE_EXIT(PDAStateType.RULECALL_EXIT), //
		UNASSIGNED_TERMINAL_RULE_CALL(PDAStateType.ELEMENT);

		protected IPDAState.PDAStateType simpleType;

		private SynStateType(PDAStateType simpleType) {
			this.simpleType = simpleType;
		}

		public IPDAState.PDAStateType getSimpleType() {
			return simpleType;
		}

		public boolean isElement() {
			return simpleType == PDAStateType.ELEMENT;
		}

		public boolean isRuleCallEnter() {
			return simpleType == PDAStateType.RULECALL_ENTER;
		}

		public boolean isRuleCallExit() {
			return simpleType == PDAStateType.RULECALL_EXIT;
		}

		public boolean isStart() {
			return simpleType == PDAStateType.START;
		}

		public boolean isStop() {
			return simpleType == PDAStateType.STOP;
		}

	}

	ISynAbsorberState getPDA(Action contet);

	ISynAbsorberState getPDA(ParserRule context);

}