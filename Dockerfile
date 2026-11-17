FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY PrimeNumber.java .
COPY index.html .
RUN javac PrimeNumber.java
EXPOSE 8080
ENV PORT=8080
CMD ["java", "PrimeNumber"]
