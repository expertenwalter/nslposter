import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

public class NSLPoster {
	public static void post(String board, String subject, String picture, String message) {

		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://neuschwabenland.org/board.php");

			MultipartEntity entity = new MultipartEntity();
			entity.addPart(new FormBodyPart("board", new StringBody(board)));
			entity.addPart(new FormBodyPart("subject", new StringBody(subject,
					Charset.defaultCharset())));
			entity.addPart(new FormBodyPart("message", new StringBody(message,
					Charset.defaultCharset())));
			entity.addPart(new FormBodyPart("embed", new StringBody("")));
			entity.addPart(new FormBodyPart("postpassword", new StringBody(
					"abcdefgh")));
			entity.addPart(new FormBodyPart("imagefile[0]", new FileBody(
					new File(picture))));
			post.setEntity(entity);

			HttpResponse response = httpclient.execute(post);
			HttpEntity responseentity = response.getEntity();

			// If the response does not enclose an entity, there is no need
			// to worry about connection release
			if (entity != null) {
				InputStream instream = responseentity.getContent();
				try {

					BufferedReader reader = new BufferedReader(
							new InputStreamReader(instream));
					// do something useful with the response
					String s;
					while ((s = reader.readLine()) != null)
						System.out.println(s);

				} catch (IOException ex) {
					throw ex;
				} catch (RuntimeException ex) {
					post.abort();
					throw ex;
				} finally {
					instream.close();
				}
				httpclient.getConnectionManager().shutdown();
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	public static void main(String args[]) {
		post("m", "Test ü", "test.jpg", "Das ist ein Test ü das ist ein Test");
	}
}
