FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/uberjar/projects-spec.jar /projects-spec/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/projects-spec/app.jar"]
