package org.iucn.sis.client.utilities.autocomplete;

public interface CompletionItems {
	public void addCompletionItem(String item);

	/**
	 * Returns an array of all completion items matching the string match. If
	 * match is the wild card character "*", all items will be returned.
	 * 
	 * @param match
	 *            The user-entered text all compleition items have to match
	 * @return Array of strings
	 */
	public String[] getCompletionItems(String match);

	public int getNumberOfCompletions();
}
