FROM openjdk:17-jdk-slim

WORKDIR /app

COPY src/ /app/src/
COPY lib/ /app/lib/

RUN mkdir -p /app/bin

RUN javac -d bin -cp "lib/mysql-connector-j-9.5.0.jar" src/*.java

CMD ["java", "-cp", "bin:lib/mysql-connector-j-9.5.0.jar", "Main"]
