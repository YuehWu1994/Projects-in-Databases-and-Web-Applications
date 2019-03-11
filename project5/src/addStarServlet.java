import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Statement;

/**
 * Servlet implementation class checkoutServlet
 */

@WebServlet(name = "addStarServlet", urlPatterns = "/api/addStar")
public class addStarServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;    
	
	
	@Resource(name = "jdbc/moviedb")
    private DataSource dataSource;   

    public addStarServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	String starName = request.getParameter("nm");
        String birth = request.getParameter("birth");
        
    	System.out.println(starName);
    	System.out.println(birth);

        
        HttpSession session = request.getSession();
        
        
        try {
            Context initCtx = new InitialContext();

            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            if (envCtx == null)
            	response.getWriter().println("envCtx is NULL");

            // Look up our data source
            DataSource ds = (DataSource) envCtx.lookup("jdbc/moviedb");
            if (ds == null)
            	response.getWriter().println("ds is null.");

            Connection dbcon = ds.getConnection();
            if (dbcon == null)
            	response.getWriter().println("dbcon is null.");
        	
            String query = "SELECT max(id) AS mx FROM stars";
            
            
            dbcon.setAutoCommit(false);
            PreparedStatement statement = dbcon.prepareStatement(query);

            ResultSet rs = statement.executeQuery();
            dbcon.commit();
            
            JsonArray jsonArray = new JsonArray();
            
            String largestId = "";
            while (rs.next()) {
            	largestId = rs.getString("mx");
    		}
    		rs.close();
    		statement.close();
    		
    		// add id(varchar) by 1
    		String rem = largestId.substring(2);
    		if(rem.charAt(0) >= '0' && rem.charAt(0)  <= '9') {
        		int idNum = Integer.parseInt(rem) + 1;
        		System.out.println(idNum); 
        		largestId = "nm" + Integer.toString(idNum);
        		System.out.println(largestId);     			
    		}
    		else {
    			largestId = starName;
    		}

            
            
    		// insert stars
    		PreparedStatement insertStatement = null;
    		
    		dbcon.setAutoCommit(false);
    		if(birth.isEmpty()) query = "INSERT INTO stars (id, name) VALUES(?,?);";
    		else query = "INSERT INTO stars (id, name, birthYear) VALUES(?,?,?);";
    		
    		insertStatement = dbcon.prepareStatement(query);  
    		
    		insertStatement.setString(1, largestId);
    		insertStatement.setString(2, starName);
    		if(!birth.isEmpty()) insertStatement.setInt(3, Integer.parseInt(birth));
    		
    		System.out.println(insertStatement);
    		
    		int af = insertStatement.executeUpdate();
            dbcon.commit();
            
            // response output
            JsonObject responseJsonObject = new JsonObject();
            if(af != 0) {
                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "Star " + starName + " inserted");          	
            }
            else {
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Insert Star Fail");
            }
            response.getWriter().write(responseJsonObject.toString());
        }catch (Exception e) {
        	
			// write error message JSON object to output
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("errorMessage", e.getMessage());
			
			// set reponse status to 500 (Internal Server Error)
			response.setStatus(500);

        }
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// TODO Auto-generated method stub
		//doGet(request, response);
	}

}