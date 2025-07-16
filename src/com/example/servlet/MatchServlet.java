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

    // Environment variables (Clever Cloud)
    private static final String DB_HOST = System.getenv("DB_HOST");
    private static final String DB_PORT = System.getenv("DB_PORT");
    private static final String DB_NAME = System.getenv("DB_NAME");
    private static final String DB_USER = System.getenv("DB_USER");
    private static final String DB_PASS = System.getenv("DB_PASS");

    private static final String JDBC_URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME +
            "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String action = request.getParameter("action");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS);

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
            out.println(buildHtmlPage("<h3 style='color:red;'>Error: " + e.getMessage() + "</h3>"));
            e.printStackTrace();
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
        String message = rows > 0
                ? "<h3 class='text-success'> Match inserted successfully!</h3>"
                : "<h3 class='text-danger'> Failed to insert match.</h3>";
        out.println(buildHtmlPage(message));
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
        String message = rows > 0 
			? "<h3 class='text-danger'>Match deleted successfully!</h3>" 
			: "<h3 class='text-danger'>Match not found or deletion failed.</h3>";

        out.println(buildHtmlPage(message));
        stmt.close();
    }

    private void deleteMatch(HttpServletRequest request, Connection con, PrintWriter out) throws SQLException {
        int matchId = Integer.parseInt(request.getParameter("matchId"));

        String sql = "DELETE FROM matches WHERE id = ?";
        PreparedStatement stmt = con.prepareStatement(sql);
        stmt.setInt(1, matchId);

        int rows = stmt.executeUpdate();
        String message = rows > 0
                ? "<h3 class='text-danger'>️Match deleted successfully!</h3>"
                : "<h3 class='text-danger'> Match not found or deletion failed.</h3>";
        out.println(buildHtmlPage(message));
        stmt.close();
    }

    private void displayMatches(Connection con, PrintWriter out) throws SQLException {
        String sql = "SELECT * FROM matches";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        StringBuilder html = new StringBuilder();
        html.append("<h2 class='text-center mb-4'>Match Records</h2>");
        html.append("<div class='container'>");
        html.append("<div class='row fw-bold text-white bg-primary py-2 rounded'>");
        html.append("<div class='col-md-1'>ID</div>");
        html.append("<div class='col-md-2'>Team A</div>");
        html.append("<div class='col-md-2'>Team B</div>");
        html.append("<div class='col-md-1'>Score A</div>");
        html.append("<div class='col-md-1'>Score B</div>");
        html.append("<div class='col-md-2'>Winner</div>");
        html.append("<div class='col-md-2'>Venue</div>");
        html.append("<div class='col-md-1'>Date</div>");
        html.append("</div>");

        while (rs.next()) {
            html.append("<div class='row text-white border-bottom py-2' style='border-color: rgba(255,255,255,0.2);'>");
            html.append("<div class='col-md-1'>" + rs.getInt("id") + "</div>");
            html.append("<div class='col-md-2'>" + rs.getString("team_a") + "</div>");
            html.append("<div class='col-md-2'>" + rs.getString("team_b") + "</div>");
            html.append("<div class='col-md-1'>" + rs.getInt("score_a") + "</div>");
            html.append("<div class='col-md-1'>" + rs.getInt("score_b") + "</div>");
            html.append("<div class='col-md-2'>" + rs.getString("winner") + "</div>");
            html.append("<div class='col-md-2'>" + rs.getString("venue") + "</div>");
            html.append("<div class='col-md-1'>" + rs.getDate("match_date") + "</div>");
            html.append("</div>");
        }

        rs.close();
        stmt.close();

        out.println(buildHtmlPage(html.toString()));
    }

    // 🔄 Common method to build full HTML with Bootstrap + back button
    private String buildHtmlPage(String bodyContent) {
        return "<!DOCTYPE html><html><head>" +
                "<meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1'>" +
                "<title>Football Match Response</title>" +
                "<link href='https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css' rel='stylesheet'>" +
                "<style>body { background-color: #111; color: white; padding: 2rem; }</style>" +
                "</head><body>" +
                "<div class='container'>" +
                bodyContent +
                "<div class='text-center mt-4'>" +
                "<a href='index.html' class='btn btn-outline-light'>Back to Home</a>" +
                "</div></div></body></html>";
    }
}
