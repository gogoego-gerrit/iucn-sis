package org.iucn.sis.client.utilities.autocomplete;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class SimpleAutoCompletionItems implements CompletionItems {

	private String[] completions;

	public SimpleAutoCompletionItems(Object[] items) {
		completions = new String[items.length];

		for (int i = 0; i < items.length; i++)
			completions[i] = (String) items[i];
	}

	public SimpleAutoCompletionItems(Set items) {
		completions = new String[items.size()];

		int i = 0;
		for (Iterator iterator = items.iterator(); iterator.hasNext(); i++)
			completions[i] = (String) iterator.next();
	}

	public SimpleAutoCompletionItems(String[] items) {
		completions = items;
	}

	public void addCompletionItem(String item) {
		for (int i = 0; i < completions.length; i++) {
			if (completions[i].equalsIgnoreCase(item))
				return;
		}

		String[] newCompletions = new String[completions.length + 1];

		for (int i = 0; i < completions.length; i++)
			newCompletions[i] = completions[i];

		newCompletions[newCompletions.length - 1] = item;

		completions = newCompletions;
	}

	public String[] getCompletionItems(String match) {
		if (match.equalsIgnoreCase("*"))
			return completions;

		ArrayList matches = new ArrayList();

		for (int i = 0; i < completions.length; i++) {
			if (completions[i].toLowerCase().startsWith(match.toLowerCase())) {
				matches.add(completions[i]);

				if (matches.size() >= 50)
					return new String[0];
			}
		}

		String[] returnMatches = new String[matches.size()];
		for (int i = 0; i < matches.size(); i++)
			returnMatches[i] = (String) matches.get(i);

		return returnMatches;
	}

	public int getNumberOfCompletions() {
		return completions.length;
	}

	public void setCompletions(String[] completions) {
		this.completions = completions;
	}
}