import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Servlet implementation class MainServlet
 */
@WebServlet(name="MainServlet", urlPatterns = "/main_page")
public class MainServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
    // Create a dataSource which registered in web.xml
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MainServlet() {
        super();
        // TODO Auto-generated constructor stub
    }
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json"); // Response mime type
		PrintWriter out = response.getWriter();
		
		
		try {
            // Get a connection from dataSource
            Connection dbcon = dataSource.getConnection();
            dbcon.setAutoCommit(false);

            // prepare string and statement
    		PreparedStatement genreStatement = null;
            String genreStr = "SELECT * from genres";
            
            dbcon.setAutoCommit(false);
            genreStatement = dbcon.prepareStatement(genreStr);
            
            // execute the query for genres
            ResultSet rs = genreStatement.executeQuery();
            dbcon.commit();
            
            JsonArray jsonArray = new JsonArray();
            
            // get genres
            while(rs.next()) {
            	String genre_name=rs.getString("name");
            	JsonObject jsonObject = new JsonObject();
            	jsonObject.addProperty("genre_name", genre_name);
            	jsonArray.add(jsonObject);
            }
            
            // get movie title infomration
            String query = request.getParameter("query");
            
            if (query != null && !query.trim().isEmpty()) {
            	// get movies
                PreparedStatement moviesStatement = null;
                String query_movies = "SELECT * from movies";
                moviesStatement = dbcon.prepareStatement(query_movies);
                rs = moviesStatement.executeQuery();
                dbcon.commit();
                while(rs.next()) {
                	String movie_title=rs.getString("title");
                	String movie_id=rs.getString("id");
                	if(movie_title.toLowerCase().contains(query.toLowerCase())) {
                		jsonArray.add(generate_movie_obj(movie_id, movie_title));
                	}
                }
         	}
            
            out.write(jsonArray.toString());
            response.setStatus(200);
            rs.close();
            genreStatement.close();
            dbcon.close();
            
		} catch (Exception e) {
			// write error message JSON object to output
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("errorMessage", e.getMessage());
			out.write(jsonObject.toString());

			// set reponse status to 500 (Internal Server Error)
			response.setStatus(500);
        }
		out.close();
	}
	
	/*
	 * Generate the JSON Object from hero to be like this format:
	 * {
	 *   "value": "Iron Man",
	 *   "data": { "movie_id": "11" }
	 * }
	 * 
	 */
	private static JsonObject generate_movie_obj(String movie_id, String movieName) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("value", movieName);
		
		JsonObject additionalDataJsonObject = new JsonObject();
		additionalDataJsonObject.addProperty("movie_id", movie_id);
		
		jsonObject.add("data", additionalDataJsonObject);
		return jsonObject;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
