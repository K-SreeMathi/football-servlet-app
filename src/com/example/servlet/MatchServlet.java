package com.example.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/MatchServlet")
public class MatchServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Use environment variables for secure deployment (set in Render dashboard)
    private static final String JDBC_URL = System.getenv("DB_URL"); // e.g., jdbc:mysql://host:3306/dbname?params
    private static final String JDBC_USER = System.getenv("DB_USER");
    private static final String JDBC_PASS = System.getenv("DB_PASS");

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String action = request.getParameter("action");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);

            switch (action.toLowerCase()) {
                case "insert":
                    insertMatch(request, con, out);
                    break;
                case "update":
                    updateMatch(request, con, out);
                    break;
                case "delete":
                    deleteMatch(request, con, out);
                    break;
                case "display":
                    displayMatches(con, out);
                    break;
                default:
                    out.println("<h3>Unknown action: " + action + "</h3>");
            }

            con.close();
        } catch (Exception e) {
            e.printStackTrace();
            out.println("<h3 style='color:red;'>Error: " + e.getMessage() + "</h3>");
        }
    }

    private void insertMatch(HttpServletRequest request, Connection con, PrintWriter out) throws SQLException {
        String teamA = request.getParameter("teamA");
        String teamB = request.getParameter("teamB");
        int scoreA = Integer.parseInt(request.getParameter("scoreA"));
        int scoreB = Integer.parseInt(request.getParameter("scoreB"));
        String venue = request.getParameter("venue");
        String matchDate = request.getParameter("matchDate");

        String winner = (scoreA > scoreB) ? teamA : (scoreB > scoreA) ? teamB : "Draw";

        String sql = "INSERT INTO matches (team_a, team_b, score_a, score_b, winner, venue, match_date) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = con.prepareStatement(sql);
        stmt.setString(1, teamA);
        stmt.setString(2, teamB);
        stmt.setInt(3, scoreA);
        stmt.setInt(4, scoreB);
        stmt.setString(5, winner);
        stmt.setString(6, venue);
        stmt.setString(7, matchDate);

        int rows = stmt.executeUpdate();
        out.println(rows > 0 ? "<h3 style='color:lightgreen;'>Match record inserted successfully!</h3>" : "<h3>Error inserting record.</h3>");
        stmt.close();
    }

    private void updateMatch(HttpServletRequest request, Connection con, PrintWriter out) throws SQLException {
        int matchId = Integer.parseInt(request.getParameter("matchId"));
        int scoreA = Integer.parseInt(request.getParameter("scoreA"));
        int scoreB = Integer.parseInt(request.getParameter("scoreB"));

        String sql = "UPDATE matches SET score_a = ?, score_b = ?, winner = CASE WHEN ? > ? THEN team_a WHEN ? < ? THEN team_b ELSE 'Draw' END WHERE id = ?";
        PreparedStatement stmt = con.prepareStatement(sql);
        stmt.setInt(1, scoreA);
        stmt.setInt(2, scoreB);
        stmt.setInt(3, scoreA);
        stmt.setInt(4, scoreB);
        stmt.setInt(5, scoreA);
        stmt.setInt(6, scoreB);
        stmt.setInt(7, matchId);

        int rows = stmt.executeUpdate();
        out.println(rows > 0 ? "<h3 style='color:orange;'>Match record updated successfully!</h3>" : "<h3>Error updating record.</h3>");
        stmt.close();
    }

    private void deleteMatch(HttpServletRequest request, Connection con, PrintWriter out) throws SQLException {
        int matchId = Integer.parseInt(request.getParameter("matchId"));

        String sql = "DELETE FROM matches WHERE id = ?";
        PreparedStatement stmt = con.prepareStatement(sql);
        stmt.setInt(1, matchId);

        int rows = stmt.executeUpdate();
        out.println(rows > 0 ? "<h3 style='color:red;'>Match record deleted successfully!</h3>" : "<h3>Error deleting record.</h3>");
        stmt.close();
    }

    private void displayMatches(Connection con, PrintWriter out) throws SQLException {
        String sql = "SELECT * FROM matches";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        // Bootstrap grid layout
        out.println("<div class='container mt-4'>");

        out.println("<div class='row fw-bold text-white bg-primary py-2 rounded'>");
        out.println("<div class='col-md-1'>ID</div>");
        out.println("<div class='col-md-2'>Team A</div>");
        out.println("<div class='col-md-2'>Team B</div>");
        out.println("<div class='col-md-1'>Score A</div>");
        out.println("<div class='col-md-1'>Score B</div>");
        out.println("<div class='col-md-2'>Winner</div>");
        out.println("<div class='col-md-2'>Venue</div>");
        out.println("<div class='col-md-1'>Date</div>");
        out.println("</div>");

        while (rs.next()) {
            out.println("<div class='row text-white border-bottom py-2' style='border-color: rgba(255,255,255,0.2);'>");
            out.println("<div class='col-md-1'>" + rs.getInt("id") + "</div>");
            out.println("<div class='col-md-2'>" + rs.getString("team_a") + "</div>");
            out.println("<div class='col-md-2'>" + rs.getString("team_b") + "</div>");
            out.println("<div class='col-md-1'>" + rs.getInt("score_a") + "</div>");
            out.println("<div class='col-md-1'>" + rs.getInt("score_b") + "</div>");
            out.println("<div class='col-md-2'>" + rs.getString("winner") + "</div>");
            out.println("<div class='col-md-2'>" + rs.getString("venue") + "</div>");
            out.println("<div class='col-md-1'>" + rs.getDate("match_date") + "</div>");
            out.println("</div>");
        }

        out.println("</div>"); // Close container

        rs.close();
        stmt.close();
    }
}
