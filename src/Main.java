import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Main {
	public static void printUsage() {
		System.out
				.println("Usage: java -jar RSSPoster.jar <options> <board> <feed urls>");
		System.out.println("");
		System.out.println("Options:");
		System.out
				.println("  --update-only   Download the feed and update the database, but don't post anything");
	}

	public static void saveUrl(String urlString, String filename)
			throws MalformedURLException, IOException {
		BufferedInputStream in = null;
		FileOutputStream fout = null;
		try {
			in = new BufferedInputStream(new URL(urlString).openStream());
			fout = new FileOutputStream(filename);

			byte data[] = new byte[1024];
			int count;
			while ((count = in.read(data, 0, 1024)) != -1) {
				fout.write(data, 0, count);
			}
		} finally {
			if (in != null)
				in.close();
			if (fout != null)
				fout.close();
		}
	}

	public static void doFeed(String board, String feedAddress,
			boolean optUpdateOnly) {
		List<String> inhashList = new ArrayList<String>();
		List<String> outhashList = new ArrayList<String>();

		String feedFile = feedAddress.replace("http://", "").replace('/', '-')
				.concat(".hashlist");

		// Load database
		try {
			BufferedReader inreader = new BufferedReader(new FileReader(
					feedFile));

			String s;
			while ((s = inreader.readLine()) != null) {
				inhashList.add(s.replace('\n', ' ').replace('\r', ' '));
			}

			inreader.close();
		} catch (Exception e) {

		}

		// Download feed
		try {
			URL url = new URL(feedAddress);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new Exception("HTTP Error: " + conn.getResponseCode());
			}

			InputStream is = conn.getInputStream();

			List<RSSEntry> entries = FeedParser.parse(is);

			for (RSSEntry entry : entries) {

				if (!inhashList.contains("" + entry.hashCode())) {
					System.out.println("Date: " + entry.date);
					System.out.println("Title: " + entry.title);
					System.out.println("Picture: " + entry.picture);
					System.out.println("Link: " + entry.link);
					System.out.println("Description: " + entry.description);
					System.out.println();

					if (!optUpdateOnly) {
						// Fetch picture or generate it
						if (entry.picture != null && !entry.picture.equals("")) {
							saveUrl(entry.picture, entry.title + ".jpg");
						} else {
							PictureGenerator.generate(entry.title, entry.title
									+ ".jpg");
						}

						NSLPoster.post(board, entry.title,
								entry.title + ".jpg", entry.link + "\n\n"
										+ entry.description);

						// Delete picture
						new File(entry.title + ".jpg").delete();

						// wait between posts
						try {
							Thread.sleep(121000);
						} catch (Exception e) {
						}
					}
				}

				outhashList.add("" + entry.hashCode());
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.exit(1);
		}

		// Write hashlist to file
		try {
			BufferedWriter outwriter = new BufferedWriter(new FileWriter(
					feedFile));

			for (String s : outhashList)
				outwriter.write(s + "\n");

			outwriter.close();
		} catch (Exception e) {

		}

	}

	public static void main(String args[]) {
		boolean optUpdateOnly = false;

		// Parse options
		ArrayList<String> arguments = new ArrayList<>();

		for (String s : args)
			arguments.add(s);

		if (arguments.contains("--update-only")) {
			arguments.remove("--update-only");
			optUpdateOnly = true;
		}

		if (arguments.size() < 2) {
			printUsage();
			System.exit(1);
		}

		// Command line arguments
		String board = arguments.get(0);
		arguments.remove(0);

		for (String feedAddress : arguments)
			doFeed(board, feedAddress, optUpdateOnly);
	}
}
