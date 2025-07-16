FROM tomcat:9.0

# Remove default apps
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy your WAR file (must exist in target/)
COPY target/football.war /usr/local/tomcat/webapps/ROOT.war

# Install curl and download MySQL connector JAR
RUN apt-get update && apt-get install -y curl && \
    curl -L -o /usr/local/tomcat/lib/mysql-connector-java-8.0.33.jar \
    https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.33/mysql-connector-java-8.0.33.jar

EXPOSE 8080
