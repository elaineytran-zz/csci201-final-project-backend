

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.Semaphore;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

//http://35.215.113.104:8080/CSCI201FinalProject/GetRequest

/**
 * Servlet implementation class GetUsers
 */
@WebServlet("/GetRequest")
public class GetRequest extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String data = "";
       
    public GetRequest() {
        super();
    }

    protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {

			String json  = readData();

			PrintWriter out = response.getWriter();
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			
			// Add root node around the returned data
			out.print("{\"root\": " +  json + "}");
			out.flush();
			
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}
    
    public String readData() {
		Semaphore semaphore;
		FirebaseOptions options = null;
		
		try {	
			// Initialize Firebase
			// If the default app is initialized, don't not do it again
			// https://stackoverflow.com/questions/39186425/firebaseapp-name-default-already-exists
			if(!isInitialized()) {			
				String firebaseServiceAccountFile = getServletContext().getRealPath("/ServiceKey.json");
				FileInputStream serviceAccount = new FileInputStream(firebaseServiceAccountFile);
				options = new FirebaseOptions.Builder().setCredentials(GoogleCredentials.fromStream(serviceAccount))
						.setDatabaseUrl("https://csci201fp-a4384.firebaseio.com").build();
	
				FirebaseApp.initializeApp(options);
			}

			DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

			// GET ALL REQUESTS
			DatabaseReference requestsRef = ref.child("requests");
			
			//GET SPECIFIC REQUEST
			//Pass in request.getParameter("something"), ref.child("requests/specificRequest");
						
			// Use Semaphore to block thread until Firebase returned because Firebase call is asynchronous
			// https://stackoverflow.com/questions/33203379/setting-singleton-property-value-in-firebase-listener
			semaphore = new Semaphore(0);

			// Attach a listener to read the data at users reference
			requestsRef.addListenerForSingleValueEvent(new ValueEventListener() {

				@Override
				public void onDataChange(DataSnapshot dataSnapshot) {
									
					/* Return Firebase query */
					Gson gson = new Gson();
		            data = gson.toJson(dataSnapshot.getValue());
					
					semaphore.release();
				}

				@Override
				public void onCancelled(DatabaseError databaseError) {
					data = "The read failed: " + databaseError.getCode();
					semaphore.release();
				}
			});
			
			semaphore.acquire();

		} catch (Exception ex) {
			data = "{\"error\"= \"" + ex.getMessage() + "\"}";
		}

		return data;
	};
	
	public boolean isInitialized() {
	
		List<FirebaseApp> firebaseApps = FirebaseApp.getApps();
		for(FirebaseApp app : firebaseApps){
		    if(app.getName().equals(FirebaseApp.DEFAULT_APP_NAME)){
		        return true;
		    }
		}
		return false;
	}	

}
