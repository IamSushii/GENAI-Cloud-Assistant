FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY . /app
RUN javac GenAIAgent.java
EXPOSE 8080
CMD ["java", "GenAIAgent"]