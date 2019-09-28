FROM openjdk:8-jdk-alpine
COPY ./target/trader-0.0.1-SNAPSHOT.jar /app/
RUN ls /app/
WORKDIR /app/
CMD java -jar trader-0.0.1-SNAPSHOT.jar