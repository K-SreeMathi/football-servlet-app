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
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/FootballDB?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASS = "sree";

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        
        String action = request.getParameter("action");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
            
            if ("insert".equalsIgnoreCase(action)) {
                insertMatch(request, con, out);
            } else if ("update".equalsIgnoreCase(action)) {
                updateMatch(request, con, out);
            } else if ("delete".equalsIgnoreCase(action)) {
                deleteMatch(request, con, out);
            } else if ("display".equalsIgnoreCase(action)) {
                displayMatches(con, out);
            }
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
            out.println("<h3>Error: " + e.getMessage() + "</h3>");
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
        out.println(rows > 0 ? "<h3>Match record inserted successfully!</h3>" : "<h3>Error inserting record.</h3>");
        stmt.close();
    }

    private void updateMatch(HttpServletRequest request, Connection con, PrintWriter out) throws SQLException {
        int matchId = Integer.parseInt(request.getParameter("matchId"));
        int scoreA = Integer.parseInt(request.getParameter("scoreA"));
        int scoreB = Integer.parseInt(request.getParameter("scoreB"));
        
        String sql = "UPDATE matches SET score_a = ?, score_b = ?, winner = CASE WHEN score_a > score_b THEN team_a WHEN score_b > score_a THEN team_b ELSE 'Draw' END WHERE id = ?";
        PreparedStatement stmt = con.prepareStatement(sql);
        stmt.setInt(1, scoreA);
        stmt.setInt(2, scoreB);
        stmt.setInt(3, matchId);
        
        int rows = stmt.executeUpdate();
        out.println(rows > 0 ? "<h3>Match record updated successfully!</h3>" : "<h3>Error updating record.</h3>");
        stmt.close();
    }

    private void deleteMatch(HttpServletRequest request, Connection con, PrintWriter out) throws SQLException {
        int matchId = Integer.parseInt(request.getParameter("matchId"));
        
        String sql = "DELETE FROM matches WHERE id = ?";
        PreparedStatement stmt = con.prepareStatement(sql);
        stmt.setInt(1, matchId);
        
        int rows = stmt.executeUpdate();
        out.println(rows > 0 ? "<h3>Match record deleted successfully!</h3>" : "<h3>Error deleting record.</h3>");
        stmt.close();
    }

    private void displayMatches(Connection con, PrintWriter out) throws SQLException {
        String sql = "SELECT * FROM matches";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        
        out.println("<h2>Match Records</h2>");
        out.println("<table border='1'><tr><th>ID</th><th>Team A</th><th>Team B</th><th>Score A</th><th>Score B</th><th>Winner</th><th>Venue</th><th>Match Date</th></tr>");
        
        while (rs.next()) {
            out.println("<tr><td>" + rs.getInt("id") + "</td><td>" + rs.getString("team_a") + "</td><td>" + rs.getString("team_b") + "</td><td>" + rs.getInt("score_a") + "</td><td>" + rs.getInt("score_b") + "</td><td>" + rs.getString("winner") + "</td><td>" + rs.getString("venue") + "</td><td>" + rs.getString("match_date") + "</td></tr>");
        }
        out.println("</table>");
        rs.close();
        stmt.close();
    }
}
