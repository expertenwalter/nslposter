public class RSSEntry {
	public String date;
	public String title;
	public String picture;
	public String link;
	public String description;

	public int hashCode() {
		String data = title + link + description;

		int hash = 0;
		for (char c : data.toCharArray()) {
			hash += c;
			hash = hash << 1;
		}

		return hash;
	}

	public String toString() {
		return "Date: " + date + " Title: " + title + " Link: " + link
				+ " Description: " + description;
	}
}
