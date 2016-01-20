package org.alicebot.ab;

public class Clause {
	public String subj;
	public String pred;
	public String obj;
	public Boolean affirm;

	public Clause(final String s, final String p, final String o) {
		this(s, p, o, true);
	}

	public Clause(final String s, final String p, final String o, final Boolean affirm) {
		subj = s;
		pred = p;
		obj = o;
		this.affirm = affirm;
	}

	public Clause(final Clause clause) {
		this(clause.subj, clause.pred, clause.obj, clause.affirm);
	}
}
