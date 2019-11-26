import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserRecord.CreateRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.google.api.core.ApiFuture;
import org.slf4j.LoggerFactory;


/**
 * Servlet implementation class RegisterUserServelet
 */
@WebServlet("/RegisterUserServelet")
public class RegisterUserServelet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    public String data = "\"SUCCESS\"";
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RegisterUserServelet() {
        super();
        // TODO Auto-generated constructor stub
    }

    protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {

			String json  = addUser(request);
			
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
    
    public String addUser(HttpServletRequest request) {
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
			UserRecord userRecord = null;
			
			String email = request.getParameter("email");
			String password = request.getParameter("password");
			String displayName = request.getParameter("displayName");
			
			// Sample values delete in actual implementation
			email = "test@usc.edu";
			password = "password";
			displayName = "John";
		
			// Uncomment to add User to Auth tab of firebase ----This works---
			// If user already exists then returns String "FAILED"
			/*
			try {
				CreateRequest userRequest = new CreateRequest()
					    .setEmail(email)
					    .setPassword(password)
					    .setDisplayName(displayName)
					    .setDisabled(false);
				userRecord = FirebaseAuth.getInstance().createUser(userRequest);
			} catch (FirebaseAuthException e) {
				// TODO Auto-generated catch block
				return "\"FAILED\"";
			}
			*/
			
			DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
			
			DatabaseReference usersRef = ref.child("users");
			
			// Cannot get this to work when converting to servlet
			// Adds user to actual database under user tab
			// Map stores user data
			Map<String, String> user = new HashMap<String, String>();
			user.put("email", email);
			user.put("password", password);
			user.put("username", displayName);
			
			// .push() creates unique key (look at database for examples) and then .setValyeAsync will put the map under the key
			usersRef.push().setValueAsync(user);
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
