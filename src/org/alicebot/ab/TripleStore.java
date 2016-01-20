package org.alicebot.ab;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class TripleStore {
	public int idCnt = 0;
	public String name = "unknown";
	public Chat chatSession;
	public Bot bot;
	public HashMap<String, Triple> idTriple = new HashMap<String, Triple>();
	public HashMap<String, String> tripleStringId = new HashMap<String, String>();
	public HashMap<String, HashSet<String>> subjectTriples = new HashMap<String, HashSet<String>>();
	public HashMap<String, HashSet<String>> predicateTriples = new HashMap<String, HashSet<String>>();
	public HashMap<String, HashSet<String>> objectTriples = new HashMap<String, HashSet<String>>();

	public TripleStore(final String name, final Chat chatSession) {
		this.name = name;
		this.chatSession = chatSession;
		bot = chatSession.bot;
	}

	public class Triple {
		public String id;
		public String subject;
		public String predicate;
		public String object;

		public Triple(String s, String p, String o) {
			final Bot bot = TripleStore.this.bot;
			if (bot != null) {
				s = bot.preProcessor.normalize(s);
				p = bot.preProcessor.normalize(p);
				o = bot.preProcessor.normalize(o);
			}
			if (s != null && p != null && o != null) {
				// System.out.println("New triple "+s+":"+p+":"+o);
				subject = s;
				predicate = p;
				object = o;
				id = name + idCnt++;
				// System.out.println("New triple "+id+"="+s+":"+p+":"+o);

			}
		}
	}

	public String mapTriple(final Triple triple) {
		final String id = triple.id;
		idTriple.put(id, triple);
		String s, p, o;
		s = triple.subject;
		p = triple.predicate;
		o = triple.object;

		s = s.toUpperCase();
		p = p.toUpperCase();
		o = o.toUpperCase();

		String tripleString = s + ":" + p + ":" + o;
		tripleString = tripleString.toUpperCase();

		if (tripleStringId.keySet().contains(tripleString)) {
			// System.out.println("Found "+tripleString+" "+tripleStringId.get(tripleString));
			return tripleStringId.get(tripleString); // triple already exists
		} else {
			// System.out.println(tripleString+" not found");
			tripleStringId.put(tripleString, id);

			HashSet<String> existingTriples;
			if (subjectTriples.containsKey(s)) {
				existingTriples = subjectTriples.get(s);
			} else {
				existingTriples = new HashSet<String>();
			}
			existingTriples.add(id);
			subjectTriples.put(s, existingTriples);

			if (predicateTriples.containsKey(p)) {
				existingTriples = predicateTriples.get(p);
			} else {
				existingTriples = new HashSet<String>();
			}
			existingTriples.add(id);
			predicateTriples.put(p, existingTriples);

			if (objectTriples.containsKey(o)) {
				existingTriples = objectTriples.get(o);
			} else {
				existingTriples = new HashSet<String>();
			}
			existingTriples.add(id);
			objectTriples.put(o, existingTriples);

			return id;
		}
	}

	public String unMapTriple(Triple triple) {
		String id = MagicStrings.undefined_triple;
		String s, p, o;
		s = triple.subject;
		p = triple.predicate;
		o = triple.object;

		s = s.toUpperCase();
		p = p.toUpperCase();
		o = o.toUpperCase();

		String tripleString = s + ":" + p + ":" + o;

		System.out.println("unMapTriple " + tripleString);
		tripleString = tripleString.toUpperCase();

		triple = idTriple.get(tripleStringId.get(tripleString));

		System.out.println("unMapTriple " + triple);
		if (triple != null) {
			id = triple.id;
			idTriple.remove(id);
			tripleStringId.remove(tripleString);

			HashSet<String> existingTriples;
			if (subjectTriples.containsKey(s)) {
				existingTriples = subjectTriples.get(s);
			} else {
				existingTriples = new HashSet<String>();
			}
			existingTriples.remove(id);
			subjectTriples.put(s, existingTriples);

			if (predicateTriples.containsKey(p)) {
				existingTriples = predicateTriples.get(p);
			} else {
				existingTriples = new HashSet<String>();
			}
			existingTriples.remove(id);
			predicateTriples.put(p, existingTriples);

			if (objectTriples.containsKey(o)) {
				existingTriples = objectTriples.get(o);
			} else {
				existingTriples = new HashSet<String>();
			}
			existingTriples.remove(id);
			objectTriples.put(o, existingTriples);
		} else {
			id = MagicStrings.undefined_triple;
		}

		return id;

	}

	public Set<String> allTriples() {
		return new HashSet<String>(idTriple.keySet());
	}

	public String addTriple(final String subject, final String predicate, final String object) {
		if (subject == null || predicate == null || object == null) {
			return MagicStrings.undefined_triple;
		}
		final Triple triple = new Triple(subject, predicate, object);
		final String id = mapTriple(triple);
		return id;
	}

	public String deleteTriple(final String subject, final String predicate, final String object) {
		if (subject == null || predicate == null || object == null) {
			return MagicStrings.undefined_triple;
		}
		if (MagicBooleans.trace_mode) {
			System.out.println("Deleting " + subject + " " + predicate + " " + object);
		}
		final Triple triple = new Triple(subject, predicate, object);
		final String id = unMapTriple(triple);
		return id;
	}

	public void printTriples() {
		for (final String x : idTriple.keySet()) {
			final Triple triple = idTriple.get(x);
			System.out.println(x + ":" + triple.subject + ":" + triple.predicate + ":" + triple.object);
		}
	}

	HashSet<String> emptySet() {
		return new HashSet<String>();
	}

	public HashSet<String> getTriples(String s, String p, String o) {
		Set<String> subjectSet;
		Set<String> predicateSet;
		Set<String> objectSet;
		Set<String> resultSet;
		if (MagicBooleans.trace_mode) {
			System.out.println("TripleStore: getTriples [" + idTriple.size() + "] " + s + ":" + p + ":" + o);
		}
		// printAllTriples();
		if (s == null || s.startsWith("?")) {
			subjectSet = allTriples();
		} else {
			s = s.toUpperCase();
			// System.out.println("subjectTriples.keySet()="+subjectTriples.keySet());
			// System.out.println("subjectTriples.get("+s+")="+subjectTriples.get(s));
			// System.out.println("subjectTriples.containsKey("+s+")="+subjectTriples.containsKey(s));
			if (subjectTriples.containsKey(s)) {
				subjectSet = subjectTriples.get(s);
			} else {
				subjectSet = emptySet();
			}
		}
		// System.out.println("subjectSet="+subjectSet);

		if (p == null || p.startsWith("?")) {
			predicateSet = allTriples();
		} else {
			p = p.toUpperCase();
			if (predicateTriples.containsKey(p)) {
				predicateSet = predicateTriples.get(p);
			} else {
				predicateSet = emptySet();
			}
		}

		if (o == null || o.startsWith("?")) {
			objectSet = allTriples();
		} else {
			o = o.toUpperCase();
			if (objectTriples.containsKey(o)) {
				objectSet = objectTriples.get(o);
			} else {
				objectSet = emptySet();
			}
		}

		resultSet = new HashSet(subjectSet);
		resultSet.retainAll(predicateSet);
		resultSet.retainAll(objectSet);

		final HashSet<String> finalResultSet = new HashSet(resultSet);

		// System.out.println("TripleStore.getTriples: "+finalResultSet.size()+" results");
		/*
		 * System.out.println("getTriples subjectSet="+subjectSet); System.out.println("getTriples predicateSet="+predicateSet); System.out.println("getTriples objectSet="+objectSet); System.out.println("getTriples result="+resultSet);
		 */

		return finalResultSet;
	}

	public HashSet<String> getSubjects(final HashSet<String> triples) {
		final HashSet<String> resultSet = new HashSet<String>();
		for (final String id : triples) {
			final Triple triple = idTriple.get(id);
			resultSet.add(triple.subject);
		}
		return resultSet;
	}

	public HashSet<String> getPredicates(final HashSet<String> triples) {
		final HashSet<String> resultSet = new HashSet<String>();
		for (final String id : triples) {
			final Triple triple = idTriple.get(id);
			resultSet.add(triple.predicate);
		}
		return resultSet;
	}

	public HashSet<String> getObjects(final HashSet<String> triples) {
		final HashSet<String> resultSet = new HashSet<String>();
		for (final String id : triples) {
			final Triple triple = idTriple.get(id);
			resultSet.add(triple.object);
		}
		return resultSet;
	}

	public String formatAIMLTripleList(final HashSet<String> triples) {
		String result = MagicStrings.default_list_item;// "NIL"
		for (final String x : triples) {
			result = x + " " + result;// "CONS "+x+" "+result;
		}
		return result.trim();
	}

	public String getSubject(final String id) {
		if (idTriple.containsKey(id)) {
			return idTriple.get(id).subject;
		} else {
			return "Unknown subject";
		}
	}

	public String getPredicate(final String id) {
		if (idTriple.containsKey(id)) {
			return idTriple.get(id).predicate;
		} else {
			return "Unknown predicate";
		}
	}

	public String getObject(final String id) {
		if (idTriple.containsKey(id)) {
			return idTriple.get(id).object;
		} else {
			return "Unknown object";
		}
	}

	public String stringTriple(final String id) {
		final Triple triple = idTriple.get(id);
		return id + " " + triple.subject + " " + triple.predicate + " " + triple.object;
	}

	public void printAllTriples() {
		for (final String id : idTriple.keySet()) {
			System.out.println(stringTriple(id));
		}
	}

	public HashSet<Tuple> select(final HashSet<String> vars, final HashSet<String> visibleVars, final ArrayList<Clause> clauses) {
		HashSet<Tuple> result = new HashSet<Tuple>();
		try {

			final Tuple tuple = new Tuple(vars, visibleVars);
			// System.out.println("TripleStore: select vars = "+tuple.printVars());
			result = selectFromRemainingClauses(tuple, clauses);
			if (MagicBooleans.trace_mode) {
				for (final Tuple t : result) {
					System.out.println(t.printTuple());
				}
			}

		} catch (final Exception ex) {
			System.out.println("Something went wrong with select " + visibleVars);
			ex.printStackTrace();

		}
		return result;
	}

	public Clause adjustClause(final Tuple tuple, final Clause clause) {
		final Set vars = tuple.getVars();
		final String subj = clause.subj;
		final String pred = clause.pred;
		final String obj = clause.obj;
		final Clause newClause = new Clause(clause);
		if (vars.contains(subj)) {
			final String value = tuple.getValue(subj);
			if (!value.equals(MagicStrings.unbound_variable)) {/* System.out.println("adjusting "+subj+" "+value); */
				newClause.subj = value;
			}
		}
		if (vars.contains(pred)) {
			final String value = tuple.getValue(pred);
			if (!value.equals(MagicStrings.unbound_variable)) {/* System.out.println("adjusting "+pred+" "+value); */
				newClause.pred = value;
			}
		}
		if (vars.contains(obj)) {
			final String value = tuple.getValue(obj);
			if (!value.equals(MagicStrings.unbound_variable)) {/* System.out.println("adjusting "+obj+" "+value); */
				newClause.obj = value;
			}
		}
		return newClause;

	}

	public Tuple bindTuple(final Tuple partial, final String triple, final Clause clause) {
		final Tuple tuple = new Tuple(partial);
		if (clause.subj.startsWith("?")) {
			tuple.bind(clause.subj, getSubject(triple));
		}
		if (clause.pred.startsWith("?")) {
			tuple.bind(clause.pred, getPredicate(triple));
		}
		if (clause.obj.startsWith("?")) {
			tuple.bind(clause.obj, getObject(triple));
		}
		return tuple;
	}

	public HashSet<Tuple> selectFromSingleClause(final Tuple partial, final Clause clause, final Boolean affirm) {
		final HashSet<Tuple> result = new HashSet<Tuple>();
		final HashSet<String> triples = getTriples(clause.subj, clause.pred, clause.obj);
		// System.out.println("TripleStore: selected "+triples.size()+" from single clause "+clause.subj+" "+clause.pred+" "+clause.obj);
		if (affirm) {
			for (final String triple : triples) {
				final Tuple tuple = bindTuple(partial, triple, clause);
				result.add(tuple);
			}
		} else {
			if (triples.size() == 0) {
				result.add(partial);
			}
		}
		return result;
	}

	public HashSet<Tuple> selectFromRemainingClauses(final Tuple partial, final ArrayList<Clause> clauses) {
		// System.out.println("TripleStore: partial = "+partial.printTuple()+" clauses.size()=="+clauses.size());
		HashSet<Tuple> result = new HashSet<Tuple>();
		Clause clause = clauses.get(0);
		clause = adjustClause(partial, clause);
		final HashSet<Tuple> tuples = selectFromSingleClause(partial, clause, clause.affirm);
		if (clauses.size() > 1) {
			final ArrayList<Clause> remainingClauses = new ArrayList<Clause>(clauses);
			remainingClauses.remove(0);
			for (final Tuple tuple : tuples) {
				result.addAll(selectFromRemainingClauses(tuple, remainingClauses));
			}
		} else {
			result = tuples;
		}
		return result;
	}

}
