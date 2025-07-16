FROM tomcat:9.0

# Remove default apps
RUN rm -rf /usr/local/tomcat/webapps/*

# Add your WAR file (rename to ROOT.war for default context)
COPY target/football.war /usr/local/tomcat/webapps/ROOT.war

# Add MySQL connector JAR manually into Tomcat's lib folder
ADD https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.33/mysql-connector-java-8.0.33.jar /usr/local/tomcat/lib/

EXPOSE 8080
